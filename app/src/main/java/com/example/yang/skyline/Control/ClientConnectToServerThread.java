package com.example.yang.skyline.Control;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;

import com.example.yang.skyline.R;
import com.example.yang.skyline.UI.Contact_Fragment;
import com.example.yang.skyline.UI.Conversaction_Activity;
import com.example.yang.skyline.UI.Login_Activity;
import com.example.yang.skyline.UI.MainActivity;
import com.example.yang.skyline.UI.Test;

import Model.Message;
import Model.MessageType;
import Model.User;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yang on 2016/4/28.
 * 位于客户端用来和服务器保持通信的线程，不断的读取服务器发来的数据,给服务器发送过去的数据，是由各个不同的控件上绑定的监听器分散完成
 */
public class ClientConnectToServerThread extends Thread {
    private Context context;
    private static Socket client_To_Server_Socket;
    private ObjectInputStream ois = null;
    private NotificationManager manager;//管理新消息的通知
    private ArrayList<String> notification_message = new ArrayList<String>();//用于从Notification中接收消息，然后传给Conversation_Activity的容器
    private File file_received = null;

    public static Socket getSocket() {
        return client_To_Server_Socket;
    }

    public ClientConnectToServerThread(Context context, Socket client_To_Server_Socket) {
        this.context = context;
        this.client_To_Server_Socket = client_To_Server_Socket;
    }

    public void run() {
        while (true) {
            try {
                ois = new ObjectInputStream(client_To_Server_Socket.getInputStream());
                Message message_received = (Message) ois.readObject();


                switch (message_received.getType()) {
                    case MessageType.ADD_CONTACT:

                        android.os.Message message_addContact = android.os.Message.obtain();
                        message_addContact.what = 150;
                        Bundle bundle_addContact = new Bundle();
                        bundle_addContact.putString("id", message_received.getSender());
                        bundle_addContact.putString("nickName",message_received.getSenderNickname());
                        message_addContact.setData(bundle_addContact);

                        MainActivity.mHandler.sendMessage(message_addContact);


                        break;

                    case MessageType.ADD_CONTACT_FAIL:
                        MainActivity.mHandler.sendEmptyMessage(151);
                        break;

                    case MessageType.ADD_CONTACT_REFUSED:
                        String id_refuseing = message_received.getSender();
                        android.os.Message message_refused = android.os.Message.obtain();
                        message_refused.what = 153;
                        Bundle bundle_refused = new Bundle();
                        bundle_refused.putString("id_refuseing",id_refuseing);
                        message_refused.setData(bundle_refused);

                        MainActivity.mHandler.sendMessage(message_refused);

                        break;

                    case MessageType.ADD_CONTACT_AGREED:
                        String id_agreed = message_received.getSender();
                        android.os.Message message_agreed = android.os.Message.obtain();
                        message_agreed.what = 154;
                        Bundle bundle_agreed = new Bundle();
                        bundle_agreed.putString("id_agreed",id_agreed);
                        message_agreed.setData(bundle_agreed);

                        MainActivity.mHandler.sendMessage(message_agreed);

                        break;

                    case MessageType.COMMON_MESSAGE:
                        String content = message_received.getContent();
                        //如果Conversation_Activity在前台，那么静态变量Conversation_Activity.user_id就已经被初始化了
                        if(isForeground(context,"com.example.yang.skyline.UI.Conversaction_Activity") && (message_received.getSender()).equals(Conversaction_Activity.user_id) ){
                            android.os.Message comm_message = android.os.Message.obtain();
                            comm_message.what = 550;
                            Bundle bundle_com_message = new Bundle();
                            bundle_com_message.putString("com_mes", content);
                            comm_message.setData(bundle_com_message);

                            Conversaction_Activity.conversation_Handler.sendMessage(comm_message);
                        }else{

                            //---------------用以Conversation_Activity关闭与打开时的备份指定联系人消息的容器--------------
                            String contact_id = message_received.getSender();
                            if(!MainActivity.message_list_MainActivity.containsKey(contact_id)){
                                Log.i("id","传入id为：" + contact_id);
                                ArrayList<HashMap<String,Object>> message_ArrayList = new ArrayList<HashMap<String,Object>>();
                                MainActivity.message_list_MainActivity.put(contact_id,message_ArrayList);

                            }
                            //----------------------------------------------------------------------------------------




                            notification_message.add(content);

                            manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            Notification.Builder builder = new Notification.Builder(context);
                            builder.setTicker("你收到了一条新消息");
                            builder.setSmallIcon(R.mipmap.skyline_pic);

                            User user_info = new User();

                            user_info.setId(message_received.getSender());
                            user_info.setNickName(message_received.getSenderNickname());

                            //Intent intent = new Intent(Contact_Fragment.getContactFragmentContext(),Conversaction_Activity.class);
                            Intent intent = new Intent(Contact_Fragment.getContactFragmentContext(),Contact_Fragment.class);


                            intent.putExtra("source","Notification");
                            intent.putExtra("message",notification_message);
                            intent.putExtra("user_info", user_info);

                            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

                            Notification notification = builder.setContentIntent(pendingIntent).setContentTitle("Skye").setContentText(content).build();
                            manager.notify(100, notification);

                        }

                        break;

                    case MessageType.REQUEST_CONTACT_SUCCESS:
                        ArrayList<User> contacts = message_received.getContactList();

                        android.os.Message message_latest_contacts = android.os.Message.obtain();
                        message_latest_contacts.what = 1001;
                        Bundle bundle_latest_contacts = new Bundle();
                        bundle_latest_contacts.putSerializable("latest_contact_list",contacts);
                        message_latest_contacts.setData(bundle_latest_contacts);

                        Contact_Fragment.contactFragment_Handler.sendMessage(message_latest_contacts);
                        break;

                    case MessageType.CONTACT_DELETED:

                        android.os.Message message_Deleted = android.os.Message.obtain();
                        message_Deleted.what = 2001;
                        Bundle bundle_Deleted = new Bundle();
                        bundle_Deleted.putString("sender_id",message_received.getSender());
                        bundle_Deleted.putString("sender_nickName",message_received.getSenderNickname());

                        message_Deleted.setData(bundle_Deleted);
                        MainActivity.mHandler.sendMessage(message_Deleted);

                        break;

                    case MessageType.AUDIO_MESSAGE:
                        //从另一个客户端发送过来的音频文件的大小

                        long file_Size_Theory =Long.parseLong(message_received.getContent());
                        Log.i("yang","接收到另外一个客户端发送过来的音频资源，其大小为：" + file_Size_Theory);

                        try {
                            String path = Environment.getExternalStorageDirectory().getCanonicalFile() + "/sound_received.amr";

                            BufferedInputStream input = new BufferedInputStream(client_To_Server_Socket.getInputStream());

                            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path));

                            byte []buff = new byte[128];

                            long fileSize = 0;
                            Log.i("yang","文件开始存往手机");
                            while(true){
                                int read = 0;
                                if(input != null){
                                    read = input.read(buff);
                                    fileSize += read;
                                }

                                Log.i("yang","fileSize = " + fileSize);

                                if(fileSize >= file_Size_Theory){
                                    Log.i("yang","即将跳出循环");
                                    break;
                                }
                                out.write(buff,0,read);
                                Log.i("yang","---->>><<<----");
                            }
                            out.flush();
                            out.close();
                            Log.i("ranger","文件存入本地完成");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        //如果Conversation_Activity在前台，那么静态变量Conversation_Activity.user_id就已经被初始化了
                        if(isForeground(context,"com.example.yang.skyline.UI.Conversaction_Activity") && (message_received.getSender()).equals(Conversaction_Activity.user_id) ){
                            android.os.Message comm_message = android.os.Message.obtain();
                            comm_message.what = 552;
                            Bundle bundle_audio_message = new Bundle();
                            bundle_audio_message.putString("com_audio_mes", "▶▶▶[你收到一条语音信息，点击收听]");
                            comm_message.setData(bundle_audio_message);

                            Conversaction_Activity.conversation_Handler.sendMessage(comm_message);
                        }else{

                            //---------------用以Conversation_Activity关闭与打开时的备份指定联系人消息的容器--------------
                            String contact_id = message_received.getSender();
                            if(!MainActivity.message_list_MainActivity.containsKey(contact_id)){
                                Log.i("id","传入id为：" + contact_id);
                                ArrayList<HashMap<String,Object>> message_ArrayList = new ArrayList<HashMap<String,Object>>();
                                MainActivity.message_list_MainActivity.put(contact_id,message_ArrayList);

                            }
                            //-------------------------notification_message.add("▶▶▶你收到一条语音信息，点击收听");---------------------------------------------------------------
                            notification_message.add("▶▶▶[你收到一条语音信息，点击收听]");

                            manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            Notification.Builder builder = new Notification.Builder(context);
                            builder.setTicker("你收到了一条语音消息");
                            builder.setSmallIcon(R.mipmap.skyline_pic);

                            User user_info = new User();

                            user_info.setId(message_received.getSender());
                            user_info.setNickName(message_received.getSenderNickname());

                            //Intent intent = new Intent(Contact_Fragment.getContactFragmentContext(),Conversaction_Activity.class);
                            Intent intent = new Intent(Contact_Fragment.getContactFragmentContext(),Contact_Fragment.class);


                            intent.putExtra("source","Notification");
                            intent.putExtra("message",notification_message);
                            intent.putExtra("user_info", user_info);

                            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

                            Notification notification = builder.setContentIntent(pendingIntent).setContentTitle("Skye").setContentText("▶你收到一条语音信息").build();
                            manager.notify(100, notification);

                        }

                        //----------------------------------------
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }
    }


    private boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

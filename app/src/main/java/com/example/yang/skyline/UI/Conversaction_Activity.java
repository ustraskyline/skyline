package com.example.yang.skyline.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.yang.skyline.Control.ClientConnectToServerThread;
import com.example.yang.skyline.Control.SkylineClient;
import com.example.yang.skyline.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import Model.Message;
import Model.MessageType;
import Model.User;

/**
 * Created by yang on 2016/4/14.
 */
public class Conversaction_Activity extends Activity implements View.OnClickListener {

    private ImageView back_button, tabhost_pic;
    private TextView message_logo, chater_nickname;
    private boolean isMic = false;
    private ImageView audio_picture;
    private AlertDialog.Builder builder = null;

    private FragmentManager fm = null;
    private FragmentTransaction transaction = null;

    private Fragment keyboard_fragment, microphone_fragment;

    private String client_info = SkylineClient.myself_information;
    private String info[]  = client_info.split("_");
    private String sender_id = info[0];
    private String sender_nickName = info[1];

    private User user;//是点击ListView上的某一项时传进来的
    public static String user_id;
    protected static int avatar_index;

    private File soundFile;
    private MediaRecorder mRecorder;
   // private MediaPlayer player;


    //--------------------------------------------------------------------------------------------------------------
    private ListView conversation_listview;              //listview
    private ArrayList<HashMap<String, Object>> chatList;  //数据源
    private Skyline_Chat_Adapter adapter;                //适配器

    protected final static int ME = 0;
    protected final static int OTHER = 1;
    //--------------------------------------------------------------------------------------------------------------

    public static Handler conversation_Handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.conversation_layout);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user_info");

        String source_info = intent.getStringExtra("source");

        user_id = user.getId();
        avatar_index = user.getAvatar();

        setDefaultFragment();
        fm = getFragmentManager();

        back_button = (ImageView) findViewById(R.id.back_to_login_activity_imageview);
        message_logo = (TextView) findViewById(R.id.message_logo_in_conversation);
        chater_nickname = (TextView) findViewById(R.id.title_in_conversation);
        chater_nickname.setText(user.getNickName());


        tabhost_pic = (ImageView) findViewById(R.id.tabhost_index_pic);
        conversation_listview = (ListView) findViewById(R.id.conversation_listview);

        chatList = new ArrayList<HashMap<String, Object>>();

        //得到MainActivity为当前用户创建的用于保存历史消息的ArrayList，从中恢复数据
        ArrayList<HashMap<String,Object>> chatList_from_buffered = (ArrayList<HashMap<String, Object>>) MainActivity.message_list_MainActivity.get(user_id);

        //如果此Activity是从Notification启动的，那么就要将先前发送过来的消息插入到chatList中
        if("Notification".equals(source_info)){
            if(chatList_from_buffered.size() > 0){
                chatList.clear();
                chatList.addAll(chatList_from_buffered);
            }

            ArrayList<String> message_list = (ArrayList<String>) intent.getSerializableExtra("message");

            for(int i=0; i<message_list.size(); i++){
                insertMessageToList(message_list.get(i),OTHER);
            }
            NotificationManager manager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(100);

        }else if("Contact_Fragment".equals(source_info) && chatList_from_buffered.size() > 0){
            chatList.clear();
            chatList.addAll(chatList_from_buffered);
        }


        //实例化ListView的自定义适配器
        adapter = new Skyline_Chat_Adapter(Conversaction_Activity.this, chatList);
        //为ListView设置适配器
        conversation_listview.setAdapter(adapter);



        back_button.setOnClickListener(this);
        message_logo.setOnClickListener(this);
        tabhost_pic.setOnClickListener(this);


        conversation_Handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 550:
                        Bundle bun = msg.getData();
                        String str = bun.getString("com_mes");

                        insertMessageToList(str, OTHER);

                        //通知适配器更新listView
                        adapter.notifyDataSetChanged();
                        //将listview总是定位到最低端的一个记录
                        conversation_listview.setSelection(chatList.size() - 1);

                        break;

                    case 552:
                        Bundle audio_bun = msg.getData();
                        String str_audio = audio_bun.getString("com_audio_mes");
                        insertMessageToList(str_audio, OTHER);

                        //通知适配器更新listView
                        adapter.notifyDataSetChanged();
                        //将listview总是定位到最低端的一个记录
                        conversation_listview.setSelection(chatList.size() - 1);
                        break;
                }
            }
        };


        IntentFilter inf = new IntentFilter();
        inf.addAction("send_common_message");
        inf.addAction("record_audio");
        inf.addAction("stop_record_audio");
        this.registerReceiver(receiver, inf);

    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case "send_common_message":
                    final Message common_message = new Message();
                    String message_content = intent.getStringExtra("message");

                    common_message.setContent(message_content);
                    common_message.setReceiver(user_id);
                    common_message.setSender(sender_id);
                    common_message.setSenderNickname(sender_nickName);
                    common_message.setType(MessageType.COMMON_MESSAGE);

                    insertMessageToList(message_content, ME);

                    new Thread(){
                        @Override
                        public void run() {

                            ObjectOutputStream oos = null;
                            try {
                                oos = new ObjectOutputStream(ClientConnectToServerThread.getSocket().getOutputStream());
                                oos.writeObject(common_message);



                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                    adapter.notifyDataSetChanged();
                    conversation_listview.setSelection(chatList.size() - 1);

                    break;

                case "record_audio":
                    Log.i("yang","开始录制音频");

                    if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                        Log.i("yang","SD卡不在，请插入SD卡！");
                        return;
                    }else{

                        try {
                            //创建保存录音的音频文件
                            soundFile = new File(Environment.getExternalStorageDirectory().getCanonicalFile() + "/sound.amr");
                            if(soundFile.exists()){
                                soundFile.delete();
                            }
                            mRecorder = new MediaRecorder();
                            //设置声音的来源为麦克风
                            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            //设置录制音频的输出格式，必须在设置声音编码格式之前完成
                            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                            //设置声音编码格式
                            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                            //设置用于保存录音的输出文件
                            mRecorder.setOutputFile(soundFile.getAbsolutePath());

                            mRecorder.prepare();
                            //开始录音
                            mRecorder.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;


                case "stop_record_audio":
                    Log.i("yang","停止录制音频");

                    if (soundFile != null && soundFile.exists()) {
                        //停止录音
                        mRecorder.stop();
                        //释放资源
                        mRecorder.release();
                        mRecorder = null;
                    }

//                    player = new MediaPlayer();
//                    try {
//                        player.setDataSource(Environment.getExternalStorageDirectory().getCanonicalFile() + "/sound_sended.amr");
//                        player.prepare();
//                        player.start();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    final Message audio_message = new Message();
                    audio_message.setType(MessageType.AUDIO_MESSAGE);
                    audio_message.setReceiver(user_id);
                    audio_message.setSender(sender_id);
                    audio_message.setContent(String.valueOf(soundFile.length()));
                    audio_message.setSenderNickname(sender_nickName);
                    insertMessageToList("[你发送了一条语音信息]▶▶▶", ME);


                    new Thread(){
                        @Override
                        public void run() {
                            ObjectOutputStream oos_audio = null;
                            try {
                                //先发送一条类型标识为AUDIO_MESSAGE的普通消息，以让服务器接收线程进入相应的case块
                                oos_audio = new ObjectOutputStream(ClientConnectToServerThread.getSocket().getOutputStream());
                                oos_audio.writeObject(audio_message);

                                BufferedInputStream input = new BufferedInputStream(new FileInputStream(Environment.getExternalStorageDirectory().getCanonicalFile() + "/sound.amr"));

                                BufferedOutputStream out = new BufferedOutputStream(ClientConnectToServerThread.getSocket().getOutputStream());

                                byte []buff = new byte[128];
                                while(true){
                                    int read = 0;
                                    if(input != null){
                                        read = input.read(buff);
                                    }

                                    if(read == -1){
                                        Log.i("yang","客户端跳出while循环");

                                        break;
                                    }
                                    out.write(buff,0,read);
                                }
                                out.flush();
                                input.close();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                    adapter.notifyDataSetChanged();
                    conversation_listview.setSelection(chatList.size() - 1);
                    break;
            }
        }
    };


    private void setDefaultFragment() {
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        isMic = true;
        keyboard_fragment = new Keyboard_Fragment();
        transaction.replace(R.id.content, keyboard_fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        //得到MainActivity为当前用户创建的用于保存历史消息的ArrayList, 更新里面的数据
        ArrayList<HashMap<String,Object>> latest_message = (ArrayList<HashMap<String, Object>>) MainActivity.message_list_MainActivity.get(user_id);

        latest_message.clear();
        latest_message.addAll(chatList);


        //-----------------------用以在Message_Fragment中显示的历史消息的数据源---------------------
        HashMap<String,Object>  latest_message_left = new HashMap<String,Object>();

        //如果找到了以前曾经存在的item，删除，重新插入以刷新信息
        for(HashMap<String,Object> latest : MainActivity.message_fragment_MainActivity){
            if((user.getId()).equals(latest.get("id"))){
                MainActivity.message_fragment_MainActivity.remove(latest);
                break;
            }
        }

        if(chatList.size() > 0){
            String latest_message_content = (String) ((HashMap<String, Object>)chatList.get(chatList.size()-1)).get("text");
            Log.i("latest","content:  " + latest_message_content);
            int online = user.getOnline();
            latest_message_left.put("contact_avatar",MainActivity.contact_pics[ online == 1 ? user.getAvatar() : user.getAvatar() + 9]);
            latest_message_left.put("id",user.getId());
            latest_message_left.put("contact_nickName",user.getNickName());
            latest_message_left.put("contact_latestMessage",latest_message_content);

            MainActivity.message_fragment_MainActivity.add(0,latest_message_left);
        }
        //--------------------------------------------------------------------------------------

        finish();
        overridePendingTransition(R.anim.new_enter_from_left, R.anim.old_exit_to_right);

    }


    /**
     * 此方法的作用是将消息内容添加到ArrayList
     * message: 消息的内容
     * who: 发送方
     */
    protected void insertMessageToList(String message, int who) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("person", who);
        map.put("text", message);
        chatList.add(map);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.back_to_login_activity_imageview:
            case R.id.message_logo_in_conversation:
                onBackPressed();

                break;

            case R.id.tabhost_index_pic:


                transaction = fm.beginTransaction();
                if(!isMic){  //this moment is microphone, should
                    tabhost_pic.setImageResource(R.mipmap.microphone);
                    if(keyboard_fragment == null){
                        keyboard_fragment = new Keyboard_Fragment();
                    }

                    transaction.replace(R.id.content, keyboard_fragment);

                    isMic = true;
                }else if(isMic){ //this moment is keyboard
                    tabhost_pic.setImageResource(R.mipmap.keyboard);
                    if(microphone_fragment == null){
                        microphone_fragment = new Microphone_Fragment();
                    }

                    transaction.replace(R.id.content, microphone_fragment);

                    isMic = false;
                }

                transaction.commit();

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}

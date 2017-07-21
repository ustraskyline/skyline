package com.example.yang.skyline.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yang.skyline.Control.ClientConnectToServerThread;
import com.example.yang.skyline.Control.ManageClientConnToServer;
import com.example.yang.skyline.Control.ManageSocketToServer;
import com.example.yang.skyline.Control.SkylineClient;
import com.example.yang.skyline.R;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import Model.Message;
import Model.MessageType;

public class MainActivity extends Activity implements View.OnClickListener {
    //public static String myself_information;
    private ImageView mMessage, mFriend, mAbout;
    private TextView title_Top;
    private ImageView addFriend;

    boolean messageIsFocused = false;  //false表示message按钮刚开始未选中
    boolean friendIsFocused = true;
    boolean aboutIsFocused = false;

    private Message_Fragment message_fragment;
    private Contact_Fragment contact_fragment;
    private About_Fragment about_fragment;

    //--------------本客户端作为信息发送方时的发送方id和发送方昵称
    /**
     * 设置为静态的时候，在类被装载时就会被初始化，此时SkylineClient还没有执行到为myself_information赋值的那一段，
     * 那么此时client_info内容为null；将client_info设置为非静态时，执行到这一步时SkylineClient中的赋值操作已经完成，
     * 此时client_info内容才不为空
     */
    private String client_info = SkylineClient.myself_information;
    private String info[]  = client_info.split("_");
    private String sender_id = info[0];
    private String sender_nickName = info[1];

    private FragmentManager fm = null;
    private FragmentTransaction transaction = null;

    public static int[] contact_pics = {R.drawable.item_pic_1,R.drawable.item_pic_2,R.drawable.item_pic_3,
            R.drawable.item_pic_4,R.drawable.item_pic_5,R.drawable.item_pic_6,
            R.drawable.item_pic_7,R.drawable.item_pic_8,R.drawable.item_pic_9,
            R.drawable.item_pic_1_off,R.drawable.item_pic_2_off,R.drawable.item_pic_3_off,
            R.drawable.item_pic_4_off,R.drawable.item_pic_5_off,R.drawable.item_pic_6_off,
            R.drawable.item_pic_7_off,R.drawable.item_pic_8_off,R.drawable.item_pic_9_off
    };

    public static Handler mHandler;

    //放在MainActivity中用来保存Contact_Fragment中消息的一个缓冲区，当Contact_Fragment重新载入时，用此缓冲区中
    //存放的消息填充历史消息记录
    public static HashMap<String, Object> message_list_MainActivity = new HashMap<String, Object>();

    //用于放在MainActivity中用来保存显示在Message_Fragment中上的每一个item上信息的ArrayList
    public static ArrayList<HashMap<String,Object>> message_fragment_MainActivity = new ArrayList<HashMap<String,Object>>();
    protected static int client_avatar = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mMessage = (ImageView) findViewById(R.id.message);
        mFriend = (ImageView) findViewById(R.id.contact);
        mAbout = (ImageView) findViewById(R.id.other);
        title_Top = (TextView) findViewById(R.id.title_in_mainactivity);
        addFriend = (ImageView) findViewById(R.id.add_friend_in_mainactivity);
        title_Top.setText("联系人");

        //myself_information为客户端登录成功时服务器发送回来的消息
        Toast.makeText(MainActivity.this, SkylineClient.myself_information, Toast.LENGTH_SHORT).show();

        setDefaultFragment();

        mHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {

                    case 150:
                        Bundle bun_addContact = msg.getData();
                        final String id_1 = bun_addContact.getString("id");
                        String nickname = bun_addContact.getString("nickName");

                        final AlertDialog.Builder add_contact = new AlertDialog.Builder(MainActivity.this);
                        add_contact.setCancelable(false);
                        add_contact.setMessage("用户  " + nickname + "(" + id_1 + ")  想添加你为好友，是否同意?");
                        add_contact.setPositiveButton("同意", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                add_contact.setCancelable(true);

                                final Message add_Contact_Agreed = new Message();
                                add_Contact_Agreed.setSender(sender_id);
                                add_Contact_Agreed.setReceiver(id_1);
                                Log.i("yang4","id_1 = " + id_1);
                                add_Contact_Agreed.setType(MessageType.ADD_CONTACT_AGREED);

                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            ObjectOutputStream oos = new ObjectOutputStream(ClientConnectToServerThread.getSocket().getOutputStream());
                                            oos.writeObject(add_Contact_Agreed);

                                            Thread.sleep(2000);
                                            //------>在此处更新好友列表
                                            ObjectOutputStream oos_2 = new ObjectOutputStream(ClientConnectToServerThread.getSocket().getOutputStream());
                                            Message request_Contact = new Message();
                                            request_Contact.setType(MessageType.REQUEST_CONTACT);
                                            oos_2.writeObject(request_Contact);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }
                        });

                        add_contact.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                add_contact.setCancelable(true);

                                final Message add_Contact_Refused = new Message();

                                add_Contact_Refused.setSender(sender_id);
                                add_Contact_Refused.setReceiver(id_1);
                                add_Contact_Refused.setType(MessageType.ADD_CONTACT_REFUSED);

                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            ObjectOutputStream oos = new ObjectOutputStream(ClientConnectToServerThread.getSocket().getOutputStream());
                                            oos.writeObject(add_Contact_Refused);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }
                        });
                        add_contact.create();
                        add_contact.show();

                        break;

                    case 151:

                        final AlertDialog.Builder builder_fail = new AlertDialog.Builder(MainActivity.this);
                        builder_fail.setCancelable(false);
                        builder_fail.setMessage("你所查找的Id没有注册成为Skyline用户，无法添加其为好友!");
                        builder_fail.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                builder_fail.setCancelable(true);
                            }
                        });
                        builder_fail.create();
                        builder_fail.show();

                        break;

                    case 153:
                        Bundle bun_refused = msg.getData();
                        String id_refusing = bun_refused.getString("id_refuseing");

                        final AlertDialog.Builder builder_addContactRefused = new AlertDialog.Builder(MainActivity.this);
                        builder_addContactRefused.setCancelable(false);
                        builder_addContactRefused.setTitle("添加好友反馈:");
                        builder_addContactRefused.setMessage("你申请添加ID为 " + id_refusing + " 的用户为好友，被对方拒绝！");
                        builder_addContactRefused.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                builder_addContactRefused.setCancelable(true);
                            }
                        });

                        builder_addContactRefused.create();
                        builder_addContactRefused.show();

                        break;

                    case 154:
                        Bundle bun_agreed = msg.getData();
                        String id_agreed = bun_agreed.getString("id_agreed");

                        final AlertDialog.Builder builder_addContactAgreed = new AlertDialog.Builder(MainActivity.this);
                        builder_addContactAgreed.setCancelable(false);
                        builder_addContactAgreed.setTitle("添加好友反馈:");

                        builder_addContactAgreed.setMessage("ID为 " + id_agreed + " 的用户已通过你的好友申请！");
                        builder_addContactAgreed.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                builder_addContactAgreed.setCancelable(true);

                                final Message request_Contact = new Message();
                                request_Contact.setType(MessageType.REQUEST_CONTACT);

                                new Thread(){
                                    @Override
                                    public void run() {
                                        try {
                                            ObjectOutputStream request_contact_oos = new ObjectOutputStream(ClientConnectToServerThread.getSocket().getOutputStream());
                                            request_contact_oos.writeObject(request_Contact);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }
                        });

                        builder_addContactAgreed.create();
                        builder_addContactAgreed.show();

                        break;

                    case 2001:
                        Bundle bundle_deleted = msg.getData();
                        String sender_id = bundle_deleted.getString("sender_id");
                        String sender_NickName = bundle_deleted.getString("sender_nickName");

                        final AlertDialog.Builder builder_deleted = new AlertDialog.Builder(MainActivity.this);
                        builder_deleted.setTitle("联系人更新");
                        builder_deleted.setCancelable(false);
                        builder_deleted.setMessage("用户 " + sender_NickName + "(" + sender_id + ")  解除了与你的好友关系"  );
                        builder_deleted.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                builder_deleted.setCancelable(true);

                                new Thread(){
                                    @Override
                                    public void run() {
                                        Message request_Contact = new Message();
                                        request_Contact.setType(MessageType.REQUEST_CONTACT);
                                        try {
                                            Thread.sleep(1500);

                                            ObjectOutputStream request_contact_oos = new ObjectOutputStream(ClientConnectToServerThread.getSocket().getOutputStream());
                                            request_contact_oos.writeObject(request_Contact);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();


                            }
                        });

                        builder_deleted.create();
                        builder_deleted.show();

                        break;
                }
            }
        };

        mMessage.setOnClickListener(this);
        mFriend.setOnClickListener(this);
        mAbout.setOnClickListener(this);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View view = inflater.inflate(R.layout.add_friend_layout, null);


                builder.setCancelable(false);
                builder.setView(view);

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        builder.setCancelable(false);
                    }
                });

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText id_edittext = (EditText) view.findViewById(R.id.added_friend_id);
                        String id = id_edittext.getText().toString();

                        //将添加好友的消息发送到服务器
                        final Message id_message = new Message();
                        id_message.setContent(id);
                        id_message.setSender(sender_id);
                        id_message.setSenderNickname(sender_nickName);

                        id_message.setType(MessageType.ADD_CONTACT);
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    ObjectOutputStream oos = new ObjectOutputStream(ClientConnectToServerThread.getSocket().getOutputStream());
                                    oos.writeObject(id_message);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        builder.setCancelable(true);
                    }
                });
                builder.create();
                builder.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.new_enter_from_left,R.anim.old_exit_to_right);
    }

    private void setDefaultFragment() {
        fm = getFragmentManager();
        transaction = fm.beginTransaction();
        contact_fragment = new Contact_Fragment();
        transaction.replace(R.id.content, contact_fragment);
        transaction.commit();
    }

    /*必须要使得  mMessage || mFriend || mAbout = true, 即同一时刻必须要有一个按钮是蓝色的
      那么，当一个按钮本身是蓝色，再次点击该按钮，此时应该什么都不做。只有当点击了其他按钮时才能够使该按钮变成灰色
      当点击一个按钮时，代表该按钮被选中，其颜色变为蓝色，同时FrameLayout显示与其相对应的Fragment.
     */
    @Override
    public void onClick(View v) {
        fm = getFragmentManager();
        transaction = fm.beginTransaction();

        switch (v.getId()) {
            case R.id.message:
                if (messageIsFocused == false) {
                    mMessage.setImageResource(R.mipmap.message_on);
                    title_Top.setText("消息");
                    //当切换到消息页面的时候，联系人界面上的添加好友按钮要设置为不可见
                    addFriend.setVisibility(View.INVISIBLE);

                    //只有一个图标能够在同一时刻表示为蓝色
                    mFriend.setImageResource(R.mipmap.friend_close);
                    mAbout.setImageResource(R.mipmap.about_close);

                    messageIsFocused = true;
                    friendIsFocused = false;
                    aboutIsFocused = false;

                    if (message_fragment == null) {
                        message_fragment = new Message_Fragment();
                    }
                    transaction.replace(R.id.content, message_fragment);
                }
                break;

            case R.id.contact:
                if (friendIsFocused == false) {
                    mFriend.setImageResource(R.mipmap.friend_on);
                    title_Top.setText("联系人");
                    addFriend.setVisibility(View.VISIBLE);

                    //只有一个图标能够在同一时刻表示为蓝色
                    mMessage.setImageResource(R.mipmap.message_close);
                    mAbout.setImageResource(R.mipmap.about_close);

                    friendIsFocused = true;
                    messageIsFocused = false;
                    aboutIsFocused = false;

                    if (contact_fragment == null) {
                        contact_fragment = new Contact_Fragment();
                    }
                    transaction.replace(R.id.content, contact_fragment);
                }
                break;

            case R.id.other:
                if (aboutIsFocused == false) {
                    mAbout.setImageResource(R.mipmap.about_on);
                    title_Top.setText("关于");
                    addFriend.setVisibility(View.INVISIBLE);

                    //只有一个图标能够在同一时刻表示为蓝色
                    mMessage.setImageResource(R.mipmap.message_close);
                    mFriend.setImageResource(R.mipmap.friend_close);

                    aboutIsFocused = true;
                    messageIsFocused = false;
                    friendIsFocused = false;

                    if (about_fragment == null) {
                        about_fragment = new About_Fragment();
                    }
                    transaction.replace(R.id.content, about_fragment);
                }
                break;
        }
        transaction.commit();
    }
}

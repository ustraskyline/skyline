package com.example.yang.skyline.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yang.skyline.Control.ClientConnectToServerThread;
import com.example.yang.skyline.R;

import java.io.IOException;
import java.io.ObjectOutputStream;

import Model.Message;
import Model.MessageType;

/**
 * Created by yang on 2016/4/19.
 */
public class Setting_In_About_Fragment_Activity extends Activity implements View.OnClickListener {
    private ImageView back_button;
    private TextView back_textview, exit_application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.setting_in_about_layout);

        back_button = (ImageView) findViewById(R.id.back_to_setting);
        back_textview = (TextView) findViewById(R.id.exit_to_setting);
        exit_application = (TextView) findViewById(R.id.exit_this_application);

        back_button.setOnClickListener(this);
        back_textview.setOnClickListener(this);
        exit_application.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); //super.onBackPressed()会自动调用finish()方法,关闭当前Activity
        overridePendingTransition(R.anim.new_enter_from_left, R.anim.old_exit_to_right);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_to_setting:
            case R.id.exit_to_setting:
                finish();
                overridePendingTransition(R.anim.new_enter_from_left, R.anim.old_exit_to_right);

                break;

            case R.id.exit_this_application:
                Log.i("yang","execute exit operation");
                final AlertDialog.Builder builder = new AlertDialog.Builder(Setting_In_About_Fragment_Activity.this);
                Log.i("yang","1");
                builder.setCancelable(false);
                Log.i("yang","2");
                builder.setTitle("退出应用程序");
                Log.i("yang","3");
                builder.setMessage("你确定注销并退出吗?");
                Log.i("yang","4");
                builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        builder.setCancelable(true);

//                        Log.i("yang","5");
//                        // 广播发消息给MainActivity.java,后者接收消息，然后finish()自己。
//                        Intent intent_exit_application = new Intent();
//                        intent_exit_application.setAction("exit_application");
//                         Setting_In_About_Fragment_Activity.this.sendBroadcast(intent_exit_application);
//                        Log.i("yang","The broadcast that make app exited have send");
//
//                        finish();
//                        overridePendingTransition(R.anim.old_exit_to_right,R.anim.new_enter_from_left);



                        final Message exit_message = new Message();
                        exit_message.setType(MessageType.EXIT);

                        new Thread(){
                            @Override
                            public void run() {
                                try {
                                    ObjectOutputStream oos_DeleteContact = new ObjectOutputStream(ClientConnectToServerThread.getSocket().getOutputStream());
                                    oos_DeleteContact.writeObject(exit_message);
                                    Log.i("yang","The message that client exit have send");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();

                        Intent intent = new Intent(Setting_In_About_Fragment_Activity.this, Login_Activity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.new_enter_from_left,R.anim.old_exit_to_right);

                    }
                });

                builder.setNegativeButton("取消",new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        builder.setCancelable(true);
                    }
                });

                builder.create();
                builder.show();

                break;
        }

    }
}

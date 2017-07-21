package com.example.yang.skyline.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import Model.User;
import Model.UserOperation;

import com.example.yang.skyline.Control.SkylineClient;
import com.example.yang.skyline.R;

/**
 * Created by yang on 2016/4/28.
 */
public class Register_activity extends Activity {
    private ImageView back_pic;
    private TextView back_text, register_notice;
    private EditText id_edittext, password_edittext, nick_edittext;

    private RadioGroup sex_choice;
    private Button register_button;

    private User user;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.register_layout);

        user = new User();

        back_pic = (ImageView) findViewById(R.id.back_to_login_activity_imageview);
        back_text = (TextView) findViewById(R.id.back_to_login_activity_textview);
        id_edittext = (EditText) findViewById(R.id.id_in_register_activity);
        password_edittext = (EditText) findViewById(R.id.password_in_register_activity);
        nick_edittext = (EditText) findViewById(R.id.nick_in_register_activity);
        sex_choice = (RadioGroup) findViewById(R.id.sex_radiogroup);
        register_notice = (TextView) findViewById(R.id.notice_of_register);
        register_button = (Button) findViewById(R.id.confirm_register);

        sex_choice.setOnCheckedChangeListener(new MyRadioClickListener());

        back_pic.setOnClickListener(new MyClickListener());
        back_text.setOnClickListener(new MyClickListener());
        register_notice.setOnClickListener(new MyClickListener());
        register_button.setOnClickListener(new MyClickListener());


        //当用户注册成功后，用对话框给出提示信息
        mHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 1123) {

                    //Toast.makeText(getApplicationContext(),"接收到了服务器的消息",Toast.LENGTH_SHORT).show();

                    AlertDialog.Builder builder = new AlertDialog.Builder(Register_activity.this);
                    builder.setTitle("来自服务器的反馈:");
                    builder.setMessage("用户注册成功，需要返回登录界面登录吗？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            overridePendingTransition(R.anim.new_enter_from_left, R.anim.old_exit_to_right);
                        }
                    });

                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //只是把控件上的信息消去，但是User的各项属性已经设定了，所以还需要调用User类的set()方法消去User的属性值
                            id_edittext.setText("");
                            password_edittext.setText("");
                            nick_edittext.setText("");
                            sex_choice.clearCheck();

                            user.setId("");
                            user.setPassword("");
                            user.setNickName("");
                            user.setSex("");
                        }
                    });

                    builder.create();
                    builder.show();
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); //super.onBackPressed()会自动调用finish()方法,关闭当前Activity
        overridePendingTransition(R.anim.new_enter_from_left, R.anim.old_exit_to_right);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event){
//        if(keyCode == KeyEvent.KEYCODE_BACK){
//            finish();
//            overridePendingTransition(R.anim.new_enter_from_left,R.anim.old_exit_to_right);
//        }
//        return true;
//    }

    class MyClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.back_to_login_activity_imageview:
                case R.id.back_to_login_activity_textview:
                    finish();
                    overridePendingTransition(R.anim.new_enter_from_left, R.anim.old_exit_to_right);
                    break;

                case R.id.notice_of_register:

                    final AlertDialog.Builder builder = new AlertDialog.Builder(Register_activity.this);
                    builder.setTitle("注册注意事项：");
                    builder.setMessage("1. 用户id和密码只能包含英文和数字，且必须以英文字母开头\n" +
                            "2. 昵称可以包含汉字、英文、数字以及特殊符号\n" +
                            "3. id、密码和昵称都不能超过20个字符\n");
                    builder.setCancelable(false);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            builder.setCancelable(true);
                        }
                    });
                    builder.create();
                    builder.show();

                    break;

                case R.id.confirm_register:
                    //进行用户id、密码、昵称、性别的非空判断
                    if (TextUtils.isEmpty(id_edittext.getText()) || TextUtils.isEmpty(password_edittext.getText())
                            || TextUtils.isEmpty(nick_edittext.getText()) || TextUtils.isEmpty(user.getSex())) {
                        Toast.makeText(Register_activity.this, "用户信息没有填写完全!", Toast.LENGTH_SHORT).show();
                    } else {
                        user.setId(id_edittext.getText().toString());
                        user.setPassword(password_edittext.getText().toString());
                        user.setNickName(nick_edittext.getText().toString());
                        user.setOperation(UserOperation.REGISTER);

                        //开启一个新的线程，向服务器注册用户
                        new Thread() {
                            public void run() {
                                boolean registerSucceed = new SkylineClient(Register_activity.this).RegisterToServer(user);
                                if (registerSucceed) {
                                    mHandler.sendEmptyMessage(1123);
                                }
                            }

                            ;
                        }.start();
                    }
                    break;
            }
        }
    }

    class MyRadioClickListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (R.id.male_in_register_activity == checkedId) {
                user.setSex("男");
            } else {
                user.setSex("女");
            }
        }
    }
}

package com.example.yang.skyline.UI;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import Model.User;
import Model.UserOperation;

import com.example.yang.skyline.Control.SkylineClient;
import com.example.yang.skyline.R;

/**
 * Created by yang on 2016/4/27.
 */
public class Login_Activity extends Activity {
    private EditText id_edittext, password_edittext;
    private TextView user_register;
    private Button login_button;
    public static User user;
    private CheckBox rememberID, rememberPassword;

//    private static ArrayList<User> contacts = null;//该客户端的联系人列表
    public static Handler mhandler;

    //记录登录者帐号和密码的SharedPreferences
//    private SharedPreferences  id_passsword;
//    private SharedPreferences.Editor id_password_Editor;

    private String user_id, user_password;

    private SharedPreferences pref; //记录登录到此界面时id、password选择框要不要被选中
    private SharedPreferences.Editor pref_Editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_layout);

        user = new User();

        pref = getSharedPreferences("pref_info",MODE_PRIVATE);
        pref_Editor = pref.edit();

        //默认情况下不记录用户帐号
        boolean pref_id = pref.getBoolean("id_boolean",false);
        Log.i("bool","写入了吗？ " + pref.contains("id_boolean"));
        Log.i("bool","需要记录帐号吗？ " +  pref_id);
        boolean pref_password = pref.getBoolean("password_boolean",false);


//        id_passsword = getSharedPreferences("client_info",MODE_PRIVATE);
//        id_password_Editor = id_passsword.edit();


        id_edittext = (EditText) findViewById(R.id.edittext_user_id);
        password_edittext = (EditText) findViewById(R.id.edittext_user_password);
        login_button = (Button) findViewById(R.id.login_to_mainactivity);
        rememberID = (CheckBox) findViewById(R.id.remember_the_id);
        rememberPassword = (CheckBox) findViewById(R.id.remember_the_password);

        if(pref_id){
           // String user_id = id_passsword.getString("user_id","");
            id_edittext.setText(user_id);
            rememberID.setChecked(true);
        }else{
            rememberID.setChecked(false);
        }

        if(pref_password){
           // String user_password = id_passsword.getString("user_password","");
            password_edittext.setText(user_password);
            rememberPassword.setChecked(true);
        }else{
            rememberPassword.setChecked(false);
        }



        login_button.setOnClickListener(new MyClickListener());
        user_register = (TextView) findViewById(R.id.new_user_register);
        user_register.setOnClickListener(new MyClickListener());



        rememberID.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    pref_Editor.putBoolean("id_boolean",true);
                    Log.i("bool","===>写入了吗？ " + pref.contains("id_boolean"));
                 //   id_password_Editor.putString("user_id",id_edittext.getText().toString());
                    Log.i("bool","--->需要记录帐号");
                }else{
                    pref_Editor.putBoolean("id_boolean",false);

                 //   if(id_passsword.contains("user_id")){
                 //       id_password_Editor.remove("user_id");
                 //   }
                    Log.i("bool","--->不记录帐号");
                }
            }
        });


        rememberPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    pref_Editor.putBoolean("password_boolean",true);
                //    id_password_Editor.putString("user_password",password_edittext.getText().toString());
                    Log.i("bool","--->需要记录密码");
                }else{
                    pref_Editor.putBoolean("password_boolean",false);
                //    if(id_passsword.contains("user_password")){
               //         id_password_Editor.remove("user_password");

                //    }
                    Log.i("bool","--->不记录密码");
                }
            }
        });

        mhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 700:

                        //在打开到Contact_Fragment页面之前，先把该客户端的联系人列表获取到
//                        Bundle contact_bundle = msg.getData();
//                        contacts = (ArrayList<User>) contact_bundle.getSerializable("contact_list");
//                        Toast.makeText(Login_Activity.this,"好友数目为：" + contacts.size(), Toast.LENGTH_SHORT).show();


                    case 1123:
                        Intent login_intent = new Intent(Login_Activity.this, MainActivity.class);
                        startActivity(login_intent);
                        overridePendingTransition(R.anim.new_enter_from_right, R.anim.old_exit_to_left);
                        break;

                    case 3211:
                        Toast.makeText(getApplicationContext(), "用户名或密码错误", Toast.LENGTH_SHORT).show();
                        break;


                    default:
                        Toast.makeText(getApplicationContext(), "出现意外情况!", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        id_edittext.setText("");
        id_edittext.clearFocus();
        password_edittext.setText("");
        password_edittext.clearFocus();

    }

    public static User getUser() {
        return user;
    }

    //处理点击事件的内部类
    class MyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.login_to_mainactivity:
                    if (id_edittext.getText().toString().equals("") || password_edittext.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "帐号或密码不可为空!", Toast.LENGTH_SHORT).show();
                    } else {
                        //执行登录进程
                        user.setId(id_edittext.getText().toString());
                        user.setPassword(password_edittext.getText().toString());
                        user.setOperation(UserOperation.LOGIN);

                        //开启一个新线程登录
                        new Thread() {
                            @Override
                            public void run() {
                                boolean loginSucceed = new SkylineClient(Login_Activity.this).LoginToServer(user);
                                if (loginSucceed) {
                                    mhandler.sendEmptyMessage(1123);
                                } else {
                                    mhandler.sendEmptyMessage(3211);
                                }
                            }
                        }.start();
                    }

                    break;
                case R.id.new_user_register:
                    Intent register_intent = new Intent(Login_Activity.this, Register_activity.class);
                    startActivity(register_intent);
                    overridePendingTransition(R.anim.new_enter_from_right, R.anim.old_exit_to_left);
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}

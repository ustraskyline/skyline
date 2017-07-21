package com.example.yang.skyline.UI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yang.skyline.Control.ClientConnectToServerThread;
import com.example.yang.skyline.Control.SkylineClient;
import com.example.yang.skyline.R;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import Model.MessageType;
import Model.User;

/**
 * Created by yang on 2016/4/12.
 */
public class About_Fragment extends Fragment implements View.OnClickListener, View.OnLongClickListener{
    private ImageView client_pic;
    private TextView message_Notificate, setting, nickName_client, id_client;
    int position = 0;

    private String client_info = SkylineClient.myself_information;
    private String info[]  = client_info.split("_");
    private String sender_id = info[0];
    private String sender_nickName = info[1];


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_about, container, false);

        message_Notificate = (TextView) view.findViewById(R.id.new_message_notification);
        setting = (TextView) view.findViewById(R.id.setting_in_about);
        client_pic = (ImageView) view.findViewById(R.id.client_pic);
        client_pic.setImageResource(MainActivity.contact_pics[position]);

        nickName_client = (TextView) view.findViewById(R.id.nickName_in_about);
        nickName_client.setText("昵称：" + sender_nickName);
        id_client = (TextView) view.findViewById(R.id.id_in_about);
        id_client.setText("Id：" + sender_id);

        message_Notificate.setOnClickListener(this);
        setting.setOnClickListener(this);

        client_pic.setOnLongClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_message_notification:
                Intent intent_message_notificate = new Intent(getActivity(), New_Message_Notificate_Activity.class);
                startActivity(intent_message_notificate);
                getActivity().overridePendingTransition(R.anim.new_enter_from_right, R.anim.old_exit_to_left);

                break;

            case R.id.setting_in_about:
                Intent intent_setting = new Intent(getActivity(), Setting_In_About_Fragment_Activity.class);
                startActivity(intent_setting);
                getActivity().overridePendingTransition(R.anim.new_enter_from_right, R.anim.old_exit_to_left);

                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId() == R.id.client_pic){
            ImageView  pic1,pic2,pic3,pic4,pic5,pic6,pic7,pic8,pic9;

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.contact_pics,null);

            pic1 = (ImageView) view.findViewById(R.id.pic_1);
            pic2 = (ImageView) view.findViewById(R.id.pic_2);
            pic3 = (ImageView) view.findViewById(R.id.pic_3);
            pic4 = (ImageView) view.findViewById(R.id.pic_4);
            pic5 = (ImageView) view.findViewById(R.id.pic_5);
            pic6 = (ImageView) view.findViewById(R.id.pic_6);
            pic7 = (ImageView) view.findViewById(R.id.pic_7);
            pic8 = (ImageView) view.findViewById(R.id.pic_8);
            pic9 = (ImageView) view.findViewById(R.id.pic_9);



            pic1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = 0;
                }
            });

            pic2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = 1;
                }
            });

            pic3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = 2;
                }
            });

            pic4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = 3;
                }
            });

            pic5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = 4;
                }
            });

            pic6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = 5;
                }
            });

            pic7.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = 6;
                }
            });

            pic8.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = 7;
                }
            });

            pic9.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = 8;
                }
            });

            builder.setView(view);
            builder.setCancelable(false);

            builder.setTitle("请选择头像图片：");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    builder.setCancelable(true);
                }
            });

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    builder.setCancelable(true);
                    client_pic.setImageResource(MainActivity.contact_pics[position]);
                    MainActivity.client_avatar = position;


                    final Model.Message update_avatar = new Model.Message();
                    update_avatar.setType(MessageType.UPDATE_AVATAR);
                    update_avatar.setContent(String.valueOf(position));
                    final ObjectOutputStream[] oos_update_avatar = {null};

                    new Thread(){
                        @Override
                        public void run() {
                            try {
                                oos_update_avatar[0] = new ObjectOutputStream(ClientConnectToServerThread.getSocket().getOutputStream());
                                oos_update_avatar[0].writeObject(update_avatar);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();


                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            layoutParams.width = 665;
            layoutParams.height = 895;
            dialog.getWindow().setAttributes(layoutParams);
        }
        return true;
    }
}

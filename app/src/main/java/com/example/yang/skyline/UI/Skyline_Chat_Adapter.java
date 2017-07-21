package com.example.yang.skyline.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yang.skyline.Control.SkylineClient;
import com.example.yang.skyline.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yang on 2016/5/12.
 */
public class Skyline_Chat_Adapter extends BaseAdapter {
    private ArrayList<HashMap<String, Object>> chatList;
    private TextView textview;
    private Context context;

    private String client_info = SkylineClient.myself_information;
    private String info[]  = client_info.split("_");
    private int client_Avatar = Integer.parseInt(info[3]);

    private ImageView avatar_in_conversation = null; //聊天列表中的头像

    private int[] item_layout = {R.layout.conversation_item_layout_me, R.layout.conversation_item_layout_other};
    private int[] chater_info = {R.id.chat_text_me, R.id.chat_text_other};


    public Skyline_Chat_Adapter(Context context, ArrayList<HashMap<String, Object>> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @Override
    public int getCount() {
        return chatList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int who = (Integer) chatList.get(position).get("person");
        convertView = LayoutInflater.from(context).inflate(item_layout[who == Conversaction_Activity.ME ? 0 : 1], null);
        if(who == Conversaction_Activity.ME){
             avatar_in_conversation = (ImageView) convertView.findViewById(R.id.chat_image_me);
             avatar_in_conversation.setImageResource(MainActivity.contact_pics[client_Avatar]);
        }else if(who == Conversaction_Activity.OTHER){
             avatar_in_conversation = (ImageView) convertView.findViewById(R.id.chat_image_other);
             avatar_in_conversation.setImageResource(MainActivity.contact_pics[Conversaction_Activity.avatar_index]);
        }


        textview = (TextView) convertView.findViewById(chater_info[who]);
        textview.setText(chatList.get(position).get("text").toString());

        return convertView;
    }
}

package com.example.yang.skyline.UI;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.yang.skyline.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Model.User;

/**
 * Created by yang on 2016/4/12.
 */
public class Message_Fragment extends Fragment {
    private ListView listview_Message;
    private SimpleAdapter adapter_messageFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_message, container, false);

        listview_Message = (ListView) view.findViewById(R.id.listview_message);

        adapter_messageFragment = new SimpleAdapter(getActivity(),
                MainActivity.message_fragment_MainActivity,
                R.layout.item_layout_from_listview_message,//用MainActivity中保存的数据源来填充listview
                new String[]{"contact_avatar","contact_nickName", "contact_latestMessage"}, new int[]{R.id.contact_avatar_in_message,R.id.contact_id_in_message, R.id.contact_message});
        listview_Message.setAdapter(adapter_messageFragment);

        listview_Message.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //---------------用以Conversation_Activity关闭与打开时的备份指定联系人消息的容器--------------
                //String contact_id = contacts_list.get(position).getId();
                HashMap<String,Object> map = MainActivity.message_fragment_MainActivity.get(position);
                String contact_id = (String) map.get("id");
                String contact_name = (String) map.get("contact_nickName");

                User user_inner = new User();
                user_inner.setId(contact_id);
                user_inner.setNickName(contact_name);

                if(!MainActivity.message_list_MainActivity.containsKey(contact_id)){

                    ArrayList<HashMap<String,Object>> message_ArrayList = new ArrayList<HashMap<String,Object>>();
                    MainActivity.message_list_MainActivity.put(contact_id,message_ArrayList);

                }
                //----------------------------------------------------------------------------------------

                Intent intent = new Intent(getActivity(), Conversaction_Activity.class);
                intent.putExtra("source","Contact_Fragment");
                intent.putExtra("user_info", user_inner);
                startActivity(intent);
                //两个参数：第一个是新Activity进入时的动画，第二个是旧Activity退出时的动画
                getActivity().overridePendingTransition(R.anim.new_enter_from_right, R.anim.old_exit_to_left);
            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter_messageFragment.notifyDataSetChanged();
    }
}

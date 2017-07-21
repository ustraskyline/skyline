package com.example.yang.skyline.UI;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
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
import java.util.List;

import Model.Message;
import Model.MessageType;
import Model.User;

/**
 * Created by yang on 2016/4/12.
 */
public class Contact_Fragment extends Fragment {
    private ArrayList<HashMap<String, Object>> list; //往listView中填充的形式上的数据源，里面的每一个item都是Hashmap
    private ListView listview_Contact;
    private SimpleAdapter adapter;
    public static ArrayList<User> contacts_list;  //从服务器返回的往listView上填充的实际数据源，里面的每一项都是User对象
    private User contact_info;

    private String client_info = SkylineClient.myself_information;
    private String info[]  = client_info.split("_");
    private String sender_nickName = info[1];


    private static Context context;

    public static Handler contactFragment_Handler;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();


        final Message update_contact = new Message();
        update_contact.setType(MessageType.REQUEST_CONTACT);

        new Thread(){
            @Override
            public void run() {
                ObjectOutputStream oos_update_contact = null;
                try {
                    oos_update_contact = new ObjectOutputStream(ClientConnectToServerThread.getSocket().getOutputStream());
                    oos_update_contact.writeObject(update_contact);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


        contactFragment_Handler = new Handler(){
            @Override
            public void handleMessage(android.os.Message msg) {
                //通知适配器更新好友列表,此Handler中只更新形式上的数据源，ListView是由其在onCreateView方法中被填充
                if(msg.what == 1001){
                    Bundle latest_Contacts_Bundle = msg.getData();
                    //得到最新的实际上的数据源
                    ArrayList<User> latest_Contacts = (ArrayList<User>) latest_Contacts_Bundle.getSerializable("latest_contact_list");
                    //在此处用新的实际数据源填充新的形式数据源，然后用新的形式数据源去填充原来的形式数据源
                    //因为此时onCreateView方法已经执行完成，原来的绘制过程并不会再次进行
                    ArrayList<HashMap<String,Object>> latest_list = new ArrayList<HashMap<String,Object>>();
                    HashMap<String,Object> map;
                    for(int i=0; i<latest_Contacts.size(); i++){
                        map = new HashMap<String,Object>();
                        contact_info = latest_Contacts.get(i);
                        //--------新增加的好友头像----------
                        int online = contact_info.getOnline();
                        map.put("contact_avatar",MainActivity.contact_pics[ online == 1 ? contact_info.getAvatar() : contact_info.getAvatar() + 9]);
                        //--------------------------------
                        map.put("contact_id",contact_info.getNickName());
                        latest_list.add(map);
                    }

                    list.clear();
                    list.addAll(latest_list);

                    //用最新的实际上的数据源刷新原来的实际数据源
                    contacts_list.clear();
                    contacts_list.addAll(latest_Contacts);

                    adapter.notifyDataSetChanged();

                }
            }
        };
    }

    public static Context getContactFragmentContext(){
        return context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_contact, container, false);
        listview_Contact = (ListView) view.findViewById(R.id.listview_contact);

        list = new ArrayList<HashMap<String,Object>>();
        HashMap<String, Object> map;

        //用实际数据源填充形式数据源
        for (int i = 0; i < contacts_list.size(); i++) {
            map = new HashMap<String, Object>();
            contact_info = contacts_list.get(i);
            int online = contact_info.getOnline();
            //根据用户是否在线，来决定是采用彩色的图片还是黑白图片显示其头像
            map.put("contact_avatar",MainActivity.contact_pics[ online == 1 ? contact_info.getAvatar() : contact_info.getAvatar() + 9]);
            map.put("contact_id", contact_info.getNickName());
            list.add(map);
        }

        adapter = new SimpleAdapter(getActivity(), list, R.layout.item_layout_from_listview_contact,
                new String[]{"contact_avatar","contact_id"}, new int[]{R.id.contact_avatar_in_contact,R.id.contact_id_in_contact});

        listview_Contact.setAdapter(adapter);
        listview_Contact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //---------------用以Conversation_Activity关闭与打开时的备份指定联系人消息的容器--------------
                String contact_id = contacts_list.get(position).getId();
                if(!MainActivity.message_list_MainActivity.containsKey(contact_id)){

                    ArrayList<HashMap<String,Object>> message_ArrayList = new ArrayList<HashMap<String,Object>>();
                    MainActivity.message_list_MainActivity.put(contact_id,message_ArrayList);

                }
                //----------------------------------------------------------------------------------------

                Intent intent = new Intent(getActivity(), Conversaction_Activity.class);
                User user = contacts_list.get(position);
                intent.putExtra("source","Contact_Fragment");
                intent.putExtra("user_info", user);
                startActivity(intent);
                //两个参数：第一个是新Activity进入时的动画，第二个是旧Activity退出时的动画
                getActivity().overridePendingTransition(R.anim.new_enter_from_right, R.anim.old_exit_to_left);


            }
        });


        listview_Contact.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


                View _view_inflater = LayoutInflater.from(getActivity()).inflate(R.layout.conversation_longclick_layout,null);

                builder.setView(_view_inflater);
                final AlertDialog dialog = builder.create();

                //---------------------------------Contact Detail--------------------------------
                TextView contact_Detail = (TextView) _view_inflater.findViewById(R.id.contact_detail);
                contact_Detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();

                        final AlertDialog.Builder builder3 = new AlertDialog.Builder(getActivity());
                        builder3.setCancelable(false);

                        View inflater_inner = LayoutInflater.from(getActivity()).inflate(R.layout.contact_detail_layout, null);
                        TextView detail_id = (TextView) inflater_inner.findViewById(R.id.contact_id_detail);
                        TextView detail_nickName = (TextView) inflater_inner.findViewById(R.id.contact_nickName_detail);
                        TextView detail_sex = (TextView) inflater_inner.findViewById(R.id.contact_sex_detail);

                        User user_detail = contacts_list.get(position);
                        detail_id.setText("帐号: " + user_detail.getId());
                        detail_nickName.setText("昵称: " + user_detail.getNickName());
                        detail_sex.setText("性别: " + user_detail.getSex());

                        builder3.setView(inflater_inner);
                        builder3.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                builder3.setCancelable(true);
                            }
                        });

                        builder3.create();
                        builder3.show();
                    }
                });

                //---------------------------------Delete Contact--------------------------------
                TextView delete_Contact = (TextView) _view_inflater.findViewById(R.id.delete_contact);


                delete_Contact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.cancel();

                        final AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                        builder2.setCancelable(false);
                        builder2.setTitle("删除联系人");
                        User user_deleted = contacts_list.get(position);
                        String nickName_Deleted = user_deleted.getNickName();
                        final String id_Deleted = user_deleted.getId();
                        builder2.setMessage("此操作也会将你从对方的联系人列表中删除，你确定删除联系人 " + nickName_Deleted + "(" + id_Deleted + ")" + " 吗？");

                        builder2.setNegativeButton("取消", new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                builder2.setCancelable(true);
                            }
                        });

                        builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                builder2.setCancelable(true);

                                final Message message_DeleteContact = new Message();
                                message_DeleteContact.setContent(id_Deleted);
                                message_DeleteContact.setSenderNickname(sender_nickName);
                                message_DeleteContact.setType(MessageType.DELETE_CONTACT);

                                new Thread(){
                                    @Override
                                    public void run() {
                                        try {
                                            ObjectOutputStream oos_DeleteContact = new ObjectOutputStream(ClientConnectToServerThread.getSocket().getOutputStream());
                                            oos_DeleteContact.writeObject(message_DeleteContact);

                                            Model.Message request_Contact = new Model.Message();
                                            request_Contact.setType(MessageType.REQUEST_CONTACT);

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

                        builder2.create();
                        builder2.show();
                    }
                });

                dialog.show();

                WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
                layoutParams.width = 400;
                layoutParams.height = 250;
                dialog.getWindow().setAttributes(layoutParams);

                return true;
            }
        });

        return view;
    }
}

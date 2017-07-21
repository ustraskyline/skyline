package com.example.yang.skyline.Control;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.yang.skyline.UI.Contact_Fragment;
import com.example.yang.skyline.UI.MainActivity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import Model.Message;
import Model.MessageType;
import Model.User;

/**
 * Created by yang on 2016/5/8.
 * 负责管理用户登录和注册
 */
public class SkylineClient {
    private Context context;
    private Message message;
    private Socket toServer;
    private static ArrayList<User> contacts = null;//该客户端的联系人列表

    public static String myself_information;

    public SkylineClient(Context context) {
        this.context = context;
    }


    //登录操作除了需要得到服务器返回的消息类型是不是MessageType.LOGIN_SUCCESS，还需要得到服务器返回给用户的message的内容
    public boolean LoginToServer(User user) {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            toServer = ManageSocketToServer.getSocket();
            oos = new ObjectOutputStream(toServer.getOutputStream());
            oos.writeObject(user);
            //注册和登录，发送的都是user对象，但是user对象的operation属性不同，服务器根据operation进行不同的操作
            ois = new ObjectInputStream(toServer.getInputStream());
            message = (Message) ois.readObject();



            if (MessageType.LOGIN_SUCCESS.equals(message.getType())) {

                /**
                 * 当进入到此if块时，表示服务器已经进入了ServerToClientThread线程，可以接收Message对象了
                 */

                //向服务器请求好友列表
                Model.Message request_Contact = new Model.Message();
                request_Contact.setContent("First");
                request_Contact.setType(MessageType.REQUEST_CONTACT);

                ObjectOutputStream request_contact_oos = null;

                ObjectInputStream get_contact_ois = null;
                try {
                    request_contact_oos = new ObjectOutputStream(toServer.getOutputStream());
                    request_contact_oos.writeObject(request_Contact);

                    get_contact_ois = new ObjectInputStream(toServer.getInputStream());
                    Message contact_message = (Message) get_contact_ois.readObject();

                    contacts = contact_message.getContactList();

                    //登录时会先进入到SkylineClient类，若验证通过，才会进入到MainActivity中
                    //程序执行到此处时，MainActivity尚未创建
                   // MainActivity.myself_information = message.getContent();
                    myself_information = message.getContent();

                    Contact_Fragment.contacts_list = contacts;
                    ClientConnectToServerThread ccts = new ClientConnectToServerThread(context, toServer);
                    ccts.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Log.i("tangz","return FALSE");
        return false;
    }

    //注册用户时，只需要得到服务器返回的消息类型是不是MessageType.REGISTER_SUCCESS即可
    public boolean RegisterToServer(User user) {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            toServer = ManageSocketToServer.getSocket();
            oos = new ObjectOutputStream(toServer.getOutputStream());
            oos.writeObject(user);

            ois = new ObjectInputStream(toServer.getInputStream());
            message = (Message) ois.readObject();

            if (MessageType.REGISTER_SUCCESS.equals(message.getType())) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}

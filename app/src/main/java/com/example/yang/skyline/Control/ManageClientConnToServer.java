package com.example.yang.skyline.Control;

import java.util.HashMap;

/**
 * Created by yang on 2016/5/8.
 */
public class ManageClientConnToServer {
    //创建一个HashMap,里面存放的都是ClientConnectToServerThread类的对象
    private static HashMap map = new HashMap<String, ClientConnectToServerThread>();

    //用户的id就是ClientConnectToServerThread对象的键名
    public static void addClientConnectToServerThread(String id, ClientConnectToServerThread ccts) {
        map.put(id, ccts);
    }

    //可以通过id取得该线程
    public static ClientConnectToServerThread getClientConnectToServerThread(String id) {
        return (ClientConnectToServerThread) map.get(id);
    }
}

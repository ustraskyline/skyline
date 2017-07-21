package com.example.yang.skyline.Control;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by yang on 2016/5/8.
 * 该类的主要作用：获取和关闭与服务器连接的Socket
 */
public class ManageSocketToServer {
    private static Socket socket;

    public synchronized static Socket getSocket() {
        try {
            socket = new Socket("192.168.1.106", 3312);

            return socket;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

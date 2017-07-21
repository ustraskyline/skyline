package com.example.Model;

import java.io.Serializable;

/**
 * Created by yang on 2016/5/4.
 */
public class User implements Serializable {
    private int id;
    private String password;
    private String nickname;
    private String sex;
    private String time;//该用户发送消息的时间

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPassword(){
        return password;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public String getNickname(){
        return nickname;
    }

    public void setSex(String sex){
        this.sex = sex;
    }

    public String getSex(){
        return sex;
    }

    public void setTime(String time){
        this.time = time;
    }

    public String getTime(){
        return time;
    }
}


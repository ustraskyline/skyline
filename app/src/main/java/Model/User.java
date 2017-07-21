package Model;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String password;
    private String nickName;
    private int avatar;
    private String sex;
    private int onLine;
    private String operation;

    private static final long serialVersionUID = 51;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setOnline(int onLine) {
        this.onLine = onLine;
    }

    public int getOnline() {
        return onLine;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

}



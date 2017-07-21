package Model;

public class MessageType {
    public static final String CONN_SUCCESS = "0";             //表示客户端和服务器连接成功

    public static final String LOGIN_SUCCESS = "1";                  //表明登录成功
    public static final String REGISTER_SUCCESS = "11";        //表明注册成功
    public static final String LOGIN_FAIL = "2";                     //表明用户登陆失败
    public static final String REGISTER_FAIL = "22";          //表明用户注册失败
    public static final String COMMON_MESSAGE = "3";           //普通信息包
    public static final String GET_ONLINE_FRIENDS = "4";       //要求在线好友
    public static final String RETUEST_ONLINE_FRIENDS = "5";   //返回在线好友
    public static final String LOGIN = "7";                    //请求验证登陆

    public static final String ADD_CONTACT = "8";              //添加好友
    public static final String ADD_CONTACT_FAIL = "888";

    public static final String ADD_CONTACT_REFUSED = "8888";
    public static final String ADD_CONTACT_AGREED = "8881";

    public static final String DELETE_CONTACT = "9";           //删除好友
    public static final String CONTACT_DELETED = "91";

    public static final String REQUEST_CONTACT = "54";     //请求服务器返回该帐号的联系人
    public static final String REQUEST_CONTACT_SUCCESS = "541";
    public static final String REQUEST_CONTACT_FAIL = "542";
   // public static final String CONTACT_NOT_ONLINE =

    public static final String UPDATE_AVATAR = "601";  //更新头像
    public static final String AUDIO_MESSAGE = "1001";  //所传输的消息是音频

    public static final String EXIT = "3001";    //用户下线

}



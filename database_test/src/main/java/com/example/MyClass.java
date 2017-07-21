package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MyClass {

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://127.0.0.1:3306/yang_database";

            Connection conn = DriverManager.getConnection(url, "root",
                    "marine1942");

            // Statement是传入一个固定的SQL语句，每次执行都要进行编译，执行时才传入SQL语句
            // String sql =
            // "insert into student values('9','zhanghan','male','68')";
            // Statement stmt = conn.createStatement();
            // int affectedRow = stmt.executeUpdate(sql);

             //PreparedStatement允许数据库预编译SQL语句，以后只传入SQL命令的参数，避免数据库每次都编译固定的SQL语句，得到该对象时就传入SQL语句
             String sql = "insert into student values(?,?,?,?)";
             PreparedStatement stmt = conn.prepareStatement(sql);

             stmt.setInt(1, 145);
             stmt.setString(2, "wangfang");
             stmt.setString(3, "female");
             stmt.setInt(4, 78);

             int affectedRow = stmt.executeUpdate();

             System.out.println("插入了 " + affectedRow + " 笔数据 ");

            stmt.close();

            if (!conn.isClosed()) {
                conn.close();
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

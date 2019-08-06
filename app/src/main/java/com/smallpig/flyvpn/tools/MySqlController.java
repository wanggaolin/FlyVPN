package com.smallpig.flyvpn.tools;

import com.smallpig.flyvpn.core.Properties;

import java.sql.*;

public class MySqlController {

    Connection con;
    static MySqlController instance;

    private MySqlController() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
    }

    public static MySqlController getInstance() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (instance == null) {
            instance = new MySqlController();
        }
        return instance;
    }

    public boolean RegisterUser(String user, String password) throws SQLException {
        con = DriverManager.getConnection(Properties.mysqlURL, "smallpig", "156318aq");
        Statement state = con.createStatement();

        String sqlselect = "select * from users where user='" + user + "'";
        ResultSet result = state.executeQuery(sqlselect);
        while (result.next()) {
            con.close();
            return false;
        }

        String sqlinsert = "insert into users values('" + user + "','" + password + "',0)";
        state.executeUpdate(sqlinsert);

        con.close();
        return true;
    }

    public boolean LoginUser(String user, String password) throws SQLException {
    con = DriverManager.getConnection(Properties.mysqlURL, "smallpig", "156318aq");
    Statement state = con.createStatement();
    String sql = "select * from users where user='" + user + "' and password='" + password + "'";
    ResultSet result = state.executeQuery(sql);

    while (result.next()) {
        con.close();
        return true;
    }

    con.close();
    return false;
}

    public long getFlow(String user) throws SQLException {
        con = DriverManager.getConnection(Properties.mysqlURL, "smallpig", "156318aq");
        Statement state = con.createStatement();
        String sql = "select * from users where user='" + user + "'";
        ResultSet result = state.executeQuery(sql);

        while (result.next()) {
            long rate = result.getLong("rate");
            con.close();
            return rate;
        }

        con.close();
        throw new SQLException("查询剩余流量失败！");
    }

    public void setFlow(String user,long rate) throws SQLException {
        con = DriverManager.getConnection(Properties.mysqlURL, "smallpig", "156318aq");
        Statement state = con.createStatement();
        String sql ="update users set rate="+rate+" where user='"+user+"'";
        state.executeUpdate(sql);
    }
}

package com.smallpig.flyvpn.tools;

import com.smallpig.flyvpn.core.Global;

import java.sql.*;

public class MySqlController {

    Connection con;

    public MySqlController() throws ClassNotFoundException,SQLException, IllegalAccessException, InstantiationException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        con = DriverManager.getConnection(Global.mysqlURL, "smallpig", "156318aq");
    }

    public boolean RegisterUser(String user, String password) throws SQLException {
        Statement state = con.createStatement();

        String sqlselect = "select * from users where user='" + user + "'";
        ResultSet result = state.executeQuery(sqlselect);
        while (result.next()) {
            return false;
        }

        String sqlinsert = "insert into users values('" + user + "','" + password + "',0)";
        state.executeUpdate(sqlinsert);
        return true;
    }

    public boolean LoginUser(String user, String password) throws SQLException {
        Statement state = con.createStatement();
        String sql = "select * from users where user='" + user + "' and password='" + password + "'";
        ResultSet result = state.executeQuery(sql);

        while (result.next()) {
            return true;
        }

        return false;
    }

    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

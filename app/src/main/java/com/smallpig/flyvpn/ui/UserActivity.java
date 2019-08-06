package com.smallpig.flyvpn.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.smallpig.flyvpn.R;
import com.smallpig.flyvpn.tools.MySqlController;

import java.sql.SQLException;

public class UserActivity extends AppCompatActivity {

    TextView usernameTextView;
    TextView flowTextView;
    EditText rechargeEditText;
    Button rechargeButton;
    Button signoutButton;

    SharedPreferences sp;
    String username;
    long flow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        usernameTextView = findViewById(R.id.textview_username);
        sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        username = sp.getString("username", "用户名");
        usernameTextView.setText(username);

        flowTextView = findViewById(R.id.textview_flow);
        try {
            showFlow();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(UserActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        signoutButton = findViewById(R.id.button_signout);
        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
                sp.edit().putBoolean("islogin", false).apply();

                startActivity(new Intent(UserActivity.this, MainActivity.class));
            }
        });

        rechargeEditText = findViewById(R.id.edittext_recharge);
        rechargeButton = findViewById(R.id.button_recharge);
        rechargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long rechargeFlow = Long.parseLong(rechargeEditText.getText().toString()) * 1024 * 1024 * 1024;
                flow += rechargeFlow;
                try {
                    MySqlController.getInstance().setFlow(username, flow);
                    showFlow();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(UserActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void showFlow() throws IllegalAccessException, InstantiationException, ClassNotFoundException, SQLException {
        flow = MySqlController.getInstance().getFlow(username);
        flowTextView.setText(Formatter.formatFileSize(UserActivity.this, flow));
        sp.edit().putLong("flow", flow).apply();
    }
}
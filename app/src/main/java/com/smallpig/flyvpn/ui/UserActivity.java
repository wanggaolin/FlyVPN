package com.smallpig.flyvpn.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.smallpig.flyvpn.R;

public class UserActivity extends AppCompatActivity {

    TextView usernameTextView;
    Button signoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        usernameTextView = findViewById(R.id.textview_username);
        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        usernameTextView.setText(sp.getString("username", "用户名"));

        signoutButton = findViewById(R.id.button_signout);
        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
                sp.edit().putBoolean("islogin", false).apply();

                startActivity(new Intent(UserActivity.this, MainActivity.class));
            }
        });
    }
}

package com.smallpig.flyvpn.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.smallpig.flyvpn.R;
import com.smallpig.flyvpn.tools.MySqlController;

public class LoginActivity extends AppCompatActivity {

    EditText userEditText;
    EditText passwordEditText;
    Button loginButton;
    Button registerButton;

    MySqlController sqlController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            sqlController = new MySqlController();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        userEditText = findViewById(R.id.edittext_username);
        passwordEditText = findViewById(R.id.edittext_password);

        loginButton = findViewById(R.id.button_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = userEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                try {
                    if (user.isEmpty() || password.isEmpty()) {
                        throw new Exception("用户名或密码为空！");
                    }

                    if (sqlController.LoginUser(user, password)) {
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    } else {
                        throw new Exception("登录失败，请检查用户名和密码！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerButton = findViewById(R.id.button_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = userEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                try {
                    if (user.isEmpty() || password.isEmpty()) {
                        throw new Exception("用户名或密码为空！");
                    }

                    if (sqlController.RegisterUser(user, password)) {
                        Toast.makeText(LoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    } else {
                        throw new Exception("注册失败，用户已存在！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        sqlController.close();
        super.onDestroy();
    }
}

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
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

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

                wxPay();

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

    void wxPay(){
        IWXAPI api = WXAPIFactory.createWXAPI(UserActivity.this, null);
        api.registerApp("你的appid");
        PayReq req = new PayReq();
        req.appId           = "wx8888888888888888";//你的微信appid
        req.partnerId       = "1900000109";//商户号
        req.prepayId        = "WX1217752501201407033233368018";//预支付交易会话ID
        req.nonceStr        = "5K8264ILTKCH16CQ2502SI8ZNMTM67VS";//随机字符串
        req.timeStamp       = "1412000000";//时间戳
        req.packageValue    = "Sign=WXPay";//扩展字段,这里固定填写Sign=WXPay
        req.sign            = "C380BEC2BFD727A4B6845133519F3AD6";//签名
 //            req.extData         = "app data"; // optional
        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
        api.sendReq(req);
    }
}
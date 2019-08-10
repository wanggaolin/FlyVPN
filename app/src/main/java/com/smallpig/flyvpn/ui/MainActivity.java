package com.smallpig.flyvpn.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;
import com.smallpig.flyvpn.R;
import com.smallpig.flyvpn.core.Properties;
import com.smallpig.flyvpn.service.NotificationService;
import com.smallpig.flyvpn.tools.MySqlController;
import com.vm.shadowsocks.core.AppProxyManager;
import com.vm.shadowsocks.core.LocalVpnService;
import com.vm.shadowsocks.core.ProxyConfig;
import com.vm.shadowsocks.ui.AppManager;

public class MainActivity extends AppCompatActivity implements LocalVpnService.onStatusChangedListener {

    Button proxyListButton;
    Switch globalSwitch;
    ToggleButton proxyToggleButton;

    SwipeRefreshLayout refreshLayout;
    ListView nodeListView;
    NodeListAdapter nodeListadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new AppProxyManager(this);

        nodeListView = findViewById(R.id.listview_node);
        initialize();

        refreshLayout = findViewById(R.id.refreshlayout_nodelist);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Properties.isProxyRefresh = true;
                initialize();
                refreshLayout.setRefreshing(false);
            }
        });

        proxyListButton = findViewById(R.id.button_proxylist);
        proxyListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AppManager.class));
            }
        });

        globalSwitch = findViewById(R.id.switch_global);

        SharedPreferences sp = getSharedPreferences("proxy", Context.MODE_PRIVATE);
        boolean globalChecked = sp.getBoolean("global", false);
        globalSwitch.setChecked(globalChecked);
        ProxyConfig.Instance.globalMode = globalChecked;

        globalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!buttonView.isPressed())
                    return;

                ProxyConfig.Instance.globalMode = isChecked;

                SharedPreferences sp = getSharedPreferences("proxy", Context.MODE_PRIVATE);
                sp.edit().putBoolean("global", isChecked).apply();

                if (isChecked)
                    Toast.makeText(MainActivity.this, "全局模式开启", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this, "全局模式关闭", Toast.LENGTH_SHORT).show();
            }
        });

        proxyToggleButton = findViewById(R.id.togglebutton_proxy);
        proxyToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!buttonView.isPressed())
                    return;

                SharedPreferences spl = getSharedPreferences("login", Context.MODE_PRIVATE);
                if (!spl.getBoolean("islogin", false)) {
                    Toast.makeText(MainActivity.this, "未登录，无法使用代理！", Toast.LENGTH_SHORT).show();
                    proxyToggleButton.setChecked(false);
                    return;
                }

                Intent intent = LocalVpnService.prepare(MainActivity.this);

                if (isChecked) {
                    if (intent == null) {
                        startVPNService();
                    } else {
                        startActivityForResult(intent, 1985);
                    }
                } else {
                    LocalVpnService.IsRunning = false;
                    stopService(new Intent(MainActivity.this, NotificationService.class));
                }
            }
        });
    }

    void initialize() {
        try {
            nodeListadapter = new NodeListAdapter(MainActivity.this);
            nodeListView.setAdapter(nodeListadapter);
            setListViewHeightBasedOnChildren(nodeListView);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    void startVPNService() {
        LocalVpnService.ProxyUrl = Properties.proxyURL;
        startService(new Intent(this, LocalVpnService.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, NotificationService.class));
        } else {
            startService(new Intent(this, NotificationService.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem loginMenuItem = menu.findItem(R.id.action_login);

        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        if (sp.getBoolean("islogin", false)) {
            loginMenuItem.setTitle(sp.getString("username", "登录"));
        } else {
            loginMenuItem.setTitle("登录");
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_login) {
            SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
            if (sp.getBoolean("islogin", false)) {
                startActivity(new Intent(this, UserActivity.class));
            } else {
                startLoginDialog();
            }
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void startLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_login, null);

        final EditText userEditText = view.findViewById(R.id.edittext_username);
        final EditText passwordEditText = view.findViewById(R.id.edittext_password);
        Button loginButton = view.findViewById(R.id.button_login);
        Button registerButton = view.findViewById(R.id.button_register);

        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        String username = sp.getString("username", "");
        if (!username.isEmpty()) userEditText.setText(username);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        String user = userEditText.getText().toString();
                        String password = passwordEditText.getText().toString();
                        try {
                            if (user.isEmpty() || password.isEmpty()) {
                                throw new Exception("用户名或密码为空！");
                            }

                            if (MySqlController.getInstance().LoginUser(user, password)) {
                                Looper.prepare();
                                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                                SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
                                sp.edit().putBoolean("islogin", true).putString("username", user).apply();

                                startActivity(new Intent(MainActivity.this, UserActivity.class));
                                Looper.loop();
                            } else {
                                throw new Exception("登录失败，请检查用户名和密码！");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Looper.prepare();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        } finally {
                            finish();
                        }
                    }
                }.start();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        String user = userEditText.getText().toString();
                        String password = passwordEditText.getText().toString();
                        try {
                            if (user.isEmpty() || password.isEmpty()) {
                                throw new Exception("用户名或密码为空！");
                            }

                            if (MySqlController.getInstance().RegisterUser(user, password)) {
                                Looper.prepare();
                                Toast.makeText(MainActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else {
                                throw new Exception("注册失败，用户已存在！");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Looper.prepare();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        } finally {
                            finish();
                        }
                    }
                }.start();
            }
        });

        builder.setTitle("请登录").setView(view).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1985) {
            if (resultCode == RESULT_OK) {
                startVPNService();
            } else {
                proxyToggleButton.setChecked(false);
                LocalVpnService.IsRunning = false;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStatusChanged(String status, Boolean isRunning) {
        proxyToggleButton.setChecked(isRunning);
        onLogReceived(status);
    }

    @Override
    public void onLogReceived(String logString) {
        System.out.println(logString);
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
package com.smallpig.flyvpn.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.smallpig.flyvpn.R;
import com.smallpig.flyvpn.core.Global;
import com.vm.shadowsocks.core.AppProxyManager;
import com.vm.shadowsocks.core.LocalVpnService;
import com.vm.shadowsocks.core.ProxyConfig;
import com.vm.shadowsocks.ui.AppManager;

public class MainActivity extends AppCompatActivity implements LocalVpnService.onStatusChangedListener {

    Button proxyListButton;
    Switch globalSwitch;
    ToggleButton proxyToggleButton;

    ListView nodeListView;
    NodeListAdapter nodeListadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new AppProxyManager(this);

        nodeListView = findViewById(R.id.listview_node);
        nodeListadapter = new NodeListAdapter(MainActivity.this);
        nodeListView.setAdapter(nodeListadapter);
        setListViewHeightBasedOnChildren(nodeListView);

        proxyListButton = findViewById(R.id.button_proxylist);
        proxyListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AppManager.class));
            }
        });

        globalSwitch = findViewById(R.id.switch_global);
        globalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!buttonView.isPressed())
                    return;

                ProxyConfig.Instance.globalMode = isChecked;
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

                Intent intent = LocalVpnService.prepare(MainActivity.this);

                if (isChecked) {
                    if (intent == null) {
                        startVPNService();
                    } else {
                        startActivityForResult(intent, 1985);
                    }
                } else {
                    LocalVpnService.IsRunning = false;
                }
            }
        });
    }

    void startVPNService() {
        LocalVpnService.ProxyUrl = Global.proxyURL;
        startService(new Intent(this, LocalVpnService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_login) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

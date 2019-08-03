package com.smallpig.flyvpn.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.smallpig.flyvpn.R;
import com.smallpig.flyvpn.core.Properties;
import com.smallpig.flyvpn.tools.JsonReader;
import com.smallpig.flyvpn.tools.PingNet;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NodeListAdapter extends BaseAdapter {

    Context context;

    Map<String, Boolean> states;

    public NodeListAdapter(Context context) throws JSONException, IOException {
        this.context = context;
        states = new HashMap<String, Boolean>();
        JsonReader.GetNodeList(Properties.jsonPath);
    }

    @Override
    public int getCount() {
        return JsonReader.count;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = View.inflate(context, R.layout.activity_node_list_item, null);
        } else {
            view = convertView;
        }

        ImageView imageView = view.findViewById(R.id.nodelist_imgaeview_country);
        TextView textView = view.findViewById(R.id.nodelist_textview_name);
        final TextView pingView = view.findViewById(R.id.nodelist_textview_ping);
        RadioButton radioButton = view.findViewById(R.id.nodelist_radiobutton);

        switch (JsonReader.nodeList.get(position).getCountry()) {
            case US:
                imageView.setBackgroundResource(R.drawable.us);
                break;
            case UK:
                imageView.setBackgroundResource(R.drawable.uk);
                break;
            case Singapore:
                imageView.setBackgroundResource(R.drawable.singapore);
                break;
            case Japan:
                imageView.setBackgroundResource(R.drawable.japan);
                break;
            case HongKong:
                imageView.setBackgroundResource(R.drawable.hongkong);
                break;
        }
        textView.setText(JsonReader.nodeList.get(position).getName());

        if(Properties.isProxyRefresh) {
            try {
                PingNet pingNet = new PingNet(JsonReader.nodeList.get(position).getUrl());
                pingNet.ping();
                if (pingNet.isResult()) {
                    pingView.setText(pingNet.getPingTime());
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                e.printStackTrace();
                pingView.setText("超时");
            }
        }


        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Properties.isProxyRefresh = false;
                for (String key : states.keySet()) {
                    states.put(key, false);
                }
                states.put(String.valueOf(position), true);
                NodeListAdapter.this.notifyDataSetChanged();

                Properties.proxyURL = JsonReader.nodeList.get(position).getUrl();
            }
        });
        boolean res = false;
        if (states.get(String.valueOf(position)) == null || states.get(String.valueOf(position)) == false) {
            res = false;
            states.put(String.valueOf(position), false);
        } else
            res = true;

        radioButton.setChecked(res);

        return view;
    }
}

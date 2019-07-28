package com.smallpig.flyvpn.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.smallpig.flyvpn.R;
import com.smallpig.flyvpn.core.Global;
import com.smallpig.flyvpn.tools.JsonReader;

import java.util.HashMap;
import java.util.Map;

public class NodeListAdapter extends BaseAdapter {

    Context context;

    Map<String, Boolean> states;

    public NodeListAdapter(Context context) {
        this.context = context;
        states = new HashMap<String, Boolean>();
        JsonReader.GetNodeList(Global.jsonPath);
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
        RadioButton radioButton = view.findViewById(R.id.nodelist_radiobutton);

        switch (JsonReader.nodeList.get(position).getCountry()) {
            case US:
                imageView.setBackgroundResource(R.mipmap.us);
                break;
            case UK:
                imageView.setBackgroundResource(R.mipmap.uk);
                break;
            case Singapore:
                imageView.setBackgroundResource(R.mipmap.singapore);
                break;
            case Japan:
                imageView.setBackgroundResource(R.mipmap.japan);
                break;
            case HongKong:
                imageView.setBackgroundResource(R.mipmap.hongkong);
                break;
        }
        textView.setText(JsonReader.nodeList.get(position).getName());

        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String key : states.keySet()) {
                    states.put(key, false);
                }
                states.put(String.valueOf(position), true);
                NodeListAdapter.this.notifyDataSetChanged();

                Global.proxyURL = JsonReader.nodeList.get(position).getUrl();
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

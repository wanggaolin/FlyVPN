package com.smallpig.flyvpn.tools;

import com.smallpig.flyvpn.core.Country;
import com.smallpig.flyvpn.core.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonReader {

    public static int count;
    public static ArrayList<Node> nodeList;

    public static void GetNodeList(String url) {
        nodeList = new ArrayList<Node>();
        String file = DownloadFile.LoadURL(url);

        try {
            JSONObject root = new JSONObject(file);
            JSONArray nodeArray = root.getJSONArray("nodes");
            count = nodeArray.length();

            for (int i = 0; i < nodeArray.length(); i++) {
                JSONObject jsonNode = nodeArray.getJSONObject(i);
                Node node = new Node();
                node.setCountry(Enum.valueOf(Country.class, jsonNode.getString("country")));
                node.setName(jsonNode.getString("name"));
                node.setUrl(jsonNode.getString("url"));
                nodeList.add(node);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

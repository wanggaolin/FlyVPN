package com.smallpig.flyvpn.tools;

import com.smallpig.flyvpn.ui.MainActivity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFile {

    public static String LoadURL(String urlPath) {
        int HttpResult; // 服务器返回的状态
        String ee = new String();
        try {
            URL url = new URL(urlPath); // 创建URL
            URLConnection urlconn = url.openConnection(); // 试图连接并取得返回状态码
            urlconn.connect();
            HttpURLConnection httpconn = (HttpURLConnection) urlconn;
            HttpResult = httpconn.getResponseCode();
            if (HttpResult != HttpURLConnection.HTTP_OK) {
                System.out.print("无法连接到");
            } else {
                int filesize = urlconn.getContentLength(); // 取数据长度
                InputStreamReader isReader = new InputStreamReader(urlconn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(isReader);
                StringBuffer buffer = new StringBuffer();
                String line; // 用来保存每行读取的内容
                line = reader.readLine(); // 读取第一行
                while (line != null) { // 如果 line 为空说明读完了
                    buffer.append(line); // 将读到的内容添加到 buffer 中
                    buffer.append(" "); // 添加换行符
                    line = reader.readLine(); // 读取下一行
                }
                System.out.print(buffer.toString());
                ee = buffer.toString();
            }
        } catch (FileNotFoundException e) {
            MainActivity.instance.showException(e);
        } catch (IOException e) {
            MainActivity.instance.showException(e);
        }
        return ee;
    }

    public static String openFile(String filePath) {
        try {
            File file = new File(filePath);
            StringBuilder localStrBulider = new StringBuilder();
            if (file.isFile() && file.exists()) {
                InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), "utf-8");
                BufferedReader bufferReader = new BufferedReader(inputStreamReader);
                String lineStr = null;

                while ((lineStr = bufferReader.readLine()) != null) {
                    localStrBulider.append(lineStr);
                }
                bufferReader.close();
                inputStreamReader.close();
            }
            return localStrBulider.toString();
        } catch (Exception e) {
            MainActivity.instance.showException(e);
        }
        return null;
    }
}

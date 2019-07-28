package com.smallpig.flyvpn.tools;

import android.os.StrictMode;

import java.io.*;
import java.net.URL;

public class DownloadFile {

    public static String LoadURL(String urlPath) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

        try {
            URL url = new URL(urlPath);
            StringBuilder localStrBuilder = new StringBuilder();
            InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String lineStr = null;

            while ((lineStr = bufferedReader.readLine()) != null) {
                localStrBuilder.append(lineStr);
            }
            bufferedReader.close();
            inputStreamReader.close();

            return localStrBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
            e.printStackTrace();
        }
        return null;
    }
}

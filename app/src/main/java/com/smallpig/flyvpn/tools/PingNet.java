package com.smallpig.flyvpn.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PingNet {
    private String ip;
    private String pingTime = null;
    private boolean result = false;

    private StringBuffer resultBuffer = new StringBuffer();

    public PingNet(String ssIP) {
        ip = ssIP.split("@")[1].split(":")[0];
    }

    public String getPingTime() {
        return pingTime;
    }

    public boolean isResult() {
        return result;
    }

    public void ping() throws IOException {
        String line = null;
        Process process = null;
        BufferedReader successReader = null;
        String command = "ping -c 1 -w 5 " + ip;
        process = Runtime.getRuntime().exec(command);
        if (process == null) {
            pingTime = null;
            result = false;
            return;
        }
        successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = successReader.readLine()) != null) {
            resultBuffer.append(line + "\n");
            String time;
            if ((time = getTime(line)) != null) {
                pingTime = time;
                result = true;
            }
        }
        process.destroy();
        successReader.close();
    }

    private String getTime(String line) {
        String[] lines = line.split("\n");
        String time = null;
        for (String l : lines) {
            if (!l.contains("time="))
                continue;
            int index = l.indexOf("time=");
            time = l.substring(index + "time=".length());
        }
        return time;
    }
}
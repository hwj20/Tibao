package com.example.tibao;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Huang
 */

public class DownloadUtil {
    private static final String TAG = "DownloadUtil";


    public static String getHttpResult(String urlStr, int port){
        try {
            URL targetUrl = new URL(urlStr);
            URL url = new URL(targetUrl.getProtocol(),targetUrl.getHost(), port, targetUrl.getFile());
            HttpURLConnection connect=(HttpURLConnection)url.openConnection();
            InputStream input=connect.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String line = null;
            System.out.println(connect.getResponseCode());
            StringBuilder sb = new StringBuilder();
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            Log.d(TAG, sb.toString());
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static JSONObject readJsonString(String jsonString){
        return null;
    }


}

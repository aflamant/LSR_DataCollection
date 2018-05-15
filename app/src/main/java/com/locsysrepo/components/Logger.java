package com.locsysrepo.components;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.sql.Timestamp;


import static android.provider.Settings.Secure.*;

/**
 * Created by valentin
 */

public class Logger {

    private final static String LOGS_FOLDER = "Localization/";

    private BufferedWriter bw;
    private Context context;
    private String buildingName;

    public Logger(Context context, String prefix, String buildingName) {
        this.context = context;
        this.buildingName = buildingName;
        open(prefix);
    }

    private synchronized void write(String data) {
        try {
            Log.i("logger write", data);
            bw.append(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void writeLine(String data) {
        try {
            bw.append(data).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    private boolean isExternalStorageWritable() {
        return isExternalStorageAvailable() && !isExternalStorageReadOnly();
    }

    private void open(String prefix) {
        if (!isExternalStorageWritable()) {
            Toast.makeText(context, "Check if memory card is inserted", Toast.LENGTH_LONG).show();
            return;
        }

        if (bw == null) {
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            String filename = timestamp.toString();
            filename = prefix + "_" + filename.substring(0, filename.length() - 4).replaceAll(":", "-").replaceAll(" ", "_") + ".JSON";

            File file = new File(context.getExternalFilesDir(LOGS_FOLDER), filename);

            FileWriter fw = null;
            try {
                fw = new FileWriter(file.getAbsoluteFile());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(fw != null) {
                bw = new BufferedWriter(fw);
                LogHeader logHeader = new LogHeader(buildingName, timestamp);
                writeLine(logHeader.toString());
            } else throw new NullPointerException();
        }

    }

    class LogHeader {
        private String phoneID;
        private String buildingName;
        private String timestamp;

        LogHeader(String buildingName, Timestamp timestamp) {
            this.phoneID = getString(context.getContentResolver(), ANDROID_ID);
            this.timestamp = timestamp.toString();
            this.buildingName = buildingName;
        }

        JSONObject toJSON() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("phoneID", phoneID);
                jsonObject.put("buildingName", buildingName);
                jsonObject.put("timestamp", timestamp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        public String toString() {
            return toJSON().toString();
        }
    }

    public void close() {
        if (bw != null) {
            try {
                this.bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bw = null;
        }
    }

}

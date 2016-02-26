package com.signal.dis.powersignal;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by User on 20.11.2015.
 */
public class WifiService extends Service {


    private DatabaseHelper mDatabaseHelper;
    public SQLiteDatabase sdb;

    public Timer mTimer;
    public MyTimerTask mMyTimerTask;

    public String WEIGHT_JSON = "";
    public String response;
    public String log;
    public WifiManager mainWifiObj;
    public PendingIntent pi;
    public String FLAG = "FALSE";

    public void onCreate() {
        super.onCreate();
        mDatabaseHelper = new DatabaseHelper(this, "mydatabase.db", null, 6);
        sdb = mDatabaseHelper.getWritableDatabase();
        mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        log = "test_heatmap";
        Log.d("LOGI", "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LOGI", "onStartCommand");
        pi = intent.getParcelableExtra("pend");
        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 500, 60000);
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }
        Log.d("LOGI", "onDestroy");
    }

    public IBinder onBind(Intent intent) {
        Log.d("LOGI", "onBind");
        return null;
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            if(!(mainWifiObj.getConnectionInfo().getSSID().toString().equals("<unknown ssid>"))) {
                Send();
                int i = 2;
                try {
                    // сообщаем об окончании задачи
                    Intent intent = new Intent().putExtra(TabMenu.PARAM_RESULT, i);
                    pi.send(WifiService.this, TabMenu.STATUS_FINISH, intent);

                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    public void Send(){
        Cursor cursor = sdb.query(mDatabaseHelper.DATABASE_TABLE_2, new String[]{DatabaseHelper._ID, DatabaseHelper.LOC_X_COLUMN,
                        DatabaseHelper.LOC_Y_COLUMN, DatabaseHelper.TIME_COLUMN, DatabaseHelper.WEIGHT_COLUMN, DatabaseHelper.WEIGHT_COLUMN_HUNDREND,
                        DatabaseHelper.CID, DatabaseHelper.SECTOR, DatabaseHelper.LAC}, null, null, null, null, null) ;

        while (cursor.moveToNext()) {
            String loc_x = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOC_X_COLUMN));
            String loc_y = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOC_Y_COLUMN));
            //String adr = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ADDRESS_COLUMN));
            String time = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TIME_COLUMN));
            String weight = cursor.getString(cursor.getColumnIndex(DatabaseHelper.WEIGHT_COLUMN));
            String weight_hundred = cursor.getString(cursor.getColumnIndex(DatabaseHelper.WEIGHT_COLUMN_HUNDREND));
            String cid = cursor.getString(cursor.getColumnIndex(DatabaseHelper.CID));
            String sector = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SECTOR));
            String lac = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LAC));

            if(cursor.isFirst() && cursor.isLast())
                WEIGHT_JSON += ("[{\"x\":"+loc_x+",\"y\":"+loc_y+",\"weight\":"+weight+",\"time\":"+time+",\"weight_hundred\":"+weight_hundred+",\"cid\":"+cid+sector+",\"lac\":"+lac+"}]");
            else
            if(cursor.isFirst())
                WEIGHT_JSON += ("[{\"x\":"+loc_x+",\"y\":"+loc_y+",\"weight\":"+weight+",\"time\":"+time+",\"weight_hundred\":"+weight_hundred+",\"cid\":"+cid+sector+",\"lac\":"+lac+"},");
            else
            if(cursor.isLast())
                WEIGHT_JSON += ("{\"x\":"+loc_x+",\"y\":"+loc_y+",\"weight\":"+weight+",\"time\":"+time+",\"weight_hundred\":"+weight_hundred+",\"cid\":"+cid+sector+",\"lac\":"+lac+"}]");
            else
                WEIGHT_JSON += ("{\"x\":"+loc_x+",\"y\":"+loc_y+",\"weight\":"+weight+",\"time\":"+time + ",\"weight_hundred\":"+weight_hundred+",\"cid\":"+cid+sector+",\"lac\":"+lac+ "},");
        }
        // не забываем закрывать курсор
        cursor.close();
        Log.d("LOGI", "WEIGHT_JSON" + WEIGHT_JSON);

        if(!(WEIGHT_JSON.equals("")))
            new RequestTask().execute(getString(R.string.adress));
    }

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                //создаем запрос на сервер
                DefaultHttpClient hc = new DefaultHttpClient();
                ResponseHandler<String> res = new BasicResponseHandler();
                //он у нас будет посылать post запрос
                HttpPost postMethod = new HttpPost(params[0]);
                //будем передавать два параметра
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                //передаем параметры из наших текстбоксов
                //логин
                nameValuePairs.add(new BasicNameValuePair("unique", md5Custom(log)));
                //пароль
                nameValuePairs.add(new BasicNameValuePair("data", WEIGHT_JSON));
                Log.d("LOGI", "WEIGHT_JSON_REQEUST" + WEIGHT_JSON);
                //собераем их вместе и посылаем на сервер
                postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

                response = hc.execute(postMethod, res);
                JSONURL(response);

            } catch (Exception e) {
                System.out.println("Exp=" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            WEIGHT_JSON = "";
            Log.d("LOGI", FLAG);
            if(FLAG.equals("TRUE")) {
                sdb.delete("WeightSignal_2", null, null);
                sdb.delete("WeightSignal", null, null);
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    public void JSONURL(String result) {

        try {
            //создали читателя json объектов и отдали ему строку - result
            JSONObject json = new JSONObject(result);
            //дальше находим вход в наш json им является ключевое слово data
            final JSONArray urls = json.getJSONArray("data");
            //проходим циклом по всем нашим параметрам
            FLAG = urls.getJSONObject(0).getString("flag").toString();
            Log.d("LOGI", "TIME: " + FLAG);
        } catch (JSONException e) {
            Log.e("log_tag", "Error parsing data " + e.toString());
        }

    }

    public static String md5Custom(String st) {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            // тут можно обработать ошибку
            // возникает она если в передаваемый алгоритм в getInstance(,,,) не существует
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 ){
            md5Hex = "0" + md5Hex;
        }

        return md5Hex;
    }
}

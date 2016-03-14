package com.signal.dis.powersignal.OldVersion;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.signal.dis.powersignal.DatabaseHelper;
import com.signal.dis.powersignal.GPSTracker;
import com.signal.dis.powersignal.R;
import com.signal.dis.powersignal.TabMenu;

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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by User on 20.11.2015.
 */
public class InteractiveActivity extends Activity {

    private DatabaseHelper mDatabaseHelper;
    public SQLiteDatabase sdb;

    public Button determine;

    public double dol,shi;
    public String dolstr, shistr;
    public GPSTracker gps;
    public Locale local;
    public DateFormat df;
    public Date currentDate;
    public long unixTime;
    public String time_mil;

    public TelephonyManager Tel;
    public MyPhoneStateListener MyListener;
    public static final int UNKNOW_CODE = 99;
    public int MAX_SIGNAL_DBM_VALUE = 31;
    public String weight;
    public String weight_hundred;
    public GsmCellLocation gsmCell;
    public String mccmnc, mcc,mnc;
    public String yandex_json;
    public String response;
    public String cid_lat,cid_lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interactive);

        mDatabaseHelper = new DatabaseHelper(this, "mydatabase.db", null, 5);
        sdb = mDatabaseHelper.getWritableDatabase();

        gps = new GPSTracker(InteractiveActivity.this);

        local = new Locale("ru","RU");
        df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, local);

        MyListener   = new MyPhoneStateListener();
        Tel = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        gsmCell = (GsmCellLocation)Tel.getCellLocation();

        determine = (Button) findViewById(R.id.determine);

        determine.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(Tel.getSimOperatorName().equals("PHoenix"))
                {
                    Determine();
                    cid_json();
                }
                else
                    Toast.makeText(getApplicationContext(), "Пожалуйста, проверьте, чтобы sim-карта PHoenix находилась в первом слоте или замените саму карточку на соответствующую.", Toast.LENGTH_LONG).show();
                //Determine();
            }
        });
    }

    public void Determine(){
        GPSsetting();
        TIMEsetting();

        ContentValues newValues = new ContentValues();
        // Задайте значения для каждого столбца
        newValues.put(DatabaseHelper.LOC_X_COLUMN, dolstr);
        newValues.put(DatabaseHelper.LOC_Y_COLUMN, shistr);
        newValues.put(DatabaseHelper.TIME_COLUMN, time_mil);
        newValues.put(DatabaseHelper.WEIGHT_COLUMN, weight);
        newValues.put(DatabaseHelper.WEIGHT_COLUMN_HUNDREND, weight_hundred);
        newValues.put(DatabaseHelper.CID, gsmCell.getCid());
        newValues.put(DatabaseHelper.LAC, gsmCell.getLac());
        // Вставляем данные в таблицу
        sdb.insert("WeightSignal", null, newValues);

        ContentValues newValues_2 = new ContentValues();
        // Задайте значения для каждого столбца
        newValues_2.put(DatabaseHelper.LOC_X_COLUMN, dolstr);
        newValues_2.put(DatabaseHelper.LOC_Y_COLUMN, shistr);
        newValues_2.put(DatabaseHelper.TIME_COLUMN, unixTime);
        newValues_2.put(DatabaseHelper.WEIGHT_COLUMN, weight);
        newValues_2.put(DatabaseHelper.WEIGHT_COLUMN_HUNDREND, weight_hundred);
        newValues_2.put(DatabaseHelper.CID, gsmCell.getCid());
        newValues_2.put(DatabaseHelper.LAC, gsmCell.getLac());
        // Вставляем данные в таблицу
        sdb.insert("WeightSignal_2", null, newValues_2);

        CreateList();
        TabMenu.progressBar.setProgress(TabMenu.progress);
    }

    public void GPSsetting(){

        if(gps.canGetLocation()) {

            shi = gps.getLongitude();
            shistr = "" + shi;

            dol = gps.getLatitude();
            dolstr = "" + dol;
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    public void TIMEsetting(){
        currentDate = new Date();
        time_mil = df.format(currentDate);
        unixTime = System.currentTimeMillis() / 1000L;
    }

    private class MyPhoneStateListener extends PhoneStateListener
    {
        /* Get the Signal strength from the provider, each tiome there is an update */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            super.onSignalStrengthsChanged(signalStrength);
            if (null != signalStrength && signalStrength.getGsmSignalStrength() != UNKNOW_CODE) {
                float signalStrengthPercent = calculateSignalStrengthInPercent(signalStrength.getGsmSignalStrength());
                //viewModel.setSignalStrengthString(IntegerHelper.getString(signalStrengthPercent));
                weight = "" + (int)signalStrengthPercent;
                weight_hundred = "" + (-113 + 2 * signalStrength.getGsmSignalStrength());
                TabMenu.progress = (int)signalStrengthPercent;

                mccmnc = Tel.getNetworkOperator();
                mcc = mccmnc.substring(0, 3);
                mnc = mccmnc.substring(3, mccmnc.length());
                /*Log.d("LOGI", "GSS: " + signalStrength.getGsmSignalStrength());
                Log.d("LOGI", "GBER: " + signalStrength.getGsmBitErrorRate());
                Log.d("LOGI", "CDMAdbm: " + signalStrength.getCdmaDbm());
                Log.d("LOGI", "CDMAecio: " + signalStrength.getCdmaEcio());
                Log.d("LOGI", "EVDOdbm: " + signalStrength.getEvdoDbm());
                Log.d("LOGI", "EVDOecio: " + signalStrength.getEvdoEcio());
                Log.d("LOGI", "EVDOsnr: " + signalStrength.getEvdoSnr());
                Log.d("LOGI", "isGSM: " + signalStrength.isGsm());*/
            }
        }

        private float calculateSignalStrengthInPercent(int signalStrength) {
            //return (int) ((float) signalStrength / MAX_SIGNAL_DBM_VALUE); //* 100);
            return (int) ((float) signalStrength / MAX_SIGNAL_DBM_VALUE* 1000);
        }
    };/* End of private Class */

    public void CreateList(){
        Cursor cursor = sdb.query(mDatabaseHelper.DATABASE_TABLE, new String[]{DatabaseHelper._ID, DatabaseHelper.LOC_X_COLUMN,
                        DatabaseHelper.LOC_Y_COLUMN, DatabaseHelper.TIME_COLUMN, DatabaseHelper.WEIGHT_COLUMN_HUNDREND,
                        DatabaseHelper.CID, DatabaseHelper.LAC},
                        null, null, null, null, DatabaseHelper._ID + " DESC") ;


        TabMenu.adapter = new SimpleCursorAdapter(this, // Связь.
                R.layout.list, // Определения шаблона элемента
                cursor, // Переход к курсору, который надо запомнить.
                // Массив курсоров, которые надо запомнить.
                new String[] { "Loc_X", "Loc_Y", "Time", "Weight_hundred", "CID", "LAC"},
                // Массив, связывающий запомненные курсоры и шаблоны с ними связанные
                new int[] { R.id.loc_x, R.id.loc_y, R.id.time, R.id.weight, R.id.cid, R.id.lac });
        TabMenu.listView.setAdapter(TabMenu.adapter);
        TabMenu.listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
       /* TabMenu.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //(view).setBackgroundColor(Color.parseColor("#FF7d31df"));
                float buf = Float.valueOf(((TextView) view.findViewById(R.id.weight)).getText().toString());
                int buff = (int) buf;
            }
        });*/
    }

    public void cid_json(){
        yandex_json = "{\"common\": {\"version\": \"1.0\",\"api_key\": \"" + getString(R.string.api_key) + "\"}, \"gsm_cells\": [{\"countrycode\": " + mcc +",\"operatorid\": "
                + 01 + ",\"cellid\": " + 15043 + ",\"lac\": " + 60917 + "}]}";

        new RequestTask().execute(getString(R.string.yandex_locator));
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
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                //передаем параметры из наших текстбоксов
                //логин
                nameValuePairs.add(new BasicNameValuePair("json", yandex_json));
                //пароль
                Log.d("LOGI", "yandex_json: " + yandex_json);
                Log.d("LOGI", "yandex_json: " + nameValuePairs.get(0));
                //собераем их вместе и посылаем на сервер
                postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

                response = hc.execute(postMethod, res);
                Log.d("LOGI", "res: " + response);
                JSONURL(response);

            } catch (Exception e) {
                System.out.println("Exp=" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            yandex_json = "";
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
            final JSONArray urls = json.getJSONArray("");
            //проходим циклом по всем нашим параметрам
            Log.d("LOGI", "urls: " + json);
            cid_lat = urls.getJSONObject(0).getString("latitude").toString();
            cid_lon = urls.getJSONObject(0).getString("longitude").toString();
            Log.d("LOGI", "cid_lat: " + cid_lat);
            Log.d("LOGI", "cid_lon: " + cid_lon);
        } catch (JSONException e) {
            Log.e("log_tag", "Error parsing data " + e.toString());
        }

    }
}

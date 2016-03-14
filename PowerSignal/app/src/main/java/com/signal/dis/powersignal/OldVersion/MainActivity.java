package com.signal.dis.powersignal.OldVersion;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.signal.dis.powersignal.DatabaseHelper;
import com.signal.dis.powersignal.GPSTracker;
import com.signal.dis.powersignal.R;

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

public class MainActivity extends AppCompatActivity {

    public GPSTracker gps;
    public TextView t1,t2,t3,t4,t5;
    public TextView t6,t7,t8,t9,t10, t11, t12;
    public double dol,shi;
    public String dolstr, shistr, dolstr2, shistr2;
    public Date currentDate;
    public TelephonyManager Tel, manager;
    public MyPhoneStateListener MyListener;
    public Locale local;
    public DateFormat df;
    public String time_mil, powerSignal,unixTimeStr, powerSignal_2;
    public Button b1,b2,b3,b4,b5;
    public ProgressDialog dialog;
    public String log,pas;
    public String LOG,PAS,X,Y,WEIGHT,TIME;
    public String response;
    public String GSS, GBER, CDMAdbm, CDMAecio, EVDOdbm, EVDOecio, EVDOsnr, isGSM, LEVEL;

    private DatabaseHelper mDatabaseHelper;
    public SQLiteDatabase sdb;

    public static final int UNKNOW_CODE = 99;
    int MAX_SIGNAL_DBM_VALUE = 31;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabaseHelper = new DatabaseHelper(this, "mydatabase.db", null, 1);
        sdb = mDatabaseHelper.getWritableDatabase();

        log = "test_heatmap";
        pas = "kjsFk03kl#fkob";

        local = new Locale("ru","RU");
        df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, local);



        MyListener   = new MyPhoneStateListener();
        Tel = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);




        gps = new GPSTracker(MainActivity.this);
        GPSsetting();
        t1 = (TextView) findViewById(R.id.textView);
        t2 = (TextView) findViewById(R.id.textView2);
        t3 = (TextView) findViewById(R.id.textView3);
        t4 = (TextView) findViewById(R.id.textView4);
        t5 = (TextView) findViewById(R.id.textView5);
        t6 = (TextView) findViewById(R.id.textView6);
        t7 = (TextView) findViewById(R.id.textView7);
        t8 = (TextView) findViewById(R.id.textView8);
        t9 = (TextView) findViewById(R.id.textView9);
        t10 = (TextView) findViewById(R.id.textView10);
        t11 = (TextView) findViewById(R.id.textView11);
        t12 = (TextView) findViewById(R.id.textView12);
        b1 = (Button) findViewById(R.id.button);
        b2 = (Button) findViewById(R.id.button2);
        b3 = (Button) findViewById(R.id.button3);
        b4 = (Button) findViewById(R.id.button4);
        b5 = (Button) findViewById(R.id.button5);

        manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        Log.d("TEL", "PhoneType: " + ConvertPhoneType(manager.getPhoneType()));
        Log.d("TEL", "PhoneType: " + manager.getPhoneType());
        Log.d("TEL", "NetworkType: " + manager.getNetworkType());
        Log.d("TEL", "CellLocation: " + manager.getCellLocation());

        GsmCellLocation gsmCell = (GsmCellLocation)manager.getCellLocation();
        if(gsmCell != null)
        {
            Log.d("TEL", "Cid" + gsmCell.getCid());
            Log.d("TEL", "Lac" + gsmCell.getLac());
            Log.d("TEL", "Psc" + gsmCell.getPsc());
        }

        /*CdmaCellLocation cdmCell = (CdmaCellLocation)manager.getCellLocation();
        if(cdmCell != null)
        {
            Log.d("TEL", "getBaseStationID: " + cdmCell.getBaseStationId());
            Log.d("TEL", "NetworkID: " + cdmCell.getNetworkId());
            Log.d("TEL", "SystemId: " + cdmCell.getSystemId());
            Log.d("TEL", "Latitude: " + cdmCell.getBaseStationLatitude());
            Log.d("TEL", "Longtitude: " + cdmCell.getBaseStationLongitude());
        }*/
        WEIGHT = "";

        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                currentDate = new Date();
                time_mil = df.format(currentDate);
                long unixTime = System.currentTimeMillis() / 1000L;
                unixTimeStr = "" + unixTime;
                t1.setText(dolstr2);
                t2.setText(shistr2);
                t3.setText("" + unixTime);
                t4.setText(GSS);
                t5.setText(GBER);
                t6.setText(CDMAdbm);
                t7.setText(CDMAecio);
                t8.setText(EVDOdbm);
                t9.setText(EVDOecio);
                t10.setText(EVDOsnr);
                t11.setText(isGSM);
                t12.setText(LEVEL);

                GsmCellLocation gsmCell = (GsmCellLocation)manager.getCellLocation();
                if(gsmCell != null)
                {
                    Log.d("TEL", "Cid: " + gsmCell.getCid());
                    Log.d("TEL", "Lac: " + gsmCell.getLac());
                    Log.d("TEL", "Psc: " + gsmCell.getPsc());
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(!(powerSignal.equals("0")))
                    new RequestTask().execute(getString(R.string.adress));
                else
                    Toast.makeText(MainActivity.this, "Нет сигнала", Toast.LENGTH_SHORT).show();
            }
        });

        b3.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                ContentValues newValues = new ContentValues();
                // Задайте значения для каждого столбца
                newValues.put(DatabaseHelper.LOC_X_COLUMN, t1.getText().toString());
                newValues.put(DatabaseHelper.LOC_Y_COLUMN, t2.getText().toString());
                newValues.put(DatabaseHelper.TIME_COLUMN, t3.getText().toString());
                newValues.put(DatabaseHelper.WEIGHT_COLUMN, t4.getText().toString());
                // Вставляем данные в таблицу
                sdb.insert("WeightSignal", null, newValues);
            }
        });

        b4.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Cursor cursor = sdb.query(mDatabaseHelper.DATABASE_TABLE, new String[]{DatabaseHelper._ID, DatabaseHelper.LOC_X_COLUMN,
                                DatabaseHelper.LOC_Y_COLUMN, DatabaseHelper.TIME_COLUMN, DatabaseHelper.WEIGHT_COLUMN},
                        null, null, null, null, null) ;

                //cursor.moveToFirst();

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper._ID));
                    String loc_x = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOC_X_COLUMN));
                    String loc_y = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOC_Y_COLUMN));
                    String time = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TIME_COLUMN));
                    String weight = cursor.getString(cursor.getColumnIndex(DatabaseHelper.WEIGHT_COLUMN));


                    //Log.d("DB", "ID: " + id + " loc_x: " + loc_x + " loc_y: " + loc_y + " time: " + time + " weight: " + weight);
                    //WEIGHT = (loc_x + " : " + loc_y + " : " + time + " : " + weight);
                    if(cursor.isFirst())
                        WEIGHT += ("[{\"x\":"+loc_x+",\"y\":"+loc_y+",\"weight\":"+weight+",\"time\":"+time+"},");
                    else
                        if(cursor.isLast())
                            WEIGHT += ("{\"x\":"+loc_x+",\"y\":"+loc_y+",\"weight\":"+weight+",\"time\":"+time+"}]");
                        else
                            WEIGHT += ("{\"x\":"+loc_x+",\"y\":"+loc_y+",\"weight\":"+weight+",\"time\":"+time+"},");
                    Log.d("weight", WEIGHT);
                    //[{"x":65,"y":48,"weight":898,"time":1232133},{"x":24,"y":55,"weight":24,"time":2434133},{"x":33,"y":66,"weight":35,"time":23424133}]


                }
                // не забываем закрывать курсор
                cursor.close();
            }
        });

        b5.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                sdb.delete("WeightSignal", null, null);
            }
        });
    }

    private String ConvertPhoneType(int phoneType) {
        switch (phoneType){
            case TelephonyManager.PHONE_TYPE_GSM:
                return "GSM";
            case TelephonyManager.PHONE_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.PHONE_TYPE_NONE:
                return "NONE";
            default:
                return "Not defined";
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
    }

    /* Called when the application resumes */
    @Override
    protected void onResume()
    {
        super.onResume();
        Tel.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }


    /* —————————– */
    /* Start the PhoneState listener */
   /* —————————– */
    private class MyPhoneStateListener extends PhoneStateListener
    {
        /* Get the Signal strength from the provider, each tiome there is an update */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            super.onSignalStrengthsChanged(signalStrength);
            //Toast.makeText(getApplicationContext(), "Go to Firstdroid!!! GSM Cinr = "
            //        + String.valueOf(signalStrength.getGsmSignalStrength()), Toast.LENGTH_SHORT).show();
            //powerSignal = String.valueOf((int)((113+(-113 + 2 * signalStrength.getGsmSignalStrength()))*15.873));
            //powerSignal_2 = String.valueOf((-113 + 2 * signalStrength.getGsmSignalStrength()));
            //powerSignal_2 = String.valueOf(signalStrength.getCdmaDbm());

            powerSignal_2 = String.valueOf(signalStrength.getGsmBitErrorRate());
            if (signalStrength.isGsm()) {
                if (signalStrength.getGsmBitErrorRate() != 99)
                    powerSignal = String.valueOf((int)((113+(-113 + 2 * signalStrength.getGsmSignalStrength()))*15.873));
                else
                    powerSignal = String.valueOf((int) ((113 - (signalStrength.getGsmSignalStrength())) * 15.873));
            } else {
                powerSignal_2 = String.valueOf(signalStrength.getCdmaDbm());
            }
            GSS = "" + signalStrength.getGsmSignalStrength();
            GBER = "GBER: " + signalStrength.getGsmBitErrorRate();
            Log.d("OBJ", "" + signalStrength);
            CDMAdbm = "CDMAdbm: " + signalStrength.getCdmaDbm();
            CDMAecio = "CDMAecio: " + signalStrength.getCdmaEcio();
            EVDOdbm = "EVDOdbm: " + signalStrength.getEvdoDbm();
            EVDOecio = "EVDOecio: " + signalStrength.getEvdoEcio();
            EVDOsnr = "EVDOsnr: " + signalStrength.getEvdoSnr();
            isGSM = "isGSM: " + signalStrength.isGsm();
            LEVEL = "Level: " + signalStrength.getLevel();

            Log.d("LOGI", "GSS: " + signalStrength.getGsmSignalStrength());
            Log.d("LOGI", "GBER: " + signalStrength.getGsmBitErrorRate());
            Log.d("LOGI", "CDMAdbm: " + signalStrength.getCdmaDbm());
            Log.d("LOGI", "CDMAecio: " + signalStrength.getCdmaEcio());
            Log.d("LOGI", "EVDOdbm: " + signalStrength.getEvdoDbm());
            Log.d("LOGI", "EVDOecio: " + signalStrength.getEvdoEcio());
            Log.d("LOGI", "EVDOsnr: " + signalStrength.getEvdoSnr());
            Log.d("LOGI", "isGSM: " + signalStrength.isGsm());
            Log.d("LOGI", "Level: " + signalStrength.getLevel());

            if (null != signalStrength && signalStrength.getGsmSignalStrength() != UNKNOW_CODE) {
                int signalStrengthPercent = calculateSignalStrengthInPercent(signalStrength.getGsmSignalStrength());
                //viewModel.setSignalStrengthString(IntegerHelper.getString(signalStrengthPercent));
                LEVEL = "signGSM: " + signalStrengthPercent;
            }
        }

        private int calculateSignalStrengthInPercent(int signalStrength) {
            return (int) ((float) signalStrength / MAX_SIGNAL_DBM_VALUE * 100);
        }
    };/* End of private Class */

    public void GPSsetting(){

        if(gps.canGetLocation()) {

            dol = gps.getLongitude();
            dolstr = "Долгота: " + dol;
            dolstr2 = "" + dol;

            shi = gps.getLatitude();
            shistr = "Широта: " + shi;
            shistr2 = "" + shi;
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
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
                nameValuePairs.add(new BasicNameValuePair("data",  WEIGHT));

                //собераем их вместе и посылаем на сервер
                postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

                response = hc.execute(postMethod, res);

                JSONURL(response.toString());

            } catch (Exception e) {
                System.out.println("Exp=" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            dialog.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Загружаюсь...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
            super.onPreExecute();
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

    public void JSONURL(String result) {

        try {
            //создали читателя json объектов и отдали ему строку - result
            JSONObject json = new JSONObject(result);
            //дальше находим вход в наш json им является ключевое слово data
            final JSONArray urls = json.getJSONArray("$_POST");
            //проходим циклом по всем нашим параметрам
            LOG = urls.getJSONObject(0).getString("log").toString();
            PAS = urls.getJSONObject(0).getString("pas").toString();
            X = urls.getJSONObject(0).getString("x").toString();
            Y = urls.getJSONObject(0).getString("y").toString();
            WEIGHT = urls.getJSONObject(0).getString("weight").toString();
            TIME = urls.getJSONObject(0).getString("time").toString();
            Log.d("LOGI", "LOG: " + LOG);
            Log.d("LOGI", "PAS: " + PAS);
            Log.d("LOGI", "X: " + X);
            Log.d("LOGI", "Y: " + Y);
            Log.d("LOGI", "WEIGHT: " + WEIGHT);
            Log.d("LOGI", "TIME: " + TIME);
        } catch (JSONException e) {
            Log.e("log_tag", "Error parsing data " + e.toString());
        }

    }
}

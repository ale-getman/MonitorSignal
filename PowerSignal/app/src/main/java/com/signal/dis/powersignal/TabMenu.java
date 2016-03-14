package com.signal.dis.powersignal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by User on 20.11.2015.
 */
public class TabMenu extends Activity {

    public static ListView listView;
    public static ProgressBar progressBar;
    public static int progress;
    public static ListAdapter adapter;

    public static TabHost tabHost;
    public Intent intent, intent_2;

    public WifiManager mainWifiObj;
    public PendingIntent pi;
    public final static int STATUS_FINISH = 200;
    public final static String PARAM_RESULT = "result";
    final int TASK1_CODE = 1;
    public int result;
    public TelephonyManager manager;

    public Button determine;
    public TelephonyManager Tel;
    public MyPhoneStateListener MyListener;
    public static final int UNKNOW_CODE = 99;
    public int MAX_SIGNAL_DBM_VALUE = 31;
    public String weight;
    public String weight_hundred;
    public GsmCellLocation gsmCell;
    public String mccmnc, mcc,mnc;
    public double dol,shi;
    public String dolstr, shistr;
    public GPSTracker gps;
    public Locale local;
    public DateFormat df;
    public Date currentDate;
    public long unixTime;
    public String time_mil;
    private DatabaseHelper mDatabaseHelper;
    public SQLiteDatabase sdb;
    public String cid,sector,lac;

    public Button startService;
    public ImageButton imageButton;
    public PowerManager pm;
    public PowerManager.WakeLock wl;
    public int buf_flag;

    public Context context;
    public Intent notificationIntent;
    public PendingIntent contentIntent;

    public String TAG = "LOGI";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_menu);

        buf_flag = 0;

        Intent intent_t = new Intent();
        pi = createPendingResult(TASK1_CODE, intent_t, 0);

        startService(new Intent(TabMenu.this, WifiService.class).putExtra("pend", pi));

        mDatabaseHelper = new DatabaseHelper(this, "mydatabase.db", null, 7);
        sdb = mDatabaseHelper.getWritableDatabase();

        String querySelect = "SELECT * FROM " + DatabaseHelper.BS_TABLE;
        Cursor cursorSelect = sdb.rawQuery(querySelect,null);
        if(cursorSelect.moveToFirst())
            Log.d(TAG, "Таблица заполнена");
        else {
            Log.d(TAG, "Таблица не заполнена");
            importExcel2Sqlite();
        }
        cursorSelect.close();

        gps = new GPSTracker(TabMenu.this);

        local = new Locale("ru","RU");
        df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, local);

        mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        Log.d("LOGI", "wifi: " + mainWifiObj.getWifiState());
        //infofield = (TextView) findViewById(R.id.infoField);
        //infofield.setText("wifi: " + mainWifiObj.getConnectionInfo());
        /*if(!(mainWifiObj.getConnectionInfo().getSSID().toString().equals("<unknown ssid>")))
            if(executeCommand())
                infofield.setText("wifi true " + mainWifiObj.getConnectionInfo().getSSID().toString());
            else
                infofield.setText("wifi false" + mainWifiObj.getConnectionInfo().getSSID().toString());*/

        listView = (ListView) findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.pb_horizontal);
        CreateList();

        /*tabHost = getTabHost();
        TabHost.TabSpec spec;
        View view;

        view = createTabView(tabHost.getContext(), "Ручной режим");
        intent = new Intent().setClass(this, InteractiveActivity.class);
        spec = tabHost.newTabSpec("tab1").setIndicator(view).setContent(intent); //устанавливаем view
        tabHost.addTab(spec);

        view = createTabView(tabHost.getContext(), "Автоматический режим");
        intent_2 = new Intent().setClass(this, AutospaceActivity.class);
        spec = tabHost.newTabSpec("tab2").setIndicator(view).setContent(intent_2); //устанавливаем view
        tabHost.addTab(spec);*/

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");

        MyListener   = new MyPhoneStateListener();
        Tel = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        //gsmCell = (GsmCellLocation)Tel.getCellLocation();

        determine = (Button) findViewById(R.id.determine);

        determine.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (Tel.getSimOperatorName().equals("PHoenix")) {
                    Determine();
                    //cid_json();
                } else
                    Toast.makeText(getApplicationContext(), "Пожалуйста, проверьте, чтобы sim-карта PHoenix находилась в первом слоте или замените саму карточку на соответствующую.", Toast.LENGTH_LONG).show();
                //Determine();
            }
        });

        startService = (Button) findViewById(R.id.startService);

        startService.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(Tel.getSimOperatorName().equals("PHoenix")) {
                    wl.acquire();
                    buf_flag = 1;
                    startService(new Intent(TabMenu.this, AutoService.class).putExtra("pend", pi));
                    startService.setEnabled(false);
                }
                else
                    Toast.makeText(getApplicationContext(), "Пожалуйста, проверьте, чтобы sim-карта PHoenix находилась в первом слоте или замените саму карточку на соответствующую.", Toast.LENGTH_LONG).show();

            }
        });

        imageButton = (ImageButton) findViewById(R.id.imageButton);

        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog(0);
            }
        });
        /*manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation gsmCell = (GsmCellLocation)manager.getCellLocation();
        if(gsmCell != null)
        {
            Log.d("TEL", "Cid" + gsmCell.getCid());
            Log.d("TEL", "Lac" + gsmCell.getLac());
            Log.d("TEL", "Psc" + gsmCell.getPsc());
        }*/
        context = getApplicationContext();

        notificationIntent = new Intent(context, TabMenu.class);
        contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Закрыть приложение?")
                .setCancelable(false)
                .setPositiveButton("Да",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                if(buf_flag == 1)
                                {
                                    wl.release();
                                    buf_flag=0;
                                }
                                stopService(new Intent(TabMenu.this, AutoService.class));
                                stopService(new Intent(TabMenu.this, WifiService.class));
                                finish();
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Нет",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });

        return builder.create();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("LOGI", "requestCode = " + requestCode + ", resultCode = "
                + resultCode);

        // Ловим сообщения об окончании задач
        if (resultCode == STATUS_FINISH) {
            result = data.getIntExtra(PARAM_RESULT, 0);
            if(result == 1) {
                progressBar.setProgress(progress);
                CreateList();
            }
            if(result == 2)
            {
                CreateList();
                progressBar.setProgress(0);
            }
            result = 0;
        }
    }

    private void importExcel2Sqlite() {
        AssetManager am = this.getAssets();
        InputStream inStream;
        Workbook wb = null;
        try {
            // Читайте .xls файлы: в папке активов
            inStream = am.open("base_stations.xls");
            // HSSF
            wb = new HSSFWorkbook(inStream);
            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (wb == null) {
            Log.e(TAG, "Ошибка чтения документа Excel из активов");
            return;
        }

        Sheet sheet1 = wb.getSheetAt(0);// Первая форма
        Sheet sheet2 = wb.getSheetAt(1);
        if (sheet1 == null) {
            return;
        }

        Excel2SQLiteHelper.insertExcelToSqlite(sdb, sheet1);
        Excel2SQLiteHelper.insertExcelToSqlite(sdb, sheet2);
        Log.e(TAG, "Данные вставляются успешно");
    }

    public static View createTabView(final Context context, final String text) {

        View view = LayoutInflater.from(context).inflate(R.layout.tab_bg, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    public void CreateList(){
        Cursor cursor = sdb.query(mDatabaseHelper.DATABASE_TABLE, new String[]{DatabaseHelper._ID, DatabaseHelper.LOC_X_COLUMN,
                        DatabaseHelper.LOC_Y_COLUMN, DatabaseHelper.TIME_COLUMN, DatabaseHelper.WEIGHT_COLUMN_HUNDREND,
                        DatabaseHelper.CID, DatabaseHelper.SECTOR, DatabaseHelper.LAC},
                null, null, null, null, DatabaseHelper._ID + " DESC") ;


        adapter = new SimpleCursorAdapter(this, // Связь.
                R.layout.list, // Определения шаблона элемента
                cursor, // Переход к курсору, который надо запомнить.
                // Массив курсоров, которые надо запомнить.
                new String[] { "Loc_X", "Loc_Y", "Time", "Weight_hundred", "CID", "sector", "LAC"},
                // Массив, связывающий запомненные курсоры и шаблоны с ними связанные
                new int[] { R.id.loc_x, R.id.loc_y, R.id.time, R.id.weight, R.id.cid, R.id.sector, R.id.lac });
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ColorDrawable divcolor = new ColorDrawable(Color.parseColor("#FF12212f"));
        listView.setDivider(divcolor);
        listView.setDividerHeight(2);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //(view).setBackgroundColor(Color.parseColor("#FF7d31df"));
                float buf = Float.valueOf(((TextView) view.findViewById(R.id.weight)).getText().toString());
                int buff = (int) ((113 + buf) / 2);
                buff = (int) ((float) buff / MAX_SIGNAL_DBM_VALUE * 1000);
                progressBar.setProgress(buff);
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
        newValues.put(DatabaseHelper.CID, cid);
        newValues.put(DatabaseHelper.SECTOR, sector);
        newValues.put(DatabaseHelper.LAC, lac);
        // Вставляем данные в таблицу
        sdb.insert("WeightSignal", null, newValues);

        ContentValues newValues_2 = new ContentValues();
        // Задайте значения для каждого столбца
        newValues_2.put(DatabaseHelper.LOC_X_COLUMN, dolstr);
        newValues_2.put(DatabaseHelper.LOC_Y_COLUMN, shistr);
        newValues_2.put(DatabaseHelper.TIME_COLUMN, unixTime);
        newValues_2.put(DatabaseHelper.WEIGHT_COLUMN, weight);
        newValues_2.put(DatabaseHelper.WEIGHT_COLUMN_HUNDREND, weight_hundred);
        newValues_2.put(DatabaseHelper.CID, cid);
        newValues_2.put(DatabaseHelper.SECTOR, sector);
        newValues_2.put(DatabaseHelper.LAC, lac);
        // Вставляем данные в таблицу
        sdb.insert("WeightSignal_2", null, newValues_2);

        check_bs();

        CreateList();
        TabMenu.progressBar.setProgress(TabMenu.progress);
    }

    public void check_bs(){
        String cid_buff = cid + ".0";
        String querySelect = "SELECT * FROM " + DatabaseHelper.BS_TABLE + " WHERE " + DatabaseHelper.BS_SID1 + " = " + cid_buff;
        Cursor cursorSelect = sdb.rawQuery(querySelect,null);
        if(cursorSelect.moveToFirst()) {
            Log.d(TAG, "cid_1 : " + cursorSelect.getString(cursorSelect.getColumnIndex(DatabaseHelper.BS_SID1)));
            if(cursorSelect.getString(cursorSelect.getColumnIndex(DatabaseHelper.BS_PLACE)).equals("Украина"))
                notificationBS("Опасная базовая станция");
        }
        else
        {
            String querySelect_2 = "SELECT * FROM " + DatabaseHelper.BS_TABLE + " WHERE " + DatabaseHelper.BS_SID2 + " = " + cid_buff;
            Cursor cursorSelect_2 = sdb.rawQuery(querySelect_2,null);
            if(cursorSelect_2.moveToFirst()) {
                Log.d(TAG, "cid_2 : " + cursorSelect_2.getString(cursorSelect_2.getColumnIndex(DatabaseHelper.BS_SID2)));
                if(cursorSelect_2.getString(cursorSelect_2.getColumnIndex(DatabaseHelper.BS_PLACE)).equals("Украина"))
                    notificationBS("Опасная базовая станция");
            }
            else
                notificationBS("Базовой станции нет в базе");
            cursorSelect_2.close();
        }
        cursorSelect.close();
    }

    public void notificationBS(String msg_alert){
        long[] vibrate = new long[] { 1000, 1000, 1000 };

        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.logo)
                .setTicker("ВНИМАНИЕ")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setVibrate(vibrate)
                .setContentTitle("Предупреждение")
                .setSound(Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, "1"))
                .setContentText(msg_alert); // Текст уведомления

        // Notification notification = builder.getNotification(); // до API 16
        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(321321, notification);
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

                gsmCell = (GsmCellLocation)Tel.getCellLocation();
                lac = "" + gsmCell.getLac();
                cid = "" + gsmCell.getCid();
                sector = cid.substring(cid.length()-1);
                cid = cid.substring(0,cid.length()-1);
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

    @Override
    protected void onResume() {
        CreateList();
        super.onResume();
    }
}

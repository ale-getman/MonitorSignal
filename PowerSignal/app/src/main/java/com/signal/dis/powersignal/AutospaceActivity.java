package com.signal.dis.powersignal;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by User on 20.11.2015.
 */
public class AutospaceActivity extends Activity {

    private DatabaseHelper mDatabaseHelper;
    public SQLiteDatabase sdb;

    public Button startService, stopService, plus_min, minus_min, plus_sec, minus_sec;
    //public EditText interval;
    public String erval;
    public PendingIntent pi;
    public TextView minuts,secunds;
    public int min = 0;
    public int sec = 0;
    public int time_period = 0;

    public TelephonyManager Tel;

    public final static int STATUS_FINISH = 200;
    public final static String PARAM_RESULT = "result";
    final int TASK1_CODE = 1;
    public int result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autospace);

        mDatabaseHelper = new DatabaseHelper(this, "mydatabase.db", null, 5);
        sdb = mDatabaseHelper.getWritableDatabase();

        startService = (Button) findViewById(R.id.startService);
        stopService = (Button) findViewById(R.id.stopService);
        plus_min = (Button) findViewById(R.id.plus_min);
        plus_sec = (Button) findViewById(R.id.plus_sec);
        minus_min = (Button) findViewById(R.id.minus_min);
        minus_sec = (Button) findViewById(R.id.minus_sec);
        minuts = (TextView) findViewById(R.id.minuts);
        secunds = (TextView) findViewById(R.id.secunds);
        //interval = (EditText) findViewById(R.id.interval);
        min = Integer.valueOf(minuts.getText().toString());
        sec = Integer.valueOf(secunds.getText().toString());
        min = min*60000;
        sec = sec*1000;
        time_period = min + sec;
        stopService.setEnabled(false);
        Tel = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        Intent intent_t = new Intent();
        pi = createPendingResult(TASK1_CODE, intent_t, 0);

        startService.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(Tel.getSimOperatorName().equals("PHoenix"))
                {
                    //erval = interval.getText().toString();
                    erval = String.valueOf(time_period);
                    Log.d("LOGI", "erval: " + erval);
                    if(erval.equals(""))
                        Toast.makeText(getApplicationContext(), "Введите интервал.", Toast.LENGTH_SHORT).show();
                    else
                    {
                        startService(new Intent(AutospaceActivity.this, AutoService.class).putExtra("interval", erval).putExtra("pend", pi));
                        //interval.setFocusable(false);
                        startService.setEnabled(false);
                        plus_min.setEnabled(false);
                        plus_sec.setEnabled(false);
                        minus_min.setEnabled(false);
                        minus_sec.setEnabled(false);
                        stopService.setEnabled(true);
                    }
                }
                else
                    Toast.makeText(getApplicationContext(), "Пожалуйста, проверьте, чтобы sim-карта PHoenix находилась в первом слоте или замените саму карточку на соответствующую.", Toast.LENGTH_LONG).show();

            }
        });

        stopService.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                stopService(new Intent(AutospaceActivity.this, AutoService.class));
                startService.setEnabled(true);
                stopService.setEnabled(false);
                plus_min.setEnabled(true);
                plus_sec.setEnabled(true);
                minus_min.setEnabled(true);
                minus_sec.setEnabled(true);
                //interval.setFocusable(true);
                //interval.setText("");
                //interval.setHint("Введите интервал (мин.)");
            }
        });

        plus_min.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int i = Integer.valueOf(minuts.getText().toString());
                if(i!=59)
                {
                    i++;
                    minuts.setText(""+i);
                }
            }
        });

        minus_min.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int i = Integer.valueOf(minuts.getText().toString());
                if(i!=0)
                {
                    i--;
                    minuts.setText(""+i);
                }
            }
        });

        plus_sec.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int i = Integer.valueOf(secunds.getText().toString());
                int j = Integer.valueOf(minuts.getText().toString());
                if(i!=45)
                {
                    i+=15;
                    secunds.setText(""+i);
                }
                else
                {
                    j++;
                    minuts.setText(""+j);
                    i=0;
                    secunds.setText(""+i);
                }
            }
        });

        minus_sec.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int i = Integer.valueOf(secunds.getText().toString());
                int j = Integer.valueOf(minuts.getText().toString());
                /*if(i>=0 && j>0)
                {
                    i=45;
                    secunds.setText(""+i);
                    j--;
                    minuts.setText(""+j);
                }
                if(j==0 && i!=15)
                {
                    i-=15;
                    secunds.setText(""+i);
                }*/
                if(j==0 && i==15)
                {
                }
                else
                {
                    if(j==0 && i>15)
                    {
                        i-=15;
                        secunds.setText(""+i);
                    }
                    if(j>0 && i==0)
                    {
                        j--;
                        i=45;
                        minuts.setText(""+j);
                        secunds.setText(""+i);
                    }
                    if(j>0 && i>=15)
                    {
                        i-=15;
                        secunds.setText(""+i);
                    }
                }

            }
        });
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
                TabMenu.progressBar.setProgress(0);
                CreateList();
            }
            result = 0;
        }
    }

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
        TabMenu.progressBar.setProgress(TabMenu.progress);
    }

}

package com.signal.dis.powersignal;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by User on 13.11.2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns{

    private static final String DATABASE_NAME = "mydatabase.db";
    private static final int DATABASE_VERSION = 6;
    public static final String DATABASE_TABLE = "WeightSignal";
    public static final String DATABASE_TABLE_2 = "WeightSignal_2";
    public static final String LOC_X_COLUMN = "Loc_X";
    public static final String LOC_Y_COLUMN = "Loc_Y";
    public static final String ADDRESS_COLUMN = "Adress";
    public static final String TIME_COLUMN = "Time";
    public static final String WEIGHT_COLUMN = "Weight";
    public static final String WEIGHT_COLUMN_HUNDREND = "Weight_hundred";
    public static final String CID = "CID";
    public static final String SECTOR = "sector";
    public static final String LAC = "LAC";

    private static final String DATABASE_CREATE_SCRIPT = "create table "
            + DATABASE_TABLE + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + LOC_X_COLUMN
            + " text not null, " + LOC_Y_COLUMN + " text not null, " + TIME_COLUMN
            + " text not null, " + WEIGHT_COLUMN + " text not null, " + WEIGHT_COLUMN_HUNDREND + " text not null, " + CID
            + " text not null, " + SECTOR + " text not null, " + LAC + " text not null);";

    private static final String DATABASE_CREATE_SCRIPT_2 = "create table "
            + DATABASE_TABLE_2 + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + LOC_X_COLUMN
            + " text not null, " + LOC_Y_COLUMN + " text not null, " + TIME_COLUMN
            + " text not null, " + WEIGHT_COLUMN + " text not null, " + WEIGHT_COLUMN_HUNDREND + " text not null, " + CID
            + " text not null, " + SECTOR + " text not null, " + LAC + " text not null);";


    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SCRIPT);
        db.execSQL(DATABASE_CREATE_SCRIPT_2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Запишем в журнал
        Log.w("SQLite", "Обновляемся с версии " + oldVersion + " на версию " + newVersion);

        // Удаляем старую таблицу и создаём новую
        db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE);
        db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE_2);
        // Создаём новую таблицу
        onCreate(db);
    }
}

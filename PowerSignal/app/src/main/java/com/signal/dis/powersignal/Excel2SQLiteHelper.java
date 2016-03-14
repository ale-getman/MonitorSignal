package com.signal.dis.powersignal;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Iterator;

public class Excel2SQLiteHelper {
	/**
	 * Прочитайте документ EXCEL форму с пакетом ContentValues, а затем вставляется в базу данных
	 * 
	 * @param sheet
	 */
	public static void insertExcelToSqlite(SQLiteDatabase sdb, Sheet sheet) {
		for (Iterator<Row> rit = sheet.rowIterator(); rit.hasNext();) {
			Row row = rit.next();
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.BS_SID1, row.getCell(0).getNumericCellValue());//.getStringCellValue());
			values.put(DatabaseHelper.BS_SID2, row.getCell(1).getNumericCellValue());//.getStringCellValue());
			values.put(DatabaseHelper.BS_PLACE, row.getCell(2).getStringCellValue());
			if ((int) sdb.insert(DatabaseHelper.BS_TABLE, null, values) < 0) {
				Log.e("Error", "Вставка ошибок данных");
				return;
			}
		}
	}
}

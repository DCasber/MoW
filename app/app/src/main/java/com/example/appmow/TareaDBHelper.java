package com.example.appmow;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TareaDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "tarea.db";
    public TareaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TareaContract.TareaEntry.TABLE_NAME + " ("
                + TareaContract.TareaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TareaContract.TareaEntry.ID + " TEXT NOT NULL,"
                + TareaContract.TareaEntry.ASUNTO + " TEXT NOT NULL,"
                + TareaContract.TareaEntry.HORA + " TIME NOT NULL,"
                + TareaContract.TareaEntry.FECHA + " DATE NOT NULL,"
                + TareaContract.TareaEntry.ALARMA + " DATE NOT NULL,"
                + TareaContract.TareaEntry.ORIGEN + " TEXT NOT NULL,"
                + TareaContract.TareaEntry.DESTINO + " TEXT NOT NULL,"
                + TareaContract.TareaEntry.TRANSPORTE + " TEXT NOT NULL,"
                + "UNIQUE (" + TareaContract.TareaEntry.ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

package com.example.appmow;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TareaHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Tarea.DictEntry.TABLE_NAME + " (" +
                    Tarea.DictEntry._ID + " INTEGER PRIMARY KEY, " +
                    Tarea.DictEntry.COLUMN_NAME_KEY_ASUNTO + " TEXT, " +
                    Tarea.DictEntry.COLUMN_NAME_VAL_ASUNTO + " TEXT, " +
                    Tarea.DictEntry.COLUMN_NAME_KEY_UB_ORIGEN + " TEXT, " +
                    Tarea.DictEntry.COLUMN_NAME_VAL_UB_ORIGEN + " TEXT, " +
                    Tarea.DictEntry.COLUMN_NAME_KEY_UB_DESTINO + " TEXT, " +
                    Tarea.DictEntry.COLUMN_NAME_VAL_UB_DESTINO + " TEXT, " +
                    Tarea.DictEntry.COLUMN_NAME_KEY_FECHA + " TEXT, " +
                    Tarea.DictEntry.COLUMN_NAME_VAL_FECHA + " TEXT, " +
                    Tarea.DictEntry.COLUMN_NAME_KEY_ALARMA + " TEXT, " +
                    Tarea.DictEntry.COLUMN_NAME_VAL_ALARMA + " TEXT, " +
                    Tarea.DictEntry.COLUMN_NAME_KEY_TRANSPORTE + " INTEGER, " +
                    Tarea.DictEntry.COLUMN_NAME_VAL_TRANSPORTE + " INTEGER" + ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Tarea.DictEntry.TABLE_NAME;

    public TareaHelper(Context context, String database_name) {
        super(context, database_name, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}

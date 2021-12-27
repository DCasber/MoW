package com.example.appmow;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

public final class Tarea {
    private Tarea() {}
    public static abstract class DictEntry implements BaseColumns {
        public static final String TABLE_NAME = "Tarea";
        public static final String COLUMN_NAME_KEY_ASUNTO = "Asunto";
        public static final String COLUMN_NAME_VAL_ASUNTO = "valorAsunto";
        public static final String COLUMN_NAME_KEY_UB_DESTINO = "UbDestino";
        public static final String COLUMN_NAME_VAL_UB_DESTINO = "valorUbDestino";
        public static final String COLUMN_NAME_KEY_UB_ORIGEN = "UbOrigen";
        public static final String COLUMN_NAME_VAL_UB_ORIGEN = "valorUbOrigen";
        public static final String COLUMN_NAME_KEY_FECHA = "Fecha";
        public static final String COLUMN_NAME_VAL_FECHA = "valorFecha";
        public static final String COLUMN_NAME_KEY_ALARMA = "Alarma";
        public static final String COLUMN_NAME_VAL_ALARMA = "valorAlarma";
        public static final String COLUMN_NAME_KEY_TRANSPORTE = "Transporte";
        public static final String COLUMN_NAME_VAL_TRANSPORTE = "valorTransporte";

        //public static final String COLUMN_NAME_KEY_FRECUENCIA = "Frecuencia";
        //public static final String COLUMN_NAME_VAL_FRECUENCIA = "valorFrecuencia";

    }
}




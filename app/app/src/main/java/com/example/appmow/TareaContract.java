package com.example.appmow;

import android.provider.BaseColumns;

public class TareaContract {
    public static abstract class TareaEntry implements BaseColumns {
        public static final String TABLE_NAME = "tarea";

        public static final String ID = "id";
        public static final String ASUNTO = "asunto";
        public static final String HORA = "hora";
        public static final String FECHA = "fecha";
        public static final String ALARMA = "alarma";
        public static final String ORIGEN = "origen";
        public static final String DESTINO = "destino";
        public static final String TRANSPORTE = "transporte";
    }
}

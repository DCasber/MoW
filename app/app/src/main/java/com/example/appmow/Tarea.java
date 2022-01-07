package com.example.appmow;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

import java.sql.Date;
import java.sql.Time;

public final class Tarea {
    private String asunto;
    private String fecha;
    private String alarma;
    private String origen;
    private String destino;
    private String transporte;

    public Tarea(String asunto, String fecha,  String alarma, String origen, String destino, String transporte) {
        this.asunto = asunto;
        this.fecha = fecha;
        this.origen = origen;
        this.destino = destino;
        this.alarma = alarma;
        this.transporte = transporte;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getFecha() {
        return fecha;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public String getAlarma() {
        return alarma;
    }

    public String getTransporte() {
        return transporte;
    }






}




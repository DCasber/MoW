package com.example.appmow;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Locale;

public class NuevaTarea extends AppCompatActivity {
    EditText fecha, hora, asunto;
    TextView transporte,latOrigen, latDestino, lonOrigen, lonDestino;
    private int a, m, d, h, min;
    static final int DATE_ID = 0;
    static final int TIME_ID = 1;
    private long duracion = 0;
    static final long WAIT = 900000;

    Button continuar;
    TextView titulo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_tarea);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        latOrigen = (TextView) findViewById(R.id.idLatOrigen);
        latDestino = (TextView) findViewById(R.id.idLatDestino);
        lonOrigen = (TextView) findViewById(R.id.idLonOrigen);
        lonDestino = (TextView) findViewById(R.id.idLonDestino);
        fecha = (EditText) findViewById(R.id.idFecha);
        hora = (EditText) findViewById(R.id.idHora);
        asunto = (EditText) findViewById(R.id.idAsunto);
        transporte = (TextView) findViewById(R.id.idTransporte);

        titulo = (TextView) findViewById(R.id.idTitulo);
        continuar = (Button) findViewById(R.id.bCrearEditar);

        titulo.setText(R.string.crearTarea);
        continuar.setText(R.string.crear);

        Calendar C = Calendar.getInstance();

        a = C.get(Calendar.YEAR);
        m = C.get(Calendar.MONTH);
        d = C.get(Calendar.DAY_OF_MONTH);

        fecha.setOnClickListener((View) -> {
            showDialog(DATE_ID);
        });

        h = C.get(Calendar.HOUR);
        min = C.get(Calendar.MINUTE);

        hora.setOnClickListener((View) -> {
            showDialog(TIME_ID);
        });

        Button bBuscar = (Button) findViewById(R.id.bBuscar);

        bBuscar.setOnClickListener((View v) -> {
            Intent intent = new Intent(v.getContext(), Ubicacion.class);
            intent.putExtra("id", 0);

            buscarUbicacion.launch(intent);
        });

        Button bCrear = (Button) findViewById(R.id.bCrearEditar);

        bCrear.setOnClickListener((View v) -> {
            if(!excepciones()) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(this);
                alerta.setTitle(R.string.advertencia);
                alerta.setMessage(R.string.modificar);
                alerta.setCancelable(false);
                alerta.setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface alerta, int id) {
                        crear(duracion);
                        Intent intent = new Intent(v.getContext(), MainActivity.class);
                        startActivity(intent);
                    }
                });
                alerta.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface alerta, int id) {

                    }
                });
                alerta.show();
            }
        });
    }


    private void crear(long duracion){
        Calendar fechaTarea = Calendar.getInstance();
        fechaTarea.set(a, m, d, h, min, 0);
        long timeTarea = fechaTarea.getTimeInMillis();
        timeTarea = timeTarea - duracion -  WAIT;


        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy,HH:mm");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeTarea);
        String alarma = formatter.format(calendar.getTime());

        Toast.makeText(getApplicationContext(), getString(R.string.changed_to, alarma), Toast.LENGTH_LONG).show();
        setAlarm(1, timeTarea, NuevaTarea.this);

        insertarBD(alarma);

    }

    private void insertarBD(String alarma){

        TareaDBHelper dbHelper = new TareaDBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        String origen = latOrigen.getText() + "," + lonOrigen.getText();
        String destino = latDestino.getText() + "," + lonDestino.getText();

        values.put(TareaContract.TareaEntry.ASUNTO, asunto.getText().toString());
        values.put(TareaContract.TareaEntry.FECHA, fecha.getText() + "," + hora.getText());
        values.put(TareaContract.TareaEntry.ALARMA, alarma + "");

        String mode = transporte.getText().toString();

        if(!Locale.getDefault().getLanguage().equals("es")){
            if(transporte.getText().toString().equals("Walking")){
                mode = "Andando";
            } else if(transporte.getText().toString().equals("Vehicle")){
                mode = "Vehiculo";
            }else if(transporte.getText().toString().equals("Bicycle")){
                mode = "Bicicleta";
            }
        }

        values.put(TareaContract.TareaEntry.TRANSPORTE, mode);
        values.put(TareaContract.TareaEntry.ORIGEN, origen);
        values.put(TareaContract.TareaEntry.DESTINO, destino);

        db.insert(TareaContract.TareaEntry.TABLE_NAME, null, values);

    }


    ActivityResultLauncher<Intent> buscarUbicacion = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Bundle extras = data.getExtras();
                    LatLng origen = (LatLng) extras.get("origen");
                    LatLng destino = (LatLng) extras.get("destino");
                    String strTransporte = (String) extras.get("transporte");
                    latOrigen.setText(origen.latitude + "");
                    latDestino.setText(destino.latitude + "");
                    lonOrigen.setText(origen.longitude + "");
                    lonDestino.setText(destino.longitude + "");
                    transporte.setText(strTransporte);
                    duracion = (long) extras.get("duracion");
                    duracion = duracion * 60000;
                }
            });




    private void colocar_hora() {
        if(h < 10 && min < 10) {
            hora.setText( "0" + h + ":0" + min);
        } else if(h < 10 && min >= 10){
            hora.setText( "0" + h + ":" + min);
        } else if(min < 10 && h >= 10){
            hora.setText( h + ":0" + min);
        } else {
            hora.setText( h + ":" + min);
        }

    }

    private TimePickerDialog.OnTimeSetListener onTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    h = hourOfDay;
                    min = minute;
                    colocar_hora();
                }
            };

    private void colocar_fecha() {
        if(m < 9 && d < 10) {
            fecha.setText( "0" + d + "/0" + (m + 1) + "/" + a);
        } else if(m < 9 && d >= 10){
            fecha.setText( d + "/0" + (m + 1) + "/" + a);
        } else if(d < 10 && m >= 9){
            fecha.setText( "0" + d + "/" + (m + 1) + "/" + a);
        } else {
            fecha.setText( d + "/" + (m + 1) + "/" + a);
        }

    }

    private DatePickerDialog.OnDateSetListener nDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    a = year;
                    m = month;
                    d = dayOfMonth;
                    colocar_fecha();
                }
            };

    @Override
    protected Dialog onCreateDialog(int id){
        switch(id) {
            case DATE_ID:
                return new DatePickerDialog(this, nDateSetListener, a, m, d);
            case TIME_ID:
                return new TimePickerDialog(this, onTimeSetListener, h, min, false);
        }

        return null;
    }

    private boolean excepciones() {
        AlertDialog.Builder al = new AlertDialog.Builder(this);
        al.setTitle(R.string.error);

        Calendar cal = Calendar.getInstance();

        if(asunto.getText().toString() == null || asunto.getText().toString().isEmpty()){
            al.setMessage(R.string.asuntoExcepcion);
            al.show();
            return true;
        }
        if(fecha.getText().toString() == null || fecha.getText().toString().isEmpty()){
            al.setMessage(R.string.fechaNula);
            al.show();
            return true;
        } else if(a < cal.get(Calendar.YEAR)) {
            al.setMessage(R.string.anyoMenor);
            al.show();
            return true;
        } else if(a == cal.get(Calendar.YEAR) && m < cal.get(Calendar.MONTH) ) {
            al.setMessage(R.string.mesMenor);
            al.show();
            return true;
        } else if(a == cal.get(Calendar.YEAR) && m == cal.get(Calendar.MONTH)  && d < cal.get(Calendar.DAY_OF_MONTH) ){
            al.setMessage(R.string.diaMenor);
            al.show();
            return true;
        }

        if(hora.getText().toString() == null || hora.getText().toString().isEmpty()){
            al.setMessage(R.string.horaNula);
            al.show();
            return true;
        } else if (a == cal.get(Calendar.YEAR) && m == cal.get(Calendar.MONTH) && d == cal.get(Calendar.DAY_OF_MONTH)
                && (h < cal.get(Calendar.HOUR))){
            al.setMessage(R.string.horaMenor);
            al.show();
            return true;
        } else if (a == cal.get(Calendar.YEAR) && m == cal.get(Calendar.MONTH) && d == cal.get(Calendar.DAY_OF_MONTH)
                && (h == cal.get(Calendar.HOUR) && min < cal.get(Calendar.MINUTE))) {
            al.setMessage(R.string.horaMenor);
            al.show();
            return true;
        }

        if(latOrigen.getText().toString() == null || latOrigen.getText().toString().isEmpty()
                || lonOrigen.getText().toString() == null || lonOrigen.getText().toString().isEmpty()) {
            al.setMessage(R.string.origenExcepcion);
            al.show();
            return true;
        }

        if (latDestino.getText().toString() == null || latDestino.getText().toString().isEmpty()
                || lonDestino.getText().toString() == null || lonDestino.getText().toString().isEmpty()) {
            al.setMessage(R.string.destinoExcepcion);
            al.show();
            return true;
        }


        Calendar today = Calendar.getInstance();
        Calendar aux = Calendar.getInstance();
        aux.set(a, m, d, h, min, 0);

        long milToday = today.getTimeInMillis() + WAIT + duracion;
        today.setTimeInMillis(milToday);

        if(today.after(aux)){
            al.setMessage(R.string.fechaMenor);
            al.show();
            return true;
        }

        return false;
    }

    public static void setAlarm(int i, Long timestamp, Context ctx) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(ctx, AlarmReceiver.class);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(ctx, i, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));
        alarmManager.set(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
    }
}
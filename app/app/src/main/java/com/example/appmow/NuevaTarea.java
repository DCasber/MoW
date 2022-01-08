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
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
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

public class NuevaTarea extends AppCompatActivity {
    EditText fecha, hora, asunto;
    TextView transporte,latOrigen, latDestino, lonOrigen, lonDestino;
    private int a, m, d, h, min;
    static final int DATE_ID = 0;
    static final int TIME_ID = 1;
    private Spinner sp;
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

        Intent idInt = getIntent();
        int id = idInt.getIntExtra("id", 0);

        if(id != 0) {
            TareaDBHelper th = new TareaDBHelper(getApplicationContext());
            SQLiteDatabase db = th.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TareaContract.TareaEntry.TABLE_NAME + " WHERE _ID = ?", new String[] {id + ""});
            while(cursor.moveToNext()){
                asunto.setText(cursor.getString(1));
                transporte.setText(cursor.getString(6));
                String txtFecha = cursor.getString(2);
                String [] fechaHora = txtFecha.split(",");

                titulo.setText(R.string.editarTarea);
                continuar.setText(R.string.editar);


                fecha.setText(fechaHora[0]);
                hora.setText(fechaHora[1]);

                String [] valFecha = fechaHora[0].split("/");
                String [] valHora = fechaHora[1].split(":");

                a = Integer.parseInt(valFecha[2]);
                m = Integer.parseInt(valFecha[1]);
                d = Integer.parseInt(valFecha[0]);

                h = Integer.parseInt(valHora[0]);
                min = Integer.parseInt(valHora[1]);

                System.out.println("El dia es: " + d + "/" + m + "/" + a + " a las " + h + ":" + min);


                String ubicacionOrigen = cursor.getString(4);
                String ubicacionDestino = cursor.getString(5);


                String[] ubOrigen = ubicacionOrigen.split(",");
                String[] ubDestino = ubicacionDestino.split(",");

                latOrigen.setText(ubOrigen[0]);
                latDestino.setText(ubDestino[0]);
                lonOrigen.setText(ubOrigen[1]);
                lonDestino.setText(ubDestino[1]);

                try {
                    duracion = parseDuracion(getDuracion(new LatLng(Double.parseDouble(ubOrigen[0]), Double.parseDouble(ubOrigen[1])),
                                            new LatLng(Double.parseDouble(ubDestino[0]), Double.parseDouble(ubDestino[1])),
                                            transporte.getText().toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

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
            if (id != 0){
                intent.putExtra("id", id);
                intent.putExtra("latOrigen", latOrigen.getText());
                intent.putExtra("latDestino", latDestino.getText());
                intent.putExtra("lonOrigen", lonOrigen.getText());
                intent.putExtra("lonDestino", lonDestino.getText());
                intent.putExtra("modoTransporte", transporte.getText());
            }

            buscarUbicacion.launch(intent);
        });

        Button bCrear = (Button) findViewById(R.id.bCrearEditar);

        bCrear.setOnClickListener((View v) -> {
            if(!excepciones()) {
                crear(duracion, id);
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public long parseDuracion(String input) {
        input = input.toLowerCase()
                .replaceAll("days?", "DT")
                .replaceAll("mins?", "M")
                .replaceAll("hours?", "H")
                .replaceAll("\\s+", "");

        input = "PT" + input;
        if(input.contains("D")) {
            input = input.replaceFirst("T", "");
        }


        Duration d = Duration.parse(input);
        return d.toMinutes();
    }

    private String getDuracion(LatLng origen, LatLng destino, String mode) throws IOException, JSONException {


        mode = parseMode(mode);

        String stringUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins="+
                origen.latitude+","+origen.longitude+
                "&destinations="+destino.latitude+","+destino.longitude+"&key=AIzaSyD6A7Zni9DVryKVro8--jjmGmy8Zq3auxc&mode="
                + mode;

        String json;
        json = NuevaTarea.NetworkUtils.getJSONFromAPI(stringUrl);

        String duration = new JSONObject(json)
                .getJSONArray("rows")
                .getJSONObject(0)
                .getJSONArray("elements")
                .getJSONObject(0)
                .getJSONObject("duration")
                .get("text").toString();



        return duration;


    }

    private String parseMode(String mode) {

        if(mode.equals("Andando")){
            mode = "walking";
        } else if (mode.equals("Vehiculo")){
            mode = "driving";
        }
        else if(mode.equals("Bicicleta")){
            mode = "bicycling";
        }
        return mode;

    }


    public static class NetworkUtils {

        public static String getJSONFromAPI (String url){
            String output = "";
            try {
                URL apiEnd = new URL(url);
                int responseCode;
                HttpURLConnection connection;
                InputStream is;

                connection = (HttpURLConnection) apiEnd.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);
                connection.connect();

                responseCode = connection.getResponseCode();
                if(responseCode < HttpURLConnection.HTTP_BAD_REQUEST){
                    is = connection.getInputStream();
                }else {
                    is = connection.getErrorStream();
                }

                output = convertISToString(is);
                is.close();
                connection.disconnect();

            }  catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return output;
        }

        private static String convertISToString(InputStream is){
            StringBuffer buffer = new StringBuffer();

            try {

                BufferedReader br;
                String row;

                br = new BufferedReader(new InputStreamReader(is));
                while ((row = br.readLine())!= null){
                    buffer.append(row);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return buffer.toString();
        }
    }



    private void crear(long duracion, int id){
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

        insertarBD(alarma, id);

    }

    private void insertarBD(String alarma, int id){

        TareaDBHelper dbHelper = new TareaDBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        String origen = latOrigen.getText() + "," + lonOrigen.getText();
        String destino = latDestino.getText() + "," + lonDestino.getText();

        values.put(TareaContract.TareaEntry.ASUNTO, asunto.getText().toString());
        values.put(TareaContract.TareaEntry.FECHA, fecha.getText() + "," + hora.getText());
        values.put(TareaContract.TareaEntry.ALARMA, alarma + "");
        values.put(TareaContract.TareaEntry.TRANSPORTE, transporte.getText().toString());
        values.put(TareaContract.TareaEntry.ORIGEN, origen);
        values.put(TareaContract.TareaEntry.DESTINO, destino);

        if(id != 0){
            String where = TareaContract.TareaEntry._ID + " = ?";
            String[] whereArg = {String.valueOf(id)};
            db.update(TareaContract.TareaEntry.TABLE_NAME, values, where, whereArg);
        } else {
            db.insert(TareaContract.TareaEntry.TABLE_NAME, null, values);
        }

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
                    System.out.println("La duracion es: " + duracion);
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
        al.setTitle("Error en la aplicacion");

        Calendar cal = Calendar.getInstance();

        if(asunto.getText().toString() == null || asunto.getText().toString().isEmpty()){
            al.setMessage("El asunto no puede ser nulo");
            al.show();
            return true;
        }
        if(fecha.getText().toString() == null || fecha.getText().toString().isEmpty()){
            al.setMessage("La fecha no puede ser nula");
            al.show();
            return true;
        } else if(a < cal.get(Calendar.YEAR)) {
            al.setMessage("El año no puede ser menor al actual");
            al.show();
            return true;
        } else if(a == cal.get(Calendar.YEAR) && m < cal.get(Calendar.MONTH) ) {
            al.setMessage("El mes no puede ser menor al actual");
            al.show();
            return true;
        } else if(a == cal.get(Calendar.YEAR) && m == cal.get(Calendar.MONTH)  && d < cal.get(Calendar.DAY_OF_MONTH) ){
            al.setMessage("El dia no puede ser menor al actual");
            al.show();
            return true;
        }

        if(hora.getText().toString() == null || hora.getText().toString().isEmpty()){
            al.setMessage("La hora no puede ser nulo");
            al.show();
            return true;
        } else if (a == cal.get(Calendar.YEAR) && m == cal.get(Calendar.MONTH) && d == cal.get(Calendar.DAY_OF_MONTH)
                && (h < cal.get(Calendar.HOUR) || min < cal.get(Calendar.MINUTE))){
            al.setMessage("La hora no puede ser menor a la actual");
            al.show();
            return true;
        }


        if(latOrigen.getText().toString() == null || latOrigen.getText().toString().isEmpty()
                || lonOrigen.getText().toString() == null || lonOrigen.getText().toString().isEmpty()) {
            al.setMessage("No se ha seleccionado un punto de origen");
            al.show();
            return true;
        }

        if (latDestino.getText().toString() == null || latDestino.getText().toString().isEmpty()
                || lonDestino.getText().toString() == null || lonDestino.getText().toString().isEmpty()) {
            al.setMessage("No se ha seleccionado un punto de destino");
            al.show();
            return true;
        }


        Calendar today = Calendar.getInstance();
        Calendar aux = Calendar.getInstance();
        aux.set(a, m, d, h, min, 0);

        long milToday = today.getTimeInMillis();
        long milAux = aux.getTimeInMillis();

        if(milAux <= milToday + 90000 + duracion){
            al.setMessage("La fecha seleccionada para la alarma es anterior a la fecha actual");
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
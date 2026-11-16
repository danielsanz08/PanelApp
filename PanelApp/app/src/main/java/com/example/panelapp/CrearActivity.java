package com.example.panelapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CrearActivity extends AppCompatActivity {

    private EditText inputNombre, inputDescripcion;
    private Button btnGuardar, btnSeleccionarFecha, btnObtenerUbicacion;
    private TextView mensajeAlerta, txtFechaSeleccionada, txtUbicacion;
    private BD db;

    // Variables para guardar los datos de los intents
    private long fechaSeleccionada = System.currentTimeMillis();
    private double latitud = 0;
    private double longitud = 0;

    private LocationManager ubicacionGps;
    private ToneGenerator sonidoNotificacion;

    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear);

        // 1. Se conectan los elementos del XML
        inputNombre = findViewById(R.id.inputNombre);
        inputDescripcion = findViewById(R.id.inputDescripcion);
        btnGuardar = findViewById(R.id.btnGuardar);
        mensajeAlerta = findViewById(R.id.mensajeAlerta);

        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        txtFechaSeleccionada = findViewById(R.id.txtFechaSeleccionada);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion);
        txtUbicacion = findViewById(R.id.txtUbicacion);

        db = new BD(this);
        ubicacionGps = (LocationManager) getSystemService(LOCATION_SERVICE);
        sonidoNotificacion = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        // Se muestra la fecha actual formateada al inicio
        txtFechaSeleccionada.setText("Fecha: " + formatoFecha.format(new Date(fechaSeleccionada)));

        // botón Fecha (Abre el calendario y luego la hora)
        btnSeleccionarFecha.setOnClickListener(v -> {
            Calendar calendario = Calendar.getInstance();
            calendario.setTimeInMillis(fechaSeleccionada);

            int anio = calendario.get(Calendar.YEAR);
            int mes = calendario.get(Calendar.MONTH);
            int dia = calendario.get(Calendar.DAY_OF_MONTH);

            //Se abre el calendario para elegir el día
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendario.set(year, month, dayOfMonth);

                // Se abre el reloj para elegir la hora
                int hora = calendario.get(Calendar.HOUR_OF_DAY);
                int minuto = calendario.get(Calendar.MINUTE);


                TimePickerDialog timePicker = new TimePickerDialog(this, (viewTime, hourOfDay, minute) -> {
                    calendario.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendario.set(Calendar.MINUTE, minute);
                    calendario.set(Calendar.SECOND, 0);

                    fechaSeleccionada = calendario.getTimeInMillis();

                    txtFechaSeleccionada.setText("Fecha: " + formatoFecha.format(new Date(fechaSeleccionada)));
                }, hora, minuto, false);

                timePicker.show();

            }, anio, mes, dia);

            datePicker.show();
        });

        //Acción del botón Ubicación
        btnObtenerUbicacion.setOnClickListener(v -> solicitarUbicacion());

        //Acción del botón Guardar
        btnGuardar.setOnClickListener(v -> guardarRecordatorio());
    }

    private void guardarRecordatorio() {
        String nombre = inputNombre.getText().toString().trim();
        String descripcion = inputDescripcion.getText().toString().trim();

        if (nombre.isEmpty()) {
            mensajeAlerta.setText("Se debe escribir un nombre obligatorio.");
            return;
        }

        // Se reproduce el sonido (Intent de sonar) al guardar
        sonidoNotificacion.startTone(ToneGenerator.TONE_PROP_BEEP, 150);


        Recordatorio nuevoRecordatorio = new Recordatorio(
                nombre, descripcion, fechaSeleccionada, null, latitud, longitud
        );

        long idGenerado = db.insertar(nuevoRecordatorio);

        if (idGenerado > 0) {
            Toast.makeText(this, "¡Guardado con éxito!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            mensajeAlerta.setText("Error al intentar guardar.");
        }
    }

    //METODO PARA OBTENER LA CIUDAD
    private void solicitarUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }
        try {
            Location ubicacion = ubicacionGps.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (ubicacion != null) {
                latitud = ubicacion.getLatitude();
                longitud = ubicacion.getLongitude();

                txtUbicacion.setText("Buscando ciudad...");

                Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                new Thread(() -> {
                    try {
                        List<Address> direcciones = geocoder.getFromLocation(latitud, longitud, 1);
                        if (direcciones != null && !direcciones.isEmpty()) {
                            String ciudad = direcciones.get(0).getLocality();

                            if (ciudad == null) ciudad = direcciones.get(0).getSubAdminArea();
                            if (ciudad == null) ciudad = direcciones.get(0).getAdminArea();
                            if (ciudad == null) ciudad = "Ubicación obtenida";

                            String ciudadFinal = ciudad;
                            runOnUiThread(() -> {
                                txtUbicacion.setText("Ciudad: " + ciudadFinal);
                                txtUbicacion.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            });
                        } else {
                            runOnUiThread(() -> txtUbicacion.setText("¡Ubicación obtenida!"));
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> txtUbicacion.setText("¡Ubicación obtenida!"));
                    }
                }).start();

            } else {
                txtUbicacion.setText("No se encontró GPS activo");
            }
        } catch (SecurityException e) {
            txtUbicacion.setText("Error de permisos de ubicación");
        }
    }
}
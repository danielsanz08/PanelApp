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

public class EditarActivity extends AppCompatActivity {

    private EditText inputNombre, inputDescripcion;
    private Button btnActualizar, btnEditarFecha, btnEditarUbicacion;
    private TextView mensajeAlerta, txtFecha, txtUbicacion;
    private BD db;
    private int idRecordatorio = -1;

    private long fechaSeleccionada;
    private double latitud;
    private double longitud;
    private String rutaFoto;

    private LocationManager ubicacionGps;
    private ToneGenerator sonidoNotificacion;

    // "hh:mm a" en lugar de "HH:mm"
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        // Se conectan los elementos del XML
        inputNombre = findViewById(R.id.inputEditarNombre);
        inputDescripcion = findViewById(R.id.inputEditarDescripcion);
        btnActualizar = findViewById(R.id.btnActualizar);
        mensajeAlerta = findViewById(R.id.mensajeAlertaEditar);

        btnEditarFecha = findViewById(R.id.btnEditarFecha);
        txtFecha = findViewById(R.id.txtEditarFecha);
        btnEditarUbicacion = findViewById(R.id.btnEditarUbicacion);
        txtUbicacion = findViewById(R.id.txtEditarUbicacion);

        db = new BD(this);
        ubicacionGps = (LocationManager) getSystemService(LOCATION_SERVICE);
        sonidoNotificacion = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        //Se reciben los datos que envió la pantalla de la lista
        if (getIntent().hasExtra("ID_RECORDATORIO")) {
            idRecordatorio = getIntent().getIntExtra("ID_RECORDATORIO", -1);

            // Se llenan los textos
            inputNombre.setText(getIntent().getStringExtra("NOMBRE_RECORDATORIO"));
            inputDescripcion.setText(getIntent().getStringExtra("DESCRIPCION_RECORDATORIO"));

            // Se recuperan los datos extra como fecha, lat, lon
            fechaSeleccionada = getIntent().getLongExtra("FECHA_RECORDATORIO", System.currentTimeMillis());
            latitud = getIntent().getDoubleExtra("LATITUD_RECORDATORIO", 0);
            longitud = getIntent().getDoubleExtra("LONGITUD_RECORDATORIO", 0);
            rutaFoto = getIntent().getStringExtra("FOTO_RECORDATORIO");

            // Se muestra la fecha
            txtFecha.setText("Fecha: " + formatoFecha.format(new Date(fechaSeleccionada)));

            // Si ya tenía ubicación guardada, se busca la ciudad automáticamente
            if (latitud != 0 && longitud != 0) {
                txtUbicacion.setText("Buscando ciudad...");
                buscarCiudad(latitud, longitud);
            } else {
                txtUbicacion.setText("Sin ubicación registrada");
            }
        }

        // Acción del botón Fecha y Hora
        btnEditarFecha.setOnClickListener(v -> {
            Calendar calendario = Calendar.getInstance();
            calendario.setTimeInMillis(fechaSeleccionada);

            //Se abre el calendario
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendario.set(year, month, dayOfMonth);

                //Se abre el reloj
                int hora = calendario.get(Calendar.HOUR_OF_DAY);
                int minuto = calendario.get(Calendar.MINUTE);

                //El último parámetro en false para formato de 12 horas (AM/PM)
                TimePickerDialog timePicker = new TimePickerDialog(this, (viewTime, hourOfDay, minute) -> {
                    calendario.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendario.set(Calendar.MINUTE, minute);
                    calendario.set(Calendar.SECOND, 0);

                    fechaSeleccionada = calendario.getTimeInMillis();
                    txtFecha.setText("Fecha: " + formatoFecha.format(new Date(fechaSeleccionada)));
                }, hora, minuto, false);

                timePicker.show();

            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));

            datePicker.show();
        });

        //Acción del botón Ubicación
        btnEditarUbicacion.setOnClickListener(v -> solicitarUbicacion());

        // Acción del botón Actualizar
        btnActualizar.setOnClickListener(v -> {
            String nuevoNombre = inputNombre.getText().toString().trim();
            String nuevaDesc = inputDescripcion.getText().toString().trim();

            if (nuevoNombre.isEmpty()) {
                mensajeAlerta.setText("El nombre no puede estar vacío");
                return;
            }

            sonidoNotificacion.startTone(ToneGenerator.TONE_PROP_BEEP, 150);

            // Se crea el objeto con los datos modificados
            Recordatorio editado = new Recordatorio(
                    nuevoNombre, nuevaDesc, fechaSeleccionada, rutaFoto, latitud, longitud
            );
            editado.setId(idRecordatorio);

            int filasAfectadas = db.actualizar(editado);

            if (filasAfectadas > 0) {
                Toast.makeText(this, "Actualizado con éxito", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                mensajeAlerta.setText("Error al actualizar");
            }
        });
    }

    //METODO PARA OBTENER NUEVA UBICACIÓN
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

                txtUbicacion.setText("Actualizando ciudad...");
                buscarCiudad(latitud, longitud);

            } else {
                txtUbicacion.setText("No se encontró GPS activo");
            }
        } catch (SecurityException e) {
            txtUbicacion.setText("Error de permisos de ubicación");
        }
    }

    //METODO PARA TRADUCIR COORDENADAS
    private void buscarCiudad(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        new Thread(() -> {
            try {
                List<Address> direcciones = geocoder.getFromLocation(lat, lon, 1);
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
    }
}
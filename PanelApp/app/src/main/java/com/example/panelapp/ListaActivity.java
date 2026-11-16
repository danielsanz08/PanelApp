package com.example.panelapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListaActivity extends AppCompatActivity implements AdaptadorRecordatorio.OnItemClickListener {

    private RecyclerView recyclerRecordatorios;
    private BD db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        recyclerRecordatorios = findViewById(R.id.recyclerRecordatorios);
        db = new BD(this);
        recyclerRecordatorios.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    private void cargarLista() {
        List<Recordatorio> lista = db.obtenerTodos();
        AdaptadorRecordatorio adaptador = new AdaptadorRecordatorio(lista, this);
        recyclerRecordatorios.setAdapter(adaptador);
    }

    //ACCIONES DE LOS BOTONES

    // BOTÓN VER V
    @Override
    public void onVerClick(Recordatorio recordatorio) {
        // Se le da formato  a la fecha
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String fechaBonita = formato.format(new Date(recordatorio.getFechaHora()));

        // Se verifica si el recordatorio tiene coordenadas guardadas
        if (recordatorio.getLatitud() != 0 && recordatorio.getLongitud() != 0) {
            // Si tiene, se busca el nombre de la ciudad
            buscarCiudadParaMostrar(recordatorio, fechaBonita);
        } else {
            // Si no tiene, se muestra el texto de una vez
            mostrarDialogo(recordatorio, fechaBonita, "Sin ubicación registrada");
        }
    }

    // METODO PARA TRADUCIR COORDENADAS ANTES DE MOSTRAR EL TEXTO
    private void buscarCiudadParaMostrar(Recordatorio recordatorio, String fechaBonita) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        new Thread(() -> {
            String ciudad = "Ubicación registrada";
            try {
                List<Address> direcciones = geocoder.getFromLocation(recordatorio.getLatitud(), recordatorio.getLongitud(), 1);
                if (direcciones != null && !direcciones.isEmpty()) {
                    ciudad = direcciones.get(0).getLocality(); // Ciudad
                    if (ciudad == null) ciudad = direcciones.get(0).getSubAdminArea();
                    if (ciudad == null) ciudad = direcciones.get(0).getAdminArea();
                    if (ciudad == null) ciudad = "Ubicación registrada";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String ciudadFinal = ciudad;
            runOnUiThread(() -> mostrarDialogo(recordatorio, fechaBonita, ciudadFinal));
        }).start();
    }

    // MeTODO PARA MOSTRAR EL CUADRO DE TEXTO
    private void mostrarDialogo(Recordatorio recordatorio, String fechaBonita, String ubicacion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detalles del Recordatorio");
        builder.setMessage("ID: " + recordatorio.getId() + "\n\n" +
                "Nombre: " + recordatorio.getNombre() + "\n\n" +
                "Descripción: " + recordatorio.getDescripcion() + "\n\n" +
                "Fecha: " + fechaBonita + "\n\n" +
                "Ubicación: " + ubicacion);
        builder.setPositiveButton("Cerrar", null);
        builder.show();
    }

    // BOTÓN EDITAR (E)
    @Override
    public void onEditarClick(Recordatorio recordatorio) {
        Intent intent = new Intent(this, EditarActivity.class);
        intent.putExtra("ID_RECORDATORIO", recordatorio.getId());
        intent.putExtra("NOMBRE_RECORDATORIO", recordatorio.getNombre());
        intent.putExtra("DESCRIPCION_RECORDATORIO", recordatorio.getDescripcion());
        intent.putExtra("FECHA_RECORDATORIO", recordatorio.getFechaHora());
        intent.putExtra("LATITUD_RECORDATORIO", recordatorio.getLatitud());
        intent.putExtra("LONGITUD_RECORDATORIO", recordatorio.getLongitud());
        intent.putExtra("FOTO_RECORDATORIO", recordatorio.getUbicacionFoto());
        startActivity(intent);
    }

    // BOTÓN ELIMINAR (X)
    @Override
    public void onEliminarClick(Recordatorio recordatorio) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Recordatorio")
                .setMessage("¿Seguro que deseas eliminar '" + recordatorio.getNombre() + "'?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    db.eliminar(recordatorio.getId());
                    vibrar();
                    cargarLista();
                    Toast.makeText(this, "Recordatorio eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    //METODO DE VIBRACIÓN
    private void vibrar() {
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrador != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrador.vibrate(android.os.VibrationEffect.createOneShot(2000, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrador.vibrate(2000);
            }
        }
    }
}
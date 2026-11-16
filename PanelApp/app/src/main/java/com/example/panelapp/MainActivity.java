package com.example.panelapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Se carga el diseño del menú principal
        setContentView(R.layout.activity_main);

        // Se asigna la acción al botón de Crear Recordatorio
        findViewById(R.id.btnMenuCrear).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrearActivity.class);
            startActivity(intent);
        });

        // Se asigna la acción al botón de Ver Recordatorios
        findViewById(R.id.btnMenuVer).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListaActivity.class);
            startActivity(intent);
        });

        // Se asigna la acción al botón de Pruebas de Intents
        findViewById(R.id.btnMenuIntents).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, IntentsActivity.class);
            startActivity(intent);
        });
    }
}

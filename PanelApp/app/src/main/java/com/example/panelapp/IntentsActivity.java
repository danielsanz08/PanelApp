package com.example.panelapp;

import android.Manifest;// para pedir permisos al usuario (como usar camara o gps)
import android.app.NotificationChannel;// para crear y enviar notificaciones que aparecen arriba en el celular
import android.app.NotificationManager;
import android.content.Context;// para saber en que parte de la app y conectar con el sistema
import android.content.Intent; // para abrir otras apps (como la camara o el menu de compartir)
import android.content.pm.PackageManager; // para verificar si el celular tiene ciertas funciones instaladas
import android.graphics.Bitmap; // para manejar la imagen  que toma la camara
import android.location.Location; // para conectar  el gps y obtener la ubicacion del celular
import android.location.LocationManager;
import android.media.AudioManager;// para controlar el volumen y reproducir sonidos o  de prueba
import android.media.ToneGenerator;
import android.os.Build;  // para saber que version de android tiene el celular
import android.os.Bundle;// para guardar informacion temporal cuando la app se abre o se cierra
import android.os.Vibrator; // para activar el motor de vibracion fisico del celular
import android.provider.MediaStore; // para darle la orden al sistema de abrir la aplicacion de la camara
import android.widget.ImageView; // para controlar el espacio donde va la foto en la pantalla
import android.widget.TextView;  // para controlar el cuadro de texto donde muestra los mensajes

import androidx.activity.result.ActivityResultLauncher; // para esperar y recibir el resultado (la foto) despues de abrir la camara
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;// es la base que hace que esta pantalla funcione como una app de android
import androidx.core.app.ActivityCompat;// para que los permisos y las notificaciones funcionen bien en celulares viejos y nuevos
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class IntentsActivity extends AppCompatActivity {

    // Este es el cuadro de texto donde se muestran los mensajes al usuario
    private TextView alertaMensaje;

    // Este es el espacio de la pantalla donde se muestra la foto tomada
    private ImageView imagenCamara;

    // Esta variable permite conectar con el GPS del celular para conocer su ubicación
    private LocationManager ubicacionGps;

    // Esta variable permite reproducir el tono de prueba
    private ToneGenerator sonidoNotificacion;

    // Este es el identificador único del canal de notificaciones
    private static final String idNotificacion = "canal_panel_app";

    // Esta herramienta abre la cámara y espera a que el usuario tome una foto
    private final ActivityResultLauncher<Intent> tomadorCamara = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Si la foto se toma correctamente y contiene datos
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap foto = (Bitmap) extras.get("data");

                    // Se muestra la foto en el espacio de la pantalla
                    imagenCamara.setImageBitmap(foto);

                    // Se muestra el mensaje de confirmación
                    alertaMensaje.setText("Foto tomada y mostrada en pantalla");
                } else {
                    // Si el usuario cancela la acción o no toma la foto
                    alertaMensaje.setText("No se tomó ninguna foto");
                }
            }
    );

    // Este metodo se ejecuta cuando se abre la aplicación
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intents);

        // Se conectan las variables de Java con los elementos visuales del diseño XML
        alertaMensaje = findViewById(R.id.txtResultado);
        imagenCamara = findViewById(R.id.imgFoto);
        ubicacionGps = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sonidoNotificacion = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        // Se llama al metodo que crea el canal de notificaciones
        crearCanalNotificacion();

        // Se asigna a cada botón la función que ejecuta cuando el usuario lo toca
        findViewById(R.id.btnGps).setOnClickListener(v -> solicitarUbicacion());
        findViewById(R.id.btnCamara).setOnClickListener(v -> abrirCamara());
        findViewById(R.id.btnVibrar).setOnClickListener(v -> vibrar());
        findViewById(R.id.btnAudio).setOnClickListener(v -> reproducirSonido());
        findViewById(R.id.btnCompartir).setOnClickListener(v -> compartir());
        findViewById(R.id.btnNotif).setOnClickListener(v -> mostrarNotificacion());

    }

    // GPS
    // Este metodo solicita permiso e intenta obtener la ubicación actual del celular
    private void solicitarUbicacion() {
        // Primero se verifica si la aplicación tiene permiso del usuario
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Si no tiene permiso, se solicita al usuario
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        try {
            // Se intenta obtener la última ubicación conocida del GPS
            Location ubicacion = ubicacionGps.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (ubicacion != null) {
                // Si se encuentra la ubicación, se muestra un mensaje de confirmación
                alertaMensaje.setText("Ubicación encontrada ");
            } else {
                // Si no se encuentra la ubicación, se indica que se debe activar el GPS
                alertaMensaje.setText("No se encontró ubicación activa el gps");
            }
        } catch (SecurityException e) {
            // Si ocurre un error con los permisos, se muestra un mensaje de aviso
            alertaMensaje.setText("Error de permisos de ubicación");
        }
    }

    // CÁMARA
    // Este metodo prepara el celular para abrir la cámara
    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Se verifica que el celular tenga una aplicación de cámara disponible
        if (intent.resolveActivity(getPackageManager()) != null) {

            // Se abre la cámara y se espera el resultado
            tomadorCamara.launch(intent);
        }
    }

    //VIBRACIÓN
    // hace que el celular vibre durante un momento
    private void vibrar() {
        // Se obtiene el servicio de vibración del sistema
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrador != null) {

            // Si Android es una versión reciente, se utiliza este metodo de vibración
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrador.vibrate(android.os.VibrationEffect.createOneShot(2000, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {

                // Si Android es una versión antigua, se utiliza este otro metodo
                vibrador.vibrate(2000);
            }

            // Se muestra el mensaje de confirmación
            alertaMensaje.setText("El celular vibró por dos segundos");
        }
    }

    // AUDIO
    // Este metodo reproduce un pitido de prueba
    private void reproducirSonido() {
        sonidoNotificacion.startTone(ToneGenerator.TONE_PROP_BEEP, 1000);
        alertaMensaje.setText("Sonó un tono de prueba");
    }

    // COMPARTIR
    // Este metodo prepara un texto y abre el menú del celular para compartirlo con otras aplicaciones
    private void compartir() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        // Se establece el texto que se va a compartir
        intent.putExtra(Intent.EXTRA_TEXT, "Hola, estoy probando la función de compartir en Panel App");

        // Se abre el menú para elegir la aplicación con la que se comparte el texto
        startActivity(Intent.createChooser(intent, "Compartir con"));

        // Se muestra un mensaje para indicar que se abrió el menú de envío
        alertaMensaje.setText("Menú de envío");
    }

    //NOTIFICACIÓN (PARTE 1)
    // Este metodo crea el canal de notificaciones que requieren las versiones recientes de Android
    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Se crea el canal con su identificador, nombre e importancia
            NotificationChannel canal = new NotificationChannel(idNotificacion, "Canal Panel App", NotificationManager.IMPORTANCE_DEFAULT);

            // Se obtiene el administrador de notificaciones del sistema
            NotificationManager notificacionCompleta = getSystemService(NotificationManager.class);

            // Si el administrador está disponible, se registra el canal de notificaciones
            if (notificacionCompleta != null) notificacionCompleta.createNotificationChannel(canal);
        }
    }

    //NOTIFICACIÓN (PARTE 2)
    // Este metodo prepara y envía una notificación a la barra superior del celular
    private void mostrarNotificacion() {

        // Se verifica si la aplicación tiene permiso para enviar notificaciones
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            // Si no tiene permiso, se solicita al usuario
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 102);
            return;
        }

        // Se prepara la notificación con su título y mensaje
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, idNotificacion)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Panel App")
                .setContentText("La notificación funciona correctamente")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Se obtiene el administrador de notificaciones para enviarla
        NotificationManager notificacionCompleta = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificacionCompleta != null) {

            // Se envía la notificación y se muestra un mensaje de confirmación en la pantalla
            notificacionCompleta.notify(1, builder.build());
            alertaMensaje.setText("Notificación enviada");
        }
    }
}
package com.example.panelapp;

// Esta clase guarda los datos de un recordatorio.
public class Recordatorio {

    // Se declaran las variables que guardan los datos del recordatorio.
    private int id;
    private String nombre;
    private String descripcion;
    private long fechaHora;
    private String ubicacionFoto;

    // Se guarda la posición norte-sur de la ubicación.
    private double latitud;

    // Se guarda la posición este-oeste de la ubicación.
    private double longitud;

    // Constructor vacío.
    // Se usa cuando se crea un recordatorio sin datos.
    public Recordatorio() {
    }

    // Constructor con datos.
    // Se asignan los valores que llegan por parámetro.
    public Recordatorio(String nombre, String descripcion, long fechaHora,
                        String ubicacionFoto, double latitud, double longitud) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaHora = fechaHora;
        this.ubicacionFoto = ubicacionFoto;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    // Se devuelve el id.
    public int getId() { return id; }

    // Se cambia el id.
    public void setId(int id) { this.id = id; }

    // Se devuelve el nombre.
    public String getNombre() { return nombre; }

    // Se cambia el nombre.
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Se devuelve la descripción.
    public String getDescripcion() { return descripcion; }

    // Se cambia la descripción.
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // Se devuelve la fecha y hora.
    public long getFechaHora() { return fechaHora; }

    // Se cambia la fecha y hora.
    public void setFechaHora(long fechaHora) { this.fechaHora = fechaHora; }

    public String getUbicacionFoto() { return ubicacionFoto; }

    // Se cambia la ubicación del archivo de la foto.
    public void setUbicacionFoto(String ubicacionFoto) { this.ubicacionFoto = ubicacionFoto; }

    // Se devuelve la latitud.
    public double getLatitud() { return latitud; }

    // Se cambia la latitud.
    public void setLatitud(double latitud) { this.latitud = latitud; }

    // Se devuelve la longitud de la ubicación.
    public double getLongitud() { return longitud; }

    // Se cambia la longitud de la ubicación.
    public void setLongitud(double longitud) { this.longitud = longitud; }
}
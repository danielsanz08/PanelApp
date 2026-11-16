package com.example.panelapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

// Esta clase se encarga de guardar y Ver los recordatorios en la base de datos.
public class BD extends SQLiteOpenHelper {

    // Se define el nombre del archivo de la base de datos y la versión de la base de datos.
    private static final String NOMBRE_BD = "panelapp.db";
    private static final int VERSION_BD = 1;

    // Se define el nombre de la tabla y las columnas de la tabla
    private static final String TABLA = "recordatorios";
    private static final String COL_ID = "id";
    private static final String COL_NOMBRE = "nombre";
    private static final String COL_DESCRIPCION = "descripcion";
    private static final String COL_FECHA_HORA = "fecha_hora";
    private static final String COL_UBICACION_FOTO = "ubicacion_foto";
    private static final String COL_LATITUD = "latitud";
    private static final String COL_LONGITUD = "longitud";

    // Constructor de la clase.
    // Se recibe el contexto de la app y se pasa a la clase padre.
    public BD(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    // Se crea la tabla cuando la base de datos se crea por primera vez.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String crearTabla = "CREATE TABLE " + TABLA + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NOMBRE + " TEXT NOT NULL, "
                + COL_DESCRIPCION + " TEXT, "
                + COL_FECHA_HORA + " INTEGER NOT NULL, "
                + COL_UBICACION_FOTO + " TEXT, "
                + COL_LATITUD + " REAL, "
                + COL_LONGITUD + " REAL)";
        db.execSQL(crearTabla);

    }

    // Se borra la tabla anterior y se crea de nuevo
    // cuando cambia la versión de la base de datos.
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA);
        onCreate(db);
    }

    // Se convierten los datos de un recordatorio en valores
    // que la base de datos pueda guardar.
    private ContentValues aValores(Recordatorio r) {
        ContentValues valores = new ContentValues();
        valores.put(COL_NOMBRE, r.getNombre());
        valores.put(COL_DESCRIPCION, r.getDescripcion());
        valores.put(COL_FECHA_HORA, r.getFechaHora());
        valores.put(COL_UBICACION_FOTO, r.getUbicacionFoto());
        valores.put(COL_LATITUD, r.getLatitud());
        valores.put(COL_LONGITUD, r.getLongitud());
        return valores;
    }

    // Se crea un recordatorio a partir de una fila de la base de datos.
    private Recordatorio desdeCursor(Cursor c) {
        Recordatorio r = new Recordatorio();
        r.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
        r.setNombre(c.getString(c.getColumnIndexOrThrow(COL_NOMBRE)));
        r.setDescripcion(c.getString(c.getColumnIndexOrThrow(COL_DESCRIPCION)));
        r.setFechaHora(c.getLong(c.getColumnIndexOrThrow(COL_FECHA_HORA)));
        r.setUbicacionFoto(c.getString(c.getColumnIndexOrThrow(COL_UBICACION_FOTO)));
        r.setLatitud(c.getDouble(c.getColumnIndexOrThrow(COL_LATITUD)));
        r.setLongitud(c.getDouble(c.getColumnIndexOrThrow(COL_LONGITUD)));
        return r;
    }

    // Se inserta un nuevo recordatorio en la base de datos.
    public long insertar(Recordatorio r) {
        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(TABLA, null, aValores(r));
        db.close();
        return id;
    }

    // Se obtienen todos los recordatorios ordenados por fecha y hora.
    public List<Recordatorio> obtenerTodos() {
        List<Recordatorio> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLA, null, null, null, null, null, COL_FECHA_HORA + " ASC");
        while (c.moveToNext()) {
            lista.add(desdeCursor(c));
        }
        c.close();
        db.close();
        return lista;
    }

    // Se obtiene un recordatorio por su id.
    public Recordatorio obtenerPorId(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLA, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Recordatorio r = null;
        if (c.moveToFirst()) {
            r = desdeCursor(c);
        }
        c.close();
        db.close();
        return r;
    }

    // Se actualiza un recordatorio existente.
    public int actualizar(Recordatorio r) {
        SQLiteDatabase db = getWritableDatabase();
        int filas = db.update(TABLA, aValores(r), COL_ID + " = ?",
                new String[]{String.valueOf(r.getId())});
        db.close();
        return filas;
    }

    // Se elimina un recordatorio por su id.
    public int eliminar(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int filas = db.delete(TABLA, COL_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return filas;
    }
}
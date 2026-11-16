package com.example.panelapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdaptadorRecordatorio extends RecyclerView.Adapter<AdaptadorRecordatorio.ViewHolder> {

    private List<Recordatorio> listaRecordatorios;
    private OnItemClickListener listener;

    // Se crea una interfaz para saber qué botón se presionó en cada fila
    public interface OnItemClickListener {
        void onVerClick(Recordatorio recordatorio);
        void onEditarClick(Recordatorio recordatorio);
        void onEliminarClick(Recordatorio recordatorio);
    }

    // Se recibe la lista de datos
    public AdaptadorRecordatorio(List<Recordatorio> lista, OnItemClickListener listener) {
        this.listaRecordatorios = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Se conecta con el diseño de la fila
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recordatorio, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Se obtiene un recordatorio de la lista según su posición
        Recordatorio r = listaRecordatorios.get(position);

        // Se muestran los datos en la pantalla
        holder.txtId.setText(String.valueOf(r.getId()));
        holder.txtNombre.setText(r.getNombre());

        // Se activan los botones de la fila
        holder.btnVer.setOnClickListener(v -> listener.onVerClick(r));
        holder.btnEditar.setOnClickListener(v -> listener.onEditarClick(r));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminarClick(r));
    }

    @Override
    public int getItemCount() {
        return listaRecordatorios.size();
    }

    // Se conectan las variables con los IDs del XML
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtId, txtNombre;
        Button btnVer, btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtItemId);
            txtNombre = itemView.findViewById(R.id.txtItemNombre);
            btnVer = itemView.findViewById(R.id.btnItemVer);
            btnEditar = itemView.findViewById(R.id.btnItemEditar);
            btnEliminar = itemView.findViewById(R.id.btnItemEliminar);
        }
    }
}
package pe.uch.nigosha.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import pe.uch.nigosha.R;
import pe.uch.nigosha.activities.ReservarCitaActivity;
import pe.uch.nigosha.models.Servicio;

public class ServicioAdapter extends RecyclerView.Adapter<ServicioAdapter.ViewHolder> {
    private Context context;
    private List<Servicio> lista;

    public ServicioAdapter(Context context, List<Servicio> lista) {
        this.context = context;
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_servicio, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Servicio s = lista.get(position);
        holder.tvNombre.setText(s.nombre);
        holder.tvCategoria.setText(s.categoria + " • " + s.duracionMinutos + " min");
        holder.tvPrecio.setText(String.format("S/ %.2f", s.precio));

        holder.btnReservar.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReservarCitaActivity.class);
            intent.putExtra("servicio", s);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCategoria, tvPrecio;
        MaterialButton btnReservar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreServicio);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            btnReservar = itemView.findViewById(R.id.btnReservar);
        }
    }
}
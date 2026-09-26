package pe.uch.nigosha.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import pe.uch.nigosha.R;
import pe.uch.nigosha.models.Cita;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.ViewHolder> {
    private Context context;
    private List<Cita> lista;

    public CitaAdapter(Context context, List<Cita> lista) {
        this.context = context;
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cita, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cita c = lista.get(position);
        holder.tvServicio.setText(c.servicioNombre);
        holder.tvEstado.setText(c.estado);
        holder.tvFechaHora.setText("📅 " + c.fecha + "  |  ⏰ " + c.hora);
        holder.tvAdelanto.setText(String.format("Total: S/ %.2f (Adelanto: S/ %.2f)", c.precio, c.adelanto));
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServicio, tvEstado, tvFechaHora, tvAdelanto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServicio = itemView.findViewById(R.id.tvCitaServicio);
            tvEstado = itemView.findViewById(R.id.tvCitaEstado);
            tvFechaHora = itemView.findViewById(R.id.tvCitaFechaHora);
            tvAdelanto = itemView.findViewById(R.id.tvCitaAdelanto);
        }
    }
}
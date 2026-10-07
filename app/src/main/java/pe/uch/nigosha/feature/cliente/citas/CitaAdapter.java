package pe.uch.nigosha.feature.cliente.citas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import pe.uch.nigosha.R;
import pe.uch.nigosha.core.util.MoneyFormatter;
import pe.uch.nigosha.domain.models.Cita;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.Holder> {

  public interface OnCitaClick {
    void onClick(Cita cita);
  }

  private final List<Cita> items = new ArrayList<>();
  private final OnCitaClick callback;

  public CitaAdapter(OnCitaClick callback) {
    this.callback = callback;
  }

  public void submit(List<Cita> citas) {
    items.clear();
    items.addAll(citas);
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new Holder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_cita_cliente, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull Holder holder, int position) {
    Cita cita = items.get(position);

    holder.estado.setText("Estado: " + readable(cita.estado));

    holder.servicio.setText(cita.servicioNombre == null ? "Servicio Nigosha" : cita.servicioNombre);

    holder.fecha.setText(
        (cita.fecha == null ? "Fecha por revisar" : cita.fecha)
            + " · "
            + (cita.hora == null ? "Hora por revisar" : cita.hora));

    holder.pago.setText("Pago: " + readable(cita.estadoPago));

    holder.total.setText("Total: " + MoneyFormatter.format(cita.precio));

    holder.detalle.setOnClickListener(view -> callback.onClick(cita));
  }

  private String readable(String value) {
    return value == null || value.trim().isEmpty() ? "Por verificar" : value.replace('_', ' ');
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static class Holder extends RecyclerView.ViewHolder {

    final TextView estado;
    final TextView servicio;
    final TextView fecha;
    final TextView pago;
    final TextView total;
    final MaterialButton detalle;

    Holder(@NonNull View view) {
      super(view);

      estado = view.findViewById(R.id.tvMiCitaEstado);
      servicio = view.findViewById(R.id.tvMiCitaServicio);
      fecha = view.findViewById(R.id.tvMiCitaFecha);
      pago = view.findViewById(R.id.tvMiCitaPago);
      total = view.findViewById(R.id.tvMiCitaTotal);
      detalle = view.findViewById(R.id.btnMiCitaDetalle);
    }
  }
}

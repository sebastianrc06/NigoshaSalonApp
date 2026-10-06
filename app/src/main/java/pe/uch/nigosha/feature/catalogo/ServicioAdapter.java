package pe.uch.nigosha.feature.catalogo;

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
import pe.uch.nigosha.domain.models.Servicio;

public class ServicioAdapter
        extends RecyclerView.Adapter<ServicioAdapter.Holder> {

  public interface OnServicioClick {
    void onClick(Servicio servicio);
  }

  private final List<Servicio> items = new ArrayList<>();
  private final OnServicioClick callback;

  public ServicioAdapter(OnServicioClick callback) {
    this.callback = callback;
  }

  public void submit(List<Servicio> servicios) {
    items.clear();
    items.addAll(servicios);
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public Holder onCreateViewHolder(
          @NonNull ViewGroup parent,
          int viewType
  ) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(
                    R.layout.item_servicio_catalogo,
                    parent,
                    false
            );

    return new Holder(view);
  }

  @Override
  public void onBindViewHolder(
          @NonNull Holder holder,
          int position
  ) {
    Servicio servicio = items.get(position);

    holder.nombre.setText(servicio.nombre);
    holder.categoria.setText(servicio.categoria);

    holder.duracion.setText(
            servicio.duracionMinutos > 0
                    ? "Duración estimada: "
                    + servicio.duracionMinutos + " min"
                    : "Duración por confirmar"
    );

    holder.precio.setText(
            servicio.precioPorConfirmar
                    ? "Precio por confirmar"
                    : (servicio.precioDesde ? "Desde " : "")
                    + MoneyFormatter.format(servicio.precio)
    );

    holder.detalle.setContentDescription(
            "Ver detalle de " + servicio.nombre
    );

    holder.detalle.setOnClickListener(
            view -> callback.onClick(servicio)
    );
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static class Holder extends RecyclerView.ViewHolder {

    final TextView nombre;
    final TextView categoria;
    final TextView duracion;
    final TextView precio;

    final MaterialButton detalle;

    Holder(@NonNull View view) {
      super(view);

      nombre = view.findViewById(R.id.tvServicioNombre);
      categoria = view.findViewById(R.id.tvServicioCategoria);
      duracion = view.findViewById(R.id.tvServicioDuracion);
      precio = view.findViewById(R.id.tvServicioPrecio);
      detalle = view.findViewById(R.id.btnServicioDetalle);
    }
  }
}
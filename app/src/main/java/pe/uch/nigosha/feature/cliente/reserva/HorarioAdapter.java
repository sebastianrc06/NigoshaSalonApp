package pe.uch.nigosha.feature.cliente.reserva;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import pe.uch.nigosha.R;
import pe.uch.nigosha.core.util.DateTimeFormatter;

public final class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.Holder> {
  public interface Listener {
    void onSelect(long inicio);
  }

  public static final class Horario {
    public final long inicio;
    public final boolean disponible, seleccionado;

    public Horario(long inicio, boolean disponible, boolean seleccionado) {
      this.inicio = inicio;
      this.disponible = disponible;
      this.seleccionado = seleccionado;
    }
  }

  private final List<Horario> items = new ArrayList<>();
  private final Listener listener;

  public HorarioAdapter(Listener listener) {
    this.listener = listener;
  }

  public void submit(List<Horario> values) {
    items.clear();
    items.addAll(values);
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
    return new Holder(
        (MaterialButton)
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horario, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull Holder holder, int position) {
    Horario item = items.get(position);
    MaterialButton button = holder.button;
    String hora = DateTimeFormatter.format(item.inicio, "HH:mm");
    button.setText(hora);
    button.setEnabled(item.disponible);
    button.setSelected(item.seleccionado);
    button.setAlpha(item.disponible ? 1f : 0.42f);
    int fondo = item.seleccionado ? R.color.nigosha_primary : R.color.nigosha_surface;
    int texto = item.seleccionado ? R.color.nigosha_on_brand : R.color.nigosha_text_title;
    button.setBackgroundTintList(
        ColorStateList.valueOf(ContextCompat.getColor(button.getContext(), fondo)));
    button.setTextColor(ContextCompat.getColor(button.getContext(), texto));
    button.setContentDescription(
        hora
            + (item.seleccionado
                ? ", seleccionado"
                : item.disponible ? ", disponible" : ", no disponible"));
    button.setOnClickListener(
        v -> {
          if (item.disponible) listener.onSelect(item.inicio);
        });
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static final class Holder extends RecyclerView.ViewHolder {
    final MaterialButton button;

    Holder(MaterialButton button) {
      super(button);
      this.button = button;
    }
  }
}

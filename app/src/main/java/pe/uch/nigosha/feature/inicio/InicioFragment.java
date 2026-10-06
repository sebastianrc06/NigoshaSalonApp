package pe.uch.nigosha.feature.inicio;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import pe.uch.nigosha.R;
import pe.uch.nigosha.core.ui.*;
import pe.uch.nigosha.core.util.DateTimeFormatter;
import pe.uch.nigosha.domain.models.Cita;

public final class InicioFragment extends Fragment {
  private InicioViewModel model;
  private View pantalla;
  private Cita proxima;
  private final Runnable refresh =
      new Runnable() {
        @Override
        public void run() {
          if (pantalla != null) {
            model.refresh();
            pantalla.postDelayed(this, 30000);
          }
        }
      };

  public InicioFragment() {
    super(R.layout.fragment_inicio);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
    super.onViewCreated(view, saved);
    pantalla = view;
    model = new ViewModelProvider(this).get(InicioViewModel.class);
    view.findViewById(R.id.btnExplorarServicios)
        .setOnClickListener(v -> seleccionarSeccion(R.id.catalogoFragment));
    view.findViewById(R.id.btnConsultarCitas)
        .setOnClickListener(v -> seleccionarSeccion(R.id.misCitasFragment));
    view.findViewById(R.id.btnConocerSalon)
        .setOnClickListener(v -> seleccionarSeccion(R.id.salonFragment));
    view.findViewById(R.id.btnReintentarEstado).setOnClickListener(v -> model.reload());
    view.findViewById(R.id.btnProximaInicio)
        .setOnClickListener(
            v -> {
              if (proxima == null) return;
              Bundle args = new Bundle();
              args.putString("citaId", proxima.id);
              NavHostFragment.findNavController(this).navigate(R.id.detalleCitaFragment, args);
            });
    model
        .getState()
        .observe(
            getViewLifecycleOwner(),
            state -> {
              StateViews.render(view, state, R.id.cardProximaInicio);
              proxima = null;
              boolean vacio =
                  state != null && state.status == UiState.Status.SUCCESS && state.data == null;
              view.findViewById(R.id.estadoVacio).setVisibility(vacio ? View.VISIBLE : View.GONE);
              if (vacio) view.findViewById(R.id.cardProximaInicio).setVisibility(View.GONE);
              if (state != null && state.status == UiState.Status.SUCCESS && state.data != null) {
                proxima = state.data;
                ((TextView) view.findViewById(R.id.tvProximaServicio))
                    .setText(DateTimeFormatter.texto(proxima.servicioNombre, "Servicio"));
                ((TextView) view.findViewById(R.id.tvProximaFecha))
                    .setText(
                        DateTimeFormatter.format(
                            proxima.inicioOrden(), "EEEE d 'de' MMMM · HH:mm"));
                ((TextView) view.findViewById(R.id.tvProximaEstado))
                    .setText("Reserva: " + DateTimeFormatter.estado(proxima.estado));
              }
            });
  }

  private void seleccionarSeccion(int destino) {
    ((BottomNavigationView) requireActivity().findViewById(R.id.clienteBottomNavigation))
        .setSelectedItemId(destino);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (pantalla != null) pantalla.post(refresh);
  }

  @Override
  public void onPause() {
    if (pantalla != null) pantalla.removeCallbacks(refresh);
    super.onPause();
  }

  @Override
  public void onDestroyView() {
    pantalla.removeCallbacks(refresh);
    pantalla = null;
    proxima = null;
    super.onDestroyView();
  }
}

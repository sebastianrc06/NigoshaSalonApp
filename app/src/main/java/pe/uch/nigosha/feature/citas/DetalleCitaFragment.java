package pe.uch.nigosha.feature.citas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import pe.uch.nigosha.R;
import pe.uch.nigosha.core.ui.*;
import pe.uch.nigosha.domain.models.Cita;
import pe.uch.nigosha.feature.salon.SalonViewModel;
import pe.uch.nigosha.integration.calendar.CalendarHelper;

public final class DetalleCitaFragment extends Fragment {
  private DetalleCitaViewModel model;
  private SalonViewModel salon;
  private Cita cita;
  private View pantalla;
  private final Runnable refresh =
      new Runnable() {
        @Override
        public void run() {
          if (pantalla != null) {
            controles();
            pantalla.postDelayed(this, 30000);
          }
        }
      };

  public DetalleCitaFragment() {
    super(R.layout.fragment_detalle_cita);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
    super.onViewCreated(view, saved);
    pantalla = view;
    model = new ViewModelProvider(this).get(DetalleCitaViewModel.class);
    salon = new ViewModelProvider(this).get(SalonViewModel.class);
    view.findViewById(R.id.btnVolverDetalle)
        .setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    view.findViewById(R.id.btnReintentarEstado).setOnClickListener(v -> model.reload());
    view.findViewById(R.id.btnAdelantoDetalle)
        .setOnClickListener(
            v -> {
              if (cita == null) return;
              Bundle args = new Bundle();
              args.putString("citaId", cita.id);
              NavHostFragment.findNavController(this).navigate(R.id.pagoFragment, args);
            });
    view.findViewById(R.id.btnCalendarioDetalle)
        .setOnClickListener(
            v ->
                CalendarHelper.agregar(
                    requireContext(), cita, salon.getSalon().direccionCompleta()));
    view.findViewById(R.id.btnCancelarDetalle)
        .setOnClickListener(
            v ->
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("¿Cancelar esta solicitud?")
                    .setMessage("Quedará en tu historial y su horario volverá a estar disponible.")
                    .setNegativeButton("Conservar", null)
                    .setPositiveButton("Sí, cancelar", (dialog, which) -> model.cancelar())
                    .show());
    model
        .getState()
        .observe(
            getViewLifecycleOwner(),
            state -> {
              StateViews.render(view, state, R.id.contenidoDetalle);
              cita = null;
              if (state != null && state.status == UiState.Status.SUCCESS) {
                cita = state.data;
                CitaViews.bind(view, cita);
              }
              controles();
            });
    model
        .getOperation()
        .observe(
            getViewLifecycleOwner(),
            op -> {
              TextView message = view.findViewById(R.id.tvOperacionDetalle);
              message.setText(
                  op == null
                      ? ""
                      : op.status == UiState.Status.LOADING
                          ? "Cancelando solicitud…"
                          : op.status == UiState.Status.ERROR ? op.message : op.data);
              controles();
            });
    model.load(requireArguments().getString("citaId"));
  }

  private void controles() {
    if (pantalla == null) return;
    pantalla
        .findViewById(R.id.btnCancelarDetalle)
        .setVisibility(
            cita != null && cita.cancelable(System.currentTimeMillis()) ? View.VISIBLE : View.GONE);
    pantalla.findViewById(R.id.btnCancelarDetalle).setEnabled(!model.isBusy());
    pantalla
        .findViewById(R.id.btnCalendarioDetalle)
        .setVisibility(CalendarHelper.disponible(cita) ? View.VISIBLE : View.GONE);
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
    cita = null;
    super.onDestroyView();
  }
}

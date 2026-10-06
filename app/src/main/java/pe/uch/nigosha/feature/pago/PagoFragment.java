package pe.uch.nigosha.feature.pago;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import pe.uch.nigosha.R;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.core.util.DateTimeFormatter;
import pe.uch.nigosha.domain.models.Cita;

public final class PagoFragment extends Fragment {
  public PagoFragment() {
    super(R.layout.fragment_pago);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
    super.onViewCreated(view, saved);
    PagoViewModel model = new ViewModelProvider(this).get(PagoViewModel.class);
    view.findViewById(R.id.btnVolverPago)
        .setOnClickListener(
            v -> {
              if (requireActivity() instanceof PagoActivity) requireActivity().finish();
              else NavHostFragment.findNavController(this).popBackStack();
            });
    view.findViewById(R.id.btnReintentarPago).setOnClickListener(v -> model.reload());
    model
        .getState()
        .observe(
            getViewLifecycleOwner(),
            state -> {
              if (state == null) return;
              boolean ok = state.status == UiState.Status.SUCCESS,
                  error = state.status == UiState.Status.ERROR;
              view.findViewById(R.id.progressPago)
                  .setVisibility(state.status == UiState.Status.LOADING ? View.VISIBLE : View.GONE);
              view.findViewById(R.id.contenidoPago).setVisibility(ok ? View.VISIBLE : View.GONE);
              view.findViewById(R.id.tvMensajePago).setVisibility(error ? View.VISIBLE : View.GONE);
              view.findViewById(R.id.btnReintentarPago)
                  .setVisibility(error ? View.VISIBLE : View.GONE);
              text(view, R.id.tvMensajePago, state.message);
              if (!ok) return;
              Cita cita = state.data.cita;
              text(
                  view,
                  R.id.tvPagoServicio,
                  DateTimeFormatter.texto(cita.servicioNombre, "Servicio"));
              text(
                  view,
                  R.id.tvPagoFecha,
                  cita.inicioOrden() > 0
                      ? DateTimeFormatter.format(cita.inicioOrden(), "dd/MM/yyyy · HH:mm")
                      : "Fecha por verificar");
              text(view, R.id.tvPagoCodigo, "Código: " + cita.id);
              text(view, R.id.tvPagoEstado, "Pago: " + DateTimeFormatter.estado(cita.estadoPago));
              text(view, R.id.tvPagoTotal, state.data.total);
              text(view, R.id.tvPagoRequerido, state.data.requerido);
              text(view, R.id.tvPagoAbonado, state.data.abonado);
              text(view, R.id.tvPagoSaldo, state.data.saldo);
              text(
                  view,
                  R.id.tvPagoAviso,
                  "Consulta el registro de tu adelanto. El salón todavía no ha habilitado pagos en"
                      + " línea desde la aplicación. No se realizará ningún cobro.");
            });
    model.load(requireArguments().getString("citaId"));
  }

  private void text(View v, int id, String value) {
    ((TextView) v.findViewById(id)).setText(value);
  }
}

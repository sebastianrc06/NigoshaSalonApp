package pe.uch.nigosha.feature.reserva;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.button.MaterialButton;
import pe.uch.nigosha.R;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.core.util.*;
import pe.uch.nigosha.domain.models.Servicio;

public final class ResumenReservaFragment extends Fragment {
  private ReservaViewModel model;
  private ReservaSubmitViewModel submit;
  private View pantalla;
  private boolean navegando;
  private OnBackPressedCallback back;

  public ResumenReservaFragment() {
    super(R.layout.fragment_resumen_reserva);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
    super.onViewCreated(view, saved);
    pantalla = view;
    navegando = false;
    model = ReservaFlow.provider(this).get(ReservaViewModel.class);
    submit = ReservaFlow.provider(this).get(ReservaSubmitViewModel.class);
    Bundle args = requireArguments();
    model.load(args.getString("servicioId"));
    text(view, R.id.tvResumenServicio, args.getString("nombreServicio"));
    long inicio = args.getLong("inicio");
    text(view, R.id.tvResumenFecha, DateTimeFormatter.format(inicio, "EEEE d 'de' MMMM · HH:mm"));
    text(
        view,
        R.id.tvResumenFin,
        "Duración: "
            + args.getInt("duracion")
            + " min · Fin estimado: "
            + DateTimeFormatter.format(inicio + args.getInt("duracion") * 60000L, "HH:mm"));
    text(view, R.id.tvResumenCliente, args.getString("cliente"));
    text(view, R.id.tvResumenTelefono, args.getString("telefono"));
    text(view, R.id.tvResumenTotal, MoneyFormatter.format(args.getDouble("precio")));
    text(
        view,
        R.id.tvResumenAdelanto,
        MoneyFormatter.format(MoneyFormatter.adelanto(args.getDouble("precio"))));
    view.findViewById(R.id.btnEditarResumen)
        .setOnClickListener(
            v -> {
              if (!submit.isBusy()) NavHostFragment.findNavController(this).popBackStack();
            });
    view.findViewById(R.id.btnConfirmarResumen).setOnClickListener(v -> confirmar());
    view.findViewById(R.id.btnReintentarResumen)
        .setOnClickListener(
            v -> {
              model.reload();
              model.retryAgenda();
            });
    back =
        new OnBackPressedCallback(false) {
          @Override
          public void handleOnBackPressed() {}
        };
    requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), back);
    model.getSlots().observe(getViewLifecycleOwner(), values -> actualizar());
    model.getServiceState().observe(getViewLifecycleOwner(), state -> actualizar());
    submit
        .getState()
        .observe(
            getViewLifecycleOwner(),
            state -> {
              actualizar();
              if (state != null && state.status == UiState.Status.SUCCESS && !navegando) {
                // Comprueba destino para evitar una segunda navegación por un callback repetido.
                if (NavHostFragment.findNavController(this).getCurrentDestination().getId()
                    != R.id.resumenReservaFragment) return;
                navegando = true;
                Bundle result = new Bundle();
                result.putString("citaId", state.data);
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_resumen_resultado, result);
              }
            });
  }

  private boolean vigente() {
    Bundle a = requireArguments();
    Servicio s = model.getServicio();
    Long start = model.getSelectedStart();
    return s != null
        && start != null
        && start == a.getLong("inicio")
        && model.canSubmit()
        && s.id.equals(a.getString("servicioId"))
        && s.nombre.equals(a.getString("nombreServicio"))
        && Double.compare(s.precio, a.getDouble("precio")) == 0
        && s.duracionMinutos == a.getInt("duracion");
  }

  private boolean puedeEnviar() {
    if (vigente()) return true;
    Servicio s = model.getServicio();
    Bundle a = requireArguments();
    // Un envío con respuesta incierta se recupera con la misma clave idempotente.
    return submit.puedeRecuperarEnvio()
        && s != null
        && s.id.equals(a.getString("servicioId"))
        && s.nombre.equals(a.getString("nombreServicio"))
        && Double.compare(s.precio, a.getDouble("precio")) == 0
        && s.duracionMinutos == a.getInt("duracion");
  }

  private void confirmar() {
    if (submit.isBusy() || submit.isSaved()) return;
    model.refreshSlots();
    actualizar();
    if (!puedeEnviar()) return;
    Bundle a = requireArguments();
    submit.submit(
        model.getServicio(), a.getString("cliente"), a.getString("telefono"), a.getLong("inicio"));
  }

  private void actualizar() {
    if (pantalla == null || back == null) return;
    boolean busy = submit.isBusy();
    back.setEnabled(busy);
    MaterialButton button = pantalla.findViewById(R.id.btnConfirmarResumen);
    button.setEnabled(puedeEnviar() && !busy && !submit.isSaved());
    button.setText(busy ? "Registrando solicitud…" : "Solicitar cita");
    pantalla.findViewById(R.id.btnEditarResumen).setEnabled(!busy);
    pantalla.findViewById(R.id.progressResumen).setVisibility(busy ? View.VISIBLE : View.GONE);
    UiState<String> op = submit.getState().getValue();
    UiState<Servicio> service = model.getServiceState().getValue();
    String message =
        busy
            ? "Comprobando disponibilidad y guardando tu solicitud…"
            : op != null && op.status == UiState.Status.ERROR
                ? op.message
                : service == null || service.status == UiState.Status.LOADING
                    ? "Consultando el servicio…"
                    : service.status == UiState.Status.ERROR
                        ? service.message
                        : model.isAgendaFailed()
                            ? "No pudimos verificar la disponibilidad. Reintenta."
                            : !vigente()
                                ? "El horario o el servicio cambió. Vuelve a editar tu selección."
                                : "Tu solicitud quedará pendiente de confirmación. Todavía no se"
                                    + " realizará un cobro.";
    text(pantalla, R.id.tvMensajeResumen, message);
    pantalla
        .findViewById(R.id.btnReintentarResumen)
        .setVisibility(
            !busy
                    && (model.isAgendaFailed()
                        || service != null && service.status == UiState.Status.ERROR)
                ? View.VISIBLE
                : View.GONE);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (model != null) model.refreshSlots();
  }

  private void text(View v, int id, String value) {
    ((TextView) v.findViewById(id)).setText(value);
  }

  @Override
  public void onDestroyView() {
    pantalla = null;
    back = null;
    super.onDestroyView();
  }
}

package pe.uch.nigosha.feature.reserva;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import pe.uch.nigosha.R;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.core.util.InputValidator;
import pe.uch.nigosha.core.util.MoneyFormatter;
import pe.uch.nigosha.domain.models.Servicio;

public final class ReservaFragment extends Fragment {
  private ReservaViewModel model;
  private ReservaSubmitViewModel submit;
  private TextInputEditText nombre, telefono;
  private TextWatcher nombreWatcher, telefonoWatcher;
  private RecyclerView horarios;
  private HorarioAdapter adapter;
  private View pantalla;
  private final Runnable refrescar =
      new Runnable() {
        @Override
        public void run() {
          if (pantalla != null) {
            model.refreshSlots();
            pantalla.postDelayed(this, 30000);
          }
        }
      };

  public ReservaFragment() {
    super(R.layout.fragment_reserva);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
    super.onViewCreated(view, saved);
    pantalla = view;
    model = ReservaFlow.provider(this).get(ReservaViewModel.class);
    submit = ReservaFlow.provider(this).get(ReservaSubmitViewModel.class);
    if (submit.isSaved()) submit.nuevaSolicitud();
    nombre = view.findViewById(R.id.etNombreReserva);
    telefono = view.findViewById(R.id.etTelefonoReserva);
    nombre.setText(model.getNombre());
    telefono.setText(model.getTelefono());
    nombreWatcher = watcher(true);
    telefonoWatcher = watcher(false);
    nombre.addTextChangedListener(nombreWatcher);
    telefono.addTextChangedListener(telefonoWatcher);
    horarios = view.findViewById(R.id.rvHorariosReserva);
    horarios.setLayoutManager(new GridLayoutManager(requireContext(), 3));
    horarios.setNestedScrollingEnabled(false);
    adapter =
        new HorarioAdapter(
            start -> {
              model.selectStart(start);
              model.refreshSlots();
            });
    horarios.setAdapter(adapter);
    view.findViewById(R.id.btnVolverReserva)
        .setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    view.findViewById(R.id.btnElegirFechaReserva).setOnClickListener(v -> elegirFecha());
    view.findViewById(R.id.btnReintentarReserva)
        .setOnClickListener(
            v -> {
              model.reload();
              model.retryAgenda();
            });
    view.findViewById(R.id.btnRevisarReserva).setOnClickListener(v -> revisar());
    model
        .getServiceState()
        .observe(
            getViewLifecycleOwner(),
            state -> {
              if (state == null) return;
              boolean ok = state.status == UiState.Status.SUCCESS;
              view.findViewById(R.id.progressReserva)
                  .setVisibility(state.status == UiState.Status.LOADING ? View.VISIBLE : View.GONE);
              view.findViewById(R.id.contenidoReserva).setVisibility(ok ? View.VISIBLE : View.GONE);
              TextView error = view.findViewById(R.id.tvErrorReserva);
              error.setVisibility(state.status == UiState.Status.ERROR ? View.VISIBLE : View.GONE);
              error.setText(state.message);
              if (ok) {
                Servicio servicio = state.data;
                ((TextView) view.findViewById(R.id.tvReservaServicio)).setText(servicio.nombre);
                ((TextView) view.findViewById(R.id.tvReservaImportes))
                    .setText(
                        servicio.duracionMinutos
                            + " minutos · "
                            + MoneyFormatter.format(servicio.precio)
                            + "\nAdelanto requerido (50%): "
                            + MoneyFormatter.format(MoneyFormatter.adelanto(servicio.precio)));
              }
              controles();
            });
    model
        .getAgendaMessage()
        .observe(
            getViewLifecycleOwner(),
            message -> {
              ((TextView) view.findViewById(R.id.tvMensajeHorarios)).setText(message);
              controles();
            });
    model
        .getSlots()
        .observe(
            getViewLifecycleOwner(),
            values -> {
              List<HorarioAdapter.Horario> items = new ArrayList<>();
              if (values != null)
                for (Long start : values)
                  items.add(
                      new HorarioAdapter.Horario(
                          start, model.isAvailable(start), start.equals(model.getSelectedStart())));
              adapter.submit(items);
              ((MaterialButton) view.findViewById(R.id.btnElegirFechaReserva))
                  .setText(model.getDateLabel());
              controles();
            });
    model.load(requireArguments().getString("servicioId"));
  }

  private void controles() {
    if (pantalla == null) return;
    pantalla.findViewById(R.id.btnRevisarReserva).setEnabled(model.canSubmit() && !submit.isBusy());
    UiState<Servicio> state = model.getServiceState().getValue();
    boolean error = state != null && state.status == UiState.Status.ERROR;
    pantalla
        .findViewById(R.id.btnReintentarReserva)
        .setVisibility(error || model.isAgendaFailed() ? View.VISIBLE : View.GONE);
  }

  private TextWatcher watcher(boolean esNombre) {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (esNombre) model.setNombre(s.toString());
        else model.setTelefono(s.toString());
      }

      @Override
      public void afterTextChanged(Editable e) {}
    };
  }

  private void elegirFecha() {
    Calendar day = model.getPickerDay();
    DatePickerDialog picker =
        new DatePickerDialog(
            requireContext(),
            (v, y, m, d) -> model.selectDay(y, m, d),
            day.get(Calendar.YEAR),
            day.get(Calendar.MONTH),
            day.get(Calendar.DAY_OF_MONTH));
    Calendar today = Calendar.getInstance();
    today.set(Calendar.HOUR_OF_DAY, 0);
    today.set(Calendar.MINUTE, 0);
    today.set(Calendar.SECOND, 0);
    today.set(Calendar.MILLISECOND, 0);
    picker.getDatePicker().setMinDate(today.getTimeInMillis());
    picker.show();
  }

  private void revisar() {
    TextInputLayout inputNombre = pantalla.findViewById(R.id.inputNombreReserva);
    TextInputLayout inputTelefono = pantalla.findViewById(R.id.inputTelefonoReserva);
    boolean nameOk = InputValidator.nombreValido(model.getNombre());
    boolean phoneOk = InputValidator.celularValido(model.getTelefono());
    inputNombre.setError(nameOk ? null : "Escribe tu nombre y apellido (3 a 100 caracteres).");
    inputTelefono.setError(phoneOk ? null : "Escribe un celular peruano de 9 dígitos.");
    if (!nameOk || !phoneOk) return;
    model.refreshSlots();
    if (!model.canSubmit()) return;
    Servicio service = model.getServicio();
    Bundle args = new Bundle();
    args.putString("servicioId", service.id);
    args.putString("nombreServicio", service.nombre);
    args.putDouble("precio", service.precio);
    args.putInt("duracion", service.duracionMinutos);
    args.putLong("inicio", model.getSelectedStart());
    args.putString("cliente", model.getNombre().trim());
    args.putString("telefono", InputValidator.celular(model.getTelefono()));
    NavHostFragment.findNavController(this).navigate(R.id.action_reserva_resumen, args);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (pantalla != null) {
      pantalla.removeCallbacks(refrescar);
      pantalla.post(refrescar);
    }
  }

  @Override
  public void onPause() {
    if (pantalla != null) pantalla.removeCallbacks(refrescar);
    super.onPause();
  }

  @Override
  public void onDestroyView() {
    pantalla.removeCallbacks(refrescar);
    nombre.removeTextChangedListener(nombreWatcher);
    telefono.removeTextChangedListener(telefonoWatcher);
    horarios.setAdapter(null);
    pantalla = null;
    horarios = null;
    adapter = null;
    nombre = null;
    telefono = null;
    super.onDestroyView();
  }
}

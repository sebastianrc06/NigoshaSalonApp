package pe.uch.nigosha.feature.cliente.reserva;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import pe.uch.nigosha.app.AppContainer;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.domain.models.ConfiguracionReservas;
import pe.uch.nigosha.domain.models.Servicio;
import pe.uch.nigosha.domain.repository.DisponibilidadRepository;
import pe.uch.nigosha.domain.repository.ServicioRepository;

public class ReservaViewModel extends ViewModel {

  private static final int OPEN_HOUR =
          ConfiguracionReservas.APERTURA;

  private static final int CLOSE_HOUR =
          ConfiguracionReservas.CIERRE;

  private static final int STEP_MINUTES =
          ConfiguracionReservas.PASO_MINUTOS;

  private static final TimeZone ZONE =
          TimeZone.getTimeZone("America/Lima");

  private final MutableLiveData<UiState<Servicio>> serviceState =
          new MutableLiveData<>();

  private final MutableLiveData<List<Long>> slots =
          new MutableLiveData<>(new ArrayList<>());

  private final MutableLiveData<String> agendaMessage =
          new MutableLiveData<>("Consultando disponibilidad...");

  private ServicioRepository.Subscription serviceSubscription;
  private DisponibilidadRepository.Subscription agendaSubscription;

  private final DisponibilidadRepository agendaRepository =
          AppContainer.disponibilidad();

  private List<DisponibilidadRepository.Intervalo> ocupados =
          new ArrayList<>();

  private Servicio servicio;
  private String serviceId;

  private Calendar selectedDay;
  private Long selectedStart;

  private boolean agendaReady;
  private boolean agendaFailed;
  private String agendaError;

  private String nombre = "";
  private String telefono = "";

  private final SavedStateHandle saved;

  public ReservaViewModel(SavedStateHandle saved) {
    this.saved = saved;

    nombre = saved.get("nombre") == null
            ? ""
            : saved.get("nombre");

    telefono = saved.get("telefono") == null
            ? ""
            : saved.get("telefono");

    serviceId = saved.get("servicioId");

    Long day = saved.get("dia");

    if (day != null) {
      selectedDay = Calendar.getInstance(ZONE);
      selectedDay.setTimeInMillis(day);
    }

    retryAgenda();
  }

  public LiveData<UiState<Servicio>> getServiceState() {
    return serviceState;
  }

  public LiveData<List<Long>> getSlots() {
    return slots;
  }

  public LiveData<String> getAgendaMessage() {
    return agendaMessage;
  }

  public Servicio getServicio() {
    return servicio;
  }

  public Long getSelectedStart() {
    return selectedStart;
  }

  public String getNombre() {
    return nombre;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setNombre(String value) {
    nombre = value;
    saved.set("nombre", value);
  }

  public void setTelefono(String value) {
    telefono = value;
    saved.set("telefono", value);
  }

  public boolean isAgendaFailed() {
    return agendaFailed;
  }

  public boolean canSubmit() {
    return servicio != null
            && selectedStart != null
            && isAvailable(selectedStart);
  }

  public void retryAgenda() {
    if (agendaSubscription != null) {
      agendaSubscription.cancel();
    }

    agendaReady = false;
    agendaFailed = false;
    agendaError = null;

    refreshSlots();

    agendaSubscription = agendaRepository.observe(
            new DisponibilidadRepository.Callback() {
              @Override
              public void onSuccess(
                      List<DisponibilidadRepository.Intervalo> values
              ) {
                ocupados = new ArrayList<>(values);
                agendaReady = true;
                agendaFailed = false;
                agendaError = null;

                refreshSlots();
              }

              @Override
              public void onError(String message) {
                agendaReady = false;
                agendaFailed = true;
                agendaError = message;

                refreshSlots();
              }
            }
    );
  }

  public void load(String id) {
    if (id != null
            && id.equals(serviceId)
            && serviceSubscription != null) {
      return;
    }

    if (serviceId != null && !serviceId.equals(id)) {
      selectedDay = null;
      selectedStart = null;

      saved.set("dia", null);
      saved.set("inicio", null);
    }

    serviceId = id;
    saved.set("servicioId", id);

    reload();
  }

  public void reload() {
    if (serviceSubscription != null) {
      serviceSubscription.cancel();
      serviceSubscription = null;
    }

    servicio = null;
    selectedStart = null;

    refreshSlots();

    if (serviceId == null || serviceId.trim().isEmpty()) {
      serviceState.setValue(
              UiState.error("No se ha seleccionado un servicio.")
      );
      return;
    }

    serviceState.setValue(UiState.loading());

    serviceSubscription = AppContainer.servicios().observe(
            new ServicioRepository.Callback() {
              @Override
              public void onSuccess(List<Servicio> servicios) {
                servicio = null;

                for (Servicio item : servicios) {
                  if (serviceId.equals(item.id)) {
                    servicio = item;
                    break;
                  }
                }

                if (servicio == null
                        || !servicio.sePuedeReservar()) {
                  selectedStart = null;

                  serviceState.setValue(
                          UiState.error(
                                  "Este servicio ya no está disponible."
                          )
                  );
                } else {
                  selectedStart = saved.get("inicio");
                  serviceState.setValue(
                          UiState.success(servicio)
                  );
                }

                refreshSlots();
              }

              @Override
              public void onError(String message) {
                servicio = null;
                selectedStart = null;

                serviceState.setValue(UiState.error(message));
                refreshSlots();
              }
            }
    );
  }

  public void selectDay(int year, int month, int day) {
    selectedDay = Calendar.getInstance(ZONE);
    selectedDay.clear();
    selectedDay.set(year, month, day, 12, 0, 0);

    selectedStart = null;

    saved.set("dia", selectedDay.getTimeInMillis());
    saved.set("inicio", null);

    refreshSlots();
  }

  public Calendar getPickerDay() {
    Calendar value = selectedDay == null
            ? Calendar.getInstance(ZONE)
            : selectedDay;

    return (Calendar) value.clone();
  }

  public String getDateLabel() {

    if (selectedDay == null) {
      return "Elegir día";
    }

    String fecha = format(
            selectedDay.getTimeInMillis(),
            "EEEE, dd 'de' MMMM"
    );

    return Character.toUpperCase(fecha.charAt(0))
            + fecha.substring(1);
  }

  public void selectStart(long start) {
    List<Long> current = slots.getValue();

    if (current != null
            && current.contains(start)
            && isAvailable(start)) {
      selectedStart = start;
      saved.set("inicio", start);
    }
  }

  public boolean isAvailable(long start) {
    if (!agendaReady
            || servicio == null
            || !ConfiguracionReservas.jornadaValida(
            start,
            start + servicio.duracionMinutos * 60L * 1000L,
            System.currentTimeMillis()
    )) {
      return false;
    }

    long end =
            start + servicio.duracionMinutos * 60L * 1000L;

    for (DisponibilidadRepository.Intervalo ocupado : ocupados) {
      if (ConfiguracionReservas.seCruzan(
              start,
              end,
              ocupado.inicio,
              ocupado.fin
      )) {
        return false;
      }
    }

    return true;
  }

  public void refreshSlots() {
    List<Long> candidates = new ArrayList<>();

    if (servicio != null
            && selectedDay != null
            && selectedDay.get(Calendar.DAY_OF_WEEK)
            != Calendar.SUNDAY) {

      Calendar start = (Calendar) selectedDay.clone();

      start.set(Calendar.HOUR_OF_DAY, OPEN_HOUR);
      start.set(Calendar.MINUTE, 0);
      start.set(Calendar.SECOND, 0);
      start.set(Calendar.MILLISECOND, 0);

      Calendar closing = (Calendar) start.clone();
      closing.set(Calendar.HOUR_OF_DAY, CLOSE_HOUR);

      long duration =
              servicio.duracionMinutos * 60L * 1000L;

      while (start.getTimeInMillis() + duration
              <= closing.getTimeInMillis()) {
        candidates.add(start.getTimeInMillis());
        start.add(Calendar.MINUTE, STEP_MINUTES);
      }
    }

    boolean lostSelection =
            agendaReady
                    && servicio != null
                    && selectedStart != null
                    && (
                    !candidates.contains(selectedStart)
                            || !isAvailable(selectedStart)
            );

    if (lostSelection) {
      selectedStart = null;
      saved.set("inicio", null);
    }

    String message;

    if (!agendaReady) {
      message = agendaFailed
              ? agendaError
              : "Consultando disponibilidad...";

    } else if (selectedDay == null) {
      message = "Selecciona primero un día.";

    } else if (candidates.isEmpty()) {
      message =
              "No hay horarios de atención para este día "
                      + "y la duración del servicio.";

    } else {
      int available = 0;

      for (Long candidate : candidates) {
        if (isAvailable(candidate)) {
          available++;
        }
      }

      message = lostSelection
              ? "La hora seleccionada ya no está disponible. "
              + "Elige otra."
              : available == 0
              ? "No hay horas disponibles. Prueba otro día."
              : available
              + " horarios disponibles. "
              + "Las horas ocupadas aparecen deshabilitadas.";
    }

    agendaMessage.setValue(message);
    slots.setValue(candidates);
  }

  public String format(long timestamp, String pattern) {
    SimpleDateFormat formatter = new SimpleDateFormat(
            pattern,
            Locale.forLanguageTag("es-PE")
    );

    formatter.setTimeZone(ZONE);
    return formatter.format(timestamp);
  }

  @Override
  protected void onCleared() {
    if (serviceSubscription != null) {
      serviceSubscription.cancel();
    }

    if (agendaSubscription != null) {
      agendaSubscription.cancel();
    }

    super.onCleared();
  }
}
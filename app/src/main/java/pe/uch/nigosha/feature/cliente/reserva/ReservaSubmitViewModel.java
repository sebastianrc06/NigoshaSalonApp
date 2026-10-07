package pe.uch.nigosha.feature.cliente.reserva;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import java.util.UUID;
import pe.uch.nigosha.app.AppContainer;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.domain.models.Servicio;
import pe.uch.nigosha.domain.repository.CitaRepository;

public class ReservaSubmitViewModel extends ViewModel {

  private final CitaRepository repository = AppContainer.reservas();

  private final MutableLiveData<UiState<String>> state = new MutableLiveData<>();

  private final SavedStateHandle saved;

  public ReservaSubmitViewModel(SavedStateHandle saved) {
    this.saved = saved;
    fingerprint = saved.get("fingerprint");
    requestId = saved.get("requestId");
    String result = saved.get("citaGuardada");
    if (result != null) state.setValue(UiState.success(result));
  }

  public void nuevaSolicitud() {
    if (isBusy()) return;
    fingerprint = null;
    requestId = null;
    saved.set("fingerprint", null);
    saved.set("requestId", null);
    saved.set("citaGuardada", null);
    state.setValue(null);
  }

  private String fingerprint;
  private String requestId;

  public LiveData<UiState<String>> getState() {
    return state;
  }

  public boolean isBusy() {
    UiState<String> current = state.getValue();

    return current != null && current.status == UiState.Status.LOADING;
  }

  public boolean isSaved() {
    UiState<String> current = state.getValue();

    return current != null && current.status == UiState.Status.SUCCESS;
  }

  public boolean puedeRecuperarEnvio() {
    UiState<String> current = state.getValue();
    return requestId != null && (current == null || current.status == UiState.Status.ERROR);
  }

  public void submit(Servicio servicio, String nombre, String telefono, long inicio) {
    if (isBusy() || isSaved()) {
      return;
    }

    String newFingerprint =
        servicio.id
            + "|"
            + inicio
            + "|"
            + nombre
            + "|"
            + telefono
            + "|"
            + servicio.precio
            + "|"
            + servicio.duracionMinutos;

    if (!newFingerprint.equals(fingerprint)) {
      fingerprint = newFingerprint;
      requestId = UUID.randomUUID().toString();
      saved.set("fingerprint", fingerprint);
      saved.set("requestId", requestId);
    }

    state.setValue(UiState.loading());

    repository.reservar(
        requestId,
        servicio,
        nombre,
        telefono,
        inicio,
        new CitaRepository.Callback() {
          @Override
          public void onSuccess(String citaId) {
            saved.set("citaGuardada", citaId);
            state.setValue(UiState.success(citaId));
          }

          @Override
          public void onError(String message) {
            state.setValue(UiState.error(message));
          }
        });
  }
}

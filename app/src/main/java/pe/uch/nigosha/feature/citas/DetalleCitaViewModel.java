package pe.uch.nigosha.feature.citas;

import androidx.lifecycle.*;
import pe.uch.nigosha.app.AppContainer;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.domain.models.Cita;
import pe.uch.nigosha.domain.repository.MisCitasRepository;

public final class DetalleCitaViewModel extends ViewModel {
  private final MisCitasRepository repository = AppContainer.citas();
  private final MutableLiveData<UiState<Cita>> state = new MutableLiveData<>();
  private final MutableLiveData<UiState<String>> operation = new MutableLiveData<>();
  private MisCitasRepository.Subscription subscription;
  private String id;
  private boolean busy;

  public LiveData<UiState<Cita>> getState() {
    return state;
  }

  public LiveData<UiState<String>> getOperation() {
    return operation;
  }

  public boolean isBusy() {
    return busy;
  }

  public void load(String value) {
    if (value != null && value.equals(id) && subscription != null) return;
    id = value;
    reload();
  }

  public void reload() {
    if (subscription != null) subscription.cancel();
    state.setValue(UiState.loading());
    subscription =
        repository.observeCita(
            id,
            new MisCitasRepository.CitaCallback() {
              @Override
              public void onSuccess(Cita cita) {
                state.setValue(UiState.success(cita));
              }

              @Override
              public void onError(String message) {
                state.setValue(UiState.error(message));
              }
            });
  }

  public void cancelar() {
    UiState<Cita> current = state.getValue();
    if (busy || current == null || current.status != UiState.Status.SUCCESS) return;
    if (!current.data.cancelable(System.currentTimeMillis())) {
      operation.setValue(UiState.error("Esta cita ya no permite cancelación desde la aplicación."));
      return;
    }
    busy = true;
    operation.setValue(UiState.loading());
    repository.cancelar(
        id,
        new MisCitasRepository.CancelCallback() {
          @Override
          public void onSuccess() {
            busy = false;
            operation.setValue(UiState.success("Solicitud cancelada. El horario quedó liberado."));
          }

          @Override
          public void onError(String message) {
            busy = false;
            operation.setValue(UiState.error(message));
          }
        });
  }

  @Override
  protected void onCleared() {
    if (subscription != null) subscription.cancel();
  }
}

package pe.uch.nigosha.feature.cliente.servicio;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import pe.uch.nigosha.app.AppContainer;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.domain.models.Servicio;
import pe.uch.nigosha.domain.repository.ServicioRepository;

public class DetalleServicioViewModel extends ViewModel {

  private final MutableLiveData<UiState<Servicio>> state = new MutableLiveData<>();

  private ServicioRepository.Subscription subscription;
  private String currentId;

  public LiveData<UiState<Servicio>> getState() {
    return state;
  }

  public void load(String id) {
    if (id != null && id.equals(currentId) && subscription != null) {
      return;
    }

    reload(id);
  }

  public void retry() {
    reload(currentId);
  }

  private void reload(String id) {
    if (subscription != null) {
      subscription.cancel();
      subscription = null;
    }

    currentId = id;

    if (id == null || id.trim().isEmpty()) {
      state.setValue(UiState.error("No se ha seleccionado un servicio."));
      return;
    }

    state.setValue(UiState.loading());

    subscription =
        AppContainer.servicios()
            .observe(
                new ServicioRepository.Callback() {
                  @Override
                  public void onSuccess(List<Servicio> servicios) {
                    for (Servicio servicio : servicios) {
                      if (id.equals(servicio.id)) {
                        state.setValue(UiState.success(servicio));
                        return;
                      }
                    }

                    state.setValue(UiState.error("Este servicio ya no está disponible."));
                  }

                  @Override
                  public void onError(String message) {
                    state.setValue(UiState.error(message));
                  }
                });
  }

  @Override
  protected void onCleared() {
    if (subscription != null) {
      subscription.cancel();
    }

    super.onCleared();
  }
}

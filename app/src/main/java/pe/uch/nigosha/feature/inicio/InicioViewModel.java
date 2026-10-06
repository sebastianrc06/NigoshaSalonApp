package pe.uch.nigosha.feature.inicio;

import androidx.lifecycle.*;
import java.util.ArrayList;
import java.util.List;
import pe.uch.nigosha.app.AppContainer;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.domain.models.Cita;
import pe.uch.nigosha.domain.repository.MisCitasRepository;

public final class InicioViewModel extends ViewModel {
  private final MisCitasRepository repository = AppContainer.citas();
  private final MutableLiveData<UiState<Cita>> state = new MutableLiveData<>();
  private MisCitasRepository.Subscription subscription;
  private final List<Cita> citas = new ArrayList<>();
  private boolean loaded;

  public InicioViewModel() {
    reload();
  }

  public LiveData<UiState<Cita>> getState() {
    return state;
  }

  public void reload() {
    if (subscription != null) subscription.cancel();
    loaded = false;
    state.setValue(UiState.loading());
    subscription =
        repository.observe(
            new MisCitasRepository.Callback() {
              @Override
              public void onSuccess(List<Cita> values) {
                citas.clear();
                citas.addAll(values);
                loaded = true;
                refresh();
              }

              @Override
              public void onError(String message) {
                loaded = false;
                state.setValue(UiState.error(message));
              }
            });
  }

  public void refresh() {
    if (!loaded) return;
    Cita proxima = null;
    long now = System.currentTimeMillis();
    for (Cita cita : citas)
      if (cita.proxima(now) && (proxima == null || cita.inicioOrden() < proxima.inicioOrden()))
        proxima = cita;
    state.setValue(UiState.success(proxima));
  }

  @Override
  protected void onCleared() {
    if (subscription != null) subscription.cancel();
  }
}

package pe.uch.nigosha.feature.pago;

import androidx.lifecycle.*;
import pe.uch.nigosha.app.AppContainer;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.domain.models.ResumenPago;
import pe.uch.nigosha.domain.repository.*;

public final class PagoViewModel extends ViewModel {
  private final PagoRepository repository = AppContainer.pagos();
  private final MutableLiveData<UiState<ResumenPago>> state = new MutableLiveData<>();
  private MisCitasRepository.Subscription subscription;
  private String id;

  public LiveData<UiState<ResumenPago>> getState() {
    return state;
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
        repository.observe(
            id,
            new PagoRepository.Callback() {
              @Override
              public void onSuccess(ResumenPago pago) {
                state.setValue(UiState.success(pago));
              }

              @Override
              public void onError(String message) {
                state.setValue(UiState.error(message));
              }
            });
  }

  @Override
  protected void onCleared() {
    if (subscription != null) subscription.cancel();
  }
}

package pe.uch.nigosha.feature.salon;

import androidx.lifecycle.*;
import pe.uch.nigosha.app.AppContainer;
import pe.uch.nigosha.domain.models.Salon;
import pe.uch.nigosha.domain.repository.SalonRepository;

public final class SalonViewModel extends ViewModel {
  private final MutableLiveData<Salon> datos = new MutableLiveData<>(new Salon());
  private final MutableLiveData<String> aviso = new MutableLiveData<>("");
  private SalonRepository.Subscription subscription;

  public SalonViewModel() {
    reload();
  }

  public LiveData<Salon> getDatos() {
    return datos;
  }

  public LiveData<String> getAviso() {
    return aviso;
  }

  public Salon getSalon() {
    return datos.getValue();
  }

  public void reload() {
    if (subscription != null) subscription.cancel();
    aviso.setValue("");
    subscription =
        AppContainer.salon()
            .observe(
                new SalonRepository.Callback() {
                  @Override
                  public void onSuccess(Salon salon) {
                    datos.setValue(salon);
                    aviso.setValue("");
                  }

                  @Override
                  public void onError(String message) {
                    aviso.setValue(message);
                  }
                });
  }

  @Override
  protected void onCleared() {
    if (subscription != null) subscription.cancel();
  }
}

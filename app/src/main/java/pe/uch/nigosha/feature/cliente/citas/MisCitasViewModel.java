package pe.uch.nigosha.feature.cliente.citas;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pe.uch.nigosha.app.AppContainer;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.domain.models.Cita;
import pe.uch.nigosha.domain.repository.MisCitasRepository;

public class MisCitasViewModel extends ViewModel {

  private final MisCitasRepository repository = AppContainer.citas();

  private final MutableLiveData<UiState<List<Cita>>> state = new MutableLiveData<>();

  private final MutableLiveData<UiState<String>> operation = new MutableLiveData<>();

  private final List<Cita> todasLasCitas = new ArrayList<>();

  private MisCitasRepository.Subscription subscription;

  private boolean historial = false;
  private boolean loaded = false;
  private boolean cancelando = false;

  public MisCitasViewModel() {
    reload();
  }

  public LiveData<UiState<List<Cita>>> getState() {
    return state;
  }

  public LiveData<UiState<String>> getOperation() {
    return operation;
  }

  public boolean isHistorial() {
    return historial;
  }

  public boolean isBusy() {
    return cancelando;
  }

  public void selectHistorial(boolean mostrarHistorial) {
    historial = mostrarHistorial;
    refresh();
  }

  public void reload() {
    if (subscription != null) {
      subscription.cancel();
      subscription = null;
    }

    loaded = false;
    state.setValue(UiState.loading());

    subscription =
        repository.observe(
            new MisCitasRepository.Callback() {
              @Override
              public void onSuccess(List<Cita> citas) {
                todasLasCitas.clear();

                if (citas != null) {
                  todasLasCitas.addAll(citas);
                }

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
    if (!loaded) {
      return;
    }

    long ahora = System.currentTimeMillis();
    List<Cita> resultado = new ArrayList<>();

    for (Cita cita : todasLasCitas) {
      if (cita == null) {
        continue;
      }

      boolean esProxima = cita.proxima(ahora);

      if ((!historial && esProxima) || (historial && !esProxima)) {
        resultado.add(cita);
      }
    }

    Collections.sort(
        resultado,
        (primera, segunda) -> {
          if (historial) {
            return Long.compare(segunda.inicioOrden(), primera.inicioOrden());
          }

          return Long.compare(primera.inicioOrden(), segunda.inicioOrden());
        });

    state.setValue(UiState.success(resultado));
  }

  public void cancelar(String citaId) {
    if (cancelando) {
      return;
    }

    cancelando = true;
    operation.setValue(UiState.loading());

    repository.cancelar(
        citaId,
        new MisCitasRepository.CancelCallback() {
          @Override
          public void onSuccess() {
            cancelando = false;

            operation.setValue(UiState.success("Cita cancelada. Su horario quedó liberado."));
          }

          @Override
          public void onError(String message) {
            cancelando = false;
            operation.setValue(UiState.error(message));
          }
        });
  }

  @Override
  protected void onCleared() {
    if (subscription != null) {
      subscription.cancel();
      subscription = null;
    }

    super.onCleared();
  }
}

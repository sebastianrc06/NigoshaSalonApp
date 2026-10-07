package pe.uch.nigosha.feature.cliente.catalogo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pe.uch.nigosha.app.AppContainer;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.domain.models.Servicio;
import pe.uch.nigosha.domain.repository.ServicioRepository;

public class CatalogoViewModel extends ViewModel {

  private final ServicioRepository repository = AppContainer.servicios();

  private final MutableLiveData<UiState<List<Servicio>>> state =
          new MutableLiveData<>();

  private List<Servicio> all = new ArrayList<>();
  private ServicioRepository.Subscription subscription;

  private String search = "";
  private String category = "";

  private boolean paquetes;
  private boolean loaded;

  public CatalogoViewModel() {
    reload();
  }

  public LiveData<UiState<List<Servicio>>> getState() {
    return state;
  }

  public String getSearch() {
    return search;
  }

  public String getCategory() {
    return category;
  }

  public boolean getPaquetes() {
    return paquetes;
  }

  public void setPaquetes(boolean value) {
    paquetes = value;
    category = "";

    if (loaded) {
      publish();
    }
  }

  public List<String> getCategories() {
    List<String> categories = new ArrayList<>();

    for (Servicio servicio : all) {
      if (servicio.esPaquete() == paquetes
              && !categories.contains(servicio.categoria)) {
        categories.add(servicio.categoria);
      }
    }

    categories.sort(String::compareToIgnoreCase);
    return categories;
  }

  public void setSearch(String value) {
    search = value == null ? "" : value;

    if (loaded) {
      publish();
    }
  }

  public void setCategory(String value) {
    category = value == null ? "" : value;

    if (loaded) {
      publish();
    }
  }

  public void reload() {
    if (subscription != null) {
      subscription.cancel();
    }

    loaded = false;
    state.setValue(UiState.loading());

    subscription = repository.observe(
            new ServicioRepository.Callback() {
              @Override
              public void onSuccess(List<Servicio> servicios) {
                all = new ArrayList<>(servicios);
                loaded = true;

                if (!category.isEmpty()
                        && !getCategories().contains(category)) {
                  category = "";
                }

                publish();
              }

              @Override
              public void onError(String message) {
                loaded = false;
                state.setValue(UiState.error(message));
              }
            }
    );
  }

  private void publish() {
    String query = normalize(search.trim());
    List<Servicio> filtered = new ArrayList<>();

    for (Servicio servicio : all) {
      boolean categoryMatches =
              servicio.esPaquete() == paquetes
                      && (
                      category.isEmpty()
                              || category.equals(servicio.categoria)
              );

      String searchable = normalize(
              servicio.nombre + " "
                      + servicio.categoria + " "
                      + (
                      servicio.descripcion == null
                              ? ""
                              : servicio.descripcion
              )
      );

      if (categoryMatches && searchable.contains(query)) {
        filtered.add(servicio);
      }
    }

    state.setValue(UiState.success(filtered));
  }

  private String normalize(String value) {
    return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT);
  }

  @Override
  protected void onCleared() {
    if (subscription != null) {
      subscription.cancel();
    }

    super.onCleared();
  }
}
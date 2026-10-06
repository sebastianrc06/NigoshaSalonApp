package pe.uch.nigosha.domain.repository;

import pe.uch.nigosha.domain.models.Salon;

public interface SalonRepository {
  interface Callback {
    void onSuccess(Salon salon);

    void onError(String message);
  }

  interface Subscription {
    void cancel();
  }

  Subscription observe(Callback callback);
}

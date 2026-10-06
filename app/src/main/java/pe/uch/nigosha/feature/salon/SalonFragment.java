package pe.uch.nigosha.feature.salon;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import pe.uch.nigosha.R;
import pe.uch.nigosha.domain.models.Salon;

public final class SalonFragment extends Fragment {
  private static final String MAPA_TAG = "mapa_nigosha";
  private SalonViewModel model;
  private GoogleMap mapa;
  private View pantalla;

  public SalonFragment() {
    super(R.layout.fragment_salon);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
    super.onViewCreated(view, saved);
    pantalla = view;
    model = new ViewModelProvider(this).get(SalonViewModel.class);
    view.findViewById(R.id.btnVerMapaSalon).setOnClickListener(v -> ubicacion());
    view.findViewById(R.id.btnComoLlegarSalon)
        .setOnClickListener(
            v -> {
              Salon s = model.getSalon();
              Uri link =
                  Uri.parse("https://www.google.com/maps/dir/")
                      .buildUpon()
                      .appendQueryParameter("api", "1")
                      .appendQueryParameter("destination", s.latitud + "," + s.longitud)
                      .build();
              abrir(new Intent(Intent.ACTION_VIEW, link));
            });
    view.findViewById(R.id.btnLlamarSalon)
        .setOnClickListener(
            v ->
                abrir(
                    new Intent(
                        Intent.ACTION_DIAL, Uri.parse("tel:+" + model.getSalon().telefono))));
    view.findViewById(R.id.btnWhatsAppSalon).setOnClickListener(v -> whatsapp());
    view.findViewById(R.id.btnReintentarSalon).setOnClickListener(v -> model.reload());
    model
        .getDatos()
        .observe(
            getViewLifecycleOwner(),
            s -> {
              ((TextView) view.findViewById(R.id.tvSalonDireccion)).setText(s.direccion);
              ((TextView) view.findViewById(R.id.tvSalonDistrito)).setText(s.distrito);
              ((TextView) view.findViewById(R.id.tvSalonTelefono)).setText("+" + s.telefono);
              ((TextView) view.findViewById(R.id.tvSalonHorario))
                  .setText(
                      s.horario == null || s.horario.trim().isEmpty()
                          ? "Por confirmar"
                          : s.horario);
              actualizarMapa();
            });
    model
        .getAviso()
        .observe(
            getViewLifecycleOwner(),
            message -> {
              TextView aviso = view.findViewById(R.id.tvAvisoSalon);
              boolean visible = message != null && !message.isEmpty();
              aviso.setText(message);
              aviso.setVisibility(visible ? View.VISIBLE : View.GONE);
              view.findViewById(R.id.btnReintentarSalon)
                  .setVisibility(visible ? View.VISIBLE : View.GONE);
            });
    SupportMapFragment fragment =
        (SupportMapFragment) getChildFragmentManager().findFragmentByTag(MAPA_TAG);
    if (fragment == null) {
      fragment =
          SupportMapFragment.newInstance(
              new GoogleMapOptions().liteMode(true).mapToolbarEnabled(false));
      getChildFragmentManager()
          .beginTransaction()
          .replace(R.id.contenedorMapaSalon, fragment, MAPA_TAG)
          .commitNow();
    }
    fragment.getMapAsync(
        googleMap -> {
          if (getView() != view) return;
          mapa = googleMap;
          actualizarMapa();
          mapa.getUiSettings().setMapToolbarEnabled(false);
          mapa.setOnMapClickListener(point -> ubicacion());
          mapa.setOnInfoWindowClickListener(marker -> ubicacion());
          view.findViewById(R.id.tvEstadoMapaSalon).setVisibility(View.GONE);
        });
  }

  private void actualizarMapa() {
    if (mapa == null || pantalla == null) return;
    Salon s = model.getSalon();
    LatLng posicion = new LatLng(s.latitud, s.longitud);
    mapa.clear();
    Marker marker =
        mapa.addMarker(
            new MarkerOptions()
                .position(posicion)
                .title(s.nombre)
                .snippet(s.direccionCompleta())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 17f));
    if (marker != null) marker.showInfoWindow();
  }

  private void ubicacion() {
    abrir(new Intent(Intent.ACTION_VIEW, Uri.parse(model.getSalon().enlaceMaps)));
  }

  private void whatsapp() {
    Uri link =
        Uri.parse("https://wa.me/" + model.getSalon().whatsapp)
            .buildUpon()
            .appendQueryParameter(
                "text",
                "Hola, Nigosha. Vengo desde su aplicación y quisiera consultar sobre sus"
                    + " servicios.")
            .build();
    Intent personal = new Intent(Intent.ACTION_VIEW, link).setPackage("com.whatsapp");
    Intent business = new Intent(Intent.ACTION_VIEW, link).setPackage("com.whatsapp.w4b");
    if (!intentar(personal) && !intentar(business)) abrir(new Intent(Intent.ACTION_VIEW, link));
  }

  private boolean intentar(Intent intent) {
    if (!isAdded()) return false;
    try {
      startActivity(intent);
      return true;
    } catch (ActivityNotFoundException e) {
      return false;
    }
  }

  private void abrir(Intent intent) {
    if (isAdded() && !intentar(intent))
      Toast.makeText(
              requireContext(),
              "No se encontró una aplicación para abrir esta opción.",
              Toast.LENGTH_SHORT)
          .show();
  }

  @Override
  public void onDestroyView() {
    mapa = null;
    pantalla = null;
    super.onDestroyView();
  }
}

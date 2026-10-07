package pe.uch.nigosha.feature.cliente.inicio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pe.uch.nigosha.R;
import pe.uch.nigosha.core.ui.StateViews;
import pe.uch.nigosha.core.ui.UiState;
import pe.uch.nigosha.core.util.DateTimeFormatter;
import pe.uch.nigosha.domain.models.Cita;
import pe.uch.nigosha.domain.models.Usuario;
import pe.uch.nigosha.feature.auth.login.LoginActivity;

public final class InicioFragment extends Fragment {

    private InicioViewModel model;

    private View pantalla;

    private Cita proxima;

    private TextView tvNombreClienteInicio;

    private final Runnable refresh =
            new Runnable() {

                @Override
                public void run() {

                    if (pantalla != null) {

                        model.refresh();

                        pantalla.postDelayed(
                                this,
                                30000
                        );
                    }
                }
            };


    public InicioFragment() {

        super(R.layout.fragment_inicio);
    }


    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        pantalla = view;

        model =
                new ViewModelProvider(this)
                        .get(InicioViewModel.class);


        /*
         * ---------------------------------------------------------
         * NOMBRE DEL CLIENTE
         * ---------------------------------------------------------
         */

        tvNombreClienteInicio =
                view.findViewById(
                        R.id.tvNombreUsuarioInicio
                );


        /*
         * ---------------------------------------------------------
         * PERFIL / MENÚ DE CUENTA
         * ---------------------------------------------------------
         */

        configurarPerfil(view);

        cargarNombreCliente();


        /*
         * ---------------------------------------------------------
         * SERVICIOS
         * ---------------------------------------------------------
         */

        view.findViewById(
                        R.id.btnExplorarServicios
                )
                .setOnClickListener(
                        v -> seleccionarSeccion(
                                R.id.catalogoFragment
                        )
                );


        /*
         * ---------------------------------------------------------
         * MIS CITAS
         * ---------------------------------------------------------
         */

        view.findViewById(
                        R.id.btnConsultarCitas
                )
                .setOnClickListener(
                        v -> seleccionarSeccion(
                                R.id.misCitasFragment
                        )
                );


        /*
         * ---------------------------------------------------------
         * SALÓN
         * ---------------------------------------------------------
         */

        view.findViewById(
                        R.id.btnConocerSalon
                )
                .setOnClickListener(
                        v -> seleccionarSeccion(
                                R.id.salonFragment
                        )
                );


        /*
         * ---------------------------------------------------------
         * REINTENTAR
         * ---------------------------------------------------------
         */

        view.findViewById(
                        R.id.btnReintentarEstado
                )
                .setOnClickListener(
                        v -> model.reload()
                );


        /*
         * ---------------------------------------------------------
         * PRÓXIMA CITA
         * ---------------------------------------------------------
         */

        view.findViewById(
                        R.id.btnProximaInicio
                )
                .setOnClickListener(
                        v -> abrirProximaCita()
                );


        observarEstado();
    }


    /*
     * =========================================================
     * MENÚ DE CUENTA
     * =========================================================
     */

    private void configurarPerfil(View view) {

        View btnPerfil =
                view.findViewById(
                        R.id.btnPerfilInicio
                );

        btnPerfil.setOnClickListener(
                this::mostrarMenuCuenta
        );
    }


    private void mostrarMenuCuenta(View anchor) {

        PopupMenu popupMenu =
                new PopupMenu(
                        requireContext(),
                        anchor
                );

        popupMenu.getMenuInflater()
                .inflate(
                        R.menu.menu_cuenta_cliente,
                        popupMenu.getMenu()
                );


        popupMenu.setOnMenuItemClickListener(
                item -> {

                    int id =
                            item.getItemId();


                    /*
                     * MI PERFIL
                     */

                    if (id == R.id.actionMiPerfil) {

                        abrirPerfil();

                        return true;
                    }


                    /*
                     * CERRAR SESIÓN
                     */

                    if (id == R.id.actionCerrarSesion) {

                        cerrarSesion();

                        return true;
                    }


                    return false;
                }
        );


        popupMenu.show();
    }


    /*
     * =========================================================
     * ABRIR PERFIL
     * =========================================================
     */

    private void abrirPerfil() {

        if (!isAdded()) {
            return;
        }

        NavHostFragment
                .findNavController(this)
                .navigate(
                        R.id.perfilFragment
                );
    }


    /*
     * =========================================================
     * CERRAR SESIÓN
     * =========================================================
     */

    private void cerrarSesion() {

        if (!isAdded()) {
            return;
        }


        FirebaseAuth
                .getInstance()
                .signOut();


        Intent intent =
                new Intent(
                        requireContext(),
                        LoginActivity.class
                );


        /*
         * Eliminamos ClienteActivity del historial.
         *
         * De esta manera, después de cerrar sesión,
         * el usuario no puede pulsar Atrás y regresar
         * al panel del cliente.
         */

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        requireActivity().finish();
    }


    /*
     * =========================================================
     * CARGAR NOMBRE REAL DESDE FIREBASE
     * =========================================================
     */

    private void cargarNombreCliente() {

        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();


        if (firebaseUser == null) {

            tvNombreClienteInicio.setText(
                    "Mi perfil"
            );

            return;
        }


        DatabaseReference referencia =
                FirebaseDatabase
                        .getInstance()
                        .getReference("usuarios")
                        .child(
                                firebaseUser.getUid()
                        );


        referencia
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            if (!isAdded()
                                    || pantalla == null
                                    || tvNombreClienteInicio == null) {

                                return;
                            }


                            Usuario usuario =
                                    snapshot.getValue(
                                            Usuario.class
                                    );


                            if (usuario == null) {

                                tvNombreClienteInicio.setText(
                                        "Mi perfil"
                                );

                                return;
                            }


                            String nombre =
                                    obtenerPrimerNombre(
                                            usuario.nombres
                                    );


                            tvNombreClienteInicio.setText(
                                    nombre
                            );
                        }
                )
                .addOnFailureListener(
                        error -> {

                            if (pantalla != null
                                    && tvNombreClienteInicio != null) {

                                tvNombreClienteInicio.setText(
                                        "Mi perfil"
                                );
                            }
                        }
                );
    }


    /*
     * =========================================================
     * OBTENER PRIMER NOMBRE
     * =========================================================
     */

    private String obtenerPrimerNombre(
            String nombres
    ) {

        if (nombres == null
                || nombres.trim().isEmpty()) {

            return "Mi perfil";
        }


        String limpio =
                nombres
                        .trim()
                        .toLowerCase();


        String[] partes =
                limpio.split("\\s+");


        if (partes.length == 0) {

            return "Mi perfil";
        }


        String primero =
                partes[0];


        if (primero.length() == 1) {

            return primero
                    .toUpperCase();
        }


        return primero
                .substring(0, 1)
                .toUpperCase()
                + primero.substring(1);
    }


    /*
     * =========================================================
     * ABRIR PRÓXIMA CITA
     * =========================================================
     */

    private void abrirProximaCita() {

        if (proxima == null) {

            return;
        }


        Bundle args =
                new Bundle();


        args.putString(
                "citaId",
                proxima.id
        );


        NavHostFragment
                .findNavController(this)
                .navigate(
                        R.id.detalleCitaFragment,
                        args
                );
    }


    /*
     * =========================================================
     * OBSERVAR PRÓXIMA CITA
     * =========================================================
     */

    private void observarEstado() {

        model.getState()
                .observe(
                        getViewLifecycleOwner(),
                        state -> {

                            if (pantalla == null) {

                                return;
                            }


                            StateViews.render(
                                    pantalla,
                                    state,
                                    R.id.cardProximaInicio
                            );


                            proxima = null;


                            boolean vacio =
                                    state != null
                                            && state.status
                                            == UiState.Status.SUCCESS
                                            && state.data == null;


                            pantalla.findViewById(
                                            R.id.estadoVacio
                                    )
                                    .setVisibility(
                                            vacio
                                                    ? View.VISIBLE
                                                    : View.GONE
                                    );


                            if (vacio) {

                                pantalla.findViewById(
                                                R.id.cardProximaInicio
                                        )
                                        .setVisibility(
                                                View.GONE
                                        );
                            }


                            if (state != null
                                    && state.status
                                    == UiState.Status.SUCCESS
                                    && state.data != null) {


                                proxima =
                                        state.data;


                                ((TextView)
                                        pantalla.findViewById(
                                                R.id.tvProximaServicio
                                        ))
                                        .setText(
                                                DateTimeFormatter.texto(
                                                        proxima.servicioNombre,
                                                        "Servicio"
                                                )
                                        );


                                ((TextView)
                                        pantalla.findViewById(
                                                R.id.tvProximaFecha
                                        ))
                                        .setText(
                                                DateTimeFormatter.format(
                                                        proxima.inicioOrden(),
                                                        "EEEE d 'de' MMMM · HH:mm"
                                                )
                                        );


                                ((TextView)
                                        pantalla.findViewById(
                                                R.id.tvProximaEstado
                                        ))
                                        .setText(
                                                "Reserva: "
                                                        + DateTimeFormatter.estado(
                                                        proxima.estado
                                                )
                                        );
                            }
                        }
                );
    }


    /*
     * =========================================================
     * CAMBIAR SECCIÓN DEL BOTTOM NAVIGATION
     * =========================================================
     */

    private void seleccionarSeccion(
            int destino
    ) {

        BottomNavigationView navigation =
                requireActivity()
                        .findViewById(
                                R.id.clienteBottomNavigation
                        );


        navigation.setSelectedItemId(
                destino
        );
    }


    /*
     * =========================================================
     * CICLO DE VIDA
     * =========================================================
     */

    @Override
    public void onResume() {

        super.onResume();


        if (pantalla != null) {

            pantalla.post(
                    refresh
            );
        }
    }


    @Override
    public void onPause() {

        if (pantalla != null) {

            pantalla.removeCallbacks(
                    refresh
            );
        }


        super.onPause();
    }


    @Override
    public void onDestroyView() {

        if (pantalla != null) {

            pantalla.removeCallbacks(
                    refresh
            );
        }


        pantalla = null;

        tvNombreClienteInicio = null;

        proxima = null;


        super.onDestroyView();
    }
}
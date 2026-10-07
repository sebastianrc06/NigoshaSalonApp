package pe.uch.nigosha.feature.cliente.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import pe.uch.nigosha.R;
import pe.uch.nigosha.domain.models.Usuario;
import pe.uch.nigosha.feature.auth.login.LoginActivity;

public class PerfilFragment extends Fragment {

    private PerfilViewModel model;

    private TextView tvPerfilNombre;
    private TextView tvPerfilRol;

    private TextView tvPerfilDni;
    private TextView tvPerfilNombres;
    private TextView tvPerfilApellidos;
    private TextView tvPerfilTelefono;
    private TextView tvPerfilCorreo;

    private View progressPerfil;
    private View contenidoPerfil;

    private MaterialButton btnVolverPerfil;
    private MaterialButton btnCerrarSesion;

    public PerfilFragment() {
        super(R.layout.fragment_perfil);
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

        enlazarVistas(view);

        model =
                new ViewModelProvider(this)
                        .get(PerfilViewModel.class);

        configurarEventos();

        observarDatos();

        model.cargarUsuario();
    }

    private void enlazarVistas(View view) {

        btnVolverPerfil =
                view.findViewById(
                        R.id.btnVolverPerfil
                );

        tvPerfilNombre =
                view.findViewById(
                        R.id.tvPerfilNombre
                );

        tvPerfilRol =
                view.findViewById(
                        R.id.tvPerfilRol
                );

        tvPerfilDni =
                view.findViewById(
                        R.id.tvPerfilDni
                );

        tvPerfilNombres =
                view.findViewById(
                        R.id.tvPerfilNombres
                );

        tvPerfilApellidos =
                view.findViewById(
                        R.id.tvPerfilApellidos
                );

        tvPerfilTelefono =
                view.findViewById(
                        R.id.tvPerfilTelefono
                );

        tvPerfilCorreo =
                view.findViewById(
                        R.id.tvPerfilCorreo
                );

        progressPerfil =
                view.findViewById(
                        R.id.progressPerfil
                );

        contenidoPerfil =
                view.findViewById(
                        R.id.contenidoPerfil
                );

        btnCerrarSesion =
                view.findViewById(
                        R.id.btnCerrarSesion
                );
    }

    private void configurarEventos() {

        /*
         * Regresa a la pantalla anterior.
         *
         * Si entramos:
         *
         * Inicio -> Mi perfil
         *
         * entonces:
         *
         * Mi perfil -> Volver -> Inicio
         *
         * No creamos otro InicioFragment.
         * Simplemente retiramos PerfilFragment
         * del back stack.
         */
        btnVolverPerfil.setOnClickListener(
                v -> volver()
        );

        btnCerrarSesion.setOnClickListener(
                v -> confirmarCerrarSesion()
        );
    }

    private void volver() {

        boolean regreso =
                NavHostFragment
                        .findNavController(this)
                        .popBackStack();

        /*
         * Protección adicional.
         *
         * Normalmente popBackStack() regresará a Inicio.
         * Si por alguna razón no hubiera una pantalla
         * anterior, navegamos directamente al Inicio.
         */
        if (!regreso) {

            NavHostFragment
                    .findNavController(this)
                    .navigate(
                            R.id.inicioFragment
                    );
        }
    }

    private void observarDatos() {

        model.getCargando()
                .observe(
                        getViewLifecycleOwner(),
                        cargando -> {

                            boolean mostrando =
                                    Boolean.TRUE.equals(
                                            cargando
                                    );

                            progressPerfil.setVisibility(
                                    mostrando
                                            ? View.VISIBLE
                                            : View.GONE
                            );
                        }
                );

        model.getUsuario()
                .observe(
                        getViewLifecycleOwner(),
                        this::mostrarUsuario
                );

        model.getError()
                .observe(
                        getViewLifecycleOwner(),
                        mensaje -> {

                            if (mensaje == null
                                    || mensaje.trim().isEmpty()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    mensaje,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private void mostrarUsuario(
            Usuario usuario
    ) {

        if (usuario == null) {
            return;
        }

        contenidoPerfil.setVisibility(
                View.VISIBLE
        );

        tvPerfilNombre.setText(
                textoSeguro(
                        usuario.getNombreCompleto()
                )
        );

        tvPerfilRol.setText(
                textoSeguro(
                        usuario.rol
                )
        );

        tvPerfilDni.setText(
                textoSeguro(
                        usuario.dni
                )
        );

        tvPerfilNombres.setText(
                textoSeguro(
                        usuario.nombres
                )
        );

        String apellidos =
                construirApellidos(usuario);

        tvPerfilApellidos.setText(
                apellidos
        );

        tvPerfilTelefono.setText(
                textoSeguro(
                        usuario.telefono
                )
        );

        tvPerfilCorreo.setText(
                textoSeguro(
                        usuario.correo
                )
        );
    }

    private String construirApellidos(
            Usuario usuario
    ) {

        String paterno =
                textoSeguro(
                        usuario.apellidoPaterno
                );

        String materno =
                textoSeguro(
                        usuario.apellidoMaterno
                );

        if ("—".equals(paterno)) {
            paterno = "";
        }

        if ("—".equals(materno)) {
            materno = "";
        }

        String resultado =
                (paterno + " " + materno)
                        .trim();

        return resultado.isEmpty()
                ? "—"
                : resultado;
    }

    private String textoSeguro(
            String valor
    ) {

        if (valor == null
                || valor.trim().isEmpty()) {

            return "—";
        }

        return valor.trim();
    }

    private void confirmarCerrarSesion() {

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Cerrar sesión"
                )
                .setMessage(
                        "¿Deseas cerrar tu sesión en Nigosha?"
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Cerrar sesión",
                        (dialog, which) ->
                                cerrarSesion()
                )
                .show();
    }

    private void cerrarSesion() {

        FirebaseAuth
                .getInstance()
                .signOut();

        Intent intent =
                new Intent(
                        requireContext(),
                        LoginActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        requireActivity().finish();
    }
}
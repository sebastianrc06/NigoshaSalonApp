package pe.uch.nigosha.feature.auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import pe.uch.nigosha.R;
import pe.uch.nigosha.feature.auth.registro.RegistroActivity;
import pe.uch.nigosha.feature.cliente.ClienteActivity;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout inputDni;
    private TextInputLayout inputPassword;

    private TextInputEditText etDni;
    private TextInputEditText etPassword;

    private MaterialButton btnLogin;
    private MaterialButton btnRegistrarme;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usuariosRef;

    private boolean procesandoLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        inicializarFirebase();
        enlazarVistas();
        configurarEventos();
    }

    private void inicializarFirebase() {

        firebaseAuth = FirebaseAuth.getInstance();

        usuariosRef = FirebaseDatabase
                .getInstance()
                .getReference("usuarios");
    }

    private void enlazarVistas() {

        inputDni = findViewById(R.id.inputDniLogin);
        inputPassword = findViewById(R.id.inputPasswordLogin);

        etDni = findViewById(R.id.etDniLogin);
        etPassword = findViewById(R.id.etPasswordLogin);

        btnLogin = findViewById(R.id.btnIniciarSesion);
        btnRegistrarme = findViewById(R.id.btnRegistrarme);
    }

    private void configurarEventos() {

        btnLogin.setOnClickListener(
                v -> validarFormulario()
        );

        btnRegistrarme.setOnClickListener(v -> {

            if (procesandoLogin) {
                return;
            }

            Intent intent = new Intent(
                    LoginActivity.this,
                    RegistroActivity.class
            );

            startActivity(intent);
        });

        etPassword.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_DONE) {

                        validarFormulario();

                        return true;
                    }

                    return false;
                }
        );
    }

    private void validarFormulario() {

        if (procesandoLogin) {
            return;
        }

        limpiarErrores();

        String dni = obtenerTexto(etDni);
        String password = obtenerTexto(etPassword);

        boolean valido = true;

        if (TextUtils.isEmpty(dni)) {

            inputDni.setError("Ingresa tu DNI");

            valido = false;

        } else if (!dni.matches("\\d{8}")) {

            inputDni.setError(
                    "El DNI debe tener 8 dígitos"
            );

            valido = false;
        }

        if (TextUtils.isEmpty(password)) {

            inputPassword.setError(
                    "Ingresa tu contraseña"
            );

            valido = false;

        } else if (password.length() < 6) {

            inputPassword.setError(
                    "La contraseña debe tener al menos 6 caracteres"
            );

            valido = false;
        }

        if (!valido) {
            return;
        }

        iniciarSesion(dni, password);
    }

    private void iniciarSesion(
            String dni,
            String password
    ) {

        mostrarCargando(true);

        /*
         * Firebase Authentication trabaja con correo + contraseña.
         *
         * El usuario solamente conoce y escribe su DNI.
         * Internamente convertimos:
         *
         * 70678522
         *
         * en:
         *
         * 70678522@nigosha.local
         */
        String correoInterno = dni + "@nigosha.local";

        firebaseAuth
                .signInWithEmailAndPassword(
                        correoInterno,
                        password
                )
                .addOnCompleteListener(this, task -> {

                    if (!task.isSuccessful()) {

                        mostrarCargando(false);

                        inputPassword.setError(
                                "DNI o contraseña incorrectos"
                        );

                        return;
                    }

                    FirebaseUser usuarioFirebase =
                            firebaseAuth.getCurrentUser();

                    if (usuarioFirebase == null) {

                        mostrarCargando(false);

                        Toast.makeText(
                                this,
                                "No se pudo obtener la sesión del usuario.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    consultarPerfilUsuario(
                            usuarioFirebase.getUid(),
                            dni
                    );
                });
    }

    private void consultarPerfilUsuario(
            String uid,
            String dniIngresado
    ) {

        usuariosRef
                .child(uid)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot snapshot
                            ) {

                                if (!snapshot.exists()) {

                                    cerrarSesionPorError(
                                            "La cuenta no tiene un perfil registrado."
                                    );

                                    return;
                                }

                                String dniGuardado =
                                        snapshot
                                                .child("dni")
                                                .getValue(String.class);

                                String rol =
                                        snapshot
                                                .child("rol")
                                                .getValue(String.class);

                                Boolean activo =
                                        snapshot
                                                .child("activo")
                                                .getValue(Boolean.class);

                                /*
                                 * Seguridad adicional:
                                 * comprobamos que el DNI almacenado
                                 * corresponda con el DNI utilizado
                                 * para iniciar sesión.
                                 */
                                if (dniGuardado == null
                                        || !dniIngresado.equals(dniGuardado)) {

                                    cerrarSesionPorError(
                                            "Los datos de la cuenta no son válidos."
                                    );

                                    return;
                                }

                                /*
                                 * La cuenta debe tener explícitamente
                                 * activo = true.
                                 */
                                if (activo == null || !activo) {

                                    cerrarSesionPorError(
                                            "Tu cuenta se encuentra desactivada. Comunícate con Nigosha."
                                    );

                                    return;
                                }

                                if (rol == null
                                        || rol.trim().isEmpty()) {

                                    cerrarSesionPorError(
                                            "La cuenta no tiene un rol asignado."
                                    );

                                    return;
                                }

                                dirigirSegunRol(
                                        rol.trim().toUpperCase()
                                );
                            }

                            @Override
                            public void onCancelled(
                                    DatabaseError error
                            ) {

                                firebaseAuth.signOut();

                                mostrarCargando(false);

                                Toast.makeText(
                                        LoginActivity.this,
                                        "No se pudo consultar tu perfil. Inténtalo nuevamente.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void dirigirSegunRol(String rol) {

        switch (rol) {

            case "CLIENTE":

                abrirPanelCliente();

                break;

            case "PERSONAL":

                /*
                 * Lo implementaremos cuando construyamos
                 * el Panel Personal.
                 */

                firebaseAuth.signOut();

                mostrarCargando(false);

                Toast.makeText(
                        this,
                        "El panel del personal estará disponible próximamente.",
                        Toast.LENGTH_LONG
                ).show();

                break;

            case "ADMIN":
            case "ADMINISTRADORA":

                /*
                 * Lo implementaremos cuando construyamos
                 * el Panel Administrador.
                 */

                firebaseAuth.signOut();

                mostrarCargando(false);

                Toast.makeText(
                        this,
                        "El panel de administración estará disponible próximamente.",
                        Toast.LENGTH_LONG
                ).show();

                break;

            default:

                firebaseAuth.signOut();

                mostrarCargando(false);

                Toast.makeText(
                        this,
                        "El rol de esta cuenta no es válido.",
                        Toast.LENGTH_LONG
                ).show();

                break;
        }
    }

    private void abrirPanelCliente() {

        mostrarCargando(false);

        Intent intent = new Intent(
                LoginActivity.this,
                ClienteActivity.class
        );

        /*
         * Eliminamos Login y pantallas anteriores
         * del historial.
         *
         * Así, después de iniciar sesión,
         * el usuario no puede pulsar Atrás
         * y regresar al Login.
         */
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    private void cerrarSesionPorError(String mensaje) {

        firebaseAuth.signOut();

        mostrarCargando(false);

        Toast.makeText(
                this,
                mensaje,
                Toast.LENGTH_LONG
        ).show();
    }

    private void mostrarCargando(boolean cargando) {

        procesandoLogin = cargando;

        btnLogin.setEnabled(!cargando);
        btnRegistrarme.setEnabled(!cargando);

        etDni.setEnabled(!cargando);
        etPassword.setEnabled(!cargando);

        if (cargando) {

            btnLogin.setText("INGRESANDO...");

        } else {

            btnLogin.setText("INICIAR SESIÓN");
        }
    }

    private void limpiarErrores() {

        inputDni.setError(null);
        inputPassword.setError(null);
    }

    private String obtenerTexto(
            TextInputEditText editText
    ) {

        if (editText.getText() == null) {
            return "";
        }

        return editText
                .getText()
                .toString()
                .trim();
    }
}
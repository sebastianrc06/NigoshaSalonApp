package pe.uch.nigosha.feature.cliente.perfil;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import pe.uch.nigosha.domain.models.Usuario;

public class PerfilViewModel extends ViewModel {

    private final MutableLiveData<Usuario> usuario =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> cargando =
            new MutableLiveData<>(false);

    private final MutableLiveData<String> error =
            new MutableLiveData<>();

    private final FirebaseAuth auth;

    private DatabaseReference usuarioRef;
    private ValueEventListener usuarioListener;

    public PerfilViewModel() {
        auth = FirebaseAuth.getInstance();
    }

    public LiveData<Usuario> getUsuario() {
        return usuario;
    }

    public LiveData<Boolean> getCargando() {
        return cargando;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void cargarUsuario() {

        FirebaseUser firebaseUser =
                auth.getCurrentUser();

        if (firebaseUser == null) {
            error.setValue(
                    "No existe una sesión activa."
            );
            return;
        }

        detenerEscucha();

        cargando.setValue(true);
        error.setValue(null);

        usuarioRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("usuarios")
                        .child(firebaseUser.getUid());

        usuarioListener =
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        cargando.setValue(false);

                        if (!snapshot.exists()) {

                            error.setValue(
                                    "No se encontraron los datos del usuario."
                            );

                            return;
                        }

                        Usuario datos =
                                snapshot.getValue(
                                        Usuario.class
                                );

                        if (datos == null) {

                            error.setValue(
                                    "No se pudieron leer los datos del usuario."
                            );

                            return;
                        }

                        usuario.setValue(datos);
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError databaseError
                    ) {

                        cargando.setValue(false);

                        error.setValue(
                                databaseError.getMessage()
                        );
                    }
                };

        usuarioRef.addValueEventListener(
                usuarioListener
        );
    }

    private void detenerEscucha() {

        if (usuarioRef != null
                && usuarioListener != null) {

            usuarioRef.removeEventListener(
                    usuarioListener
            );
        }

        usuarioRef = null;
        usuarioListener = null;
    }

    @Override
    protected void onCleared() {
        detenerEscucha();
        super.onCleared();
    }
}
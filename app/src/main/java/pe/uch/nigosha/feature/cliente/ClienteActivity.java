package pe.uch.nigosha.feature.cliente;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import pe.uch.nigosha.R;

public class ClienteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Permitimos trabajar edge-to-edge.
         */
        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );

        setContentView(
                R.layout.activity_cliente
        );

        View root =
                findViewById(
                        R.id.clienteRoot
                );

        BottomNavigationView bottomNavigation =
                findViewById(
                        R.id.clienteBottomNavigation
                );

        /*
         * Fondo oficial claro de Nigosha.
         */
        root.setBackgroundColor(
                ContextCompat.getColor(
                        this,
                        R.color.nigosha_bg
                )
        );

        /*
         * Como Nigosha utiliza fondo claro,
         * los iconos de las barras del sistema
         * deben mostrarse oscuros.
         */
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        getWindow(),
                        getWindow().getDecorView()
                );

        controller.setAppearanceLightStatusBars(
                true
        );

        controller.setAppearanceLightNavigationBars(
                true
        );

        /*
         * ---------------------------------------------------------
         * INSETS SUPERIORES
         * ---------------------------------------------------------
         *
         * Al contenedor principal SOLO le aplicamos:
         *
         * - izquierda
         * - arriba
         * - derecha
         *
         * NO agregamos el inset inferior al root.
         *
         * Esto evita el espacio blanco gigante que aparecía
         * debajo del BottomNavigationView.
         */
        ViewCompat.setOnApplyWindowInsetsListener(
                root,
                (view, windowInsets) -> {

                    Insets systemBars =
                            windowInsets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            0
                    );

                    return windowInsets;
                }
        );

        /*
         * ---------------------------------------------------------
         * BARRA DE NAVEGACIÓN INFERIOR
         * ---------------------------------------------------------
         *
         * La barra inferior recibe únicamente el espacio real
         * necesario para la navigation bar del teléfono.
         *
         * Así queda pegada visualmente a los botones de Android
         * sin generar una segunda zona vacía.
         */
        ViewCompat.setOnApplyWindowInsetsListener(
                bottomNavigation,
                (view, windowInsets) -> {

                    Insets navigationBars =
                            windowInsets.getInsets(
                                    WindowInsetsCompat.Type.navigationBars()
                            );

                    view.setPadding(
                            view.getPaddingLeft(),
                            view.getPaddingTop(),
                            view.getPaddingRight(),
                            navigationBars.bottom
                    );

                    return windowInsets;
                }
        );

        ViewCompat.requestApplyInsets(
                root
        );

        ViewCompat.requestApplyInsets(
                bottomNavigation
        );

        /*
         * ---------------------------------------------------------
         * NAVIGATION COMPONENT
         * ---------------------------------------------------------
         */
        NavHostFragment navHost =
                (NavHostFragment)
                        getSupportFragmentManager()
                                .findFragmentById(
                                        R.id.clienteNavHost
                                );

        if (navHost == null) {

            throw new IllegalStateException(
                    "No se encontró el contenedor "
                            + "de navegación del cliente."
            );
        }

        NavigationUI.setupWithNavController(
                bottomNavigation,
                navHost.getNavController()
        );

        /*
         * ---------------------------------------------------------
         * OCULTAR BOTTOM NAV DURANTE RESERVA
         * ---------------------------------------------------------
         *
         * Inicio
         * Servicios
         * Mis citas
         * El salón
         *
         * aparecen normalmente.
         *
         * Durante reserva/resumen/pago desaparecen
         * para dejar más espacio disponible.
         */
        navHost.getNavController()
                .addOnDestinationChangedListener(
                        (controllerNav, destination, args) -> {

                            int id =
                                    destination.getId();

                            boolean flujo =
                                    id == R.id.reservaFragment
                                            || id == R.id.resumenReservaFragment
                                            || id == R.id.resultadoReservaFragment
                                            || id == R.id.pagoFragment
                                            || id == R.id.perfilFragment;

                            bottomNavigation.setVisibility(
                                    flujo
                                            ? View.GONE
                                            : View.VISIBLE
                            );
                        }
                );
    }
}
package pe.uch.nigosha.feature.cliente;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
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

    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

    setContentView(R.layout.activity_cliente);

    View root = findViewById(R.id.clienteRoot);

    int colorFondo = ContextCompat.getColor(this, R.color.nigosha_bg);

    root.setBackgroundColor(colorFondo);

    boolean fondoClaro = ColorUtils.calculateLuminance(colorFondo) > 0.5;

    WindowInsetsControllerCompat controller =
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

    controller.setAppearanceLightStatusBars(fondoClaro);
    controller.setAppearanceLightNavigationBars(fondoClaro);

    // Conserva el padding original y añade las áreas del sistema.
    int paddingLeft = root.getPaddingLeft();
    int paddingTop = root.getPaddingTop();
    int paddingRight = root.getPaddingRight();
    int paddingBottom = root.getPaddingBottom();

    ViewCompat.setOnApplyWindowInsetsListener(
        root,
        (view, windowInsets) -> {
          Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

          view.setPadding(
              paddingLeft + bars.left,
              paddingTop + bars.top,
              paddingRight + bars.right,
              paddingBottom + bars.bottom);

          return WindowInsetsCompat.CONSUMED;
        });

    ViewCompat.requestApplyInsets(root);

    NavHostFragment navHost =
        (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.clienteNavHost);

    if (navHost == null) {
      throw new IllegalStateException(
          "No se encontró el contenedor " + "de navegación del cliente.");
    }

    BottomNavigationView bottomNavigation = findViewById(R.id.clienteBottomNavigation);

    NavigationUI.setupWithNavController(bottomNavigation, navHost.getNavController());
    navHost
        .getNavController()
        .addOnDestinationChangedListener(
            (controllerNav, destination, args) -> {
              int id = destination.getId();
              boolean flujo =
                  id == R.id.reservaFragment
                      || id == R.id.resumenReservaFragment
                      || id == R.id.resultadoReservaFragment
                      || id == R.id.pagoFragment;
              bottomNavigation.setVisibility(flujo ? View.GONE : View.VISIBLE);
            });
  }
}

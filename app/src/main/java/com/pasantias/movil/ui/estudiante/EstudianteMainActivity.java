package com.pasantias.movil.ui.estudiante;

import androidx.fragment.app.Fragment;

import com.pasantias.movil.R;
import com.pasantias.movil.ui.common.BaseMainActivity;
import com.pasantias.movil.ui.common.NotificacionesFragment;

public class EstudianteMainActivity extends BaseMainActivity {

    @Override
    protected int getBottomMenuRes() {
        return R.menu.menu_estudiante;
    }

    @Override
    protected Fragment createFragment(int itemId) {
        if (itemId == R.id.nav_dashboard) return new EstudianteDashboardFragment();
        if (itemId == R.id.nav_catalogo) return new EstudianteCatalogoFragment();
        if (itemId == R.id.nav_actividades) return new EstudianteActividadesFragment();
        if (itemId == R.id.nav_calendario) return new EstudianteCalendarioFragment();
        if (itemId == R.id.nav_notificaciones) return new NotificacionesFragment();
        return null;
    }
}

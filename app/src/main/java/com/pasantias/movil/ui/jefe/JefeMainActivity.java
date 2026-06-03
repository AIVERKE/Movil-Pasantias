package com.pasantias.movil.ui.jefe;

import androidx.fragment.app.Fragment;

import com.pasantias.movil.R;
import com.pasantias.movil.ui.common.BaseMainActivity;
import com.pasantias.movil.ui.common.NotificacionesFragment;

public class JefeMainActivity extends BaseMainActivity {

    @Override
    protected int getBottomMenuRes() {
        return R.menu.menu_jefe;
    }

    @Override
    protected Fragment createFragment(int itemId) {
        if (itemId == R.id.nav_dashboard) return new JefeDashboardFragment();
        if (itemId == R.id.nav_inscripciones) return new JefeInscripcionesFragment();
        if (itemId == R.id.nav_pasantes) return new JefePasantesFragment();
        if (itemId == R.id.nav_actividades) return new JefeActividadesFragment();
        if (itemId == R.id.nav_notificaciones) return new NotificacionesFragment();
        return null;
    }
}

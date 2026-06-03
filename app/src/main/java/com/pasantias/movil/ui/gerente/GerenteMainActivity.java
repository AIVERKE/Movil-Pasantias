package com.pasantias.movil.ui.gerente;

import androidx.fragment.app.Fragment;

import com.pasantias.movil.R;
import com.pasantias.movil.ui.common.BaseMainActivity;
import com.pasantias.movil.ui.common.NotificacionesFragment;

public class GerenteMainActivity extends BaseMainActivity {

    @Override
    protected int getBottomMenuRes() {
        return R.menu.menu_gerente;
    }

    @Override
    protected Fragment createFragment(int itemId) {
        if (itemId == R.id.nav_dashboard) return new GerenteDashboardFragment();
        if (itemId == R.id.nav_pasantias) return new GerentePasantiasFragment();
        if (itemId == R.id.nav_empresa) return new GerenteEmpresaFragment();
        if (itemId == R.id.nav_notificaciones) return new NotificacionesFragment();
        return null;
    }
}

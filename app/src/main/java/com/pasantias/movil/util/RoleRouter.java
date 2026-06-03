package com.pasantias.movil.util;

import android.content.Context;
import android.content.Intent;

import com.pasantias.movil.data.dto.UserDto;
import com.pasantias.movil.ui.auth.LoginActivity;
import com.pasantias.movil.ui.estudiante.EstudianteMainActivity;
import com.pasantias.movil.ui.gerente.GerenteMainActivity;
import com.pasantias.movil.ui.jefe.JefeMainActivity;

public final class RoleRouter {

    private RoleRouter() {}

    public static Intent dashboardIntent(Context context, UserDto user) {
        if (user == null || user.tipo == null) {
            return new Intent(context, LoginActivity.class);
        }
        switch (user.tipo) {
            case "gerente":
                return new Intent(context, GerenteMainActivity.class);
            case "jefe_pasantes":
                return new Intent(context, JefeMainActivity.class);
            case "estudiante":
                return new Intent(context, EstudianteMainActivity.class);
            default:
                return new Intent(context, LoginActivity.class);
        }
    }
}

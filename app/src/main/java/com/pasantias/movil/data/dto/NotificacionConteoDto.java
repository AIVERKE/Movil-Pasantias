package com.pasantias.movil.data.dto;

public class NotificacionConteoDto {
    public int count;
    public int no_leidas;

    public int getNoLeidas() {
        return count > 0 ? count : no_leidas;
    }
}

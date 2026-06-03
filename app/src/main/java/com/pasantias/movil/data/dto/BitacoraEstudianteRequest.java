package com.pasantias.movil.data.dto;

public class BitacoraEstudianteRequest {
    public int id_actividad;
    public String contenido;
    public int porcentaje;

    public BitacoraEstudianteRequest(int idActividad, String contenido, int porcentaje) {
        this.id_actividad = idActividad;
        this.contenido = contenido;
        this.porcentaje = porcentaje;
    }
}

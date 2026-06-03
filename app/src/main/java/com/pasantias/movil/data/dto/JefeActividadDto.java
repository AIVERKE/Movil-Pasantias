package com.pasantias.movil.data.dto;

public class JefeActividadDto {
    public int id_actividad;
    public String descripcion;
    public String fecha_inicio;
    public String fecha_fin;
    public String estado;
    public PasantiaRef pasantia;

    public static class PasantiaRef {
        public int id_pasantia;
        public String titulo;
    }

    public String getTitulo() {
        if (descripcion == null) return "Actividad";
        return descripcion.length() > 60 ? descripcion.substring(0, 57) + "…" : descripcion;
    }
}

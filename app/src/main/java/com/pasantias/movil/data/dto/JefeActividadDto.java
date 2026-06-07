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

    public java.util.List<PasanteRef> asignados;
    public java.util.List<PasanteRef> pasantesDisponibles;

    public static class PasanteRef {
        public int id_inscripcion;
        public String estudiante;
        public String iniciales;
    }

    public String getTitulo() {
        if (descripcion == null) return "Actividad";
        return descripcion.length() > 60 ? descripcion.substring(0, 57) + "…" : descripcion;
    }
}

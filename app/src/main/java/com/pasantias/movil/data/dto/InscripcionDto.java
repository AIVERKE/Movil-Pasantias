package com.pasantias.movil.data.dto;

public class InscripcionDto {
    public int id;
    public int id_inscripcion;
    public String estado;
    public String estudiante;
    public String estudiante_nombre;
    public String pasantia;
    public String pasantia_titulo;
    public int id_pasantia;
    public int id_estudiante;

    public int getId() {
        return id_inscripcion > 0 ? id_inscripcion : id;
    }

    public String getEstudianteNombre() {
        return estudiante != null ? estudiante : estudiante_nombre;
    }

    public String getPasantiaTitulo() {
        return pasantia != null ? pasantia : pasantia_titulo;
    }
}

package com.pasantias.movil.data.dto;

import java.util.List;

public class EstudianteCatalogoDto {
    public int id_estudiante;
    public String registro_universitario;
    public String carrera;
    public int semestre;
    public String mencion;
    public UsuarioInterno usuario;
    public List<String> habilidades;

    public static class UsuarioInterno {
        public String nombre;
        public String apellido;
        public String email;
    }

    public String getNombreCompleto() {
        if (usuario != null) {
            return usuario.nombre + " " + usuario.apellido;
        }
        return "Estudiante";
    }
}

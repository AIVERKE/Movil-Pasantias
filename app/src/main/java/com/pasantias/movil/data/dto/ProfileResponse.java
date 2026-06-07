package com.pasantias.movil.data.dto;

public class ProfileResponse {
    public UserDto usuario;
    public EstudianteProfile estudiante;
    public Object gerente;
    public Object jefe;
    public Object tutor;

    public static class EstudianteProfile {
        public int id_estudiante;
        public int semestre;
        public String carrera;
        public String universidad;
        public String ci;
    }
}

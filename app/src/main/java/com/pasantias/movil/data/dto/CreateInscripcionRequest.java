package com.pasantias.movil.data.dto;

public class CreateInscripcionRequest {
    public int id_estudiante;
    public int id_pasantia;

    public CreateInscripcionRequest(int idEstudiante, int idPasantia) {
        this.id_estudiante = idEstudiante;
        this.id_pasantia = idPasantia;
    }
}

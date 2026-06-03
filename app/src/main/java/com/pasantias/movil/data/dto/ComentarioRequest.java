package com.pasantias.movil.data.dto;

public class ComentarioRequest {
    public String texto;
    public String rol;
    public String autor;

    public ComentarioRequest(String texto, String rol, String autor) {
        this.texto = texto;
        this.rol = rol;
        this.autor = autor;
    }
}

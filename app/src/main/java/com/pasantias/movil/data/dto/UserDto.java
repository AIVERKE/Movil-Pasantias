package com.pasantias.movil.data.dto;

public class UserDto {
    public int id;
    public String nombre;
    public String apellido;
    public String email;
    public String tipo;
    public int nivel;
    public String url_foto_perfil;

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }
}

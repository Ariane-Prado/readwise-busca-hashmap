package com.seuapp.model;

public class Documento {
    private final String titulo;
    private final String descricao;

    public Documento(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }
}

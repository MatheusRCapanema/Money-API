package com.example.loginapi.models;

public class EmailDetails {

    private String destinatario;
    private String msgBody;
    private String titulo;
    private String anexo;

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAnexo() {
        return anexo;
    }

    public void setAnexo(String anexo) {
        this.anexo = anexo;
    }

    public EmailDetails(String destinatario, String msgBody, String titulo, String anexo) {
        this.destinatario = destinatario;
        this.msgBody = msgBody;
        this.titulo = titulo;
        this.anexo = anexo;
    }

    public EmailDetails(){

    }
}

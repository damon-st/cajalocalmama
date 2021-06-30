package com.damon.caja.models;

import java.util.Date;

public class ValoresDeudas {
    private double valor;
    private boolean isPay;
    private Date registerDate;
    private Date payDate;
    private String id, idDeudor;


    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public ValoresDeudas() {
    }

    public ValoresDeudas(double valor, boolean isPay, Date registerDate, Date payDate, String id, String idDeudor) {
        this.valor = valor;
        this.isPay = isPay;
        this.registerDate = registerDate;
        this.payDate = payDate;
        this.id = id;
        this.idDeudor = idDeudor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdDeudor() {
        return idDeudor;
    }

    public void setIdDeudor(String idDeudor) {
        this.idDeudor = idDeudor;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public boolean isPay() {
        return isPay;
    }

    public void setPay(boolean pay) {
        isPay = pay;
    }

    public ValoresDeudas(double valor, boolean isPay) {
        this.valor = valor;
        this.isPay = isPay;
    }

}
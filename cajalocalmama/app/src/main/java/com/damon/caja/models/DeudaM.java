package com.damon.caja.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class DeudaM implements Serializable {

    private String name;
    private String date;
    private String id;
    private List<ValoresDeudas> valor;
    private boolean isPay;
    private Date dateCreate;

    public DeudaM() {
    }

    public DeudaM(String name, String date, String id, List<ValoresDeudas> valor, boolean isPay, Date dateCreate) {
        this.name = name;
        this.date = date;
        this.id = id;
        this.valor = valor;
        this.isPay = isPay;
        this.dateCreate = dateCreate;
    }

    public Date getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }

    public boolean isPay() {
        return isPay;
    }

    public void setPay(boolean pay) {
        isPay = pay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ValoresDeudas> getValor() {
        return valor;
    }

    public void setValor(List<ValoresDeudas> valor) {
        this.valor = valor;
    }



}

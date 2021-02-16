package com.damon.caja.models;

import java.io.Serializable;
import java.util.Date;

public class CajaM  implements Serializable {

    private double ValorCasa,ValorCaja,BaseRecargaMio,BaseRecargaEvelynBase
            ,BaseRecargaEvelynVendido,BaseRecargaMovilway,InternetAnotado;

    private float FechaCreado;
    private Date fechaDate;

    private String fecha,id;

    private double ValorTotalAllegar;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CajaM() {
    }

    public CajaM(double valorCasa, double valorCaja, double baseRecargaMio, double baseRecargaEvelynBase, double baseRecargaEvelynVendido, double baseRecargaMovilway, double internetAnotado, float fechaCreado, Date fechaDate, String fecha, String id, double valorTotalAllegar) {
        ValorCasa = valorCasa;
        ValorCaja = valorCaja;
        BaseRecargaMio = baseRecargaMio;
        BaseRecargaEvelynBase = baseRecargaEvelynBase;
        BaseRecargaEvelynVendido = baseRecargaEvelynVendido;
        BaseRecargaMovilway = baseRecargaMovilway;
        InternetAnotado = internetAnotado;
        FechaCreado = fechaCreado;
        this.fechaDate = fechaDate;
        this.fecha = fecha;
        this.id = id;
        ValorTotalAllegar = valorTotalAllegar;
    }

    public double getValorTotalAllegar() {
        return ValorTotalAllegar;
    }

    public void setValorTotalAllegar(double valorTotalAllegar) {
        ValorTotalAllegar = valorTotalAllegar;
    }

    public float getFechaCreado() {
        return FechaCreado;
    }

    public void setFechaCreado(float fechaCreado) {
        FechaCreado = fechaCreado;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Date getFechaDate() {
        return fechaDate;
    }

    public void setFechaDate(Date fechaDate) {
        this.fechaDate = fechaDate;
    }

    public double getValorCasa() {
        return ValorCasa;
    }

    public void setValorCasa(double valorCasa) {
        ValorCasa = valorCasa;
    }

    public double getValorCaja() {
        return ValorCaja;
    }

    public void setValorCaja(double valorCaja) {
        ValorCaja = valorCaja;
    }

    public double getBaseRecargaMio() {
        return BaseRecargaMio;
    }

    public void setBaseRecargaMio(double baseRecargaMio) {
        BaseRecargaMio = baseRecargaMio;
    }

    public double getBaseRecargaEvelynBase() {
        return BaseRecargaEvelynBase;
    }

    public void setBaseRecargaEvelynBase(double baseRecargaEvelynBase) {
        BaseRecargaEvelynBase = baseRecargaEvelynBase;
    }

    public double getBaseRecargaEvelynVendido() {
        return BaseRecargaEvelynVendido;
    }

    public void setBaseRecargaEvelynVendido(double baseRecargaEvelynVendido) {
        BaseRecargaEvelynVendido = baseRecargaEvelynVendido;
    }

    public double getBaseRecargaMovilway() {
        return BaseRecargaMovilway;
    }

    public void setBaseRecargaMovilway(double baseRecargaMovilway) {
        BaseRecargaMovilway = baseRecargaMovilway;
    }

    public double getInternetAnotado() {
        return InternetAnotado;
    }

    public void setInternetAnotado(double internetAnotado) {
        InternetAnotado = internetAnotado;
    }
}

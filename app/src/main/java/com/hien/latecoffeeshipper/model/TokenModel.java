package com.hien.latecoffeeshipper.model;

public class TokenModel {
    private String phone, token;
    private boolean severToken,shipperToken;

    public TokenModel() {
    }

    public TokenModel(String phone, String token, boolean severToken, boolean shipperToken) {
        this.phone = phone;
        this.token = token;
        this.severToken = severToken;
        this.shipperToken = shipperToken;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSeverToken() {
        return severToken;
    }

    public void setSeverToken(boolean severToken) {
        this.severToken = severToken;
    }

    public boolean isShipperToken() {
        return shipperToken;
    }

    public void setShipperToken(boolean shipperToken) {
        this.shipperToken = shipperToken;
    }
}

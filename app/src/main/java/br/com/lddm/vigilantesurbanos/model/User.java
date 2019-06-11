package br.com.lddm.vigilantesurbanos.model;

@SuppressWarnings("unused")
public class User {
    private boolean isOap;
    private String uuid;

    public User(boolean isOap, String uuid) {
        this.isOap = isOap;
        this.uuid = uuid;
    }

    public User() {
        //empty constructor is required for Firebase
    }

    public boolean isOap() {
        return isOap;
    }

    public void setOap(boolean oap) {
        isOap = oap;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

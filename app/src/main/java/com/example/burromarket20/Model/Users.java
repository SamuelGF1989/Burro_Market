package com.example.burromarket20.Model;

public class Users {

    private String name,boleta,password,image,address;

    public Users(){

    }
    //Se crea el constructor para la parte del login con los parametros


    public Users(String name, String boleta, String password, String image, String address) {
        this.name = name;
        this.boleta = boleta;
        this.password = password;
        this.image = image;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBoleta() {
        return boleta;
    }

    public void setBoleta(String boleta) {
        this.boleta = boleta;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

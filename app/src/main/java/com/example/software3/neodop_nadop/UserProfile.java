package com.example.software3.neodop_nadop;

public class UserProfile {

    private String name;
    private int age;
    private String phoneNumber;
    private boolean isDisabled;
    private String typeOfDisabled;
    private String token;
    private String sex;
    private double rating;
    private long numOfRaters;

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public String getTypeOfDisabled() {
        return typeOfDisabled;
    }

    public String getToken(){
        return token;
    }

    public String getSex() {
        return sex;
    }

    public double getRating(){return rating;}

    public long getNumOfRaters(){return numOfRaters;}

    public UserProfile(String sex, String name, int age , String phoneNumber, boolean isDisabled, String typeOfDisabled, String token,double rating,long numOfRaters) {
        this.name = name;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.isDisabled = isDisabled;
        this.typeOfDisabled = typeOfDisabled;
        this.token = token;
        this.sex = sex;
        this.rating = rating;
        this.numOfRaters = numOfRaters;
    }
}

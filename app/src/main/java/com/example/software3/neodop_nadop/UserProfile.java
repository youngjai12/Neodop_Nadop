package com.example.software3.neodop_nadop;

public class UserProfile {

    private String name;
    private int age;
    private String phoneNumber;
    private boolean isDisabled;
    private String typeOfDisabled;
    private String token;

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
    public UserProfile(String name, int age , String phoneNumber, boolean isDisabled, String typeOfDisabled,String token) {
        this.name = name;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.isDisabled = isDisabled;
        this.typeOfDisabled = typeOfDisabled;
        this.token = token;
    }
}

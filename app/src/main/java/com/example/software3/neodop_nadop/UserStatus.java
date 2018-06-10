package com.example.software3.neodop_nadop;

public class UserStatus {
    private String changeValue;
    private boolean finished ;
    private String yourUid;
    public String getChangeValue() {
        return changeValue;
    }

    public boolean getFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public UserStatus(){
        changeValue = "";
        finished = true;
        yourUid = "";
    }
    public UserStatus(String changeValue, boolean finished,String yourUid){
        this.changeValue = changeValue;
        this.finished = finished;
        this.yourUid = yourUid;
    }

    public String getYourUid() {
        return yourUid;
    }
}

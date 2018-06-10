package com.example.software3.neodop_nadop;

public class UserRequest {
    private String uid;
    private boolean accepted;

    public UserRequest(String uid, boolean accepted) {
        this.uid = uid;
        this.accepted = accepted;
    }

    public UserRequest(){
            uid="";
            accepted=false;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }




}

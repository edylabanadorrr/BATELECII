package com.batelectwo;

public class SentFeedbacks {

    public String feedback;
    public String email;
    public String uid;

    // Default constructor (no-argument constructor)
    public SentFeedbacks() {
        // Default constructor required for Firebase Database
    }

    public SentFeedbacks(String inputFeedback, String userEmail, String userUid) {
        this.feedback = inputFeedback;
        this.email = userEmail;
        this.uid = userUid;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
}

package com.batelectwo;

public class SentFeedbacks {
    public String feedback;
    public String email;
    public String uid;

    // Default constructor (no-argument constructor)
    public SentFeedbacks() {
        // Default constructor required for Firebase Database
    }

    public SentFeedbacks(String feedback, String email, String uid) {
        this.feedback = feedback;
        this.email = email;
        this.uid = uid;
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

    public String toString() {
        return "Feedback " + feedback + "\nEmail: " + email + "\nUID: " + uid;
    }
}

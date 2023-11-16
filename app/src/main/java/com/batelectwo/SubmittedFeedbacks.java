package com.batelectwo;

public class SubmittedFeedbacks {

    public String feedback;
    public String email;
    public String uid;

    // Default constructor (no-argument constructor)

    public SubmittedFeedbacks(String feedback, String email, String uid) {
        this.feedback = feedback;
        this.email = email;
        this.uid = uid;
    }

    public String getFeedback() {
        return feedback;
    }
    public void setFeedback() {
        this.feedback = feedback;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail() {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }
    public void setUid() {
        this.uid = uid;
    }

}

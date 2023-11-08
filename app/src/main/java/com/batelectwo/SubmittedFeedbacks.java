package com.batelectwo;

public class SubmittedFeedbacks {

    public String feedback, email, uid;

    // Default constructor (no-argument constructor)
    public SubmittedFeedbacks() {
        // Default constructor required for Firebase Database
    }

    public SubmittedFeedbacks(String inputFeedback, String userEmail, String userUid) {
        this.feedback = inputFeedback;
        this.email = userEmail;
        this.uid = userUid;
    }
}

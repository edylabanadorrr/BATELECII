package com.batelectwo;

public class SubmittedTickets {
    public String location;
    public String issue;
    public String details;
    public String email;
    public String uid;

    // Default constructor (no-argument constructor)
    public SubmittedTickets() {
        // Default constructor required for Firebase Database
    }

    public SubmittedTickets(String inputLocation, String inputIssue, String inputDetails, String userEmail, String userUid) {
        this.location = inputLocation;
        this.issue = inputIssue;
        this.details = inputDetails;
        this.email = userEmail;
        this.uid = userUid;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
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

package com.batelectwo;

public class TicketListItem {
        private String location;
        private String issue;
        private String details;
        private String email;
        private String uid;

        public TicketListItem(String location, String issue, String details, String email, String uid) {
            this.uid = uid;
            this.email = email;
            this.location = location;
            this.issue = issue;
            this.details = details;
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
    // Getter and setter methods if needed
    }



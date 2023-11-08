package com.batelectwo;

public class ReadWriteUserDetails {

    public String firstName, lastName, contactNumber, address, username, accountNumber, role, bill;

    // Constructor

    public ReadWriteUserDetails(){};

    public ReadWriteUserDetails(String textfirstName, String textlastName, String textcontactNumber, String textaddress, String textusername, String textAccountNumber) {
        this.firstName = textfirstName;
        this.lastName = textlastName;
        this.contactNumber = textcontactNumber;
        this.address = textaddress;
        this.username = textusername;
        this.accountNumber = textAccountNumber;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public void setBill(String bill) {
        this.bill = bill;
    }


}

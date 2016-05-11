package com.santellia.sud.trackbuddy.model;

/**
 * Created by Sudarshan on 18/03/16.
 */
public class ContactInfo {

    private String contactName="";
    private String contactId;
    private String contactNumber = "";

    public void setContactName(String username)
    {
        this.contactName = username;
    }
    public String getContactName()
    {
        return this.contactName;
    }

    public void setContactId(String userId)
    {
        this.contactId = userId;
    }
    public String getContactId()
    {
        return this.contactId;
    }


    public void setContactNumber(String mobileNumber)
    {
        this.contactNumber = mobileNumber;
    }
    public String getContactNumber()
    {
        return this.contactNumber;
    }
}

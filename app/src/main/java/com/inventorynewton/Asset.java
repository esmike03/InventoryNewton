package com.inventorynewton;

public class Asset {
    public String assetNumber;
    public String description;
    public String location;
    public String remarks;
    public String validate;

    public String created_at;
    public String updated_at;
    public boolean isValid;
    public String error;

    public Asset(String assetNumber, String description, String location,
                 String remarks, String status) {
        this.assetNumber = assetNumber;
        this.description = description;
        this.location = location;
        this.remarks = remarks;
        this.validate = status;
    }
    public Asset() {}
}

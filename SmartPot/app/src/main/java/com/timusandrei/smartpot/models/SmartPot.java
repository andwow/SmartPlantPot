package com.timusandrei.smartpot.models;

import java.io.Serializable;

public class SmartPot implements Serializable {

    public SmartPot() {
        this.name = "";
        this.description = "";
        this.productId = "";
    }

    public SmartPot(String name, String description, String productCode) {
        this.name = name;
        this.description = description;
        this.productId = productCode;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productCode) {
        this.productId = productCode;
    }

    private String name;
    private String description;
    private String productId;
}

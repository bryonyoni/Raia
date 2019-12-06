package com.bry.raia.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class County {
    @SerializedName("name")
    @Expose
    private String name; //set to nairobi, the country's capital by default

    public County(){}

    public String getName() {
        if(name == null) return "Nairobi";
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

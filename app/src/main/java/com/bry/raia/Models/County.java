package com.bry.raia.Models;

public class County {
    private String countyName; //set to nairobi, the country's capital by default

    public String getCountyName() {
        if(countyName == null) return "Nairobi";
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

}

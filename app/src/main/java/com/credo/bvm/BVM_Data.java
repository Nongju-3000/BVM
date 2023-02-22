package com.credo.bvm;

public class BVM_Data {

    private String name;
    private String address;

    public BVM_Data(){}

    public BVM_Data(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}

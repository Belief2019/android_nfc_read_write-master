package com.example.peng.nfcreadwrite.beans;

public class Data {
    private String name;
    private String scale;
    private int amount;
    private String offer;
    private int price;
    private String type;
    private String time;
    public Data(){
        super();
    }

    public Data(String name, String scale, int amount, String offer, int price, String type,String time) {
        super();
        this.name = name;
        this.scale = scale;
        this.amount = amount;
        this.offer = offer;
        this.price = price;
        this.type = type;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Data{" +
                "name='" + name + '\'' +
                ", scale='" + scale + '\'' +
                ", amount=" + amount +
                ", offer='" + offer + '\'' +
                ", price=" + price +
                ", type='" + type + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}

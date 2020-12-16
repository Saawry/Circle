package com.example.circle;

public class CirclesInfo {
    public String name, memberAmount, image;

    public CirclesInfo(){

    }
    public CirclesInfo(String name, String memberAmount){
        this.name= name;
        this.memberAmount= memberAmount;
    }

    public CirclesInfo(String name, String memberAmount, String image){
        this.name= name;
        this.memberAmount= memberAmount;
        this.image= image;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMemberAmount() {
        return memberAmount;
    }

    public void setMemberAmount(String memberAmount) {
        this.memberAmount = memberAmount;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

package com.ag.apiaiapp;

public class Data {
    String shopname;
    String shoplink;
    String tag;

    public Data(){
    }

    public Data(String shopname,
            String shoplink,
            String tag){

        this.shopname=shopname;
        this.shoplink=shoplink;
        this.tag=tag;
    }

    public String getShopname(){
        return shopname;
    }

    public String getShoplink(){
        return shoplink;
    }

    public String getTag(){
        return tag;
    }
}

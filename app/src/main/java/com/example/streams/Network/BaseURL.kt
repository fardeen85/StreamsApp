package com.example.streams.Network

class BaseURL {


    private var url = "https://streamsappbackend-production.up.railway.app/api/"
    private var emulatorurl = "http://10.0.2.2:5000/api/"
    private var phoneUrl = "http://192.168.0.109:5000/api/"

    public fun getBaseUrl() : String{

        return  phoneUrl
    }
}
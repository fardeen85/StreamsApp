package com.example.streams.Network

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor

class SessionManager {

    lateinit var sessionPreference : SharedPreferences
    lateinit var editor : SharedPreferences.Editor

    fun Initialize(c : Context){

        sessionPreference = c.getSharedPreferences("session",Context.MODE_PRIVATE)
        editor = sessionPreference.edit()

    }

    fun Addtoken(token:String){

        editor.putString("token",token)
        editor.apply()
        editor.commit()
    }

    fun AddId(id:String){

        editor.putString("Id",id)
        editor.apply()
        editor.commit()
    }

    fun getId() : String?{

        return  sessionPreference.getString("Id",null)
    }

    fun gettoken() : String? {

        return  sessionPreference.getString("token","null")
    }

    fun SavefirstTime(status:String){

        editor.putString("firstTimestatus",status)
        editor.apply()
        editor.commit()
    }

    fun getfirsttime() : String? {

        return sessionPreference.getString("firstTimestatus","No")
    }


    fun saveUsername(status:String){

        editor.putString("username",status)
        editor.apply()
        editor.commit()
    }

    fun getUsername() : String? {

        return sessionPreference.getString("username","Unknwn")
    }
}
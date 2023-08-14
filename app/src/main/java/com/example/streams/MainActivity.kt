package com.example.streams

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.streams.Network.SessionManager
import com.example.streams.ViewModels.AuthViewModel
import com.example.streams.databinding.ActivityMainBinding
import com.google.gson.JsonObject


class MainActivity : AppCompatActivity() {

    lateinit var authviewModel : AuthViewModel
    var activityMainBinding : ActivityMainBinding? = null
    lateinit var sessionManager: SessionManager


    companion object{
        public var channel_name = "streams"
        public var channelId = "streams1"

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding!!.root)



        askpermission()

//        authviewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        sessionManager = SessionManager()
        sessionManager.Initialize(this)

        checklogin()

        authviewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(
              AuthViewModel::class.java
            )


        activityMainBinding!!.gotosignup.setOnClickListener {

            startActivity(Intent(this,ActivitySignUp::class.java))
        }

        authviewModel.sigininresponse.observe(this,object : Observer<JsonObject>{
            override fun onChanged(t: JsonObject?) {

                if (t != null){

                    var message = t.get("msg").asString
                    if (message.equals("Sign in success")) {


                        Toast.makeText(
                            this@MainActivity,
                            t.get("msg").asString,
                            Toast.LENGTH_SHORT
                        ).show()


                        if (sessionManager.getfirsttime().equals("No")){


                            startActivity(
                                Intent(this@MainActivity, UploadPhoto::class.java))

                        }
                        else {
                            startActivity(
                                Intent(this@MainActivity, Dashboard::class.java))
                            Log.d("signincheck", "1")
                        }
                    }
                    else{
                        Toast.makeText(
                            this@MainActivity,
                            t.get("msg").asString,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }

        })

        authviewModel.isloading.observe(this,object :Observer<Boolean>{
            override fun onChanged(t: Boolean?) {

                if(t!!){

                    activityMainBinding!!.progressBar2.visibility = View.VISIBLE
                    activityMainBinding!!.btnsingin.visibility = View.GONE
                }
                else{

                    activityMainBinding!!.progressBar2.visibility = View.GONE
                    activityMainBinding!!.btnsingin.visibility = View.VISIBLE
                }
            }


        })

        activityMainBinding!!.btnsingin.setOnClickListener {

            var siginobject = SignIn()
            var obj = siginobject as JsonObject

            if (obj.get("msg") != null) {
                Toast.makeText(this@MainActivity, obj?.get("msg").asString, Toast.LENGTH_SHORT)
                    .show()


            }

        }

        createNotificationChannel()
     //   startActivity(Intent(this,Dashboard::class.java))
    }


    public fun SignIn() : JsonObject{

        var email = activityMainBinding!!.editemail.text.toString()
        var pass = activityMainBinding!!.editpass.text.toString()
        var response = authviewModel.SignIn(email,pass)
        return  response
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name =channel_name
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun checklogin(){

        if ( !sessionManager.gettoken().toString().equals("null") ){

            startActivity(Intent(this,Dashboard::class.java))
            Log.d("TAG",sessionManager.gettoken().toString())

        }

    }


    fun askpermission(){


        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),19)
        }

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),21)
        }
    }

}
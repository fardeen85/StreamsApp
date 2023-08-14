package com.example.streams

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.streams.Network.SessionManager
import com.example.streams.ViewModels.AuthViewModel
import com.example.streams.databinding.ActivitySignUpBinding
import com.google.gson.JsonObject
import org.json.JSONObject

class ActivitySignUp : AppCompatActivity() {

    val authviewModel : AuthViewModel by viewModels()
    var activitySignUpBinding : ActivitySignUpBinding? = null
    lateinit var sessionManager : SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySignUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(activitySignUpBinding!!.root)

        sessionManager = SessionManager()
        sessionManager.Initialize(this)


        activitySignUpBinding!!.btnsignup.setOnClickListener {

            var signupResponse : JsonObject= signUp()



        }

        activitySignUpBinding!!.progressBar.visibility = View.GONE


        authviewModel.isloading.observe(this,object :Observer<Boolean>{
            override fun onChanged(t: Boolean?) {

                if(t!!){

                    activitySignUpBinding!!.progressBar.visibility = View.VISIBLE
                }
                else{

                    activitySignUpBinding!!.progressBar.visibility = View.GONE
                }
            }


        })


        authviewModel.sigininresponse.observe(this,object : Observer<JsonObject>{
            override fun onChanged(t: JsonObject?) {

                if (t != null){

                        Toast.makeText(
                            this@ActivitySignUp,
                            t.get("msg").asString,
                            Toast.LENGTH_SHORT
                        ).show()

                }
            }

        })


        authviewModel.successmessage.observe(this,object :Observer<Boolean>{
            override fun onChanged(t: Boolean?) {
                if (t != null){

                    if (t){

                        startActivity(Intent(this@ActivitySignUp,UploadPhoto::class.java))
                        sessionManager.SavefirstTime("No")
                        finish()
                    }

                }
            }

        })



    }


    public fun signUp() : JsonObject{

        var email = activitySignUpBinding!!.editemail.text.toString()
        var password = activitySignUpBinding!!.editpassword.text.toString()
        var username = activitySignUpBinding!!.edditusername.text.toString()
        var signupResponse : JsonObject= authviewModel.SignUp(email = email, password = password, username = username)
        return  signupResponse
    }
}
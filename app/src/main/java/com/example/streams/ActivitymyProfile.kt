package com.example.streams

import android.R
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.streams.Models.ProfileModel
import com.example.streams.Network.BaseURL
import com.example.streams.Network.SessionManager
import com.example.streams.ViewModels.AuthViewModel
import com.example.streams.ViewModels.VideoViewModel
import com.example.streams.databinding.ActivityActivitymyProfileBinding
import com.squareup.picasso.Picasso


class ActivitymyProfile : AppCompatActivity() {

   lateinit var activitymyProfileBinding: ActivityActivitymyProfileBinding
   lateinit var authViewModel: AuthViewModel
   lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitymyProfileBinding = ActivityActivitymyProfileBinding.inflate(layoutInflater)
        setContentView(activitymyProfileBinding.root)

        sessionManager = SessionManager()
        sessionManager.Initialize(this)
//        val extras = intent.extras
//        val byteArray = extras!!.getByteArray("image")
//
//        val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
//        activitymyProfileBinding.userprofilephoto.setImageBitmap(bmp)
//
       authViewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(
            AuthViewModel::class.java
        )

        authViewModel.getprofiledata("Bearer "+sessionManager.gettoken()!!)


        Picasso.get().load(BaseURL().getBaseUrl()+"getprofile/"+sessionManager.getId())
            .centerCrop()
            .fit()
            .into(activitymyProfileBinding.userprofilephoto)

        authViewModel.profiledata.observe(this, object : Observer<ArrayList<String>>{
            override fun onChanged(t: ArrayList<String>?) {
                if (t != null){


                    if (t.size != 0) {
                        activitymyProfileBinding.name.visibility = View.VISIBLE
                        activitymyProfileBinding.email.visibility = View.VISIBLE


                        var name = t.get(0)
                        var email = t.get(1)

                        activitymyProfileBinding.name.text = name
                        activitymyProfileBinding.email.text = email

                    }
                }
                else{

                    activitymyProfileBinding.name.visibility = View.GONE
                    activitymyProfileBinding.email.visibility= View.GONE


                }


            }

        })



        activitymyProfileBinding.btnsignout.setOnClickListener{

            sessionManager.AddId("")
            sessionManager.Addtoken("")
            sessionManager.saveUsername("")
            activitymyProfileBinding.progressbar.visibility = View.VISIBLE
            activitymyProfileBinding.btnsignout.visibility = View.GONE
            Handler().postDelayed(object :Runnable{
                override fun run() {

                    activitymyProfileBinding.progressbar.visibility = View.GONE
                    var i = Intent(this@ActivitymyProfile,MainActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                    finish()
                }


            },1500)

        }


    }
}
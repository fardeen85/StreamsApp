package com.example.streams

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.example.streams.Network.SessionManager
import com.example.streams.ViewModels.AuthViewModel
import com.example.streams.databinding.ActivityUploadPhotoBinding
import com.example.streams.databinding.ActivityUploadVideoBinding
import com.google.android.exoplayer2.MediaItem
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UploadPhoto : AppCompatActivity() {

    val authviewModel : AuthViewModel by viewModels()
    lateinit var uploadPhotoBinding: ActivityUploadPhotoBinding
    lateinit var sessionManager: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uploadPhotoBinding = ActivityUploadPhotoBinding.inflate(layoutInflater)
        setContentView(uploadPhotoBinding.root)
        sessionManager = SessionManager()

        uploadPhotoBinding.photo.setOnClickListener {

            getThumnail()
        }

        uploadPhotoBinding.btnuploadphoto.setOnClickListener{

            if(authviewModel.photouri.value != null){

                uploadprofile()
            }
            else{

                Toast.makeText(this,"Please Select a profile picture from gallery",
                    Toast.LENGTH_SHORT).show()
            }

    }

        sessionManager.Initialize(this)

        authviewModel.profileloading.observe(this,object :Observer<Boolean>{
            override fun onChanged(t: Boolean?) {
                if (t != null){

                    if (t){
                        uploadPhotoBinding.uploadprogressbar.visibility = View.VISIBLE
                    }
                    else{

                        uploadPhotoBinding.uploadprogressbar.visibility = View.GONE
                    }

                }
            }

        })


        authviewModel.successmessagephoto.observe(this,object : Observer<Boolean>{
            override fun onChanged(t: Boolean?) {
                if (t != null){

                    if (t){

                        startActivity(Intent(this@UploadPhoto,Dashboard::class.java))
                        sessionManager.SavefirstTime("Yes")
                         finish()
                    }
                }
            }

        })

    }


    fun getThumnail(){

        var imagepickIntent = Intent(Intent.ACTION_PICK)
        imagepickIntent.setType("image/jpeg");
        startActivityForResult(Intent.createChooser(imagepickIntent, "Please pick a Thumbnail"),7);
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)



        if (requestCode == 7 && resultCode == RESULT_OK && data!!.data != null){

            authviewModel.photouri.value = data!!.data
            uploadPhotoBinding.photo.setImageURI(authviewModel.photouri.value!!)


        }


    }

    fun getRealPathFromUri(context: Context, contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.getContentResolver().query(contentUri!!, proj, null, null, null)
            val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
    }

    fun uploadprofile(){


        var imagefile: File? = null

        try {

            imagefile = File(getRealPathFromUri(this,authviewModel.photouri.value)!!)
        }
        catch (e : Exception){

            Log.d("Error",e.message.toString())
        }


        var photobody = RequestBody.create("image/jpeg".toMediaTypeOrNull(),imagefile!!)
        var id = RequestBody.create("text/plain".toMediaTypeOrNull(),sessionManager.getId().toString())
        var uploadphoto =
            MultipartBody.Part.createFormData("thumbnailpath", imagefile.name,photobody)
        var token = sessionManager.gettoken()

        authviewModel.UploadProfile(uploadphoto,id,"Bearer "+token!!)


    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
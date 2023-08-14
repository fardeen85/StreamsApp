package com.example.streams

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.streams.Network.SessionManager
import com.example.streams.ViewModels.VideoViewModel
import com.example.streams.databinding.ActivityUploadVideoBinding
import com.example.streams.services.uploadVideoService
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class ActivityUploadVideo : AppCompatActivity() {

    lateinit var simpleExoPlayer: SimpleExoPlayer
    var videouri : Uri? = null
    var imageuri : Uri? = null

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    lateinit var videoViewModl : VideoViewModel
    var ID : String = ""
    lateinit var pd : ProgressDialog
    lateinit var sessionManager: SessionManager
    var receiver: receiveUpdate = receiveUpdate()





    public lateinit var uploadVideoBinding : ActivityUploadVideoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uploadVideoBinding = ActivityUploadVideoBinding.inflate(layoutInflater)
        setContentView(uploadVideoBinding.root)

        val filter = IntentFilter()
        filter.addAction("progressreciver")
        registerReceiver(receiver,filter)

        videoViewModl = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(
            VideoViewModel::class.java
        )

        simpleExoPlayer = SimpleExoPlayer.Builder(this).build()


        //initialize dialog
        pd = ProgressDialog(this@ActivityUploadVideo)
        pd.setTitle("Uploading Video")
        pd.setMessage("Uploading")
        pd.setCanceledOnTouchOutside(false)
        pd.setCancelable(false)
        sessionManager = SessionManager()
        sessionManager.Initialize(this)



        getPermissions()
        uploadVideoBinding.buttonploadvideo.setOnClickListener{


          // UploadVideo()
            if (videoViewModl.videouri.value == null) {

                Toast.makeText(this,"Plsease select a video to upload",Toast.LENGTH_SHORT).show()
            }

            else if (videoViewModl.imageuri.value == null){

                Toast.makeText(this,"Plsease select a image to upload",Toast.LENGTH_SHORT).show()

            }
            else if (uploadVideoBinding.editextvideoname.text.toString() == ""){

                Toast.makeText(this,"Video name is required",Toast.LENGTH_SHORT).show()

            }
            else {

                val i = Intent(this, uploadVideoService::class.java)
                i.putExtra("name", uploadVideoBinding.editextvideoname.text.toString()+" by ${sessionManager.getUsername()}")
                i.putExtra("imageuri", videoViewModl.imageuri.value)
                i.putExtra("videouri", videoViewModl.videouri.value)
                startService(i)
                uploadVideoBinding.buttonploadvideo.visibility = View.GONE
                uploadVideoBinding.progressBar5.visibility = View.VISIBLE
            }




        }


        uploadVideoBinding.buttongetvideo.setOnClickListener {

            getVideoFromStorage()

        }

        uploadVideoBinding.btnuploadthubmnail.setOnClickListener{


            getThumnail()
        }


        videoViewModl.uploadProgress.observe(this,object :Observer<String>{
            override fun onChanged(t: String?) {

                if (t != null){


                    if (t.equals("Half")) uploadVideoBinding.progressBar5.setProgress(50)
                    else if (t.equals("Error")) uploadVideoBinding.progressBar5.setProgress(10)
                    else if (t.equals("Full")) uploadVideoBinding.progressBar5.setProgress(100)
                }

            }

        })


        videoViewModl.id.observe(this,object :Observer<String>{
            override fun onChanged(t: String?) {

                if (t != null){


                    ID = t
                    Log.d("Id",t)



                    Handler().postDelayed(object : Runnable{
                        override fun run() {


                            uploadImage(ID)
                            pd.dismiss()
                        }

                    },200)

                }
            }


        })

        videoViewModl.isloading.observe(this,object : Observer<Boolean>{
            override fun onChanged(t: Boolean?) {

                if (t != null){


                    if (t){
                        uploadVideoBinding.buttongetvideo.visibility  = View.GONE
                        uploadVideoBinding.btnuploadthubmnail.visibility = View.GONE
                        uploadVideoBinding.buttonploadvideo.visibility = View.GONE
                        uploadVideoBinding.progressBar3.visibility = View.VISIBLE
                    }

                    else{

                        uploadVideoBinding.buttongetvideo.visibility  = View.VISIBLE
                        uploadVideoBinding.btnuploadthubmnail.visibility = View.VISIBLE
                        uploadVideoBinding.buttonploadvideo.visibility = View.VISIBLE
                        uploadVideoBinding.progressBar3.visibility = View.GONE

                    }
                }
            }

        })



    }


     fun getPermissions(){

         var permission  =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE
                ,Manifest.permission.WRITE_EXTERNAL_STORAGE)




         if (ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
             ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
             ActivityCompat.checkSelfPermission(this,Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){




        }
         else{
                ActivityCompat.requestPermissions(this,permission,3)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){

                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE),3);
                }
         }




    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 3){

            for (i in permissions.indices){

                if (grantResults[i] == PackageManager.PERMISSION_DENIED ){

                    requestPermissions(permissions,3)
                }
            }
        }
    }


    fun getVideoFromStorage(){



        var videopickIntent =Intent(Intent.ACTION_PICK)
        videopickIntent.setType("video/*");
        videopickIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(Intent.createChooser(videopickIntent, "Please pick a video"),6);



    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 6 && resultCode == RESULT_OK && data!!.data != null){

            videoViewModl.videouri.value = data!!.data
            var mediaItem = MediaItem.fromUri(videoViewModl.videouri.value!!);
            simpleExoPlayer.addMediaItem(mediaItem)
            initializePlayer()
            simpleExoPlayer.play()

        }


        if (requestCode == 7 && resultCode == RESULT_OK && data!!.data != null){

            videoViewModl.imageuri.value = data!!.data
            uploadVideoBinding.imageView9.setImageURI(videoViewModl.imageuri.value!!)


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


    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else null
    }

    fun getImagePath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else null
    }


    //Exoplayer Methods


    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,uploadVideoBinding.playerView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

//    public override fun onStart() {
//        super.onStart()
//        if (Util.SDK_INT > 23) {
//            initializePlayer()
//        }
//    }
//
//    public override fun onResume() {
//        super.onResume()
//        hideSystemUi()
//        if ((Util.SDK_INT <= 23 || uploadVideoBinding.playerView.player == null)) {
//            initializePlayer()
//        }
//    }
//
//    public override fun onPause() {
//        super.onPause()
//        if (Util.SDK_INT <= 23) {
//            releasePlayer()
//        }
//    }


    public override fun onStop() {
        super.onStop()

    }


    override fun onDestroy() {
        super.onDestroy()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
        unregisterReceiver(receiver)
    }

    private fun releasePlayer() {
        uploadVideoBinding.playerView.player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        uploadVideoBinding.playerView.player = null
    }

    private fun initializePlayer() {

        uploadVideoBinding.playerView.player = simpleExoPlayer
        simpleExoPlayer.playWhenReady = playWhenReady
        simpleExoPlayer.seekTo(currentItem, playbackPosition)
        simpleExoPlayer.prepare()

    }

    fun getThumnail(){

        var imagepickIntent =Intent(Intent.ACTION_PICK)
        imagepickIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(imagepickIntent, "Please pick a Thumbnail"),7);
    }

    fun UploadVideo(){

        pd.show()

        var file = File(getPath(videoViewModl.videouri.value!!))

        var name = if (!uploadVideoBinding.editextvideoname.text.isEmpty()) uploadVideoBinding.editextvideoname.text.toString()
        else file.name



        var videobody = RequestBody.create("video/*".toMediaTypeOrNull(),file)
        var uploadvideofile = MultipartBody.Part.createFormData("video",file.name,videobody)
        var Name = RequestBody.create("text/plain".toMediaTypeOrNull(),name)
        var token = sessionManager.gettoken()

         videoViewModl.UploadVideo(Name,uploadvideofile,"Bearer "+token)





    }

    fun  uploadImage(id:String) {

        if (videoViewModl.imageuri.value != null) {

            var imagefile: File? = null

            try {

                imagefile = File(getRealPathFromUri(this, videoViewModl.imageuri.value)!!)
            } catch (e: Exception) {

                Log.d("Error", e.message.toString())
            }

            if (imagefile != null) {

                var imagebody = RequestBody.create("image/*".toMediaTypeOrNull(), imagefile)
                var uploadimagefile =
                    MultipartBody.Part.createFormData("thumbnailpath", imagefile.name, imagebody)
                var imagename = RequestBody.create("text/plain".toMediaTypeOrNull(), imagefile.name)
                var ID = RequestBody.create("text/plain".toMediaTypeOrNull(), id)


                var token =
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2MzI0ZDNjZThmMWViZjUyNjg1M2I1OTAiLCJlbWFpbCI6ImZhcmRlZW5zYWxlZW01QGdtYWlsLmNvbSIsInVzZXJuYW1lIjoiRmFyZGVlbiIsImlhdCI6MTY2NDEyODkyMCwiZXhwIjoxNjk1NjY0OTIwfQ.-KPQ1C2uT5biHatUhglozxp6GOV1vjBoVg0XyW_YbGo"
                videoViewModl.UploadImage(imagename, uploadimagefile, "Bearer " + token, ID)
            } else {

                Toast.makeText(this, "Failed to upload Image", Toast.LENGTH_SHORT).show()

            }
        }
        else{

            Toast.makeText(this,"Image not selected",Toast.LENGTH_SHORT).show()
        }
    }



inner class receiveUpdate : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {


        if(p1!!.getAction().equals("progressreciver")){

            if (p1!!.extras!!.get("progress").toString().equals("20")){
                uploadVideoBinding.progressBar3.setProgress(20)

            }
            else if (p1!!.extras!!.get("progress").toString().equals("50")){

                uploadVideoBinding.progressBar3.setProgress(50)

            }

            else if (p1!!.extras!!.get("progress").toString().equals("70")){
                uploadVideoBinding.progressBar3.setProgress(70)


            }

            else if (p1!!.extras!!.get("progress").toString().equals("100")){

                uploadVideoBinding.progressBar3.setProgress(100)
                uploadVideoBinding.buttonploadvideo.visibility = View.VISIBLE
                uploadVideoBinding.progressBar5.visibility = View.GONE
                finish()


            }

            else if (p1!!.extras!!.get("progress").toString().equals("errorvideo")){

                Toast.makeText(this@ActivityUploadVideo,"",Toast.LENGTH_SHORT).show()
                uploadVideoBinding.buttonploadvideo.visibility = View.VISIBLE
                uploadVideoBinding.progressBar5.visibility = View.GONE

            }

            else if (p1!!.extras!!.get("progress").toString().equals("errorimage")){

                Toast.makeText(this@ActivityUploadVideo,"",Toast.LENGTH_SHORT).show()
                uploadVideoBinding.buttonploadvideo.visibility = View.VISIBLE
                uploadVideoBinding.progressBar5.visibility = View.GONE

            }
        }

    }

}





}
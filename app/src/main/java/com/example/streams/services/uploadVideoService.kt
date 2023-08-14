package com.example.streams.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.streams.MainActivity
import com.example.streams.Network.RetrofitClient
import com.example.streams.Network.SessionManager
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File

class uploadVideoService : Service() {



    lateinit var builder : androidx.core.app.NotificationCompat.Builder
    var  intent = Intent("progressreciver");
    lateinit var sessionManager: SessionManager

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("service","started")
        builder = androidx.core.app.NotificationCompat.Builder(this)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {



        if (!intent!!.hasExtra("name") ){

            stopSelf()

        }

        else if (!intent.hasExtra("videouri")){


            stopSelf()
        }
        else if (!intent.hasExtra("imageuri")){

            stopSelf()
        }
        else {

            var name = intent?.extras!!.getString("name")
            var videouri: Uri? = intent?.extras!!.get("videouri") as Uri
            var imageuri: Uri? = intent?.extras!!.get("imageuri") as Uri
            sessionManager = SessionManager()
            sessionManager.Initialize(this)

            UploadVideo(name!!, videouri!!, imageuri!!)
            LoadingNotification()


            intent.putExtra("progress", "20");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        }

        startForeground(1,builder.build())

        return START_STICKY





    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("service","ended")
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

    fun  uploadImage(id:String,imageuri:Uri?) {

        var imagefile: File? = null

        try {

            imagefile = File(getRealPathFromUri(this,imageuri)!!)
        }
        catch (e : Exception){

            Log.d("Error",e.message.toString())
        }

        if (imagefile != null) {

            var imagebody = RequestBody.create("image/*".toMediaTypeOrNull(), imagefile)
            var uploadimagefile =
                MultipartBody.Part.createFormData("thumbnailpath", imagefile.name, imagebody)
            var imagename = RequestBody.create("text/plain".toMediaTypeOrNull(), imagefile.name)
            var ID = RequestBody.create("text/plain".toMediaTypeOrNull(), id)


            var token = sessionManager.gettoken()

            var uploadImage : retrofit2.Call<JsonObject> = RetrofitClient().retrofitinstance().uploadImage(imagename,ID,uploadimagefile,"Bearer "+token)
            uploadImage.enqueue(object :retrofit2.Callback<JsonObject>{
                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    if (response.isSuccessful){

                        Toast.makeText(this@uploadVideoService,"Image Updated Sucessfully",Toast.LENGTH_SHORT).show()
                        intent.putExtra("progress","100");
                        sendBroadcast(intent);
                        stopSelf()
                    }
                }

                override fun onFailure(call: retrofit2.Call<JsonObject>, t: Throwable) {
                    Toast.makeText(this@uploadVideoService,"An error occured",Toast.LENGTH_SHORT).show()
                    Log.d("Error",t.message.toString())
                    intent.putExtra("progress","errorimage");
                    sendBroadcast(intent);
                    stopSelf()
                }

            })
        }
        else{

            Toast.makeText(this,"Failed to upload Image", Toast.LENGTH_SHORT).show()
            stopSelf()


        }
    }

    fun UploadVideo(name: String, videoUri: Uri, imageuri: Uri){




        var file = File(getPath(videoUri))

        var name = if (!name.isEmpty())name
        else file.name



        var videobody = RequestBody.create("video/*".toMediaTypeOrNull(),file)
        var uploadvideofile = MultipartBody.Part.createFormData("video",file.name,videobody)
        var Name = RequestBody.create("text/plain".toMediaTypeOrNull(),name)
        var token =sessionManager.gettoken()
        intent.putExtra("progress","50");
       sendBroadcast(intent);

//        videoViewModl.UploadVideo(Name,uploadvideofile,"Bearer "+token)
        var uploadvideo : retrofit2.Call<JsonObject> = RetrofitClient().retrofitinstance().UploadVideo(Name,uploadvideofile,"Bearer "+token)
        uploadvideo.enqueue(object : retrofit2.Callback<JsonObject>{
            override fun onResponse(
                call: retrofit2.Call<JsonObject>,
                response: Response<JsonObject>
            ) {


                if (response.isSuccessful){

                    var data = response.body()!!.getAsJsonObject()
                    var id = data!!.get("id").asString
                    intent.putExtra("progress","70");
                    sendBroadcast(intent);
                    uploadImage(id,imageuri)




                }

            }

            override fun onFailure(call: retrofit2.Call<JsonObject>, t: Throwable) {

                Toast.makeText(this@uploadVideoService,"en error occured",Toast.LENGTH_SHORT).show()
                Log.d("Error",t.message.toString())
                intent.putExtra("progress","errorvideo");
                sendBroadcast(intent);
                stopSelf()
            }

        })





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



     fun LoadingNotification(){

         builder = androidx.core.app.NotificationCompat.Builder(this,MainActivity.channelId).apply {
            setContentTitle("Picture Download")
            setContentText("Download in progress")
                .setSmallIcon(android.R.drawable.progress_horizontal)
            setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
        }
        val PROGRESS_MAX = 100
        val PROGRESS_CURRENT = 0
        NotificationManagerCompat.from(this).apply {
            // Issue the initial notification with zero progress
            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)


          for (i in 0..PROGRESS_MAX){

              builder.setProgress(PROGRESS_MAX,i,false)

          }
        }


            // Do the job here that tracks the progress.
            // Usually, this should be in a
            // worker thread
            // To show progress, update PROGRESS_CURRENT and update the notification with:
            // builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            // notificationManager.notify(notificationId, builder.build());

            // When done, update the notification one more time to remove the progress bar
//            builder.setContentText("Download complete")
//                .setProgress(0, 0, false)

    }




}
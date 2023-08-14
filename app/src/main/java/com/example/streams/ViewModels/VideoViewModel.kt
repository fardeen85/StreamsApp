package com.example.streams.ViewModels

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streams.Models.VideoModel
import com.example.streams.Models.commentsModel
import com.example.streams.Network.RetrofitClient
import com.example.streams.Network.SessionManager
import com.example.streams.Repositories.AuthRepositories
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.Until
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class VideoViewModel(application: Application) : AndroidViewModel(application) {


    private var repo : AuthRepositories
    var videuploadresponse : MutableLiveData<JsonObject> = MutableLiveData()
    var isloading : MutableLiveData<Boolean> = MutableLiveData()
    var imageuri : MutableLiveData<Uri> = MutableLiveData()
    var videouri : MutableLiveData<Uri> = MutableLiveData()
    var datavalues : MutableLiveData<ArrayList<VideoModel>> = MutableLiveData()
    var liveip:MutableLiveData<String> = MutableLiveData()
    var seekposition:MutableLiveData<Long> = MutableLiveData()
    var uploadProgress : MutableLiveData<String> = MutableLiveData()
    var id : MutableLiveData<String> = MutableLiveData()
    var likes : MutableLiveData<String> = MutableLiveData()
    var dislikes : MutableLiveData<String> = MutableLiveData()
    var commentsdata : MutableLiveData<ArrayList<commentsModel>> = MutableLiveData()
    var iscommentdataloaded : MutableLiveData<Boolean> = MutableLiveData()
    var commentsadded : MutableLiveData<Boolean> = MutableLiveData()
    lateinit var sessionManager: SessionManager


    init {
        repo = AuthRepositories(RetrofitClient().retrofitinstance())
        sessionManager = SessionManager()
        sessionManager.Initialize(application.applicationContext)


    }


    fun UploadVideo(name:RequestBody,video:MultipartBody.Part,token:String){


        isloading.value = true

        var obj = JsonObject()

        viewModelScope.launch(Dispatchers.IO) {

            var apicall : Call<JsonObject> = repo.UploadVideo(name,video,token)
            apicall.enqueue(object : Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                    if (response.isSuccessful){

                        isloading.value = false
                        obj = response.body()?.asJsonObject!!;
                        Log.d("response", obj.toString())
                        var responsestring = response.body().toString();
                        val convertedObject: JsonObject =
                            Gson().fromJson(responsestring, JsonObject::class.java)


                            id.value = obj.get("id").asString




                        videuploadresponse.value = convertedObject;
                    }
                    else{

//                        var  responserror = response?.errorBody()?.source()?.buffer?.snapshot()?.utf8()
//                        val convertedObject: JsonObject =
//                            Gson().fromJson(responserror, JsonObject::class.java)
//                        obj = convertedObject
//                        videuploadresponse.value = convertedObject
                        Log.d("response",  response.message() )
                        isloading.value = false

                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {

                    isloading.value = false
                    Log.d("Error",t.message.toString())
                    var o = JsonObject()
                    o.addProperty("msg",t.message.toString())
                    videuploadresponse.value = o
                    uploadProgress.value = "Error"
                }

            })
        }
    }


    fun getVideos(id:String,token:String){

        isloading.value = true

        viewModelScope.launch(Dispatchers.IO){

            try {

                var apicall = repo.getvideos()
                apicall.enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {


                        if (response.isSuccessful) {

                            var list: ArrayList<VideoModel> = ArrayList()


                            var obj1: JsonObject = response.body()!!
                            var data = obj1!!.getAsJsonArray("data")
                            uploadProgress.value = "Half"
                            for (i in 0 until data.size()) {

                                var videodata = data.get(i) as JsonObject
                                var name: String = videodata.get("name").asString.toString()
                                var thumbnail = videodata.get("imagename").asString.toString()
                                var videopath = videodata.get("videopath").asString.toString()
                                var id = videodata.get("_id").asString.toString()
                                var likes = videodata.get("likes").asString.toString()
                                var dislikes = videodata.get("dislikes").asString.toString()
                                var likers = videodata.get("likers").asJsonArray
                                var owner = videodata.get("owner").asString.toString()


                                Log.d("response", name + i)


                                var m = VideoModel(
                                    thumbnail,
                                    name,
                                    videopath,
                                    id,
                                    likes,
                                    dislikes,
                                    likers,
                                    owner
                                )



                                list.add(m)

                                isloading.value = false


                            }

                            datavalues.value = list
                        } else {

//                        var  responserror = response?.errorBody()?.source()?.buffer?.snapshot()?.utf8()
//                        val convertedObject: JsonObject =
//                            Gson().fromJson(responserror, JsonObject::class.java)
//
//                        videuploadresponse.value = convertedObject
                            Log.d("response", response.message().toString())
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Log.d("Error", t.message.toString())
                        var o = JsonObject()

                        o.addProperty("msg", t.message.toString())
                        videuploadresponse.value = o
                        isloading.value = false
                    }

                })
            }
            catch (e :Exception){

                isloading.value = false
            }

        }
    }


     @Throws(Exception::class)
    fun getExternalIpAddress() {



        viewModelScope.launch {

            val whatismyip = URL("http://checkip.amazonaws.com")
            var inn : BufferedReader? = null
            try{

                inn = BufferedReader(InputStreamReader(whatismyip.openStream()))
                var ip = inn.readLine()
                liveip.value = ip

            }
            finally {

                if (inn != null){

                    try {
                        inn.close()
                    } catch (e:Exception){

                        e.printStackTrace()
                    }

                }
            }


        }



    }


    fun UploadImage(name: RequestBody,image:MultipartBody.Part,token:String,id:RequestBody){


        viewModelScope.launch(Dispatchers.IO) {



            Log.d("id",id.toString())
            var apicall  = repo.uploadImage(name,image,token,id)
            apicall.enqueue(object :Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                    if (response.isSuccessful){

                        var result = response.body();
                        Log.d("response",result?.get("msg")!!.asString)
                        uploadProgress.value = "Full"

                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {

                    Log.d("response",t.message.toString())
                    uploadProgress.value = "Error"
                }


            })
        }

    }


    fun Addlikes(id:String,token:String,owner:String){

        viewModelScope.launch(Dispatchers.IO) {

            var apicall = repo.addlikes(id,owner,token)
            apicall.enqueue(object :Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                    if (response.isSuccessful){

                        var data = response.body()
                        var msg = data!!.get("msg").asString
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {

                    if (t.message != null){

                        Log.d("error",t.message.toString())
                    }
                }


            })
        }
    }


    fun Unlikes(id:String,owner_id:String,token:String){

        viewModelScope.launch(Dispatchers.IO) {

            var apicall = repo.Unlikes(id,owner_id,token)
            apicall.enqueue(object :Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                    if (response.isSuccessful){

                        var data = response.body()
                        var msg = data!!.get("msg").asString
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {

                    if (t.message != null){

                        Log.d("error",t.message.toString())
                    }
                }


            })
        }
    }


    fun Addcomments(id:String,body:RequestBody,token:String){

        viewModelScope.launch(Dispatchers.IO) {


            try {

                var apicall = repo.Addcomments(id, body, token)
                apicall.enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        if (response.isSuccessful) {

                            var data: JsonObject = response.body()!!
                            var message = data.get("msg").asString
                            Log.d("response", message.toString())
                            commentsadded.value = true


                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {

                        Log.d("error", t.message.toString())
                        commentsadded.value = false
                    }


                })
            }
            catch (e : Exception){

                commentsadded.value = false
            }
        }
    }


    fun getComments(token:String,id: String){

        viewModelScope.launch {

            var apicall = repo.getcomments(token,id)
            var commentsarraymodel : ArrayList<commentsModel> = ArrayList()
            apicall.enqueue(object :Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                    if (response.isSuccessful){

                       var data = response.body()
                        var commentsarray = data!!.get("comments").asJsonArray


                        for (i in 0 until  commentsarray.size()){

                            var commentdata = commentsarray.get(i) as JsonObject
                            var id = commentdata.get("_id").asString
                            var comment = commentdata.get("comment").asString
                            var username = commentdata.get("username").asString
                            //var username = ""
                            var comments = commentsModel(id, comment,"", username)
                            commentsarraymodel.add(comments)


//                            commentsarraymodel.add()
                        }

                        commentsdata.value = commentsarraymodel!!
                        iscommentdataloaded.value = true



                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d("error",t.message.toString())
                    iscommentdataloaded.value = false

                }


            })

        }

    }





}
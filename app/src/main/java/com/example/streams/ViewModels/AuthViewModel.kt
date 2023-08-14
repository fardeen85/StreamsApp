package com.example.streams.ViewModels

import android.R.string
import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract.Profile
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streams.Models.ProfileModel
import com.example.streams.Network.RetrofitClient
import com.example.streams.Network.SessionManager
import com.example.streams.Repositories.AuthRepositories
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AuthViewModel(application: Application) : AndroidViewModel(application) {


    private var repo : AuthRepositories
    lateinit var sessionManager:SessionManager
    var profileloading : MutableLiveData<Boolean> = MutableLiveData()
    var errormesage : MutableLiveData<String> = MutableLiveData()
    var photouri : MutableLiveData<Uri> = MutableLiveData()
    var successmessage : MutableLiveData<Boolean> = MutableLiveData()
    var successmessagephoto : MutableLiveData<Boolean> = MutableLiveData()
    var profiledata : MutableLiveData<ArrayList<String>> = MutableLiveData()

    init {
        repo = AuthRepositories(RetrofitClient().retrofitinstance())
        sessionManager = SessionManager()
        sessionManager.Initialize(application.applicationContext)

    }





    var isloading:MutableLiveData<Boolean> = MutableLiveData()
    var sigininresponse : MutableLiveData<JsonObject> = MutableLiveData()

    public fun SignIn(email:String,password:String) : JsonObject{


        isloading.value = true

        var jsonObject = JsonObject()


            val requestBodyEmail: RequestBody = RequestBody.create("text/plain".toMediaType(), email)
            val requestBodyPass : RequestBody = RequestBody.create("text/plain".toMediaType(),password)


        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("account", email)
            .addFormDataPart("password", password)
            .build()


        var request : Call<JsonObject> =  repo.requestsignin(requestBody)
        request.enqueue(object : Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if(response.isSuccessful) {
                    isloading.value = false;



                    jsonObject = response.body()?.asJsonObject!!;
                    Log.d("response", jsonObject.toString())
                    var responsestring = response.body().toString();
                    val convertedObject: JsonObject =
                        Gson().fromJson(responsestring, JsonObject::class.java)
                    sigininresponse.value = convertedObject;
                    sessionManager.Addtoken(jsonObject.get("token").asString)
                    sessionManager.AddId(jsonObject.get("id").asString)
                    sessionManager.saveUsername(jsonObject.get("username").asString)

                }


                else{



                    var  responserror = response?.errorBody()?.source()?.buffer?.snapshot()?.utf8()
//                    val convertedObject: JsonObject =
//                        Gson().fromJson(responserror, JsonObject::class.java)
//                    jsonObject = convertedObject
//                    sigininresponse.value = convertedObject
                    Log.d("response",  responserror.toString() )
                    isloading.value = false

                }



            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {

                isloading.value = false
                Log.d("Error",t.message.toString())
            }


        })


        return jsonObject


    }


    public fun SignUp(email: String,password: String, username:String) : JsonObject{

        isloading.value = true;

        var jsonobj = JsonObject()





        viewModelScope.launch(Dispatchers.IO) {


            var mp : HashMap<String,RequestBody> = HashMap<String,RequestBody>()
            val requestBodyEmail: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), email)
            val requestBodyPass : RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),password)
            val requestBodyUsername : RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),username)

            mp.put("email",requestBodyEmail)
            mp.put("password",requestBodyPass)
            mp.put("username",requestBodyUsername)


            val requestBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("email", email)
                .addFormDataPart("password", password)
                .addFormDataPart("username",username)
                .build()


            var request : Call<JsonObject> = repo.requestsingup(requestBody)
            request.enqueue(object :Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                   if(response.isSuccessful) {
                       isloading.value = false ;
                       jsonobj = response.body()!!}

                    Log.d("Response",response.body()!!.get("msg").toString())
                    successmessage.value = true


                }



                override fun onFailure(call: Call<JsonObject>, t: Throwable) {

                    Log.d("Error",t.message.toString())
                    isloading.value = false;
                    successmessage.value = false

                }

            })

        }

        return  jsonobj
    }


    fun test(){

        var obj = JsonObject()


        viewModelScope.launch {
            obj.addProperty("test1","Ok")
            var req = repo.test(obj)
            req.enqueue(object : Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if(response.isSuccessful) {


                    }



                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d("Error",t.message.toString())
                }

            })

        }

    }

    fun UploadProfile(img : MultipartBody.Part,id:RequestBody, token:String){

        profileloading.value = true

        viewModelScope.launch(Dispatchers.IO) {



          var apicall =   repo.uploadprofile(img,id,token)
           apicall.enqueue(object :Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                    if (response.isSuccessful){

                        profileloading.value = false
                        successmessagephoto.value = true
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {

                    Log.d("Error",t.message.toString())
                    profileloading.value = true
                    errormesage.value = t.message.toString()
                    successmessagephoto.value = true

                }


            })
        }


    }



    fun getprofiledata(token:String){

        viewModelScope.launch(Dispatchers.IO) {

            var apicall : Call<JsonObject> = repo.getmyprofile(token);
            var arraylist : ArrayList<String> = ArrayList()
            apicall.enqueue(object : Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                    if (response.isSuccessful){

                        var obj = response.body()
                        var name = obj!!.get("username").asString
                        var email = obj!!.get("email").asString
                        var password = obj.get("password").asString




                        arraylist.add(name)
                        arraylist.add(email)
                        arraylist.add(password)
                        profiledata.value = arraylist


                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {

                    arraylist = ArrayList()

                }

            })
        }

    }


}



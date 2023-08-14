package com.example.streams.Network

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiDao {


    @POST("singin")
    fun Signin(@Body requestBody: RequestBody) : Call<JsonObject>


    @POST("singup")
//   fun Signup(@Part("email") email: RequestBody,@Part("password") password: RequestBody,@Part("username") username :RequestBody) : Call<JsonObject>
    fun Signup(@Body requestBody: RequestBody) : Call<JsonObject>


    @POST("test")
    fun Test(@Body data : JsonObject) : Call<JsonObject>

    @Multipart
    @POST("video")
    fun UploadVideo(@Part("name") name:RequestBody,@Part video : MultipartBody.Part,@Header("Authorization") authHeader:String) : Call<JsonObject>

//    @GET("video-data/{id}")
//    fun getallVideos(@Path("id") id:String,@Header("Authorization") authHeader:String) : Call<JsonObject>

    @GET("getAllvideos/")
    fun getallVideos() : Call<JsonObject>

    @GET("Images/{image}")
    fun getimage(@Path("image") imagename:String,@Header("Authorization") authHeader:String)   : Call<ByteArray>

    @Multipart
    @POST("image")
    fun uploadImage(@Part("imagename") imagename: RequestBody, @Part("id") id:RequestBody, @Part thumbnailpath :MultipartBody.Part,@Header("Authorization") authHeader:String) : Call<JsonObject>

   @GET("/api/getAllvideos")
   fun getvideos() : Call<JsonObject>

   @GET("/api/video-like/{id}/{ownerid}")
   fun Addlikes(@Path("id")  id:String, @Path("ownerid") owner : String,@Header("Authorization") authHeader:String) : Call<JsonObject>


   @GET("video-unlike/{id}/{owner_id}")
   fun Unlikes(@Path("id")  id:String,@Path("owner_id")  owner_id:String,@Header("Authorization") authHeader:String)  : Call<JsonObject>


   @POST("video-comment/{id}") //request body
   fun Addcomments(@Path("id") id : String,@Body body:RequestBody,@Header("Authorization") authHeader:String) : Call<JsonObject>


   @GET("allcomments/{videoid}")
   fun getcomments(@Path("videoid") videoid:String, @Header("Authorization") authHeader:String):Call<JsonObject>

   @Multipart
    @POST("uploadprofile")
    fun uploadprofile(@Part thumbnailpath:MultipartBody.Part, @Part("id") id:RequestBody, @Header("Authorization") authHeader:String):Call<JsonObject>

    @GET("myprofile")
    fun getmyprofile(@Header("Authorization") authHeader:String) : Call<JsonObject>


}


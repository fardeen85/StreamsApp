package com.example.streams.Repositories

import com.example.streams.Network.ApiDao
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AuthRepositories(val dao: ApiDao) {

    fun requestsignin(requestBody: RequestBody) = dao.Signin(requestBody)

//  suspend fun requestsingup(email: RequestBody,pasdword: RequestBody, username:RequestBody) = dao.Signup(email,pasdword,username)
    suspend fun requestsingup(requestBody: RequestBody) = dao.Signup(requestBody)

    suspend fun test(data : JsonObject) = dao.Test(data)

    suspend fun UploadVideo(name:RequestBody,video:MultipartBody.Part,auth_token:String) = dao.UploadVideo(name,video,auth_token)

    suspend fun getvideos() = dao.getallVideos()

//    suspend fun getvideos(id:String,token:String) = dao.getallVideos(id,token)

    suspend fun uploadImage(name:RequestBody,image:MultipartBody.Part,auth_token:String,id:RequestBody) = dao.uploadImage(name,id,image,auth_token)

    suspend fun getimage(image:String,token:String) = dao.getimage(image,token)

    suspend fun getvideosAll() = dao.getvideos()

    suspend fun addlikes(id:String,owner:String,token:String) = dao.Addlikes(id,owner,token)

    suspend fun Unlikes(id:String,owner_id:String,token:String) = dao.Unlikes(id,owner_id,token)
    suspend fun Addcomments(id:String,body:RequestBody,token:String) = dao.Addcomments(id,body,token)
    suspend fun getcomments(token:String,id:String) = dao.getcomments(id,token)
    suspend fun uploadprofile(profileimage : MultipartBody.Part,id:RequestBody,token:String) = dao.uploadprofile(profileimage,id,token)
    suspend fun getmyprofile(token : String) = dao.getmyprofile(token)









}
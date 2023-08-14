package com.example.streams.Models

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName
import org.json.JSONArray

class VideoModel {

    @SerializedName("imagename")
    var imagename:String? = null

    @SerializedName("name")
    var name : String? = null

    @SerializedName("videopath")
    var videopath:String? = null

    @SerializedName("_id")
    var id : String? = null

    @SerializedName("likes")
    var likes : String? = null

    @SerializedName("dislikes")
    var dislikes : String? = null

    @SerializedName("likers")
    var likers : JsonArray? = null

    @SerializedName("owner")
    var owner : String? = null




    constructor(ImageName: String?, name: String?, videopath: String?, id : String?,likes : String, dislikes : String,likers:JsonArray,Owner : String) {
        this.imagename = ImageName
        this.name = name
        this.videopath = videopath
        this.id = id
        this.likes = likes
        this.dislikes = dislikes
        this.likers = likers
        this.owner = Owner
    }



}
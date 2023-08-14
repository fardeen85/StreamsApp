package com.example.streams.Models

import com.google.gson.annotations.SerializedName

class commentsModel {

    @SerializedName("id")
    var id : String? = null

    @SerializedName("comment")
    var comment : String? = null

    @SerializedName("owner")
    var owner : String? = null

    constructor(id: String?, comment: String?, owner: String?, username: String?) {
        this.id = id
        this.comment = comment
        this.owner = owner
        this.username = username
    }

    @SerializedName("username")
    var username : String? = null

}
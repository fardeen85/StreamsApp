package com.example.streams

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.streams.Models.VideoModel
import com.example.streams.Network.BaseURL
import com.example.streams.Network.SessionManager
import com.example.streams.ViewModels.VideoViewModel
import com.example.streams.databinding.RowBinding
import com.google.gson.JsonArray
import org.json.JSONArray
import java.lang.Byte.decode
import java.security.spec.PSSParameterSpec.DEFAULT
import java.util.Base64


class VideoAdapter(var ctx:Context, var list: ArrayList<VideoModel>) : RecyclerView.Adapter<VideoAdapter.MyviewHolder>(),Filterable{

    var filterlist = ArrayList<VideoModel>()
    var oldlist = list
    lateinit var sessionManager: SessionManager

    class MyviewHolder(var rowBinding: RowBinding) : RecyclerView.ViewHolder(rowBinding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyviewHolder {

        var rowBinding = RowBinding.inflate(LayoutInflater.from(parent.context),null,false)
        return  MyviewHolder(rowBinding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyviewHolder, position: Int) {

        var model = list.get(position)
        holder.rowBinding.videoname.text = model.name
        sessionManager = SessionManager()
        sessionManager.Initialize(ctx)

        val url = GlideUrl(
            BaseURL().getBaseUrl()+"image/"+model.id, LazyHeaders.Builder()
                .addHeader("Authorization", "Bearer "+sessionManager.gettoken())
                .build()
        )

        Log.d("error",url.toString())

        Log.d("id",model.id!!)




        Glide.with(ctx).asBitmap()
            .load(url)
            .into(holder.rowBinding.videothumbnail)


        holder.rowBinding.card.setOnClickListener {



            var i = Intent(ctx,ActivityVideoStream::class.java)

            var likersarray = model.likers as JsonArray
            i.putExtra("name",model.name)
            i.putExtra("path",model.videopath)
            i.putExtra("likes",model.likes)
            i.putExtra("dislikes",model.dislikes)
            i.putExtra("id",model.id)
            i.putExtra("likers",likersarray.toString())
            i.putExtra("owner",model.owner)



            Log.d("response",model.likes.toString())
            Log.d("response",model.dislikes.toString())

            ctx.startActivity(i)


        }

    }

    override fun getItemCount(): Int {

        return  list.size
    }

    override fun getFilter(): Filter {
        return  object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {

                var results = FilterResults()
                if (constraint.toString().isEmpty() || constraint.toString().length.equals(0)) {
                    list= oldlist
                    notifyDataSetChanged()
                    Log.d("TAG", "isempty" + list.size)
                } else {
                    val resultData = ArrayList<VideoModel>()
                    for (itemModel in list) {
                        if (itemModel.name!!.toLowerCase()
                                .contains(constraint.toString().toLowerCase().trim())
                        ) {
                            resultData.add(itemModel)
                        }

                    }
                    list = resultData
                    results.values =  list
                }
                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {

                if (results!!.values == null) {
                    Log.d("TAG", "not found" + list)
                } else{

                    filterlist = results!!.values as ArrayList<VideoModel>
                    notifyDataSetChanged()
                }

            }


        }
    }
}
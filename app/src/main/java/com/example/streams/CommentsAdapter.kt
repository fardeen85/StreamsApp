package com.example.streams

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.streams.Models.commentsModel
import com.example.streams.databinding.CommentlayoutBinding

class CommentsAdapter(var commentlist : ArrayList<commentsModel>) : RecyclerView.Adapter<CommentsAdapter.Myviewholder>() {




    inner class Myviewholder(commentlayoutBinding: CommentlayoutBinding): RecyclerView.ViewHolder(commentlayoutBinding.root){


        var commentlayoutBinding = commentlayoutBinding

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Myviewholder {

        val commentlayoutBinding = CommentlayoutBinding.inflate(LayoutInflater.from(parent.context),null,false)
        return  Myviewholder(commentlayoutBinding)
    }

    override fun onBindViewHolder(holder: Myviewholder, position: Int) {

        var commentsdata = commentlist.get(position)
        holder.commentlayoutBinding.comment.setText(commentsdata.comment)
        holder.commentlayoutBinding.username.setText(commentsdata.username)

    }

    override fun getItemCount(): Int {
       return commentlist.size
    }
}
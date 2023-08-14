package com.example.streams

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.streams.Models.VideoModel
import com.example.streams.Models.commentsModel
import com.example.streams.Network.BaseURL
import com.example.streams.Network.SessionManager
import com.example.streams.ViewModels.VideoViewModel
import com.example.streams.databinding.ActivityVideoStreamBinding
import com.example.streams.databinding.CustomcontrolsBinding
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.MimeTypes
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody


class ActivityVideoStream : AppCompatActivity() {

    lateinit var simpelexoplayer : SimpleExoPlayer
    var activityVideoStreamBinding : ActivityVideoStreamBinding? = null
    lateinit var videoViewModel: VideoViewModel
    lateinit var videpathString: String
    var getip = ""

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    var customcontrolsBinding: CustomcontrolsBinding? = null
    lateinit var bandwidthMeter: DefaultBandwidthMeter.Builder
    lateinit var trackSelection: DefaultTrackSelector
    lateinit var btf : ImageView
    lateinit var adapter: VideoAdapter
    lateinit var commentadapter : CommentsAdapter
    lateinit var sessionManager: SessionManager

    var flag  = false
    var isliked = false
    var isdisliked = false
    var isexpanded = false

    var toKen = ""
    var iD = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityVideoStreamBinding = ActivityVideoStreamBinding.inflate(layoutInflater)
        setContentView(activityVideoStreamBinding!!.root)
        customcontrolsBinding = CustomcontrolsBinding.inflate(layoutInflater)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        btf = findViewById<ImageView>(R.id.btn_fullscreen)
        sessionManager = SessionManager()
        sessionManager.Initialize(this)





        var i = intent.extras
        videpathString = i!!.getString("path")!!
        var name = i!!.getString("name")!!
        var likes = i!!.getString("likes")!!
        var dislikes = i!!.getString("dislikes")!!
        var id = i!!.getString("id")!!
        var likers = i!!.getString("likers")!!
        var ownerid = i!!.getString("owner")!!
       // var token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2MzVjMThmMDQ4MTM2Y2E1NGM0ZjhiZDkiLCJlbWFpbCI6IkFtaXJraGFuQGdtYWlsLmNvbSIsInVzZXJuYW1lIjoiS2hhbkFNaXIiLCJpYXQiOjE2NjcxNjQ4NTIsImV4cCI6MTY5ODcwMDg1Mn0.b2qp4pNRIFVb4xztRQlM9qBF4jYLrZuEvukOTCbgBsQ"
         var token = sessionManager.gettoken()
        toKen = token!!
        iD = id


        if (likers.contains(ownerid)){

            activityVideoStreamBinding!!.btnlike.setImageDrawable(getDrawable(R.drawable.ic_baseline_thumb_up_pink_24))
            isliked = true;
        }



        Log.d("owner",ownerid)
        Log.d("likers",likers)
        Log.d("isliked",isliked.toString())

        activityVideoStreamBinding!!.likes!!.text = likes
        activityVideoStreamBinding!!.dislikes!!.text = dislikes

        activityVideoStreamBinding!!.videotitle!!.text = name
        videoViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(VideoViewModel::class.java)





        videoViewModel.liveip.observe(this,object :Observer<String>{
            override fun onChanged(t: String?) {

                if (t != null){

                    getip = t
                }
            }


        })

        videoViewModel.seekposition.observe(this,object : Observer<Long>{
            override fun onChanged(t: Long?) {

                if (t != null){

                    playbackPosition = t

                }
            }




        })




//        videoViewModel.commentsdata.observe(this,object :Observer<ArrayList<commentsModel>>{
//            override fun onChanged(t: ArrayList<commentsModel>?) {
//
//                if (t != null){
//
//                    commentadapter = CommentsAdapter(t)
//
//
//                }
//            }
//
//        })



        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            videoViewModel.getComments("Bearer " + token, id)

            var data: ArrayList<commentsModel> = ArrayList()
            if (videoViewModel.commentsdata.value != null) data =
                videoViewModel.commentsdata.value!!
            else data = ArrayList()
            commentadapter = CommentsAdapter(data)
            activityVideoStreamBinding!!.morevideos!!.adapter = commentadapter
        }





        setupplayer()
       // LoadMoreVideos()

        activityVideoStreamBinding!!.btnlike!!.setOnClickListener{


          // var token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2MzVjMThmMDQ4MTM2Y2E1NGM0ZjhiZDkiLCJlbWFpbCI6IkFtaXJraGFuQGdtYWlsLmNvbSIsInVzZXJuYW1lIjoiS2hhbkFNaXIiLCJpYXQiOjE2NjcxNjQ4NTIsImV4cCI6MTY5ODcwMDg1Mn0.b2qp4pNRIFVb4xztRQlM9qBF4jYLrZuEvukOTCbgBsQ"


            if (!isliked){

                isliked = true
                var drawable = resources.getDrawable(R.drawable.ic_baseline_thumb_up_pink_24)
                activityVideoStreamBinding!!.btnlike!!.setImageDrawable(drawable)
                videoViewModel.Addlikes(id,"Bearer "+token,ownerid)
                var count = Integer.parseInt(activityVideoStreamBinding!!.likes.text.toString())+1;

                activityVideoStreamBinding!!.likes.text = count.toString()



            }

            else{

                var drawable = resources.getDrawable(R.drawable.ic_baseline_thumb_up_24)
                activityVideoStreamBinding!!.btnlike!!.setImageDrawable(drawable)
                var count = Integer.parseInt(activityVideoStreamBinding!!.likes.text.toString())-1;
                activityVideoStreamBinding!!.likes.text = count.toString()
                videoViewModel.Unlikes(id,ownerid,"Bearer "+token)
                isliked = false
            }


        }

        activityVideoStreamBinding!!.dislikes.setOnClickListener {

            if (isliked){

                var drawable = resources.getDrawable(R.drawable.ic_baseline_thumb_down_pink_24)
                activityVideoStreamBinding!!.btnlike!!.setImageDrawable(drawable)
            }
            else{



            }

        }

        btf.setOnClickListener{


            if(!flag){


                customcontrolsBinding!!.btnFullscreen.setImageDrawable(resources.getDrawable(com.google.android.exoplayer2.R.drawable.exo_ic_fullscreen_exit))
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                activityVideoStreamBinding!!.playerView2.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                flag = true



            }
            else{

                activityVideoStreamBinding!!.playerView2.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                customcontrolsBinding!!.btnFullscreen.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_fullscreen_24))
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                flag = false
                activityVideoStreamBinding!!.playerView2.player!!.seekTo(playbackPosition)

            }
        }

        videoViewModel.getComments("Bearer " +toKen, iD)
        activityVideoStreamBinding!!.nocomments!!.setText("Loading..")


        videoViewModel.iscommentdataloaded.observe(this,object :Observer<Boolean>{
            override fun onChanged(t: Boolean?) {
                if (t != null){

                    if (t){




                        Handler().postDelayed(object : Runnable{
                            override fun run() {

                                var data: ArrayList<commentsModel> = ArrayList()
                                if (videoViewModel.commentsdata.value != null) data =
                                    videoViewModel.commentsdata.value!!
                                else data = ArrayList()
                                commentadapter = CommentsAdapter(data)
                                activityVideoStreamBinding!!.morevideos!!.adapter = commentadapter
                                commentadapter.notifyDataSetChanged()
                                if (commentadapter.itemCount>0) activityVideoStreamBinding!!.nocomments!!.setText("${commentadapter.itemCount} comments found")
                                else  activityVideoStreamBinding!!.nocomments!!.setText("No comments found")

                                videoViewModel.iscommentdataloaded.value = false

                                Log.d("check","adapter set")
                                activityVideoStreamBinding!!.editextcomment!!.setText("")
                                activityVideoStreamBinding!!.sendcomment.visibility = View.VISIBLE
                                activityVideoStreamBinding!!.progress!!.visibility = View.GONE
                            }


                        },5000)



                    }
                    else{

                        activityVideoStreamBinding!!.sendcomment.visibility = View.VISIBLE
                        activityVideoStreamBinding!!.progress!!.visibility = View.GONE

                    }
                }
            }


        })



        videoViewModel.commentsadded.observe(this,object : Observer<Boolean>{
            override fun onChanged(t: Boolean?) {

                if (t != null){

                    if (t){

                        videoViewModel.getComments("Bearer "+token,id)
                    }
                    else{

                        Toast.makeText(this@ActivityVideoStream,"Failed to Add comment",Toast.LENGTH_SHORT).show()
                        activityVideoStreamBinding!!.sendcomment.visibility = View.VISIBLE
                        activityVideoStreamBinding!!.progress!!.visibility = View.GONE
                    }
                }
            }


        })


//        activityVideoStreamBinding!!.hideshowcomments!!.setOnClickListener {
//
//
//
//            if (!isexpanded) {
//
//                videoViewModel.getComments("Bearer "+token,id)
//                activityVideoStreamBinding!!.hideshowcomments!!.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
//
//                Handler().postDelayed(object :Runnable{
//                    override fun run() {
//                        isexpanded = true
//                        commentadapter = CommentsAdapter(videoViewModel.commentsdata.value!!)
//                        activityVideoStreamBinding!!.morevideos!!.adapter = commentadapter
//                    }
//
//
//                },1500)
//
//            }
//            else{
//
//                activityVideoStreamBinding!!.morevideos!!.visibility = View.GONE
//                Handler().postDelayed(object : Runnable{
//                    override fun run() {
//                        activityVideoStreamBinding!!.hideshowcomments!!.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
//                        isexpanded = false
//                        activityVideoStreamBinding!!.morevideos!!.adapter = adapter
//                    }
//
//                },1500)
//            }
//
//        }

        activityVideoStreamBinding!!.sendcomment!!.setOnClickListener {




            var comment = activityVideoStreamBinding!!.editextcomment!!.text.toString()
            var OwnerID = RequestBody.create("text/plain".toMediaTypeOrNull(),ownerid)
            var Id = RequestBody.create("text/plain".toMediaTypeOrNull(),id)
            var Comment = RequestBody.create("text/plain".toMediaTypeOrNull(),comment)


            val requestBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("video_id", id)
                .addFormDataPart("owner",ownerid)
                .addFormDataPart("comment", comment)
                .build()


            videoViewModel.Addcomments(ownerid,requestBody,"Bearer "+token)


                    activityVideoStreamBinding!!.nocomments!!.setText("Refreshing")
                    activityVideoStreamBinding!!.sendcomment.visibility = View.GONE
                    activityVideoStreamBinding!!.progress!!.visibility = View.VISIBLE


//                    Handler().postDelayed(object : Runnable{
//                        override fun run() {
//
//                            commentadapter = CommentsAdapter(videoViewModel.commentsdata.value!!)
//                            activityVideoStreamBinding!!.morevideos!!.adapter = commentadapter
//
//                            commentadapter.notifyDataSetChanged()
//                            if (commentadapter.itemCount>0) activityVideoStreamBinding!!.nocomments!!.setText("${commentadapter.itemCount} comments found")
//                            else  activityVideoStreamBinding!!.nocomments!!.setText("No comments found")
//                        }
//
//
//
//                    },5000)


        }




    }







    fun setupplayer(){


        val httpDataSource = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
        val datasource = DefaultDataSourceFactory(this,httpDataSource)


        bandwidthMeter  = DefaultBandwidthMeter.Builder(this)


        var loadcontrolls = DefaultLoadControl()
        val trackSelector: TrackSelector =
            DefaultTrackSelector(this,AdaptiveTrackSelection.Factory())

        simpelexoplayer = SimpleExoPlayer.Builder(this).setSeekBackIncrementMs(1000).setSeekForwardIncrementMs(1000).setTrackSelector(trackSelector).setLoadControl(loadcontrolls).setMediaSourceFactory(
            DefaultMediaSourceFactory(datasource)
        ).build()




        simpelexoplayer.addListener(playerlistner)
        activityVideoStreamBinding!!.playerView2.player = simpelexoplayer
        val url = Uri.parse(BaseURL().getBaseUrl()+"video/"+videpathString)

        val mediaItem = MediaItem.Builder().setUri(url).setMimeType(MimeTypes.APPLICATION_MP4)



        val mediasource = ProgressiveMediaSource.Factory(datasource).createMediaSource(mediaItem.build())


        Log.d("TAG",BaseURL().getBaseUrl()+"video/"+videpathString+"/"+"182.189.255.236")
        simpelexoplayer.setMediaSource(mediasource)
        simpelexoplayer.prepare()
        simpelexoplayer.playWhenReady = true
        simpelexoplayer.seekTo(currentItem, playbackPosition)

        simpelexoplayer.play()
        videoViewModel.seekposition.value = activityVideoStreamBinding!!.playerView2.player!!.currentPosition




    }




    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {


        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, activityVideoStreamBinding!!.playerView2).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        val constraint = ConstraintSet()
//        constraint.connect(activityVideoStreamBinding.playerView2.id,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,0)
//        constraint.connect(activityVideoStreamBinding.playerView2.id,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM,0)
//        constraint.connect(activityVideoStreamBinding.playerView2.id,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0)
//        constraint.connect(activityVideoStreamBinding.playerView2.id,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,0)
//        constraint.applyTo(activityVideoStreamBinding.constrainRootLayout)
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
//
//            hideSystemUi()
//        }
//        else{
//            layoutParams.dimensionRatio = "16:9"
//            window.decorView.requestLayout()
//
//        }
//
//
//
//
//
//
//    }

    private fun showSystemUI(){

        actionBar?.show()
        window.decorView.systemUiVisibility =(

                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN


                )

        
    }




    private fun hideUILandscape(){
        actionBar?.hide()
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN

                )

        actionBar?.hide()
        window.decorView

    }



    public override fun onPause() {
        super.onPause()
        simpelexoplayer.pause()
        simpelexoplayer.playWhenReady = false
    }


    public override fun onStop() {
        super.onStop()
        simpelexoplayer.pause()
        simpelexoplayer.playWhenReady = false
    }


    override fun onDestroy() {
        super.onDestroy()
        simpelexoplayer.stop()
        simpelexoplayer.clearMediaItems()
        simpelexoplayer.removeListener(playerlistner)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)



    }

    private  val playerlistner = object :Player.Listener{

        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()
            activityVideoStreamBinding!!.playerView2.useController = true

        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Toast.makeText(this@ActivityVideoStream,"${error.errorCode}", Toast.LENGTH_SHORT).show()
        }
    }




    private fun releasePlayer() {
        activityVideoStreamBinding!!.playerView2.player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        activityVideoStreamBinding!!.playerView2.player = null
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong("playbackposition", Math.max(0, activityVideoStreamBinding!!.playerView2.player!!.getCurrentPosition()));
        outState.putBoolean("playwhenready",activityVideoStreamBinding!!.playerView2.player!!.playWhenReady)
        outState.putBoolean("playOrpaused",activityVideoStreamBinding!!.playerView2.player!!.isPlaying)
        outState.putBoolean("flag",flag)
        super.onSaveInstanceState(outState)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        playbackPosition = savedInstanceState.getLong("playbackposition")
        var playwhenread = savedInstanceState.getBoolean("playwhenready")
        var isplaying = savedInstanceState.getBoolean("playOrpaused")
        flag = savedInstanceState.getBoolean("flag")
        super.onRestoreInstanceState(savedInstanceState)
        Log.d("playpos",playbackPosition.toString())
        activityVideoStreamBinding!!.playerView2.player!!.playWhenReady = playWhenReady

        if (!isplaying){

            activityVideoStreamBinding!!.playerView2.player!!.pause()
        }
        activityVideoStreamBinding!!.playerView2.player!!.seekTo(playbackPosition)




    }



    fun LoadMoreVideos(){

        var list : ArrayList<VideoModel> = ArrayList()

        videoViewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(VideoViewModel::class.java)


        var token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2MzVjMThmMDQ4MTM2Y2E1NGM0ZjhiZDkiLCJlbWFpbCI6IkFtaXJraGFuQGdtYWlsLmNvbSIsInVzZXJuYW1lIjoiS2hhbkFNaXIiLCJpYXQiOjE2NjcxNjQ4NTIsImV4cCI6MTY5ODcwMDg1Mn0.b2qp4pNRIFVb4xztRQlM9qBF4jYLrZuEvukOTCbgBsQ"
        videoViewModel.getVideos("6383d25a3ae4e8f155a172cc","Bearer "+token)



        videoViewModel.datavalues.observe(this, object : Observer<ArrayList<VideoModel>>{
            override fun onChanged(t: ArrayList<VideoModel>?) {

                if (t != null){
                    adapter = VideoAdapter(this@ActivityVideoStream,t)

                    if (activityVideoStreamBinding?.morevideos != null) {
                        activityVideoStreamBinding!!.morevideos!!.adapter = adapter
                    }


                }
            }


        })



        videoViewModel.isloading.observe(this,object : Observer<Boolean>{
            override fun onChanged(t: Boolean?) {

                if (t != null) {

                    if (activityVideoStreamBinding?.progressBar6 != null) {

                        if (t) {

                            activityVideoStreamBinding!!.progressBar6!!.visibility = View.VISIBLE

                        } else {

                            activityVideoStreamBinding!!.progressBar6!!.visibility = View.GONE
                        }
                    }
                }
            }


        })

    }






}
package com.example.streams


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnCloseListener
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.droidnet.DroidListener
import com.droidnet.DroidNet
import com.example.streams.Models.VideoModel
import com.example.streams.Network.BaseURL
import com.example.streams.Network.SessionManager
import com.example.streams.ViewModels.VideoViewModel
import com.example.streams.databinding.ActivityDashboardBinding
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream


class Dashboard : AppCompatActivity(),DroidListener{

    lateinit var dashboardBinding: ActivityDashboardBinding
    lateinit var videoViewModel: VideoViewModel
    lateinit var adapter: VideoAdapter
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var b : Button
    lateinit var mDroidNet : DroidNet



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashboardBinding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(dashboardBinding.root)

        DroidNet.init(this)
        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);

        dashboardBinding.progressBar4.visibility = View.GONE
        var sessionManager = SessionManager()
        sessionManager.Initialize(this)
        swipeRefreshLayout = dashboardBinding.swipe


        var list : ArrayList<VideoModel> = ArrayList()

        videoViewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(VideoViewModel::class.java)

        val url = GlideUrl(
            BaseURL().getBaseUrl()+"getprofile/"+sessionManager.getId(), LazyHeaders.Builder()
                .addHeader("Authorization", "Bearer "+sessionManager.gettoken())
                .build()
        )

        var token = sessionManager.gettoken()
        videoViewModel.getVideos(sessionManager.getId()!!,"Bearer "+token) // id token not used







//        Glide.with(dashboardBinding.userprofilephoto.context).asBitmap()
//            .load(url)
//            .placeholder(R.drawable.ic_launcher_background)
//            .error(android.R.color.background_dark)
//            .into(dashboardBinding.userprofilephoto)

        Picasso.get().load(BaseURL().getBaseUrl()+"getprofile/"+sessionManager.getId())
            .centerCrop()
            .fit()
            .into(dashboardBinding.userprofilephoto)

        Log.d("profileUrl",url.toString())


        LoadVideos()


        videoViewModel.isloading.observe(this,object : Observer<Boolean>{
            override fun onChanged(t: Boolean?) {

                if (t != null){

                    if (t){

                        dashboardBinding.progressBar4.visibility = View.VISIBLE

                    }
                    else{

                        dashboardBinding.progressBar4.visibility = View.GONE
                        swipeRefreshLayout.isRefreshing = false
                    }
                }
            }


        })


        videoViewModel.videuploadresponse.observe(this,object :Observer<JsonObject>{
            override fun onChanged(t: JsonObject?) {

                if (t != null){

                    Toast.makeText(this@Dashboard,t.get("msg").asString.toString(),Toast.LENGTH_SHORT).show()

                    if (t.get("msg").toString().contains("failed") || t.get("msg").toString().contains("Failed") ){

                        dashboardBinding.frame.visibility = View.GONE
                        dashboardBinding.errofFrame.visibility = View.VISIBLE
                        dashboardBinding.animationView2.playAnimation()
                        dashboardBinding.animationView3.visibility = View.GONE

                    }

                    else{

                        dashboardBinding.frame.visibility = View.VISIBLE
                        dashboardBinding.errofFrame.visibility = View.GONE
                        dashboardBinding.animationView3.visibility = View.GONE

                    }

                    if (swipeRefreshLayout.isRefreshing){

                        swipeRefreshLayout.isRefreshing = false
                    }
                }
            }


        })


        initializeSearch()


        dashboardBinding.addvideo.setOnClickListener{

            startActivity(Intent(this@Dashboard,ActivityUploadVideo::class.java))
            overridePendingTransition(androidx.appcompat.R.anim.abc_popup_enter,
                androidx.appcompat.R.anim.abc_popup_exit)
        }


        swipeRefreshLayout.setOnRefreshListener(object :OnRefreshListener{
            override fun onRefresh() {

                videoViewModel.getVideos(sessionManager.getId()!!,sessionManager.gettoken()!!)
            }


        })

        dashboardBinding.notifications.setOnClickListener {

            setupNotifications()
        }

        dashboardBinding.userprofilephoto.setOnClickListener {

            myprofile()
        }

    }


    fun initializeSearch(){


        dashboardBinding.searchvideos.setOnSearchClickListener {

            dashboardBinding.textView5.layoutParams.width = 140
            it.layoutParams.width = 250
        }

        dashboardBinding.searchvideos.setOnCloseListener(object : OnCloseListener{
            override fun onClose(): Boolean {

                dashboardBinding.textView5.layoutParams.width = 360
                dashboardBinding.searchvideos.layoutParams.width = 50
                return false
            }

        })
        dashboardBinding.searchvideos.setOnQueryTextListener(object : OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                adapter.filter!!.filter(newText)
                dashboardBinding.videosrecycler.scrollToPosition(0)
                return false
            }

        })





    }

    override fun onStart() {
        super.onStart()
        var token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2MzVjMThmMDQ4MTM2Y2E1NGM0ZjhiZDkiLCJlbWFpbCI6IkFtaXJraGFuQGdtYWlsLmNvbSIsInVzZXJuYW1lIjoiS2hhbkFNaXIiLCJpYXQiOjE2NjcxNjQ4NTIsImV4cCI6MTY5ODcwMDg1Mn0.b2qp4pNRIFVb4xztRQlM9qBF4jYLrZuEvukOTCbgBsQ"
        videoViewModel.getVideos("6383d25a3ae4e8f155a172cc","Bearer "+token)

        videoViewModel.datavalues.observe(this, object : Observer<ArrayList<VideoModel>>{
            override fun onChanged(t: ArrayList<VideoModel>?) {

                if (t != null){
                    adapter = VideoAdapter(this@Dashboard,t)
                    dashboardBinding.videosrecycler.adapter = adapter


                }
            }


        })

    }

    fun LoadVideos(){


        videoViewModel.datavalues.observe(this, object : Observer<ArrayList<VideoModel>>{
            override fun onChanged(t: ArrayList<VideoModel>?) {

                if (t != null){

                    if (t.size != 0) {
                        dashboardBinding.animationView3.visibility = View.GONE
                        adapter = VideoAdapter(this@Dashboard, t)
                        dashboardBinding.videosrecycler.adapter = adapter
                        if (swipeRefreshLayout.isRefreshing) {
                            swipeRefreshLayout.isRefreshing = false

                        }
                    }
                    else{

                        dashboardBinding.animationView3.visibility = View.VISIBLE
                        dashboardBinding.progressBar4.visibility = View.GONE

                    }


                }
                else{
                   if (swipeRefreshLayout.isRefreshing){
                       swipeRefreshLayout.isRefreshing = false

                   }

                }
            }


        })
    }


    fun setupNotifications(){


        dashboardBinding.notifications.setOnClickListener {

            startActivity(Intent(this,NotificationsScreen::class.java))

        }
    }


    fun myprofile(){


        val bmp = (dashboardBinding.userprofilephoto.getDrawable() as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()

        val intent = Intent(this, ActivitymyProfile::class.java)
        intent.putExtra("image",byteArray)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, dashboardBinding.userprofilephoto, "profile")
        startActivity(intent, options.toBundle())

    }

    override fun onInternetConnectivityChanged(isConnected: Boolean) {

        if (isConnected){


            var sessionManager = SessionManager()
            sessionManager.Initialize(this)
            dashboardBinding.frame.visibility = View.VISIBLE
            dashboardBinding.animationView.visibility = View.GONE
            Log.d("TAG","internet yes")
            videoViewModel.getVideos(sessionManager.getId()!!,sessionManager.gettoken()!!)

        }
        else{


            Log.d("TAG","no internet")
            dashboardBinding.frame.visibility = View.GONE
            dashboardBinding.animationView.visibility = View.VISIBLE
            dashboardBinding.errofFrame.visibility = View.GONE
            dashboardBinding.animationView3.visibility = View.GONE



        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mDroidNet.removeInternetConnectivityChangeListener(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(androidx.appcompat.R.anim.abc_popup_enter, androidx.appcompat.R.anim.abc_popup_exit);
        finishAffinity()
    }







}
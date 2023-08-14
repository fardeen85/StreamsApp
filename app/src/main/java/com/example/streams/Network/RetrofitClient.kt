package com.example.streams.Network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {

    val interceptor = HttpLoggingInterceptor()
    val okHttpClient = OkHttpClient.Builder().addInterceptor(interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)).build()
    val retrofitclient = Retrofit.Builder()
        .baseUrl(BaseURL().getBaseUrl())
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    public fun retrofitinstance() : ApiDao{

        return  retrofitclient.create(ApiDao::class.java)
    }
}
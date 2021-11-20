package com.marblevhs.clairsavedimages.network


import com.marblevhs.clairsavedimages.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitProvider {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getRetrofit(): Retrofit{
        return retrofit
    }


    companion object{
        fun newInstance() = RetrofitProvider()
    }
}
package com.hachathon.presentationHelper.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hachathon.presentationHelper.R
import com.hachathon.presentationHelper.databinding.ActivityMainBinding
import com.hachathon.presentationHelper.edit.EditActivity
import com.hachathon.presentationHelper.init.MyPageActivity
import com.hachathon.presentationHelper.main.data.MainDataResponse
import com.hachathon.presentationHelper.main.data.MainDataResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var listAdapter: MainListAdapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        binding.mainProfileBtn.setOnClickListener {
            val mIntent = Intent(this@MainActivity, MyPageActivity::class.java)
            startActivity(mIntent)
        }

        listAdapter = MainListAdapter()
        binding.mainRecycler.adapter = listAdapter

        binding.mainFloatBtn.setOnClickListener {
            val mIntent = Intent(this, EditActivity::class.java)
            startActivity(mIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        mainListAPI()
    }

    private fun mainListAPI() {
        // 서버 주소
        val serverAddress = getString(R.string.serverAddress)

        val retrofit = Retrofit.Builder()
            .baseUrl(serverAddress)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val mainService = retrofit.create(MainService::class.java)
        val accessToken = sharedPreferences.getString("TOKEN", null)!!

        // 로그인 API 호출
        mainService.getMainList(token = accessToken)
            .enqueue(object : Callback<MainDataResponse> {
                override fun onResponse(
                    call: Call<MainDataResponse>,
                    response: Response<MainDataResponse>,
                ) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        // 서버 응답 처리 로직 작성
                        if (loginResponse?.status == "success") {
                            val tempList = arrayListOf<MainDataResult>()
                            for (item in loginResponse.data) {
                                tempList.add(item)
                            }
                            listAdapter.submitList(tempList)
                        }
                    } else {
                        Log.e("MAIN", "[MAIN] API 호출 실패 - 응답 코드: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<MainDataResponse>, t: Throwable) {
                    // 네트워크 연결 실패 등 호출 실패 시 처리 로직
                    Log.e("MAIN", "[MAIN] API 호출 실패 - 네트워크 연결 실패: ${t.message}")
                }
            })
    }
}
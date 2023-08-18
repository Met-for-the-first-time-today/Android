package com.hachathon.presentationHelper.init

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hachathon.presentationHelper.R
import com.hachathon.presentationHelper.databinding.ActivityLoginBinding
import com.hachathon.presentationHelper.init.data.LoginRequest
import com.hachathon.presentationHelper.init.data.LoginResponse
import com.hachathon.presentationHelper.main.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        binding.loginClose.setOnClickListener {
            finish()
        }

        binding.loginBtn.setOnClickListener {
            val id = binding.loginIdEdit.text.toString()
            val pwd = binding.loginPwdEdit.text.toString()
            loginAPI(id, pwd)
        }
    }

    private fun loginAPI(id: String, pwd: String) {
        // 서버 주소
        val serverAddress = getString(R.string.serverAddress)

        val retrofit = Retrofit.Builder()
            .baseUrl(serverAddress)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val loginService = retrofit.create(InitService::class.java)

        // 로그인 API 호출
        loginService.login(LoginRequest(id = id, password = pwd))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>,
                ) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        // 서버 응답 처리 로직 작성
                        if (loginResponse?.status == "success") {
                            // 홈 화면으로 연결
                            saveLogin(id, pwd)
                            saveToken(loginResponse.data)

                            val mIntent = Intent(this@LoginActivity, MainActivity::class.java)
                            mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(mIntent)
                            finish()
                        }
                    } else {
                        Log.e("LOGIN", "[LOGIN AUTO] API 호출 실패 - 응답 코드: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    // 네트워크 연결 실패 등 호출 실패 시 처리 로직
                    Log.e("LOGIN", "[LOGIN AUTO] API 호출 실패 - 네트워크 연결 실패: ${t.message}")
                }
            })
    }

    private fun saveLogin(id: String, pwd: String) {
        // 서버 토큰을 SharedPreferences에 저장
        val editor = sharedPreferences.edit()
        editor.putString("ID", id)
        editor.putString("PWD", pwd)
        editor.apply()
    }

    private fun saveToken(token: String) {
        // 서버 토큰을 SharedPreferences에 저장
        val editor = sharedPreferences.edit()
        editor.putString("TOKEN", token)
        editor.apply()
    }
}
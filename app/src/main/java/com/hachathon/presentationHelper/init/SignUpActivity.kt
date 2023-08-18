package com.hachathon.presentationHelper.init

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hachathon.presentationHelper.R
import com.hachathon.presentationHelper.databinding.ActivitySignUpBinding
import com.hachathon.presentationHelper.init.data.SignUpRequest
import com.hachathon.presentationHelper.init.data.SignUpResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupClose.setOnClickListener {
            finish()
        }

        binding.signupBtn.setOnClickListener {
            val name = binding.signinUserEdit.text.toString()
            val id = binding.signinIdEdit.text.toString()
            val pwd = binding.signinPwdEdit.text.toString()
            signUpAPI(name, id, pwd)
        }

    }

    private fun signUpAPI(name: String, id: String, pwd: String) {
        // 서버 주소
        val serverAddress = getString(R.string.serverAddress)

        val retrofit = Retrofit.Builder()
            .baseUrl(serverAddress)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val SIGNUPService = retrofit.create(InitService::class.java)

        // 로그인 API 호출
        SIGNUPService.signup(SignUpRequest(username = name, id = id, password = pwd))
            .enqueue(object : Callback<SignUpResponse> {
                override fun onResponse(
                    call: Call<SignUpResponse>,
                    response: Response<SignUpResponse>,
                ) {
                    if (response.isSuccessful) {
                        val signUpResponse = response.body()
                        // 서버 응답 처리 로직 작성
                        if (signUpResponse?.status == "success") {
                            finish()
                        }
                    } else {
                        Log.e("SIGNUP", "[SIGNUP AUTO] API 호출 실패 - 응답 코드: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                    // 네트워크 연결 실패 등 호출 실패 시 처리 로직
                    Log.e("SIGNUP", "[SIGNUP AUTO] API 호출 실패 - 네트워크 연결 실패: ${t.message}")
                }
            })
    }
}
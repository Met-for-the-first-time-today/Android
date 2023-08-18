package com.hachathon.presentationHelper.edit

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.hachathon.presentationHelper.R
import com.hachathon.presentationHelper.databinding.ActivityEditBinding
import com.hachathon.presentationHelper.main.MainService
import com.hachathon.presentationHelper.main.data.MainDataCrateRequest
import com.hachathon.presentationHelper.main.data.MainDataDetailResponse
import com.hachathon.presentationHelper.main.data.MainDataUpdateRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var index: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        index = intent.getIntExtra("index", -1)
        binding.editTitleEd.setText(intent.getStringExtra("title"))
        binding.editScriptBody.setText(intent.getStringExtra("script"))

        Log.e("TEST","$index")
        binding.editBackBtn.setOnClickListener {
            finish()
        }

        binding.editFinishBtn.setOnClickListener {
            if (index == -1) {
                createAPI()
            } else {
                updateAPI()
            }
        }

        binding.editFloatBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 없는 경우 권한 요청
                requestReadFilePermissionLauncher.launch(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } else {
                // 이미 권한이 있는 경우 파일 선택 처리
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                addFile.launch(intent)
            }
        }
    }

    private val requestReadFilePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 이미 권한이 있는 경우 파일 선택 처리
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                addFile.launch(intent)
            } else {
                // 권한이 거부된 경우에 대한 처리
            }
        }
    private val addFile: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { it ->
        // 결과 코드 OK
        if (it.resultCode == RESULT_OK) {
            val uri = it.data?.data // 결과 값 저장
            uri?.let {
                val contentResolver: ContentResolver = contentResolver
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line).append("\n")
                    }

                    reader.close()
                    inputStream?.close()

                    val fileContent = stringBuilder.toString()
                    val addContent = binding.editScriptBody.text.toString() + fileContent
                    binding.editScriptBody.setText(addContent)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createAPI() {
        // 서버 주소
        val serverAddress = getString(R.string.serverAddress)

        val retrofit = Retrofit.Builder()
            .baseUrl(serverAddress)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val mainService = retrofit.create(MainService::class.java)
        val accessToken = sharedPreferences.getString("TOKEN", null)!!

        // 생성 API 호출
        mainService.createData(
            MainDataCrateRequest(
                token = accessToken,
                title = binding.editTitleEd.text.toString(),
                data = binding.editScriptBody.text.toString(),
                speed = 1.0F
            )
        )
            .enqueue(object : Callback<MainDataDetailResponse> {
                override fun onResponse(
                    call: Call<MainDataDetailResponse>,
                    response: Response<MainDataDetailResponse>,
                ) {
                    if (response.isSuccessful) {
                        val detailResponse = response.body()
                        // 서버 응답 처리 로직 작성
                        if (detailResponse?.status == "success") {
                            finish()
                        }
                    } else {
                        Log.e("CREATE", "[CREATE] API 호출 실패 - 응답 코드: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<MainDataDetailResponse>, t: Throwable) {
                    // 네트워크 연결 실패 등 호출 실패 시 처리 로직
                    Log.e("CREATE", "[CREATE] API 호출 실패 - 네트워크 연결 실패: ${t.message}")
                }
            })
    }

    private fun updateAPI() {
        // 서버 주소
        val serverAddress = getString(R.string.serverAddress)

        val retrofit = Retrofit.Builder()
            .baseUrl(serverAddress)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val mainService = retrofit.create(MainService::class.java)
        val accessToken = sharedPreferences.getString("TOKEN", null)!!

        // 수정 API 호출
        mainService.editData(
            MainDataUpdateRequest(
                token = accessToken,
                index = index,
                title = binding.editTitleEd.text.toString(),
                data = binding.editScriptBody.text.toString(),
                speed = 1.0F
            )
        )
            .enqueue(object : Callback<MainDataDetailResponse> {
                override fun onResponse(
                    call: Call<MainDataDetailResponse>,
                    response: Response<MainDataDetailResponse>,
                ) {
                    if (response.isSuccessful) {
                        val detailResponse = response.body()
                        // 서버 응답 처리 로직 작성
                        if (detailResponse?.status == "success") {
                            finish()
                        }
                    } else {
                        Log.e("UPDATE", "[UPDATE] API 호출 실패 - 응답 코드: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<MainDataDetailResponse>, t: Throwable) {
                    // 네트워크 연결 실패 등 호출 실패 시 처리 로직
                    Log.e("UPDATE", "[UPDATE] API 호출 실패 - 네트워크 연결 실패: ${t.message}")
                }
            })
    }
}
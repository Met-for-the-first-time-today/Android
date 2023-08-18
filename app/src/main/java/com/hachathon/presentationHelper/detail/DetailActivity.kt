package com.hachathon.presentationHelper.detail

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.media.PlaybackParams
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.hachathon.presentationHelper.R
import com.hachathon.presentationHelper.databinding.ActivityDetailBinding
import com.hachathon.presentationHelper.edit.EditActivity
import com.hachathon.presentationHelper.main.MainService
import com.hachathon.presentationHelper.main.data.MainDataDeleteResponse
import com.hachathon.presentationHelper.main.data.MainDataDetailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.round

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var outputFile: File
    private var index: Int = -1

    private lateinit var textToSpeech: TextToSpeech
    private var mediaPlayer: MediaPlayer? = null
    private var isPlay = false
    private var isInit = false
    private var speed = 1.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)

        index = intent.getIntExtra("index", -1)

        binding.detailSeekbar.isEnabled = false

        binding.detailBackBtn.setOnClickListener {
            if (outputFile.exists())
                outputFile.delete()
            finish()
        }

        binding.detailEditBtn.setOnClickListener {
            val mIntent = Intent(this, EditActivity::class.java)
            mIntent.putExtra("index", index)
            mIntent.putExtra("title", binding.detailTitleTv.text.toString())
            mIntent.putExtra("script", binding.detailScriptBody.text.toString())
            startActivity(mIntent)
        }

        binding.detailDeleteBtn.setOnClickListener {
            deleteAPI(index = index)
        }

        // 아이템 리스트 준비
        val items = listOf("0.3", "1.0", "1.2")

        // ArrayAdapter를 사용하여 아이템과 레이아웃을 연결
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)

        // 드롭다운 레이아웃 설정
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // 스피너에 어댑터 설정
        binding.speedSpinner.adapter = adapter

        // 아이템 선택 리스너
        binding.speedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long,
            ) {
                if (isInit) {
                    speed = items[position].toFloat()
                    outputFile.delete()
                    convertTextToAudio(
                        binding.detailScriptBody.text.toString(),
                        this@DetailActivity
                    )
                }
                // 스피너에서 선택이 변경되면 자동으로 재생 중지
                if (isPlay) {
                    mediaPlayer?.pause()
                    binding.detailPlayBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24)
                    isPlay = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무 것도 선택되지 않았을 때의 동작
            }
        }

        binding.speedSpinner.setSelection(1)
        detailAPI(index)
    }

    override fun onResume() {
        super.onResume()
        detailAPI(index)

        binding.detailPlayBtn.setOnClickListener {
            if (binding.detailPlayBtn.isClickable && isInit) {
                if (!isPlay) {
                    mediaPlayer?.start()
                    binding.detailPlayBtn.setBackgroundResource(R.drawable.baseline_pause_24)

                    /* 실시간으로 변경되는 진행시간과 시크바를 구현하기 위한 쓰레드 사용*/
                    object : Thread() {
                        var timeFormat = SimpleDateFormat("mm:ss")  //"분:초"를 나타낼 수 있도록 포멧팅
                        override fun run() {
                            super.run()
                            if (mediaPlayer == null)
                                return
                            while (mediaPlayer!!.isPlaying) {
                                runOnUiThread { //화면의 위젯을 변경할 때 사용 (이 메소드 없이 아래 코드를 추가하면 실행x)
                                    binding.detailSeekbar.progress = mediaPlayer!!.currentPosition
                                    binding.detailTimeNow.text =
                                        timeFormat.format(round(mediaPlayer!!.currentPosition / speed))
                                }
                                SystemClock.sleep(50)
                            }

                            // 음악이 종료되면 자동으로 초기상태로 전환
                            if (!mediaPlayer!!.isPlaying) {
                                runOnUiThread {
                                    binding.detailPlayBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24)
                                    binding.detailTimeNow.text = "00:00"
                                    isPlay = !isPlay

                                    if (mediaPlayer!!.currentPosition >= binding.detailSeekbar.max - 50)
                                        binding.detailSeekbar.progress = 0
                                }
                            }
                        }
                    }.start()
                } else {
                    mediaPlayer!!.pause()
                    binding.detailPlayBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24)
                }
            }
            isPlay = !isPlay
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        outputFile.delete()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    private fun convertTextToAudio(script: String, context: Context) {
        // 오디오 파일 저장 경로 및 이름 설정
        outputFile = File(
            Environment.getExternalStorageDirectory(),
            "sample_audio.mp3"
        )

        textToSpeech.synthesizeToFile(script, null, outputFile, "audio_utterance")

        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                val outputFile = File(
                    Environment.getExternalStorageDirectory(),
                    "sample_audio.mp3"
                )

                if (outputFile.exists()) {
                    mediaPlayer = MediaPlayer()
                    mediaPlayer?.setDataSource(outputFile.path)
                    mediaPlayer?.setOnPreparedListener {
                        isInit = true
                        val timeFormat = SimpleDateFormat("mm:ss")
                        binding.detailTimeTotal.text =
                            timeFormat.format(round(mediaPlayer!!.duration / speed))
                        binding.detailSeekbar.max = mediaPlayer!!.duration

                        // 속도 조절
                        val playbackParams = PlaybackParams()
                        playbackParams.speed = speed // speed 값을 설정해야 함
                        mediaPlayer?.playbackParams = playbackParams
                    }
                    mediaPlayer?.prepareAsync()
                }

                binding.detailPlayBtn.isClickable = true
                binding.detailPlayBtn.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.click_true))
            }

            override fun onError(utteranceId: String?) {}
        })
    }

    private fun detailAPI(index: Int) {
        // 서버 주소
        val serverAddress = getString(R.string.serverAddress)

        val retrofit = Retrofit.Builder()
            .baseUrl(serverAddress)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val mainService = retrofit.create(MainService::class.java)
        val accessToken = sharedPreferences.getString("TOKEN", null)!!

        // 로그인 API 호출
        mainService.searchData(token = accessToken, index = index)
            .enqueue(object : Callback<MainDataDetailResponse> {
                override fun onResponse(
                    call: Call<MainDataDetailResponse>,
                    response: Response<MainDataDetailResponse>,
                ) {
                    if (response.isSuccessful) {
                        val detailResponse = response.body()
                        // 서버 응답 처리 로직 작성
                        if (detailResponse?.status == "success") {
                            val data = detailResponse.data
                            binding.detailTitleTv.text = data.title
                            binding.detailScriptBody.text = data.data

                            textToSpeech = TextToSpeech(this@DetailActivity) { status ->
                                if (status == TextToSpeech.SUCCESS) {
                                    val locale = Locale.KOREAN // 사용할 언어 및 지역 설정
                                    if (textToSpeech.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE) {
                                        textToSpeech.language = locale
                                        convertTextToAudio(
                                            binding.detailScriptBody.text.toString(),
                                            this@DetailActivity
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Log.e("MAIN", "[MAIN DETAIL] API 호출 실패 - 응답 코드: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<MainDataDetailResponse>, t: Throwable) {
                    // 네트워크 연결 실패 등 호출 실패 시 처리 로직
                    Log.e("MAIN", "[MAIN DETAIL] API 호출 실패 - 네트워크 연결 실패: ${t.message}")
                }
            })
    }

    private fun deleteAPI(index: Int) {
        // 서버 주소
        val serverAddress = getString(R.string.serverAddress)

        val retrofit = Retrofit.Builder()
            .baseUrl(serverAddress)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val mainService = retrofit.create(MainService::class.java)
        val accessToken = sharedPreferences.getString("TOKEN", null)!!

        // 로그인 API 호출
        mainService.deleteData(token = accessToken, index = index)
            .enqueue(object : Callback<MainDataDeleteResponse> {
                override fun onResponse(
                    call: Call<MainDataDeleteResponse>,
                    response: Response<MainDataDeleteResponse>,
                ) {
                    if (response.isSuccessful) {
                        finish()
                    } else {
                        Log.e("DELETE", "[DELETE] API 호출 실패 - 응답 코드: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<MainDataDeleteResponse>, t: Throwable) {
                    // 네트워크 연결 실패 등 호출 실패 시 처리 로직
                    Log.e("DELETE", "[DELETE] API 호출 실패 - 네트워크 연결 실패: ${t.message}")
                }
            })
    }
}
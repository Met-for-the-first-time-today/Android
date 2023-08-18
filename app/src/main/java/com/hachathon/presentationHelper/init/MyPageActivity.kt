package com.hachathon.presentationHelper.init

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hachathon.presentationHelper.databinding.ActivityMyPageBinding

class MyPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyPageBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("ID", null)

        binding.mypageName.text = id.toString()

        binding.mypageClose.setOnClickListener {
            finish()
        }

        binding.logoutBtn.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putString("ID", "")
            editor.putString("PWD", "")
            editor.apply()

            val mIntent = Intent(this@MyPageActivity, WelcomeActivity::class.java)
            mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(mIntent)
            finish()
        }
    }


}
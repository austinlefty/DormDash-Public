package com.dormdash.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.dormdash.android.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
package com.example.myapplication

import android.app.Application
import com.example.myapplication.utils.SharedPreferenceHelper

class TempuskyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SharedPreferenceHelper.init(applicationContext)
    }
}
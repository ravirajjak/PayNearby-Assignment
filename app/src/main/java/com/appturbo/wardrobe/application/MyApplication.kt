package com.appturbo.tinder.application

import android.app.Application

class MyApplication : Application() {

    init {
        instance = this
    }

    //singleton reference
    companion object {
        private var instance: MyApplication? = null
        fun getContext(): MyApplication {
            return instance as MyApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
    }
}
package com.appturbo.wardrobe

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)

        openIntent(MainActivity::class.java)

    }


    private fun openIntent(mClass: Class<*>) {
        lifecycleScope.launch {
            delay(5000L)
            val intent = Intent(this@SplashActivity, newInstance(mClass))
            startActivity(intent)
            finish()
        }
    }
}
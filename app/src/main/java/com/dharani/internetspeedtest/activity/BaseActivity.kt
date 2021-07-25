package com.dharani.internetspeedtest.activity

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity


open class BaseActivity : AppCompatActivity() {

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    @SuppressLint("HardwareIds")
    fun getDeviceID(): String {
        return Settings.Secure.getString(
            this@BaseActivity.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

}
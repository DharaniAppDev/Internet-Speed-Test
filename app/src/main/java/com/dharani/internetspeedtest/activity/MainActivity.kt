package com.dharani.internetspeedtest.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.dharani.internetspeedtest.R
import com.dharani.internetspeedtest.constants.KeyNames.Companion.NETWORK_STATE_ACTION
import com.dharani.internetspeedtest.constants.KeyNames.Companion.WIFI_STATE_ACTION
import com.dharani.internetspeedtest.databinding.ActivityMainBinding
import com.dharani.internetspeedtest.viewmodel.MainViewModel
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.api.GoogleApiClient
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : BaseActivity() {
    lateinit var blinkAnimation: Animation
    lateinit var fadeIn: Animation
    lateinit var fadeOut: Animation
    private lateinit var binding: ActivityMainBinding
    lateinit var cm: ConnectivityManager
    lateinit var nc: NetworkCapabilities
    lateinit var epoch: String
    private val mainViewModel: MainViewModel by viewModels()
    private val MY_PERMISSION_REQUEST_CODE_PHONE_STATE = 1
    private val LOG_TAG = "AndroidExample"
    var fetchedphoneNumber = "Not Available"

    private val PHONE_NUMBER_HINT = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink_anim)
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        binding.tvStart.setOnClickListener {

            if (!fetchedphoneNumber.equals("Not Available")) {

                if (isNetworkAvailable()) {
                    binding.tvProgressIndicator.also {
                        it.setTextColor(getColor(R.color.colorPrimaryDark))
                        it.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_refresh, 0)
                        it.text = getString(R.string.calculating_internet_speed)
                        it.visibility = View.VISIBLE
                        it.startAnimation(blinkAnimation)
                    }

                    calculateSpeed()
                }
            } else {
                getMobileNumberRegistered()
            }
        }

        binding.tvViewLatestHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

    }

    fun getCurrentTime() {
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault())
        epoch = System.currentTimeMillis().toString()

        binding.tvTime.also {
            it.text = sdf.format(epoch.toLong())
            it.visibility = View.VISIBLE
            it.startAnimation(fadeIn)
        }
    }

    override fun onResume() {
        super.onResume()
        askPermission()
        setConnected(isNetworkAvailable())

        networkReceiver.let {
            val intentFilter = IntentFilter()
            intentFilter.addAction(NETWORK_STATE_ACTION)
            intentFilter.addAction(WIFI_STATE_ACTION)
            registerReceiver(it, intentFilter)
        }
    }

    fun setConnected(connected: Boolean) {
        if (connected) {
            binding.tvConnectionStatus.text = getString(R.string.connected)
            binding.tvConnectionStatus.background = ResourcesCompat.getDrawable(
                this.resources,
                R.drawable.online_bg,
                this.theme
            )
        } else {
            binding.tvConnectionStatus.text = getString(R.string.not_connected)
            binding.tvConnectionStatus.background = ResourcesCompat.getDrawable(
                this.resources,
                R.drawable.offline_bg,
                this.theme
            )
        }
    }

    fun calculateSpeed() {
        Handler().postDelayed({

            binding.tvProgressIndicator.also {
                it.text = getString(R.string.calculated)
                it.setTextColor(getColor(R.color.green_text))
                it.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done, 0);
            }
            cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            nc = cm.getNetworkCapabilities(cm.activeNetwork)!!
            nc.let {
                val downSpeed =
                    ((nc.linkDownstreamBandwidthKbps) / 1000).toString() + " " + getString(R.string.mbps)
                binding.tvDownloadSpeedResult.also {
                    it.text = downSpeed
                    it.visibility = View.VISIBLE
                    it.startAnimation(fadeIn)
                }

                // UpSpeed  in MBPS
                val upSpeed =
                    ((nc.linkUpstreamBandwidthKbps) / 1000).toString() + " " + getString(R.string.mbps)
                binding.tvUploadSpeedResult.also {
                    it.text = upSpeed
                    it.visibility = View.VISIBLE
                    it.startAnimation(fadeIn)
                }

                getCurrentTime()
                writedata()
            }
        }, 4000)
    }

    private val networkReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == NETWORK_STATE_ACTION || intent.action == WIFI_STATE_ACTION) {
                setConnected(isNetworkAvailable())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkReceiver)
    }

    fun writedata() {
        mainViewModel.writedataToFirebase(
            getDeviceID(),
            fetchedphoneNumber,
            binding.tvDownloadSpeedResult.text.toString(),
            binding.tvUploadSpeedResult.text.toString(),
            binding.tvTime.text.toString(),
            epoch
        )
    }


    fun getMobileNumberRegistered() {
        val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build()

        try {
            val googleApiClient =
                GoogleApiClient.Builder(this).addApi(Auth.CREDENTIALS_API).build()

            val pendingIntent =
                Auth.CredentialsApi.getHintPickerIntent(googleApiClient, hintRequest)
            startIntentSenderForResult(
                pendingIntent.intentSender,
                PHONE_NUMBER_HINT,
                null,
                0,
                0,
                0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun askPermission() {

        // With Android Level >= 23, you have to ask the user
        // for permission to get Phone Number.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 23

            // Check if we have READ_PHONE_STATE permission
            val readPhoneStatePermission = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            )
            if (readPhoneStatePermission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                requestPermissions(
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    MY_PERMISSION_REQUEST_CODE_PHONE_STATE
                )
                return
            }
        }
        getPhoneNumbers()

    }

    @SuppressLint("MissingPermission")
    fun getPhoneNumbers() {
        try {
            val manager = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            fetchedphoneNumber = manager.line1Number

        } catch (ex: Exception) {
            Log.e(LOG_TAG, "Error: ", ex)
            ex.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSION_REQUEST_CODE_PHONE_STATE -> {

                // Note: If request is cancelled, the result arrays are empty.
                // Permissions granted (SEND_SMS).
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i(LOG_TAG, "Permission granted!")
                    getPhoneNumbers()
                } else {
                    Log.i(LOG_TAG, "Permission denied!")
                }
            }
        }
    }


    // When results returned
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHONE_NUMBER_HINT && resultCode == RESULT_OK) {
            val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
            fetchedphoneNumber = credential?.getId().toString()
            binding.tvStart.performClick()
        }
    }


}
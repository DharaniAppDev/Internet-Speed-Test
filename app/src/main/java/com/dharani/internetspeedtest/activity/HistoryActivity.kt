package com.dharani.internetspeedtest.activity

import android.R
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.dharani.internetspeedtest.databinding.ActivityHistoryBinding
import com.dharani.internetspeedtest.viewmodel.HistoryViewModel

class HistoryActivity : BaseActivity() {
    private val historyViewModel: HistoryViewModel by viewModels()
    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.setDisplayHomeAsUpEnabled(true);


        historyViewModel.latestNetworkInfo.observe(this, Observer {

            binding.apply {
                tvDeviceId.text = getDeviceID()
                tvDownSpeed.text = it.downloadSpeed
                tvUpSpeed.text = it.uploadSpeed
                tvTime.text = it.timeStamp
                tvPhoneNo.text = it.mobileNo

                this.view.visibility = View.VISIBLE
                tvDeviceIdTitle.visibility = View.VISIBLE
                tvDeviceId.visibility = View.VISIBLE
                tvPhoneNoTitle.visibility = View.VISIBLE
                tvPhoneNo.visibility = View.VISIBLE
                tvUpSpeedTitle.visibility = View.VISIBLE
                tvUpSpeed.visibility = View.VISIBLE
                tvDownSpeed.visibility = View.VISIBLE
                tvDownSpeedTitle.visibility = View.VISIBLE
                tvTimeTitle.visibility = View.VISIBLE
                tvTime.visibility = View.VISIBLE

                viewDivider1.visibility = View.VISIBLE
                viewDivider2.visibility = View.VISIBLE
                viewDivider3.visibility = View.VISIBLE
                viewDivider4.visibility = View.VISIBLE

            }

        })

        historyViewModel.info.observe(this, Observer {
            Toast.makeText(this,it,Toast.LENGTH_LONG).show()
        })
    }

    override fun onResume() {
        super.onResume()
        historyViewModel.fetchLatestRecord(getDeviceID())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
package com.dharani.internetspeedtest.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dharani.internetspeedtest.constants.KeyNames
import com.dharani.internetspeedtest.model.NetworkInfo
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainViewModel(application: Application) : AndroidViewModel(application){
     val database: DatabaseReference = Firebase.database.reference

    fun writedataToFirebase(deviceId : String, phoneNumber : String, downSpeed : String, upSpeed : String, timestamp : String, epoch : String){

        val networkInfo = NetworkInfo(phoneNumber,downSpeed,upSpeed,timestamp)

        Log.d("networkInfo",networkInfo.toString())

        database.child(KeyNames.NETWORK_INFO).child(deviceId).child(epoch).setValue(networkInfo)


    }

}
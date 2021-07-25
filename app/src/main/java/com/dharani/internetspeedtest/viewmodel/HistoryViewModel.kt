package com.dharani.internetspeedtest.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dharani.internetspeedtest.constants.KeyNames
import com.dharani.internetspeedtest.model.NetworkInfo
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    val database: DatabaseReference = Firebase.database.reference
    val latestNetworkInfo : MutableLiveData<NetworkInfo> = MutableLiveData()
    val info : MutableLiveData<String> = MutableLiveData()

    fun fetchLatestRecord(deviceID : String){
        try {
            database.child(KeyNames.NETWORK_INFO).child(deviceID).orderByKey().limitToLast(1).get()
                .addOnSuccessListener {
                    if(it.hasChildren()) {
                        latestNetworkInfo.value = NetworkInfo(
                            it.children.first().child("mobileNo").value.toString(),
                            it.children.first().child("downloadSpeed").value.toString(),
                            it.children.first().child("uploadSpeed").value.toString(),
                            it.children.first().child("timeStamp").value.toString()
                        )
                    } else{
                        info.value = "No Records Found"
                    }
                }.addOnFailureListener {
                    Log.e("firebase", "Error getting data", it)
                }
        } catch (exception : Exception){
            info.value = exception.message
        }
    }

}
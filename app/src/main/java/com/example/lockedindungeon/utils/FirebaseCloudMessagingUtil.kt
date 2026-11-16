package com.example.lockedindungeon.utils

import android.os.Build
import android.util.Log
import com.example.lockedindungeon.LockedInDungeon.Companion.TAG
import com.example.lockedindungeon.api.UserControllerApi
import com.example.lockedindungeon.data.remote.AuthRepository
import com.example.lockedindungeon.dto.RegisterFirebaseRequest
import com.google.firebase.Firebase
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openapitools.client.infrastructure.ApiClient
import java.util.concurrent.Executor
import java.util.concurrent.Executors


public fun registerFirebaseMessaging(registerToServer : (String, String)-> Unit) {
    val TAG = "registerFirebaseMessaging"
    val deviceName = Build.MODEL

    val tokenExecutor : Executor = Executors.newSingleThreadExecutor()
    Firebase.messaging.register().addOnCompleteListener(tokenExecutor) { task ->
        if (task.isSuccessful) {
            Log.d(TAG, "Firebase installation registered")
        } else {
            Log.e(TAG, "Firebase installation registration failed", task.exception)
        }
    }

    FirebaseInstallations.getInstance().id.addOnCompleteListener(tokenExecutor) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Firebase installation id: ${task.result}")
                val fid = task.result
                registerToServer(fid, deviceName)
            } else {
                Log.e(TAG, "Firebase installation id failed", task.exception)
            }
    }


}

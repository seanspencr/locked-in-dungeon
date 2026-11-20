package com.example.lockedindungeon.utils.notification

import android.os.Build
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.messaging
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

package com.marblevhs.clairsavedimages

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService


class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("RESP", "New token: $token")
    }

}
package com.example.cs447_videocallapp

import android.webkit.JavascriptInterface

class Ijavascript(private val videoCallActivity: VideoCallActivity) {

    @JavascriptInterface
    public fun onPeerConnected(){
        videoCallActivity.onPeerConnected()
    }
}
package com.example.cs447_videocallapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_video_call.*
import java.util.*

class VideoCallActivity : AppCompatActivity() {
    private var usernameInput = ""
    private var friendUsernameInput = ""
    private var peerConnected = false


    private var firebaseRef = Firebase.database.getReference("peersData")

    private var videoStatus = true
    private var audioStatus = true




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        if(intent.getStringExtra("usernameEdit") == null){}
        else {
            usernameInput = intent.getStringExtra("usernameEdit")!!
        }
        callButton.setOnClickListener {
            friendUsernameInput = friendNameEditText.text.toString()
            if(peerConnected){}
            else{
                Toast.makeText(this,"Your internet connection has a problem",Toast.LENGTH_LONG).show()
                Log.e("Error","Your internet connection has a problem")
            }
            firebaseRef.child(friendUsernameInput).child("incomingCall").setValue(usernameInput)
            firebaseRef.child(friendUsernameInput).child("availabilityStatus").addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value.toString() == "true"){
                        listenForConnectionId()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }


        toggleVideoButton.setOnClickListener {
            videoStatus = !videoStatus
            callJavascriptFunction("javascript:toggleVideo(\"${videoStatus}\")")
            toggleVideoButton.setImageResource(if (videoStatus) R.drawable.ic_baseline_videocam_24 else R.drawable.ic_baseline_videocam_off_24)
        }


        toggleAudioButton.setOnClickListener {
            audioStatus = !audioStatus
            callJavascriptFunction("javascript:toggleAudio(\"${audioStatus}\")")
            toggleAudioButton.setImageResource(if (audioStatus) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24)
        }

        startWebView()

    }

    private fun listenForConnectionId() {
        firebaseRef.child(friendUsernameInput).child("connectionID").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value != null) {
                    switchToControls()
                    callJavascriptFunction("javascript:startCall(\"${snapshot.value}\")")
                } else {
                    return
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
    }

    private fun startWebView() {
        webView.webChromeClient = object: WebChromeClient(){
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }
        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.addJavascriptInterface(Ijavascript(this),"Android")

        webView.loadUrl("file:android_asset/call.html")

        webView.webViewClient = object :WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                initializePeerConnection()
            }
        }
    }

    private var uniqueId = ""

    private fun initializePeerConnection() {

        uniqueId = UUID.randomUUID().toString()

        callJavascriptFunction("javascript:init(\"${uniqueId}\")")
        firebaseRef.child(usernameInput).child("incomingCall").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                onCallRequest(snapshot.value as? String)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun onCallRequest(callerName: String?) {
        if(callerName != null) {

            callRelativeLayout.visibility = View.VISIBLE
            incomingCallTextView.text = callerName + " is calling..."
            acceptButton.setOnClickListener {
                firebaseRef.child(usernameInput).child("connectionID").setValue(uniqueId)
                firebaseRef.child(usernameInput).child("isAvailable").setValue(true)

                callRelativeLayout.visibility = View.GONE
                switchToControls()
            }
            rejectButton.setOnClickListener {
                firebaseRef.child(usernameInput).child("incoming").setValue(null)
                callRelativeLayout.visibility = View.GONE
            }
        } else {
            return ;
        }
    }


    private fun switchToControls() {
        inputLayout.visibility = View.GONE
        callControlLayout.visibility = View.VISIBLE
    }


    private fun callJavascriptFunction(functionString:String){
        webView.post { webView.evaluateJavascript(functionString,null) }
    }

    fun onPeerConnected() {
        peerConnected = true
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        firebaseRef.child(usernameInput).setValue(null)
        webView.loadUrl("about:blank")
        super.onDestroy()
    }
}
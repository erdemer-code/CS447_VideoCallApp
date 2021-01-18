package com.example.cs447_videocallapp

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {

    private val permission = arrayOf(RECORD_AUDIO,CAMERA)
    private val requestCode = 999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isPermissionGranted()){ }

        else{
            ActivityCompat.requestPermissions(this,permission,requestCode)
        }

        Firebase.initialize(this)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val target = Intent(this,VideoCallActivity::class.java) // Implement logic this class later

            target.putExtra("usernameInput",username)
            startActivity(target)
        }
    }

    private fun isPermissionGranted(): Boolean {
        var flag = true
        for(perm in permission) {
            if (ActivityCompat.checkSelfPermission(this,perm) != PackageManager.PERMISSION_GRANTED){
                flag = false
            }
        }
        return flag
    }
}
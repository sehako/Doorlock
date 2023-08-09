package com.example.doorlock

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.doorlock.databinding.ActivityMainBinding
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    // permission result callback
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permission", "ok")
        } else {
            Log.d("Permission", "fail")
        }
    }
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
        this.onBackPressedDispatcher.addCallback(this, callback)

        // 버전에 따른 권한
        val reqPermission = if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ) {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        } else {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        }

        // 권한 체크
        if( checkSelfPermission(reqPermission) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissionLauncher.launch(reqPermission)
        } else {
            Log.d("Permission", "ok")
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!allPermissionsGranted()) {
            Toast.makeText(this,
                "권한 승인이 필요합니다.",
                Toast.LENGTH_SHORT).show()
        }
    }
    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
package com.example.doorlock.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.doorlock.R
import com.example.doorlock.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val camerabtn: Button = view.findViewById(R.id.camera_test)

        camerabtn.setOnClickListener(View.OnClickListener {
            val imageCapture = ImageCapture.Builder()
                .setTargetRotation(view.display.rotation)
                .build()
        })

        return view
    }
}
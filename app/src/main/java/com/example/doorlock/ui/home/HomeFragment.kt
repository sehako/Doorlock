package com.example.doorlock.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
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
    val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val camerabtn: Button = view.findViewById(R.id.camera_test)

        camerabtn.setOnClickListener(View.OnClickListener {
            val items = arrayOf("사진 촬영", "갤러리")
            val builder = AlertDialog.Builder(context)
                .setTitle("사용자 추가").setItems(items) { dialog, which ->
                    if (which == 0) {
                        Toast.makeText(context, "카메라 촬영", Toast.LENGTH_LONG).show()
                    }
                    else {
                        Toast.makeText(context, "갤러리 접근", Toast.LENGTH_LONG).show()
                    }
                }
            builder.show()
            }
        )


        return view
    }
}
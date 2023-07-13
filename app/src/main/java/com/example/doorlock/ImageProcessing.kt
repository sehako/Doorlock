package com.example.doorlock

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Date

class ImageProcessing : Fragment(){

    private val add_option = arrayOf("Camera", "Gallery")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_processing, container, false)
        val img = view.findViewById<ImageView>(R.id.face_image)
        val name = view.findViewById<EditText>(R.id.user_name)

        val camLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Handle the result from the launched activity here
                    val data: Intent? = result.data
                    val bitmap: Bitmap = data?.extras?.get("data") as Bitmap
                    img.setImageBitmap(bitmap)
                    val file = bitmapToFile(
                        bitmap, SimpleDateFormat("yyyy-mm-dd").format(Date())
                    )
                    name.hint = "이름 입력"
                }
            }

        val galLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Handle the result from the launched activity here
                    val data: Intent? = result.data
                    // Image의 상대경로를 가져온다
                    val image = data?.data
                    // 절대 경로를 가져 오는 함수
                    val imagePath: String? = getPathFromUri(image)
                    Toast.makeText(requireContext(), "" + imagePath, Toast.LENGTH_LONG).show()
                    img.setImageURI(image)
                    // File 변수에 File을 집어넣는다
                    val destFile = imagePath?.let { File(it) }
                    // Process the data
                    name.hint = "이름 입력"
                }
            }

        img.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Choose your photo from...")

            builder.setItems(add_option) { dialog, which ->
                when(which) {
                    0 -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
                        camLauncher.launch(intent)
                    }
                    1 -> {
                        val intent = Intent(context, ImageUpload::class.java)
                        context?.startActivity(intent)
                    }
                }
            }
            builder.show()
        }
        return view
    }

    private fun getPathFromUri(uri: Uri?): String? {
        uri ?: return null

        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor =
            requireActivity().contentResolver.query(uri, projection, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val columnIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (columnIndex != -1) {
                    return c.getString(columnIndex)
                }
            }
        }
        return null
    }

    private fun bitmapToFile(bitmap: Bitmap?, saveName: String): File {
        val saveDir =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + saveName
        val file = File(saveDir)
        if (!file.exists()) file.mkdirs()

        val fileName = "$saveName.jpg"
        val tempFile = File(saveDir, fileName)

        var out: OutputStream? = null
        try {
            if (tempFile.createNewFile()) {
                out = FileOutputStream(tempFile)
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

        } finally {
            out?.close()
        }
        return tempFile
    }
}
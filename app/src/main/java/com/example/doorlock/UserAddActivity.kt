package com.example.doorlock

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.doorlock.databinding.ActivityUserAddBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Date

class UserAddActivity : AppCompatActivity() {
    private val add_option = arrayOf("Camera", "Gallery")
    private lateinit var binding: ActivityUserAddBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_add)
        val imgView = findViewById<ImageView>(R.id.face_image)
        val nameText = findViewById<EditText>(R.id.user_name)

        val camLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Handle the result from the launched activity here
                    val data: Intent? = result.data
                    val bitmap: Bitmap = data?.extras?.get("data") as Bitmap
                    imgView.setImageBitmap(bitmap)
                    val file = bitmapToFile(
                        bitmap, SimpleDateFormat("yyyy-mm-dd").format(Date())
                    )
                    Toast.makeText(this@UserAddActivity, "" + file, Toast.LENGTH_LONG).show()
                    nameText.hint = "이름 입력"
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
                    Toast.makeText(this@UserAddActivity, "" + imagePath, Toast.LENGTH_LONG).show()
                    imgView.setImageURI(image)
                    // File 변수에 File을 집어넣는다
                    val destFile = imagePath?.let { File(it) }
                    // Process the data
                    nameText.hint = "이름 입력"
                }
            }

        imgView.setOnClickListener {
            val builder = AlertDialog.Builder(this@UserAddActivity)
            builder.setTitle("Choose your photo from...")

            builder.setItems(add_option) { dialog, which ->
                when(which) {
                    0 -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
                        camLauncher.launch(intent)
                    }
                    1 -> {
                        val intent = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        galLauncher.launch(intent)
                    }
                }
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun getPathFromUri(uri: Uri?): String? {
        uri ?: return null

        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor =
            this@UserAddActivity.contentResolver.query(uri, projection, null, null, null)
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
            this@UserAddActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
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

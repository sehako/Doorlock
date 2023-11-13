package com.example.doorlock

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.doorlock.databinding.ActivityUserAddBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.Socket


class UserAddActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_REQUEST = 101
    private val GALLERY_PERMISSION_REQUEST = 102
    private lateinit var binding: ActivityUserAddBinding
    private var selectedImageUri: Uri? = null
    private lateinit var nameText : EditText
    private lateinit var bitmap: Bitmap
    private var cam = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestCameraPermission()
        requestGalleryPermission()

        val imgView = binding.faceImage
        nameText = binding.userName

        // 카메라를 실행한 후 찍은 사진을 저장
        val getTakePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if(it) {
                selectedImageUri.let {
                    imgView.setImageURI(selectedImageUri)
                    cam = true
                }
            }
        }

        val galLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Handle the result from the launched activity here
                    val data: Intent? = result.data
                    //이미지 Url
                    val image = data?.data!!
                    selectedImageUri = image
                    contentResolver.openInputStream(image)?.use { inputStream ->
                        bitmap = BitmapFactory.decodeStream(inputStream)
                    }
                    imgView.setImageURI(selectedImageUri)
                    // Process the data
                    nameText.hint = "이름 입력"
                }
            }



        imgView.setOnClickListener {
            val builder = AlertDialog.Builder(this@UserAddActivity)
            builder.setTitle("이미지 선택")

            builder.setItems(arrayOf("카메라", "갤러리")) { _, which ->
                when(which) {
                    0 -> {
                        selectedImageUri = createImageFile()
                        getTakePicture.launch(selectedImageUri)   // Require Uri
                    }
                    1 -> {
                        galLauncher.launch(
                            Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                        )
                    }
                }
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            CAMERA_PERMISSION_REQUEST -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                }
//            }
//            GALLERY_PERMISSION_REQUEST -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                }
//            }
//        }
//    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        }
    }

    private fun requestGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), GALLERY_PERMISSION_REQUEST)
        }
    }

    private fun createImageFile(): Uri? {
        val content = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "img.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.user_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when(item.itemId) {
                R.id.check -> {
                    // 이미지 선택, 이름 입력 확인
                    if(nameText.text.toString() == "") {
                        Toast.makeText(baseContext, "이름 입력", Toast.LENGTH_SHORT).show()
                    }
                    else if(selectedImageUri == null) {
                        Toast.makeText(baseContext, "사진 선택", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        // 파일 저장
                        val imgFile: File = uriToFilePath(this@UserAddActivity, selectedImageUri!!)!!
                        uploadFile(imgFile, nameText.text.toString())
                    }
                }
            }
            return true
    }

    private fun uriToFilePath(context: Context, uri: Uri): File? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndex(filePathColumn[0])
            val filePath = it.getString(columnIndex)
            return File(filePath)
        }
        return null
    }

    private fun uploadFile(imageFile: File, imageFileName: String) {
        // 요청 파일
        val requestFile: RequestBody = imageFile.asRequestBody("multipart/form-data".toMediaType())
        // 이미지 이름 + 요청 파일을 합쳐 uploaded_file이라는 바디로 만듦
        val body: MultipartBody.Part = MultipartBody.Part.createFormData("uploaded_file", "$imageFileName.png", requestFile)

        MyApi().uploadRequest(body).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.e("uploadImg()", "성공 : $response")
                if(cam) {
                    imageFile.delete()
                }
                val host = "ec2-52-79-155-171.ap-northeast-2.compute.amazonaws.com"
                val port = 9000
                sendMessageToServer("file_download", host, port)
                finish()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("uploadImg()", "에러 : ${t.message}")
                if(cam) {
                    imageFile.delete()
                }
            }
        })
    }

    fun sendMessageToServer(message: String, host: String, port: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            var client: Socket? = null
            try {
                client = Socket(host, port)
                val writer = BufferedWriter(OutputStreamWriter(client.getOutputStream()))

                // 서버에 메시지 보내기
                writer.write(message)
                writer.newLine()
                writer.flush()
            } catch (e: IOException) {
                // Handle exceptions here, e.g., log or display an error message
                e.printStackTrace()
            } finally {
                try {
                    client?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}

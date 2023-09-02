package com.example.doorlock

import android.R.attr.height
import android.R.attr.width
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.doorlock.databinding.ActivityUserAddBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class UserAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserAddBinding
    private var selectedImageUri: Uri? = null
    private lateinit var nameText : EditText
    private lateinit var bitmap: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imgView = binding.faceImage
        nameText = binding.userName

        val camLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Handle the result from the launched activity here
                    val data : Intent? = result.data
                    bitmap = data?.extras?.get("data") as Bitmap
                    imgView.setImageURI(selectedImageUri)
                    nameText.hint = "이름 입력"
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
                    imgView.setImageBitmap(bitmap)
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
                        camLauncher.launch(
                            Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE).putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri)
                        )
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

    private fun saveToStorage(filename: String, bitmap: Bitmap): Uri? {
        val imageName = "$filename.png"
        var fos : OutputStream? = null
        var imgUri: Uri? = null
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also {resolver ->
                val contentValue = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/doorlock")
                }
                val imageUri : Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValue)
                fos = imageUri?.let {
                    resolver.openOutputStream(it)
                }
                imgUri = imageUri!!
            }
        }
        else {
            val imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/doorlock")
            val image = File(imageDirectory, imageName)
            imgUri = FileProvider.getUriForFile(
                this@UserAddActivity,
                "${applicationContext.packageName}.fileprovider",
                image
            )
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, it)
        }
        return imgUri
    }

    private fun uploadFile(imageFile: File, imageFileName: String) {
        // 요청 파일
        val requestFile: RequestBody = imageFile.asRequestBody("multipart/form-data".toMediaType())
        // 이미지 이름 + 요청 파일을 합쳐 uploaded_file이라는 바디로 만듦
        val body: MultipartBody.Part = MultipartBody.Part.createFormData("uploaded_file", "$imageFileName.png", requestFile)
        // 래트로핏 싱글톤에 인터페이스 적용
//        val retrofitInterface: RetrofitInterface = retrofit!!.create(RetrofitInterface::class.java)
        // equeue할 변수 선언
//        val call: Call<String> = retrofitInterface.request(body)

        MyApi().uploadRequest(body).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.e("uploadChat()", "성공 : $response")
//                imageFile.delete()
                finish()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("uploadChat()", "에러 : " + t.message)
                imageFile.delete()
            }
        })
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
}

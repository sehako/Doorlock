package com.example.doorlock

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream


class UserAddActivity : AppCompatActivity(), UploadRequestBody.UploadCallback {
    private lateinit var binding: ActivityUserAddBinding
    private var selectedImageUri: Uri? = null
    private lateinit var nameText : EditText
    private lateinit var imgView : ImageView
    private lateinit var bitmap: Bitmap
    private val retrofit = RetrofitTestClient.client
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imgView = binding.faceImage
        nameText = binding.userName
        val add_button = binding.addButton

        val camLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Handle the result from the launched activity here
                    val data : Intent? = result.data
                    bitmap = data?.extras?.get("data") as Bitmap
                    imgView.setImageBitmap(bitmap)
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

        add_button.setOnClickListener {
            val builder = AlertDialog.Builder(this@UserAddActivity)
            builder.setTitle("이미지 선택")

            builder.setItems(arrayOf("카메라", "갤러리")) { _, which ->
                when(which) {
                    0 -> {
                        camLauncher.launch(
                            Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri)
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
//        val homeViewModel = ViewModelProvider(this@UserAddActivity)[HomeViewModel::class.java]
            when(item.itemId) {
                R.id.check -> {
                    if(nameText.text.toString() == "") {
                        Toast.makeText(baseContext, "이름 입력", Toast.LENGTH_SHORT).show()
                    }
                    else if(imgView.drawable == null) {
                        Toast.makeText(baseContext, "사진 선택", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val imageUri = saveToStorage(nameText.text.toString(), bitmap)
                        val imgFile: File = uriToFile(this@UserAddActivity, imageUri!!)!!
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
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return imgUri
    }

    private fun uploadImage() {
        if (selectedImageUri == null) {
            Toast.makeText(this@UserAddActivity, "이미지 선택해 주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val parcelFileDescriptor = contentResolver.openFileDescriptor(
            selectedImageUri!!, "r", null
        ) ?: return

        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(cacheDir, contentResolver.getFileName(selectedImageUri!!))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        val body = UploadRequestBody(file, "image", callback = this)

        val requestBody = "".toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val customFileName = "${nameText.text.toString()}.png"
        val imagePart = MultipartBody.Part.createFormData("image", customFileName, body)

        MyApi().uploadImage(imagePart, requestBody).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                response.body()?.let {
                    saveToStorage(nameText.text.toString(), bitmap)
                    Toast.makeText(this@UserAddActivity, it.message, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                Toast.makeText(this@UserAddActivity, t.message!!, Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun ContentResolver.getFileName(selectedImageUri: Uri): String {
        var name = ""
        val returnCursor = this.query(selectedImageUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
    }
    override fun onProgressUpdate(percentage: Int) {
//        binding.progressBar.progress = percentage
    }

    private fun uploadFile(imageFile: File, imageFileName: String) {
        val requestFile: RequestBody = imageFile.asRequestBody("multipart/form-data".toMediaType())
        val body: MultipartBody.Part = MultipartBody.Part.createFormData("uploaded_file", "$imageFileName.png", requestFile)
        val retrofitInterface: RetrofitInterface = retrofit!!.create(RetrofitInterface::class.java)
        val call: Call<String> = retrofitInterface.request(body)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.e("uploadChat()", "성공 : $response")
                finish()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("uploadChat()", "에러 : " + t.message)
                imageFile.delete()
            }
        })
    }
    fun uriToFile(context: Context, uri: Uri): File? {
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

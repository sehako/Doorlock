package com.example.doorlock

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaActionSound
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.doorlock.databinding.ActivityUserAddBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class UserAddActivity : AppCompatActivity(), UploadRequestBody.UploadCallback {
    private lateinit var binding: ActivityUserAddBinding
    private var listCheck : Boolean = false
    private var selectedImageUri: Uri? = null
    private lateinit var nameText : EditText
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imgView = binding.faceImage
        nameText = binding.userName
        val extras = intent.extras

        if (extras != null) {
            listCheck = extras.getBoolean("list")
            if(listCheck) {
                nameText.setText(extras.getString("userName"))
                imgView.setImageURI(extras.getString("userFace")!!.toUri())
            }
        }

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

            builder.setItems(arrayOf("카메라", "갤러리")) { dialog, which ->
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.user_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.check -> {
                if(listCheck) {
                }
                else {
                    selectedImageUri = saveToStorage(nameText.text.toString(), bitmap)
                    uploadImage()
                    finish()
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
//            binding.layoutRoot.snackbar("Select an Image First")
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
//        binding.progressBar.progress = 0
        val body = UploadRequestBody(file, "image", callback = this)

        val requestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "")
        val imagePart = MultipartBody.Part.createFormData("image", file.name, body)

        MyApi().uploadImage(imagePart, requestBody).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                response.body()?.let {
//                    binding.layoutRoot.snackbar(it.message)
                    Toast.makeText(this@UserAddActivity, it.message, Toast.LENGTH_SHORT).show()
//                    binding.progressBar.progress = 100
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
//                binding.layoutRoot.snackbar(t.message!!)
                Toast.makeText(this@UserAddActivity, t.message!!, Toast.LENGTH_SHORT).show()
//                binding.progressBar.progress = 0
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
}

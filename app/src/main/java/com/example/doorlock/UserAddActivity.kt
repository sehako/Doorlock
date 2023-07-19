package com.example.doorlock

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.doorlock.databinding.ActivityUserAddBinding

class UserAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserAddBinding
    var listCheck : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imgView = binding.faceImage
        val nameText:EditText = binding.userName
        val extras = intent.extras

        if (extras != null) {
            listCheck = extras.getBoolean("list")
            if(listCheck) {
                nameText.setText(extras.getString("userName"))
                extras.getString("userFace")
            }
        }

        val camLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Handle the result from the launched activity here
                    val data : Intent? = result.data
                    val bitmap : Bitmap = data?.extras?.get("data") as Bitmap
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
                    val image = data?.data
                    imgView.setImageURI(image)
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
                    Toast.makeText(this@UserAddActivity, "수정 완료", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(this@UserAddActivity, "업로드 완료", Toast.LENGTH_LONG).show()
                }
                finish()
            }
        }
        return true
    }
}

package com.example.doorlock.ui.home

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doorlock.R
import com.example.doorlock.UserListAdapter
import com.example.doorlock.Users
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File


class HomeFragment : Fragment() {
    private var userList = arrayListOf<Users>(
        Users("소순성", ""),
        Users("이종석", ""),
        Users("안진원", ""),
        Users("오세학", "")
    )

    var image: Uri? = null
    var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        val camLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK)
            {
                // Handle the result from the launched activity here
                val data: Intent? = result.data
                val bitmap: Bitmap = result.data?.extras?.get("data") as Bitmap
                imageBitmap = data?.extras?.get("data") as Bitmap

                Toast.makeText(requireContext(), ""+image, Toast.LENGTH_SHORT).show()
            }
        }
//        val galLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia())
//        { uri ->
//            Toast.makeText(requireContext(), "" + uri, Toast.LENGTH_LONG).show()
//        }
        val galLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                image = data?.data
                val imagePath = getPathFromUri(image)


                if (imagePath != null) {
                    uploadImageToServer(imagePath) // 이미지를 서버로 업로드
                } else {
                    Toast.makeText(requireContext(), "Invalid image file.", Toast.LENGTH_LONG).show()
                }
            }
        }




        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider
        {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater)
            {
                menuInflater.inflate(R.menu.user_add, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean
            {
                return when (menuItem.itemId)
                {
                    R.id.add_camera ->
                    {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
                        camLauncher.launch(intent)
                        true
                    }
                    else ->
                    {
                        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        galLauncher.launch(intent)
                        true
                    }
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val users: RecyclerView = view.findViewById(R.id.rv_profile)

        val userAdapter = UserListAdapter(requireContext(), userList)
        users.adapter = userAdapter

        val linearmanager = LinearLayoutManager(requireContext())
        users.layoutManager = linearmanager

        return view
    }

    fun getPathFromUri(uri: Uri?): String? {
        uri ?: return null

        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
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

    private fun uploadImageToServer(imagePath: String?) {
        val file = File(imagePath)
        val requestFile: RequestBody? = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body: MultipartBody.Part? = requestFile?.let {
            MultipartBody.Part.createFormData("image", file.name, it)
        }

        if (body != null) {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://52.79.155.171")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val uploadAPIs = retrofit.create(UploadAPIs::class.java)
            val call: Call<ResponseBody> = uploadAPIs.uploadImage(body)

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    Toast.makeText(requireContext(), "Image uploaded successfully.", Toast.LENGTH_LONG).show()
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(requireContext(), "Image upload failed.", Toast.LENGTH_LONG).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "Invalid image file.", Toast.LENGTH_LONG).show()
        }
    }

    interface UploadAPIs {
        @Multipart
        @POST("upload.php")
        fun uploadImage(@Part image: MultipartBody.Part): Call<ResponseBody>
    }
}

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
import java.io.File


class HomeFragment : Fragment() {
    private var userList = arrayListOf<Users>(
        Users("소순성", ""),
        Users("이종석", ""),
        Users("안진원", ""),
        Users("오세학", "")
    )
    val imagePath: String? = null
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
       val galLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result -> if (result.resultCode == Activity.RESULT_OK)
            {
                // Handle the result from the launched activity here
                val data: Intent? = result.data
                // Image의 상대경로를 가져온다
                image = data?.data
                // 절대경로를 가져오는 함수
                val imagePath: String? = getPathFromUri(image)
                Toast.makeText(requireContext(), "" + imagePath, Toast.LENGTH_LONG).show()
//                // File변수에 File을 집어넣는다
                val destFile = imagePath?.let { File(it) }
                // Process the data
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

        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
        cursor?.moveToNext()

        val columnIndex = cursor?.getColumnIndex("_data")
        val path = if (columnIndex != null && columnIndex >= 0) {
            cursor.getString(columnIndex)
        } else {
            null
        }

        cursor?.close()
        return path
    }
}
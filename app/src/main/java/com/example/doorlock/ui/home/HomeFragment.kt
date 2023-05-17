package com.example.doorlock.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doorlock.R
import com.example.doorlock.UserListAdapter
import com.example.doorlock.Users

class HomeFragment : Fragment() {

    private val REQUEST_CODE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    private var userList = arrayListOf<Users>(
        Users("소순성", ""),
        Users("이종석", ""),
        Users("안진원", ""),
        Users("오세학", "")
    )


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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_camera -> {

                true
            }
            R.id.add_gallery -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.setType("image/*")
                intent.setAction(Intent.ACTION_GET_CONTENT)

                startActivityForResult(intent, REQUEST_CODE)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
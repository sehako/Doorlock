package com.example.doorlock.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doorlock.R
import com.example.doorlock.UserListAdapter
import com.example.doorlock.Users


class HomeFragment : Fragment() {
    private var userList = arrayListOf<Users>(
        Users("소순성", ""),
        Users("이종석", ""),
        Users("안진원", ""),
        Users("오세학", "")
    )

    private var galLauncher : ActivityResultLauncher<Intent>? = null
    private var camLauncher : ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        camLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result from the launched activity here
                val data: Intent? = result.data
                // Process the data
            }
        }
        galLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result from the launched activity here
                val data: Intent? = result.data
                // Process the data
            }
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.user_add, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.add_camera -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
                        camLauncher?.launch(intent)
                        true
                    }
                    else -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.setType("image/*")
                        camLauncher?.launch(intent)
                        true
                    }
                }
            }
        })
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
}
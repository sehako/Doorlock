package com.example.doorlock.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doorlock.R
import com.example.doorlock.UserListAdapter
import com.example.doorlock.Users

class HomeFragment : Fragment() {
    var userList = arrayListOf<Users>(
        Users("소순성", "2023/05/06", ""),
        Users("이종석", "2023/05/06", ""),
        Users("안진원", "2023/05/06", ""),
        Users("오세학", "2023/05/05", "")
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

        val add: Button = view.findViewById(R.id.camera_test)
        add.setOnClickListener(View.OnClickListener {
            val items = arrayOf("사진 촬영", "갤러리")
            val builder = AlertDialog.Builder(context)
                .setTitle("사용자 추가").setItems(items) { dialog, which ->
                    if (which == 0) {
                        Toast.makeText(context, "카메라 촬영", Toast.LENGTH_LONG).show()
                    }
                    else {
                        Toast.makeText(context, "갤러리 접근", Toast.LENGTH_LONG).show()
                    }
                }
            builder.show()
            }
        )
        return view
    }
}
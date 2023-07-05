package com.example.doorlock.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doorlock.ImageProcessing
import com.example.doorlock.MainActivity
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val fab: View = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            val mActivity = activity as MainActivity
            mActivity.changeFrag(list = true , add_menu = true)
        }
        val users: RecyclerView = view.findViewById(R.id.rv_profile)

        val userAdapter = UserListAdapter(requireContext(), userList)
        users.adapter = userAdapter

        val lmanager = LinearLayoutManager(requireContext())
        users.layoutManager = lmanager
        return view
    }
}
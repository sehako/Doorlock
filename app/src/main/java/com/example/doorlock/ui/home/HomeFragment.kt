package com.example.doorlock.ui.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doorlock.R
import com.example.doorlock.UserAddActivity
import com.example.doorlock.UserListAdapter
import com.example.doorlock.Users
import com.example.doorlock.databinding.FragmentHomeBinding
import com.example.doorlock.databinding.FragmentNotificationsBinding
import com.example.doorlock.ui.notifications.NotificationsViewModel


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val fab: View = binding.fab
        val users: RecyclerView = binding.rvProfile
        val userAdapter = UserListAdapter(requireContext(), userList)
        val linearManager = LinearLayoutManager(requireContext())

        fab.setOnClickListener {
            val intent = Intent(requireContext(), UserAddActivity::class.java)
            startActivity(intent)
        }
        users.adapter = userAdapter
        users.layoutManager = linearManager

        userAdapter.setOnItemClickListener(object : UserListAdapter.OnItemClickListener {
            override fun onItemClick(v: View, data: Users, pos: Int) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("${data.name}을 수정하시겠습니까?")
                builder.setPositiveButton("확인") { dialog, which ->
                    val intent = Intent(requireContext(), UserAddActivity::class.java)
                    intent.putExtra("list", true)
                    intent.putExtra("userName", data.name)
                    intent.putExtra("userFace", data.img)
                    startActivity(intent)
                }
                builder.setNegativeButton("취소") { dialog, which ->
                    dialog.cancel()
                }
                val dialog = builder.create()
                dialog.show()
            }
        })

        userAdapter.setOnLongItemClickListener(object : UserListAdapter.OnItemLongClickListener {
            override fun onItemLongClick(v: View, data: Users, pos: Int) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("${data.name}을 삭제하시겠습니까?")
                builder.setPositiveButton("확인") { dialog, which ->
                    userAdapter.notifyItemRemoved(pos)
                    userList.removeAt(pos)
                }
                builder.setNegativeButton("취소") { dialog, which ->
                    dialog.cancel()
                }
                val dialog = builder.create()
                dialog.show()
            }
        })

        return root
    }
}
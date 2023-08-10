package com.example.doorlock.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doorlock.UserAddActivity
import com.example.doorlock.UserListAdapter
import com.example.doorlock.Users
import com.example.doorlock.databinding.FragmentHomeBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File


class HomeFragment : Fragment(), Observer<List<Users>> {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
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
        val userAdapter = UserListAdapter(requireContext(), homeViewModel._userList)
        val linearManager = LinearLayoutManager(requireContext())

        getImageFromFile(homeViewModel._userList)

        homeViewModel.userList. observe(requireActivity(), this)

        fab.setOnClickListener {
            if(checkForInternet(requireContext())) {
                val intent = Intent(requireContext(), UserAddActivity::class.java)
                startActivity(intent)
            }
            else {
                Toast.makeText(requireContext(), "인터넷이 연결되어 있지 않습니다!", Toast.LENGTH_LONG).show()
            }
        }
        users.adapter = userAdapter
        users.layoutManager = linearManager

        userAdapter.setOnLongItemClickListener(object : UserListAdapter.OnItemLongClickListener {
            override fun onItemLongClick(v: View, data: Users, pos: Int) {
                if(checkForInternet(requireContext())) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("${data.name}을 삭제하시겠습니까?")
                    builder.setPositiveButton("확인") { dialog, which ->
//                    Toast.makeText(requireContext(), "${getPathFromUri(data.img.toUri())}", Toast.LENGTH_LONG).show()
                        userAdapter.notifyItemRemoved(pos)
                        homeViewModel.deleteUser(pos)
                        val file = File(data.img.toUri().path!!)
                        if (file.exists()) {
                            val file2 = File(file.absolutePath)
                            file2.delete()
                            Toast.makeText(context, "사용자 삭제", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "사용자 삭제 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                    builder.setNegativeButton("취소") { dialog, which ->
                        dialog.cancel()
                    }
                    val dialog = builder.create()
                    dialog.show()
                }
                else {
                    Toast.makeText(requireContext(), "인터넷 연결 없음", Toast.LENGTH_LONG).show()
                }
            }
        })
        return root
    }
    private fun getImageFromFile(userList: ArrayList<Users>) {
        userList.clear()
        val imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/doorlock").path
        val file = File(imageDirectory)
        val files = file.listFiles()
        if(files != null) {
            for(onefile in files) {
                val fileUri = onefile.toURI().toString()
                userList.add(Users(onefile.name, fileUri))
            }
        }
    }
    private fun checkForInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    override fun onChanged(value: List<Users>) {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]
        getImageFromFile(homeViewModel._userList)
    }
}
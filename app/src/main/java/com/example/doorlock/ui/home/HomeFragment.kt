package com.example.doorlock.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.DnsResolver
import android.net.DnsResolver.Callback
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import com.example.doorlock.MyApi
import com.example.doorlock.UserAddActivity
import com.example.doorlock.UserInfo
import com.example.doorlock.UserListAdapter
import com.example.doorlock.Users
import com.example.doorlock.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Response
import java.io.File
import kotlin.concurrent.thread


class HomeFragment : Fragment() {
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
        val userAdapter = UserListAdapter(requireContext(), homeViewModel.userList)
        val linearManager = LinearLayoutManager(requireContext())

        userInfo()

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
                    builder.setPositiveButton("확인") { _, _ ->
                        deleteImage(userName = data.name)
                        userAdapter.notifyItemRemoved(pos)
                        homeViewModel.userList.removeAt(pos)
                    }
                    builder.setNegativeButton("취소") { dialog, _ ->
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

    private fun deleteImage(userName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MyApi().deleteRequest(userName).enqueue(object : retrofit2.Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    Log.e("uploadChat()", "성공 : $response")
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("uploadChat()", "에러 : " + t.message)
                }
            })
        }
    }

    private fun userInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MyApi().getInfo().enqueue(object : retrofit2.Callback<List<UserInfo>> {
                override fun onResponse(call: Call<List<UserInfo>>, response: Response<List<UserInfo>>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data != null) {
                            for (fileData in data) {
                                val fileName = fileData.name
                                val filePath = fileData.path
                                Log.d("userInfo()", "$fileName, $filePath")
                            }
                        }
                        else {
                            Log.e("userInfo()", "서버 응답이 null입니다.")
                        }
                    } else {
                        Log.e("userInfo()", "서버 응답이 실패했습니다. 코드: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<UserInfo>>, t: Throwable) {
                    Log.e("userInfo()", "에러 : " + t.message)
                }
            })
        }
    }
}
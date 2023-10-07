package com.example.doorlock.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doorlock.MyApi
import com.example.doorlock.Records

import com.example.doorlock.databinding.FragmentNotificationsBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SearchResultsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView: RecyclerView = binding.log
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = SearchResultsAdapter(emptyList())
        recyclerView.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
//                    searchDatabaseViaPHP(query)
                    getRecord(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        // 데이터베이스에서 모든 데이터를 가져와서 표시
//        fetchAllDataFromDatabase()
        getRecord("null")

        return root
    }

    private fun getRecord(name: String)
    {
        MyApi().getLog(name).enqueue(object : retrofit2.Callback<List<Records>> {
            override fun onResponse(
                call: retrofit2.Call<List<Records>>,
                response: retrofit2.Response<List<Records>>
            ) {
                val searchResults = mutableListOf<SearchResult>()
                val records = response.body()
                if(records != null)
                {
                    for(record in records)
                    {
                        searchResults.add(SearchResult(record.name, record.name))
                    }
                }
                requireActivity().runOnUiThread {
                    adapter.updateData(searchResults)
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Records>>, t: Throwable) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "네트워크 오류: $t", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

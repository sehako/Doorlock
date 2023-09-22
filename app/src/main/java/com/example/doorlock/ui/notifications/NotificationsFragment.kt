package com.example.doorlock.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

                    searchDatabaseViaPHP(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })
        return root
    }

    private fun searchDatabaseViaPHP(query: String) {
        val url = "http://52.79.155.171/search.php"


        val request = Request.Builder()
            .url("$url?query=$query")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "네트워크 오류: $e", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()


                if (!responseData.isNullOrEmpty()) {

                    val newSearchResults = parseSearchResults(responseData)


                    requireActivity().runOnUiThread {
                        adapter.updateData(newSearchResults)
                    }
                } else {

                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "검색 결과가 없습니다.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }


    private fun parseSearchResults(responseData: String): List<SearchResult> {
        val searchResults = mutableListOf<SearchResult>()
        try {
            val jsonArray = JSONArray(responseData)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val name = jsonObject.getString("name")
                val date = jsonObject.getString("date")

                val result = SearchResult(name, date)
                searchResults.add(result)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return searchResults
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
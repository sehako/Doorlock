package com.example.doorlock.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.doorlock.R
import com.example.doorlock.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val switch: Switch = view.findViewById(R.id.unlock)

        switch.setOnCheckedChangeListener({_, isChecked ->
            val message = if (isChecked) "잠금해제" else "잠금"
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        })

        return view
    }
}
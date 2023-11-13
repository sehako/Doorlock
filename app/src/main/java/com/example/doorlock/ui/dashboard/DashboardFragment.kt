package com.example.doorlock.ui.dashboard

import android.hardware.biometrics.BiometricManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.doorlock.databinding.FragmentDashboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.Executor
import kotlin.concurrent.thread


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this)[DashboardViewModel::class.java]
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val unlockButton: Button = binding.unlock

        unlockButton.setOnClickListener {
            // 지문 인식 성공 시 잠금 해제 신호 보냄
            val host = "ec2-52-79-155-171.ap-northeast-2.compute.amazonaws.com"
            val port = 9000
            sendMessageToServer("motor_operate", host, port)
        }
        return root
    }

    fun sendMessageToServer(message: String, host: String, port: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            var client: Socket? = null
            try {
                client = Socket(host, port)
                val writer = BufferedWriter(OutputStreamWriter(client.getOutputStream()))

                // 서버에 메시지 보내기
                writer.write(message)
                writer.newLine()
                writer.flush()
            } catch (e: IOException) {
                Log.e("Socket", e.printStackTrace().toString())
                e.printStackTrace()
            } finally {
                try {
                    client?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
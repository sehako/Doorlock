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
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
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


        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(requireContext(), "인증", Toast.LENGTH_LONG).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()

        unlockButton.setOnClickListener {
            // 지문 인식 성공 시 잠금 해제 신호 보냄
//            biometricPrompt.authenticate(promptInfo)
//            thread(start = true) {
//                // 소캣 설정, 연결
                val socketAdress = "ec2-52-79-155-171.ap-northeast-2.compute.amazonaws.com"
////                val HOST = "172.17.193.17"
////                val PORT = 9999
////                val client = Socket(HOST, PORT)
//
//
//                // 데이터 전송
////                val outputStream = socket.getOutputStream()
////                val writer = PrintWriter(
////                    BufferedWriter(OutputStreamWriter(outputStream, "UTF-8")), true)
//                val outputStream: OutputStream = client.getOutputStream()
//                val outputStreamWriter = OutputStreamWriter(outputStream)
//
//                val message = "2"
//                outputStreamWriter.write(message)
//                outputStreamWriter.flush()
//                outputStreamWriter.close()
//                client.close()
//            }

            val HOST = "ec2-52-79-155-171.ap-northeast-2.compute.amazonaws.com"
            val PORT = 9000
            sendMessageToServer("3", HOST, PORT)
        }
        return root
    }

    fun sendMessageToServer(message: String, HOST: String, PORT: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            var client: Socket? = null
            try {
                client = Socket(HOST, PORT)
//                val outputStream: OutputStream = client.getOutputStream()
//                val outputStreamWriter = OutputStreamWriter(outputStream)
//
//                outputStreamWriter.write(message)
//                outputStreamWriter.flush()
                val writer = BufferedWriter(OutputStreamWriter(client.getOutputStream()))

                // 서버에 메시지 보내기
                writer.write(message)
                writer.newLine()
                writer.flush()
            } catch (e: IOException) {
                // Handle exceptions here, e.g., log or display an error message
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
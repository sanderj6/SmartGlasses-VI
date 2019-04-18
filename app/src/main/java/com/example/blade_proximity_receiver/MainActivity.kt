package com.example.blade_proximity_receiver

import android.support.v7.app.AppCompatActivity
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.util.Log
import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.text.Charsets.US_ASCII


class MainActivity : AppCompatActivity() {

    private var outStream: OutputStream? = null
    private var inStream: InputStream? = null
    val blueAdapter = BluetoothAdapter.getDefaultAdapter()
    val NAME = "ServerPOS"
    val UUID = java.util.UUID.fromString("08794f7e-8d41-47f2-ad9d-be7e696884ca")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //btn_btnServerSetup.setOnClickListener{
        //    serverSetup()
        //}

        btn_btnConnect.setOnClickListener{
            clientConnect()
        }

    }

@Throws(IOException::class)
private fun clientConnect() {
    if (blueAdapter != null) {
        if (blueAdapter.isEnabled) {
            val bondedDevices = blueAdapter.bondedDevices

            if (bondedDevices.size > 0) {
                val MY_UUID = UUID
                val devices = bondedDevices.toTypedArray() as Array<Any>
                val device = devices[0] as BluetoothDevice
                val uuids = device.uuids
                var socket = device.createRfcommSocketToServiceRecord(MY_UUID)

                try {
                    socket.connect()
                    Log.e("", "Connected")
                } catch (e: IOException) {
                    Log.e("", e.message)
                    try {
                        Log.e("", "trying fallback...")

                        socket = device.javaClass.getMethod(
                            "createRfcommSocket",
                            *arrayOf<Class<*>>(Int::class.javaPrimitiveType!!)
                        ).invoke(device, 2) as BluetoothSocket
                        socket.connect()
                        val testsocket = socket
                        Log.e("", "Connected")
                    } catch (e2: Exception) {
                        Log.e("", "Couldn't establish Bluetooth connection!")
                    }

                }
                textMeasurement.text = "CONNECTED"
                readBuffer(socket) //Read incoming socket data
            }
        } else {
            Log.e("error", "Bluetooth is disabled.")
        }
    }
}

private fun readBuffer(s: BluetoothSocket){
    val mmInStream: InputStream = s.inputStream
    val mmBuffer = ByteArray(1024) // mmBuffer store for the stream
    val mmBufferOutput = ByteArray(1024) // mmBuffer store for the stream
    var numBytes: Int // bytes returned from read()


    // Listen for Input Stream
    while (true) {
        numBytes = try {
            mmInStream.read(mmBuffer)
        } catch (e: IOException) {
            //Log.d(TAG, "Input stream was disconnected", e)
            break
        }

        // Parse buffer and update UI
        var i: Int = 0//iterator

        while (mmBuffer[i].toString() != "35"){
            mmBufferOutput[i] = mmBuffer[i]
            i++
        }
        val readMsg = mmBufferOutput.toString()
        textMeasurement.text = readMsg
    }
}

private fun serverSetup() {

    var socket: BluetoothSocket ?= null
    val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) { //Bluetooth Server Socket Setup
        blueAdapter?.listenUsingInsecureRfcommWithServiceRecord(NAME, UUID)
    }

    // Listen for Socket
    var shouldLoop = true
    while (shouldLoop) {
        socket = try {
            mmServerSocket?.accept() //Accept Socket, create connection
        } catch (e: IOException) {
            //Log.e(TAG, "Socket's accept() method failed", e)
            shouldLoop = false
            null
        }
        socket?.also {
            //manageMyConnectedSocket(it)
            mmServerSocket?.close()
            shouldLoop = false
        }
    }

    //Declare Output Stream
    outStream = socket?.outputStream

    val teststring = "butt" //**REPLACE WITH DISTANCEmm + #
    while(true) {
        outStream!!.write(teststring.toByteArray())
    }
}

}

package com.example.blade_proximity_receiver
import android.support.v7.app.AppCompatActivity
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.util.Log
import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothDevice
import android.media.MediaPlayer
import android.view.View.INVISIBLE
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import android.os.AsyncTask
import android.os.Handler
import android.view.View
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.util.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener




class MainActivity : AppCompatActivity() {

    private var outStream: OutputStream? = null
    private var inStream: InputStream? = null
    val blueAdapter = BluetoothAdapter.getDefaultAdapter()
    val UUID = java.util.UUID.fromString("08794f7e-8d41-47f2-ad9d-be7e696884ca")
    var socket: BluetoothSocket ?= null
    var mmInStream: InputStream ?= null
    val mmBuffer = ByteArray(1)
    var outMsg = ""
    var i = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        callAsynchronousTask(outMsg)

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
                socket = device.createRfcommSocketToServiceRecord(MY_UUID)

                try {
                    socket?.connect()
                    Log.e("", "Connected")
                } catch (e: IOException) {
                    Log.e("", e.message)
                    try {
                        Log.e("", "trying fallback...")

                        socket = device.javaClass.getMethod(
                            "createRfcommSocket",
                            *arrayOf<Class<*>>(Int::class.javaPrimitiveType!!)
                        ).invoke(device, 2) as BluetoothSocket
                        socket?.connect()
                        Log.e("", "Connected")
                    } catch (e2: Exception) {
                        Log.e("", "Couldn't establish Bluetooth connection!")
                    }

                }
                toast("Bluetooth CONNECTED")
                btn_btnConnect.visibility = INVISIBLE
            }
        } else {
            Log.e("error", "Bluetooth is disabled.")
        }
    }
}

    fun callAsynchronousTask(result: String?) {
        val handler = Handler()
        val timer = Timer()
        val doAsynchronousTask = object : TimerTask() {
            override fun run() {
                handler.post(Runnable {
                    try {
                        doAsync{
                            mmInStream = socket!!.inputStream
                            mmInStream?.read(mmBuffer)

                            if (mmBuffer.toTypedArray()[0] > 0) {

                                // Parse buffer and update UI
                                val readMsg = mmBuffer.toTypedArray()[0]
                                val readMsg2 = readMsg.toInt()
                                outMsg = readMsg2.toString() + "m"
                                runOnUiThread() { textMeasurement.text = outMsg }
                            }

                        }
                    } catch (e: Exception) {
                        // TODO Auto-generated catch block
                        val testbreak = "butt"
                    }
                })
            }
        }
        timer.schedule(doAsynchronousTask, 0, 1000) //execute in every 1000 ms
    }

    internal inner class ReadBuffer : AsyncTask<Void, String, String>() {
        var numcycles: Int = 0  //total number of times to execute process

        override fun onPreExecute() {
            //Executes in UI thread before task begins
            btn_btnConnect.text = "..."
        }

        override fun doInBackground(vararg arg0: Void): String {
            //Runs in a background thread
            //Used to run code that could block the UI
            var outMsg = ""

            try {
                mmInStream = socket!!.inputStream
                mmInStream?.read(mmBuffer)

                if(mmBuffer.toTypedArray()[0] > 0){

                    // Parse buffer and update UI
                    val readMsg = mmBuffer.toTypedArray()[0]
                    val readMsg2 = readMsg.toInt()
                    outMsg = readMsg2.toString() + "m"

                }
            } catch (e: IOException) {
                Log.d("Bluetooth no work", "Input stream was disconnected", e)
                throw e
            }
            //publishProgress(outMsg)
            return outMsg
        }

        override fun onProgressUpdate(vararg values: String) {
            textMeasurement.text = values.toString()
            super.onProgressUpdate(*values)
        }
        override fun onPostExecute(result: String?) {
            //result comes from return value of doInBackground
            //runs on UI thread, not called if task cancelled
            btn_btnConnect.text = "done"
            textMeasurement.text = result
            btn_btnConnect.visibility = INVISIBLE
        }
    }
}

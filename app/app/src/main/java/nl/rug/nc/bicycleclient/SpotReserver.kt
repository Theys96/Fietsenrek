package nl.rug.nc.bicycleclient

import android.util.Log
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ConnectException
import java.net.Socket

class SpotReserver (private val slot: Int, private val ip: String, private val callbackObject: Callbackable<Int>): Runnable {

    override fun run() {
        var code = -1;
        try {
            val socket = Socket(ip, 8900)
            val oos = ObjectOutputStream(socket.getOutputStream())
            val ois = ObjectInputStream(socket.getInputStream())
            oos.writeInt(slot);
            oos.flush();
            code = ois.readInt();
            oos.close()
            ois.close()
        } catch (ce: ConnectException) {
            Log.e("ConnectionError", "Could not connect to rack!")
        }
        callbackObject.callback(code)
    }

}
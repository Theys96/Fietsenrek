package nl.rug.nc.bicycleclient

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class SpotReserver (private val slot: Int, private val ip: String, private val callbackObject: Callbackable<Int>): Runnable {

    override fun run() {
        val socket = Socket(ip, 8900)
        val oos = ObjectOutputStream(socket.getOutputStream())
        val ois = ObjectInputStream(socket.getInputStream())
        oos.writeInt(slot);
        oos.flush();
        val code = ois.readInt();
        oos.close()
        ois.close()
        callbackObject.callback(code)
    }

}
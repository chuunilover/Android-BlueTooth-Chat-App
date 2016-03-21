package ca.toronto.csc301.chat;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.example.siddharthgautam.csc301.chatActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler mHandler;

    public ConnectedThread(BluetoothSocket socket, Handler h) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        mHandler = h;
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public BluetoothSocket getSocket(){
        return mmSocket;
    }

    public void run() {
        byte[] buffer = new byte[8000];  // buffer store for the stream 3MB
        int bytes; // bytes returned from read()
        //chatActivity.getInstance().recieveMessage("I'm listening for messages");
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                //String readMessage = new String(buffer, 0, bytes);
                mHandler.obtainMessage(1, bytes, -1, buffer)
                        .sendToTarget();
                //chatActivity.getInstance().recieveMessage(readMessage);
            } catch (IOException e) {
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    public void sendMessage(String message) {
        // Check that there's actually something to send
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            write(send);
        }
    }

    public void sendEvent(Event e){
        try {
            write(Event.serialize(e));
        }
        catch(Exception ex){

        }
    }
    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
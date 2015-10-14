package com.example.djdetection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class MyBluetoothService extends Service{

    /**
	 * 
	 */
	// Debugging  
    private static final String TAG = "MyBluetoothOperator";  
    private static final boolean D = true;  
  
    // Name for the SDP record when creating server socket  
    private static final String NAME = "BluetoothOperator";  
  
    // Unique UUID for this application  
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
  
    // Member fields  
    private BluetoothAdapter mAdapter = null;  
//    private Handler mHandler = null;
    private AcceptThread mAcceptThread;  
    private ConnectThread mConnectThread;  
    private ConnectedThread mConnectedThread;
    private int mState;
    private BltServiceBinder mBltBinder;
  
    // Constants that indicate the current connection state  
    public static final int STATE_NONE = 0;       // we're doing nothing  
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections  
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection  
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device  
    public static int debugeTimes = 0;
    
    
//    public static final String ACTION_NONE = "ACTION_NONE";
//    public static final String ACTION_LISTEN = "ACTION_LISTEN";
//    public static final String ACTION_CONNECTING = "ACTION_CONNECTING";
//    public static final String ACTION_CONNECTED = "ACTION_CONNECTED";
    public static final String MESSAGE_STATE_CHANGE = "MESSAGE_STATE_CHANGE";
    public static final String MESSAGE_READ = "MESSAGE_STATE_CHANGE";  
    public static final String MESSAGE_WRITE = "MESSAGE_STATE_CHANGE";  
    public static final String MESSAGE_DEVICE_NAME = "MESSAGE_STATE_CHANGE";  
    public static final String MESSAGE_TOAST = "MESSAGE_STATE_CHANGE"; 
    /** 
     * Constructor. Prepares a new BluetoothChat session. 
     * @param context  The UI Activity Context 
     * @param handler  A Handler to send messages back to the UI Activity 
     */   
  
    /** 
     * Set the current state of the chat connection 
     * @param state  An integer defining the current connection state 
     */  
    private synchronized void setState(int state) {  
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);  
        mState = state;  
  
        // Give the new state to the Handler so the UI Activity can update  
//        mHandler.obtainMessage(DeviceControlActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget(); 
        Intent mIntent = new Intent(MyBluetoothService.MESSAGE_STATE_CHANGE);
        mIntent.putExtra("mState", mState);  
        sendBroadcast(mIntent); 
    }  
  
    /** 
     * Return the current connection state. */  
    public synchronized int getState() {  
        return mState;  
    }  
  
    /** 
     * Start the chat service. Specifically start AcceptThread to begin a 
     * session in listening (server) mode. Called by the Activity onResume() */  
    public synchronized void start() {  
        if (D) Log.d(TAG, "start");  
  
        // Cancel any thread attempting to make a connection  
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}  
  
        // Cancel any thread currently running a connection  
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
  
        // Start the thread to listen on a BluetoothServerSocket  
        if (mAcceptThread == null) {  
            mAcceptThread = new AcceptThread();  
            mAcceptThread.start();  
        }  
        setState(STATE_LISTEN);  
    }  
  
    /** 
     * Start the ConnectThread to initiate a connection to a remote device. 
     * @param device  The BluetoothDevice to connect 
     */  
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);  
  
        // Cancel any thread attempting to make a connection  
        if (mState == STATE_CONNECTING) {  
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}  
        }  
  
        // Cancel any thread currently running a connection  
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}  
  
        // Start the thread to connect with the given device  
        mConnectThread = new ConnectThread(device);  
        mConnectThread.start();  
        setState(STATE_CONNECTING);  
    }  
  
    /** 
     * Start the ConnectedThread to begin managing a Bluetooth connection 
     * @param socket  The BluetoothSocket on which the connection was made 
     * @param device  The BluetoothDevice that has been connected 
     */  
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");  
//        debugeTimes = debugeTimes + 1;
        // Cancel the thread that completed the connection  
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}  
  
        // Cancel any thread currently running a connection  
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}  
  
        // Cancel the accept thread because we only want to connect to one device  
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}  
  
        // Start the thread to manage the connection and perform transmissions  
        mConnectedThread = new ConnectedThread(socket);  
        mConnectedThread.start();  
  
        // Send the name of the connected device back to the UI Activity  
//        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_NAME);  
//        Bundle bundle = new Bundle();  
//        bundle.putString(BluetoothChat.DEVICE_NAME, device.getName());  
//        msg.setData(bundle);  
//        mHandler.sendMessage(msg);  
  
        setState(STATE_CONNECTED);  
    }  
  
    /** 
     * Stop all threads 
     */  
    public synchronized void stop() {  
        if (D) Log.d(TAG, "stop");  
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}  
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}  
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}  
        setState(STATE_NONE);  
    }  
  
    /** 
     * Write to the ConnectedThread in an unsynchronized manner 
     * @param out The bytes to write 
     * @see ConnectedThread#write(byte[]) 
     */  
    public boolean write(byte[] out) {  
        // Create temporary object  
        ConnectedThread r;  
        // Synchronize a copy of the ConnectedThread  
        synchronized (this) {  
            if (mState != STATE_CONNECTED) return false;  
            r = mConnectedThread;  
        }  
        // Perform the write unsynchronized  
        return r.write(out);
    }  
  
    /** 
     * Indicate that the connection attempt failed and notify the UI Activity. 
     */  
    private void connectionFailed() {  
        setState(STATE_LISTEN);  
  
        // Send a failure message back to the Activity  
//        Message msg = mHandler.obtainMessage(DeviceControlActivity.MESSAGE_TOAST);  
//        Bundle bundle = new Bundle();  
//        bundle.putString(DeviceControlActivity.TOAST, "Unable to connect device");  
//        msg.setData(bundle);  
//        mHandler.sendMessage(msg);  
        Intent mIntent = new Intent(MyBluetoothService.MESSAGE_TOAST);
        mIntent.putExtra("toastMessage", "Unable to connect device");
        sendBroadcast(mIntent);
    }  
  
    /** 
     * Indicate that the connection was lost and notify the UI Activity. 
     */  
    private void connectionLost() {  
        setState(STATE_LISTEN);
  
        // Send a failure message back to the Activity  
//        Message msg = mHandler.obtainMessage(DeviceControlActivity.MESSAGE_TOAST);  
//        Bundle bundle = new Bundle();  
//        bundle.putString(DeviceControlActivity.TOAST, "Device connection was lost");  
//        msg.setData(bundle);  
//        mHandler.sendMessage(msg);
        
        Intent mIntent = new Intent(MyBluetoothService.MESSAGE_TOAST);
        mIntent.putExtra("toastMessage", "Device connection was lost");
        sendBroadcast(mIntent);
    }  
  
    /** 
     * This thread runs while listening for incoming connections. It behaves 
     * like a server-side client. It runs until a connection is accepted 
     * (or until cancelled). 
     */   
    private class AcceptThread extends Thread {  
        // The local server socket  
        private final BluetoothServerSocket mmServerSocket;  
  
        public AcceptThread() {  
            BluetoothServerSocket tmp = null;  

            // Create a new listening server socket  
            try {
                //开启监听  
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);  
            }catch (IOException e) {  
                Log.e(TAG, "listen() failed", e);  
            } 
            mmServerSocket = tmp;  
        }  
        
        public void run() {  
            if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);  
            setName("AcceptThread");  
            BluetoothSocket socket = null;
  
            // Listen to the server socket if we're not connected  
            while (mState != STATE_CONNECTED) {  
                try {  
                    // This is a blocking call and will only return on a  
                    // successful connection or an exception  
                	
                    socket = mmServerSocket.accept();  
                } catch (IOException e) {  
                    Log.e(TAG, "accept() failed", e);  
                    break;  
                }  
  
                // If a connection was accepted  
                if (socket != null) {  
                    synchronized (MyBluetoothService.this) {  
                        switch (mState) {  
                        case STATE_LISTEN:  
                        case STATE_CONNECTING:  
                            // Situation normal. Start the connected thread.  
                            connected(socket, socket.getRemoteDevice());  
                            break;  
                        case STATE_NONE:  
                        case STATE_CONNECTED:  
                            // Either not ready or already connected. Terminate new socket.  
                            try {  
                                socket.close();  
                            } catch (IOException e) {  
                                Log.e(TAG, "Could not close unwanted socket", e);  
                            }  
                            break;  
                        }  
                    }  
                }  
            }  
            if (D) Log.i(TAG, "END mAcceptThread");  
        }  
  
        public void cancel() {  
            if (D) Log.d(TAG, "cancel " + this);  
            try {  
                mmServerSocket.close();  
            } catch (IOException e) {  
                Log.e(TAG, "close() of server failed", e);  
            }  
        }  
    }  
  
  
    /** 
     * This thread runs while attempting to make an outgoing connection 
     * with a device. It runs straight through; the connection either 
     * succeeds or fails. 
     */  
    private class ConnectThread extends Thread {  
        private final BluetoothSocket mmSocket;  
        private final BluetoothDevice mmDevice;  
        
  
        public ConnectThread(BluetoothDevice device) {  
            mmDevice = device;  
            BluetoothSocket tmp = null;  
            Method m;
            // Get a BluetoothSocket for a connection with the  
            // given BluetoothDevice  
            try { 
            	
//                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);  
            	 m =mmDevice.getClass().getMethod("createRfcommSocket",new Class[]{int.class});
            	 tmp=(BluetoothSocket) m.invoke(mmDevice,Integer.valueOf(1));
            } 
//            catch (IOException e) {  
//                Log.e(TAG, "create() failed", e);  
//            } 
            catch (NoSuchMethodException ne){
            	
            } catch (IllegalArgumentException ie){
            	
            } catch (InvocationTargetException ine){
            	
            } catch (IllegalAccessException ile){
            	
            }
            mmSocket = tmp; 
        }  
  
        public void run() {  
            Log.i(TAG, "BEGIN mConnectThread");  
            setName("ConnectThread");  
  
            // Always cancel discovery because it will slow down a connection  
            mAdapter.cancelDiscovery();  
  
            // Make a connection to the BluetoothSocket  
            try {  
                // This is a blocking call and will only return on a  
                // successful connection or an exception  
                mmSocket.connect();  
            } catch (IOException e) {  
                connectionFailed();  
                Log.e("ConnectThread", e.toString());
                // Close the socket  
                try {  
                    mmSocket.close();  
                } catch (IOException e2) {  
                    Log.e(TAG, "unable to close() socket during connection failure", e2);  
                }  
                // Start the service over to restart listening mode  
                MyBluetoothService.this.start();  
                return;  
            }  
  
            // Reset the ConnectThread because we're done  
            synchronized (MyBluetoothService.this) {  
                mConnectThread = null;  
            }  
  
            // Start the connected thread  
            connected(mmSocket, mmDevice);  
        }  
  
        public void cancel() {  
            try {  
                mmSocket.close();  
            } catch (IOException e) {  
                Log.e(TAG, "close() of connect socket failed", e);  
            }  
        }  
    }  
  
    /** 
     * This thread runs during a connection with a remote device. 
     * It handles all incoming and outgoing transmissions. 
     */  
    private class ConnectedThread extends Thread {  
        private final BluetoothSocket mmSocket;  
        private final InputStream mmInStream;  
        private final OutputStream mmOutStream;
        private boolean isTerminate = false;
  
        public ConnectedThread(BluetoothSocket socket) {  
            Log.d(TAG, "create ConnectedThread");  
            mmSocket = socket;  
            InputStream tmpIn = null;  
            OutputStream tmpOut = null;  
  
            // Get the BluetoothSocket input and output streams  
            try {  
                tmpIn = socket.getInputStream();  
                tmpOut = socket.getOutputStream();  
            } catch (IOException e) {  
                Log.e(TAG, "temp sockets not created", e);  
            }  
  
            mmInStream = tmpIn;  
            mmOutStream = tmpOut;  
        }  
  
        public void run() {  
            Log.i(TAG, "BEGIN mConnectedThread");  
            byte[] buffer = new byte[1024];  
            int bytes;  
  
            // Keep listening to the InputStream while connected  
            while (!isTerminate) {
                try {  
                    // Read from the InputStream  
                    bytes = mmInStream.read(buffer);  
  
                    // Send the obtained bytes to the UI Activity  
//                    mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer)  
//                            .sendToTarget();  
                } catch (IOException e) {  
                    Log.e(TAG, "disconnected", e);  
                    connectionLost();  
                    break;
                }  
            }  
        }  
  
        /** 
         * Write to the connected OutStream. 
         * @param buffer  The bytes to write 
         */  
        public boolean write(byte[] buffer) {
        	boolean isWrite = false;
            try {  
                mmOutStream.write(buffer);  
                isWrite = true;
            } catch (IOException e) {  
            	isWrite = false;
                Log.e(TAG, "Exception during write", e);  
            } 
            return isWrite;
        }  
  
        public void cancel() {  
            try {
            	isTerminate = true;
                mmSocket.close();  
            } catch (IOException e) {  
                Log.e(TAG, "close() of connect socket failed", e);  
            }  
        }  
    }

    
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		mBltBinder = new BltServiceBinder();
		mAdapter = BluetoothAdapter.getDefaultAdapter();  
        mState = STATE_NONE;
//        mHandler = handler;  
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stop();
	}

	
	public class BltServiceBinder extends Binder{
		public MyBluetoothService getBltService(){
			return MyBluetoothService.this;
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBltBinder;
	}  
} 

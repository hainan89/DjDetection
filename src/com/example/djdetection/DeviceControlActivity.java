package com.example.djdetection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceControlActivity extends Activity {
	
	private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_SER_KEY = "my_Blt_Device";
    
    public static final int MESSAGE_STATE_CHANGE = 1;  
    public static final int MESSAGE_READ = 2;  
    public static final int MESSAGE_WRITE = 3;  
    public static final int MESSAGE_DEVICE_NAME = 4;  
    public static final int MESSAGE_TOAST = 5; 
    
    public static final String TOAST = "toast";  

    private Button sendMsgBt;
    private Button bingMonitoringBt;
    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothDevice mBltDevice;
    private MenuItem connectMenuItem;
//    private ExpandableListView mGattServicesList;
    
//    private BluetoothLeService mBluetoothLeService;
    private MyBluetoothService mBluetoothService;
    
//    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
//            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
//    private BluetoothGattCharacteristic mNotifyCharacteristic;

//    private final String LIST_NAME = "NAME";
//    private final String LIST_UUID = "UUID";
    
    private boolean isBltTested = false;
    
    private MsgReceiver msgReceiver = null;
    
    // Code to manage Service lifecycle.
    private final ServiceConnection mBltServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothService = ((MyBluetoothService.BltServiceBinder) service).getBltService();
            if(connectMenuItem != null){
            	connectMenuItem.setVisible(true);
            }

            // Automatically connects to the device upon successful start-up initialization.
//            mBluetoothService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	mBluetoothService = null;
        }
    };
    
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
//    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
//                mConnected = true;
//                updateConnectionState(R.string.connected);
//                invalidateOptionsMenu();
//            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
//                mConnected = false;
//                updateConnectionState(R.string.disconnected);
//                invalidateOptionsMenu();
//                clearUI();
//            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
//                // Show all the supported services and characteristics on the user interface.
////                displayGattServices(mBluetoothLeService.getSupportedGattServices());
//            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
////                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
//            }
//        }
//    };
    
    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
//    private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener() {
//        @Override
//        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
//                                    int childPosition, long id) {
//            if (mGattCharacteristics != null) {
//                final BluetoothGattCharacteristic characteristic =
//                        mGattCharacteristics.get(groupPosition).get(childPosition);
//                final int charaProp = characteristic.getProperties();
//                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                    // If there is an active notification on a characteristic, clear
//                    // it first so it doesn't update the data field on the user interface.
//                    if (mNotifyCharacteristic != null) {
//                        mBluetoothLeService.setCharacteristicNotification(
//                                mNotifyCharacteristic, false);
//                        mNotifyCharacteristic = null;
//                    }
//                    mBluetoothLeService.readCharacteristic(characteristic);
//                }
//                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                    mNotifyCharacteristic = characteristic;
//                    mBluetoothLeService.setCharacteristicNotification(
//                            characteristic, true);
//                }
//                return true;
//            }
//            return false;
//        }
//    };
    
    private void clearUI() {
//        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText("no_data)");
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("Process", "DeviceControl onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mBltDevice = intent.getParcelableExtra(EXTRAS_DEVICE_SER_KEY);
        
        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
//        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
//        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
//        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
//        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        
        sendMsgBt = (Button)findViewById(R.id.send_msg_bt);
        sendMsgBt.setEnabled(false);
        sendMsgBt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mBluetoothService != null && mBluetoothService.getState() == MyBluetoothService.STATE_CONNECTED){
//					byte[] outBuffer = new byte[10];
					isBltTested = mBluetoothService.write("A".getBytes());
					if(isBltTested){
						mDataField.setText("Send Msg Done");
						bingMonitoringBt.setEnabled(true);
					}else{
						mDataField.setText("Test Failed");
					}
					
				}
			}
		});
        
        bingMonitoringBt = (Button)findViewById(R.id.bind_obj__bt);
        bingMonitoringBt.setEnabled(false);
        bingMonitoringBt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				bind2MonitoringActivity();
			}
		});
        
        //接收MyBluetoothService广播的消息
        msgReceiver = new MsgReceiver();  
        IntentFilter intentFilter = makeBltIntentFilter();
        registerReceiver(msgReceiver, intentFilter);
    }
    
    private void bind2MonitoringActivity(){
    	final Intent intent = new Intent(this, MonitoringActivity.class);
    	startActivity(intent);
    }
    
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("Process", "DeviceControl onStart");
//		mBluetoothService = new MyBluetoothService();
		Intent startIntent = new Intent(this, MyBluetoothService.class);  
        startService(startIntent);
        Intent bindIntent = new Intent(this, MyBluetoothService.class);  
        bindService(bindIntent, mBltServiceConnection, BIND_AUTO_CREATE);  
	}

	@Override
    protected void onResume() {
        super.onResume();
        Log.d("Process", "DeviceControl onResume");
        isBltTested = false;
//        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothService != null) {
//            final boolean result = mBluetoothService.connect(mDeviceAddress);
//        	Log.d(TAG, "Connect request result=" + result);
//        	mBluetoothService.connect(mBltDevice);
        	int connectState = mBluetoothService.getState();
        	switch(connectState){
	        	case MyBluetoothService.STATE_NONE:
	        	{
	        		mBluetoothService.start();
	        		break;
	        	}
	        	case MyBluetoothService.STATE_LISTEN:
	        	{
	        		break;
	        	}
	        	case MyBluetoothService.STATE_CONNECTING:
	        	{
	        		break;
	        	}
	        	case MyBluetoothService.STATE_CONNECTED:
	        	{
	        		break;
	        	}
        	}
            
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Process", "DeviceControl onPause");
//        unregisterReceiver(mGattUpdateReceiver);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Process", "DeviceControl onDestroy");
//        unbindService(mServiceConnection);
//        mBluetoothService.stop();
        unbindService(mBltServiceConnection);  
        Intent stopIntent = new Intent(this, MyBluetoothService.class);
        stopService(stopIntent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        connectMenuItem = menu.findItem(R.id.menu_connect);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
            	if(mBluetoothService != null){
            		mBluetoothService.connect(mBltDevice);
            	}else{
            		Toast.makeText(getApplicationContext(), "BluetoothService didn't start", Toast.LENGTH_SHORT).show(); 
            	}
                return true;
            case R.id.menu_disconnect:
//                mBluetoothService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }
    
    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }
    
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
//    private void displayGattServices(List<BluetoothGattService> gattServices) {
//        if (gattServices == null) return;
//        String uuid = null;
//        String unknownServiceString = getResources().getString(R.string.unknown_service);
//        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
//        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
//        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
//                = new ArrayList<ArrayList<HashMap<String, String>>>();
//        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
//
//        // Loops through available GATT Services.
//        for (BluetoothGattService gattService : gattServices) {
//            HashMap<String, String> currentServiceData = new HashMap<String, String>();
//            uuid = gattService.getUuid().toString();
//            currentServiceData.put(
//                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
//            currentServiceData.put(LIST_UUID, uuid);
//            gattServiceData.add(currentServiceData);
//
//            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
//                    new ArrayList<HashMap<String, String>>();
//            List<BluetoothGattCharacteristic> gattCharacteristics =
//                    gattService.getCharacteristics();
//            ArrayList<BluetoothGattCharacteristic> charas =
//                    new ArrayList<BluetoothGattCharacteristic>();
//
//            // Loops through available Characteristics.
//            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                charas.add(gattCharacteristic);
//                HashMap<String, String> currentCharaData = new HashMap<String, String>();
//                uuid = gattCharacteristic.getUuid().toString();
//                currentCharaData.put(
//                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
//                currentCharaData.put(LIST_UUID, uuid);
//                gattCharacteristicGroupData.add(currentCharaData);
//            }
//            mGattCharacteristics.add(charas);
//            gattCharacteristicData.add(gattCharacteristicGroupData);
//        }
//
//        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
//                this,
//                gattServiceData,
//                android.R.layout.simple_expandable_list_item_2,
//                new String[] {LIST_NAME, LIST_UUID},
//                new int[] { android.R.id.text1, android.R.id.text2 },
//                gattCharacteristicData,
//                android.R.layout.simple_expandable_list_item_2,
//                new String[] {LIST_NAME, LIST_UUID},
//                new int[] { android.R.id.text1, android.R.id.text2 }
//        );
//        mGattServicesList.setAdapter(gattServiceAdapter);
//    }
    
    private static IntentFilter makeBltIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyBluetoothService.MESSAGE_TOAST);
        intentFilter.addAction(MyBluetoothService.MESSAGE_STATE_CHANGE);
        return intentFilter;
    }
    
 // The Handler that gets information back from the BluetoothChatService  
//    private final Handler mHandler = new Handler() {
//        @Override  
//        public void handleMessage(Message msg) {  
//            switch (msg.what) {  
//            case MESSAGE_STATE_CHANGE:  
//                switch (msg.arg1) {  
//                case MyBluetoothService.STATE_CONNECTED:  
//                	updateConnectionState(R.string.connected);
//                	sendMsgBt.setEnabled(true);
//                    break;  
//                case MyBluetoothService.STATE_CONNECTING:  
//                	updateConnectionState(R.string.connecting);
//                    break;  
//                case MyBluetoothService.STATE_LISTEN:  
//                case MyBluetoothService.STATE_NONE:  
//                	updateConnectionState(R.string.none);
//                    break;  
//                }  
//                break;  
//            case MESSAGE_WRITE:  
////                byte[] writeBuf = (byte[]) msg.obj;  
//                // construct a string from the buffer  
////                String writeMessage = new String(writeBuf);  
////                mConversationArrayAdapter.add("Me:  " + writeMessage);  
//                break;  
//            case MESSAGE_READ:  
//                byte[] readBuf = (byte[]) msg.obj;  
//                // construct a string from the valid bytes in the buffer  
//                String readMessage = new String(readBuf, 0, msg.arg1);  
//                mDataField.setText(readMessage);
//                break;  
//            case MESSAGE_DEVICE_NAME:  
//                // save the connected device's name  
////                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);  
////                Toast.makeText(getApplicationContext(), "Connected to "  
////                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();  
//                break;  
//            case MESSAGE_TOAST:  
//                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),  
//                               Toast.LENGTH_SHORT).show();  
//                break;  
//            }  
//        }  
//    }; 
    
    public class MsgReceiver extends BroadcastReceiver{  
    	  
        @Override  
        public void onReceive(Context context, Intent intent) {  
        	final String action = intent.getAction();
        	if (MyBluetoothService.MESSAGE_STATE_CHANGE.equals(action)){
        		int mState = intent.getIntExtra("mState", -1);
        		switch(mState){
        		case MyBluetoothService.STATE_CONNECTED:  
                	updateConnectionState(R.string.connected);
                	sendMsgBt.setEnabled(true);
                    break;  
                case MyBluetoothService.STATE_CONNECTING:  
                	updateConnectionState(R.string.connecting);
                    break;  
                case MyBluetoothService.STATE_LISTEN:  
                case MyBluetoothService.STATE_NONE:  
                	updateConnectionState(R.string.none);
                    break;  
        		}
        	}else if(MyBluetoothService.MESSAGE_TOAST.equals(action)){
        		String toastMessage = intent.getStringExtra("toastMessage");
        		Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show(); 
        	}
        }
    } 
}

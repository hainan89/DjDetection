package com.example.djdetection;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BleScanActivity extends ListActivity {
	
	private static final int REQUEST_ENABLE_BT = 1;
	private LeDeviceListAdapter mLeDeviceListAdapter;
	private boolean mScanning;
	private Handler mHandler;
	private BluetoothAdapter mBluetoothAdapter;
	private MenuItem scanMenuItem;
	private static final long SCAN_PERIOD = 100000;
	
	public static int debugeTimes = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d("Process", "DeviceScan onCreate");
//		getActionBar().setTitle("Ble Device Scan");
		mHandler = new Handler();
		
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Ble is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
		
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "error_bluetooth_not_supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        
        // Register for broadcasts when a device is discovered  
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);  
        this.registerReceiver(mReceiver, filter);  
  
        // Register for broadcasts when discovery has finished  
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);  
        this.registerReceiver(mReceiver, filter);

	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("Process", "DeviceScan onStart");
	}

	@Override
    protected void onResume() {
        super.onResume();
        Log.d("Process", "DeviceScan onResume");
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
//        if(mLeDeviceListAdapter != null && mLeDeviceListAdapter.getCount() > 0){
//        	mLeDeviceListAdapter.notifyDataSetChanged();
//        }
        if(mLeDeviceListAdapter != null){
        	mLeDeviceListAdapter.clear();
        	mLeDeviceListAdapter.notifyDataSetChanged();
        }
        scanLeDevice(true);
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	@Override
    protected void onPause() {
        super.onPause();
        Log.d("Process", "DeviceScan onPause");
        try{
        	scanLeDevice(false);
            mLeDeviceListAdapter.clear();
            Log.v("Pause", "BleActivity");
        }catch(Exception e){
        	Log.v("Exception", e.toString());
        }
        
    }
		
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d("Process", "DeviceScan onDestroy");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d("Process", "DeviceScan onStop");
	}

	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
		debugeTimes = debugeTimes + 1;
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_SER_KEY, device);
//        Bundle mBundle = new Bundle();
//        mBundle.putSerializable(DeviceControlActivity.EXTRAS_DEVICE_SER_KEY, device);
        
        if (mScanning) {
//            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        	mBluetoothAdapter.cancelDiscovery();
            mScanning = false;
        }
        startActivity(intent);
    }
	
//	private BluetoothAdapter.LeScanCallback mLeScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
//
//        @Override
//        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mLeDeviceListAdapter.addDevice(device);
//                    mLeDeviceListAdapter.notifyDataSetChanged();
//                }
//            });
//        }
//    };
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            String action = intent.getAction();  
  
            // When discovery finds a device  
            if (BluetoothDevice.ACTION_FOUND.equals(action)) { 
                // Get the BluetoothDevice object from the Intent  
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.v("DeviceName", device.getName());
                Log.v("DeviceAddress", device.getAddress());
                mLeDeviceListAdapter.addDevice(device);
            	mLeDeviceListAdapter.notifyDataSetChanged();
                // If it's already paired, skip it, because it's been listed already
//                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//                	mLeDeviceListAdapter.addDevice(device);
//                	mLeDeviceListAdapter.notifyDataSetChanged();
////                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());  
//                }  
            // When discovery is finished, change the Activity title  
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                setProgressBarIndeterminateVisibility(false);  
//                setTitle(R.string.select_device);
//                if (mNewDevicesArrayAdapter.getCount() == 0) {  
//                    String noDevices = getResources().getText(R.string.none_found).toString();  
//                    mNewDevicesArrayAdapter.add(noDevices);
//                }  
            }  
        }  
    };
    
	private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    mBluetoothAdapter.startDiscovery();
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startDiscovery();
//            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
//            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mBluetoothAdapter.cancelDiscovery();
        }
        invalidateOptionsMenu();
    }
	
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;
        
        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = BleScanActivity.this.getLayoutInflater();
        }
        
        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }
        
        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }
        
		public void clear() {
            mLeDevices.clear();
        }
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mLeDevices.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup viewGroup) {
			// TODO Auto-generated method stub
			ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0){
            	viewHolder.deviceName.setText(deviceName);
            }else{
            	viewHolder.deviceName.setText("unknown_device");
            }
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
		}
	}
	
	static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}

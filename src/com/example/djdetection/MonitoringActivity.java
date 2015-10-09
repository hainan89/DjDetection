package com.example.djdetection;

//import javax.security.auth.callback.Callback;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MonitoringActivity extends Activity {
	/** Called when the activity is first created. */
	
	private String TAG = "MonitoringActivity";
	private MyBluetoothService mBluetoothService;
	private Button bluetoothBt;
	private MySurfaceView2 showAreaView;
	private PositionMonitoringThread mMonitoringThread;
	private boolean isMonitoring = true;
//	interface CallBack{//定义一个回调接口
//	    public void sendMsg2Blt(int objStatus);
//	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 纯SurfaceView布局
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		Display d = this.getWindowManager().getDefaultDisplay();
//		int dHeight = d.getHeight();
//		int dWidth = d.getWidth();
//		
//		setContentView(new MySurfaceView(this, dHeight, dWidth));
		
		// SurfaceView和其他View集成布局
		setContentView(R.layout.activity_main);
		
		
		
		Display d = this.getWindowManager().getDefaultDisplay();
		Point screenSize = new Point();
		d.getSize(screenSize);
		
		bluetoothBt = (Button)findViewById(R.id.bluetooth_bt);
		bluetoothBt.setX(screenSize.x - 200);
		bluetoothBt.setY(screenSize.y - 110);
		bluetoothBt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mBluetoothService != null && mBluetoothService.getState() == MyBluetoothService.STATE_CONNECTED){
					mBluetoothService.write("T".getBytes());
				}else{
					Toast.makeText(getApplicationContext(), "BluetoothService is unable", Toast.LENGTH_SHORT).show(); 
				}								
			}
		});
		
		showAreaView = (MySurfaceView2)findViewById(R.id.show_area_view);
		showAreaView.getLayoutParams().height = screenSize.y;
		showAreaView.getLayoutParams().width = screenSize.x;
		
	}
	
	private class PositionMonitoringThread extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(isMonitoring){
				if(showAreaView != null){
					if(showAreaView.isDangerous()){
						if(mBluetoothService != null)
							mBluetoothService.write("A".getBytes());
							Log.v("Monitoring", "Danger");
					}else{
						
						Log.v("Monitoring", "No Danger");
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					Log.v("Monitoring", "No View");
				}
			}
			
		}
	}
	
	// Code to manage Service lifecycle.
    private final ServiceConnection mBltServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothService = ((MyBluetoothService.BltServiceBinder) service).getBltService();
            bluetoothBt.setEnabled(true);
            // Automatically connects to the device upon successful start-up initialization.
//            mBluetoothService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	mBluetoothService = null;
        }
    };
    
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
//		Intent startIntent = new Intent(this, MyBluetoothService.class);  
//        startService(startIntent);
        Intent bindIntent = new Intent(this, MyBluetoothService.class);  
        bindService(bindIntent, mBltServiceConnection, BIND_AUTO_CREATE);
        bluetoothBt.setEnabled(false);
        mMonitoringThread = new PositionMonitoringThread();
        mMonitoringThread.start();
	}
	
	@Override
    protected void onResume() {
        super.onResume();
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        Log.v("Pause", "MainActivity");
    }
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindService(mBltServiceConnection);  
        Intent stopIntent = new Intent(this, MyBluetoothService.class);
        stopService(stopIntent);
        isMonitoring = false;
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

}
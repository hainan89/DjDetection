package com.example.djdetection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
	/** Called when the activity is first created. */
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
		
		Button bluetoothBt = (Button)findViewById(R.id.bluetooth_bt);
		bluetoothBt.setX(screenSize.x - 100);
		bluetoothBt.setY(screenSize.y - 110);
		bluetoothBt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final Intent intent = new Intent(MainActivity.this, BleScanActivity.class);
				startActivity(intent);								
			}
		});
		
		SurfaceView showAreaView = (SurfaceView)findViewById(R.id.show_area_view);
		showAreaView.getLayoutParams().height = screenSize.y;
		showAreaView.getLayoutParams().width = screenSize.x;
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
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
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}
	
	

}
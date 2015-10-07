package com.example.djdetection;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceView;
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
//		bluetoothBt.getLayoutParams().height = 100;
//		bluetoothBt.getLayoutParams().width = 200;

		
		SurfaceView showAreaView = (SurfaceView)findViewById(R.id.show_area_view);
		showAreaView.getLayoutParams().height = screenSize.y;
		showAreaView.getLayoutParams().width = screenSize.x;
		
		


	}

}
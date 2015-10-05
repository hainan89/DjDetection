package com.example.djdetection;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Display d = this.getWindowManager().getDefaultDisplay();
		int dHeight = d.getHeight();
		int dWidth = d.getWidth();
		
		setContentView(new MySurfaceView(this, dHeight, dWidth));
	}

}
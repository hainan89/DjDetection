package com.example.djdetection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class MySurfaceView2 extends SurfaceView implements Callback, Runnable {

	private Thread th;
	private SurfaceHolder sfh;
	private Canvas canvas;
	private Paint paint;
	private boolean flag;
	//固定摇杆背景圆形的X,Y坐标以及半径
	private int RockerCircleX = 0;
	private int RockerCircleY = 0;
	private int RockerCircleR = 50;
	//摇杆的X,Y坐标以及摇杆的半径
	private float SmallRockerCircleX = 0;
	private float SmallRockerCircleY = 0;
	private float SmallRockerCircleR = 20;
	
	private float MObjectX = 0;
	private float MObjectY = 0;
	// 10, 10, ScreenWidth - 10, ScreenHeight - 120
	private float ShowAreaTop = 10;
	private float ShowAreaLeft = 10;
	private float ShowAreaRight = 0;
	private float ShowAreaBottom = 0;
	
	private float DangerAreaX = 100;
	private float DangerAreaY = 100;
	private float DangerAreaR = 50;
	
	private int ScreenHeight = 0;
	private int ScreenWidth = 0;
	
	private boolean isInitialized = false;
	public MySurfaceView2(Context context, AttributeSet attrs) {
		super(context, attrs);
			
		this.setKeepScreenOn(true);
		sfh = this.getHolder();
		sfh.addCallback(this);
		paint = new Paint();
		paint.setAntiAlias(true);
		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		th = new Thread(this);
		flag = true;
		th.start();
	}

	/***
	 * 得到两点之间的弧度
	 */
	public double getRad(float px1, float py1, float px2, float py2) {
		//得到两点X的距离
		float x = px2 - px1;
		//得到两点Y的距离
		float y = py1 - py2;
		//算出斜边长
		float xie = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		//得到这个角度的余弦值（通过三角函数中的定理 ：邻边/斜边=角度余弦值）
		float cosAngle = x / xie;
		//通过反余弦定理获取到其角度的弧度
		float rad = (float) Math.acos(cosAngle);
		//注意：当触屏的位置Y坐标<摇杆的Y坐标我们要取反值-0~-180
		if (py2 < py1) {
			rad = -rad;
		}
		return rad;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
			// 当触屏区域不在活动范围内
			if (Math.sqrt(Math.pow((RockerCircleX - (int) event.getX()), 2) + Math.pow((RockerCircleY - (int) event.getY()), 2)) >= RockerCircleR) {
				//得到摇杆与触屏点所形成的角度
				double tempRad = getRad(RockerCircleX, RockerCircleY, event.getX(), event.getY());
				//保证内部小圆运动的长度限制
				getXY(RockerCircleX, RockerCircleY, RockerCircleR, tempRad);
			} else {//如果小球中心点小于活动区域则随着用户触屏点移动即可
				SmallRockerCircleX = (int) event.getX();
				SmallRockerCircleY = (int) event.getY();
			}
			
			Log.v("TouchEvent", "ACTION_DOWN or ACTION_MOVE");
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			//当释放按键时摇杆要恢复摇杆的位置为初始位置
			SmallRockerCircleX = RockerCircleX;
			SmallRockerCircleY = RockerCircleY;
		}
		Log.v("TouchEvent", "Touched me!");
		return true;
	}

	/**
	 * 
	 * @param R
	 *            圆周运动的旋转点
	 * @param centerX
	 *            旋转点X
	 * @param centerY
	 *            旋转点Y
	 * @param rad
	 *            旋转的弧度
	 */
	public void getXY(float centerX, float centerY, float R, double rad) {
		//获取圆周运动的X坐标 
		SmallRockerCircleX = (float) (R * Math.cos(rad)) + centerX;
		//获取圆周运动的Y坐标
		SmallRockerCircleY = (float) (R * Math.sin(rad)) + centerY;
	}

	public void getMObjectXY(float dx, float dy){
		MObjectX = MObjectX + dx;
		if(MObjectX >= ShowAreaRight || MObjectX <= ShowAreaLeft){
			MObjectX = MObjectX - dx;
		}
		MObjectY = MObjectY + dy;
		if(MObjectY >= ShowAreaBottom || MObjectY <= ShowAreaTop){
			MObjectY = MObjectY - dy;
		}
	}
	
	public boolean isDangerous(){
		float dMD = (float)Math.sqrt(Math.pow((MObjectX - DangerAreaX), 2) + Math.pow((MObjectY - DangerAreaY), 2));
		if (dMD < DangerAreaR){
			return true;
		}
		return false;
	}
	
	private void initParameters(){
		int dHeight = this.getHeight();
		int dWidth = this.getWidth();
		ScreenHeight = dHeight;
		ScreenWidth = dWidth;
		RockerCircleX = 60;
		RockerCircleY = dHeight - 60;
		SmallRockerCircleX = RockerCircleX;
		SmallRockerCircleY = RockerCircleY;
		
		ShowAreaRight = ScreenWidth - 10;
		ShowAreaBottom = ScreenHeight - 120;
		
		MObjectX = ShowAreaRight - 10;
		MObjectY = ShowAreaBottom - 10;
	}
	
	public void draw() {
		try {
			canvas = sfh.lockCanvas();
			canvas.drawColor(Color.WHITE);
			
			// 绘制监控区域
			paint.setColor(0xFF33B5E5);
			canvas.drawRect(ShowAreaLeft, ShowAreaTop, ShowAreaRight, ShowAreaBottom, paint);
			
			// 绘制危险区域
			paint.setColor(0xFFFFBB33);
			canvas.drawCircle(DangerAreaX, DangerAreaY, DangerAreaR, paint);
			
			// 绘制监测对象
			paint.setColor(0xFF99CC00);
			canvas.drawCircle(MObjectX, MObjectY, 5, paint);
			
			//设置透明度
			paint.setColor(0x70000000);
			//绘制摇杆背景
			canvas.drawCircle(RockerCircleX, RockerCircleY, RockerCircleR, paint);
			paint.setColor(0x70ff0000);
			//绘制摇杆
			canvas.drawCircle(SmallRockerCircleX, SmallRockerCircleY, SmallRockerCircleR, paint);
			
//			Log.v("SPx", ""+SmallRockerCircleX);
//			Log.v("SPy", ""+SmallRockerCircleY);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			try {
				if (canvas != null)
					sfh.unlockCanvasAndPost(canvas);
			} catch (Exception e2) {

			}
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		if (!isInitialized){
			isInitialized = true;
			initParameters();
		}
		
		while (flag) {
			if (RockerCircleX != SmallRockerCircleX || RockerCircleY != SmallRockerCircleY){
				float dlength = (float)Math.sqrt(Math.pow((RockerCircleX - SmallRockerCircleX), 2) + Math.pow((RockerCircleY - SmallRockerCircleY), 2));
				float dx = 5 * (SmallRockerCircleX - RockerCircleX) / dlength;
				float dy = 5 * (SmallRockerCircleY - RockerCircleY) / dlength;
				getMObjectXY(dx , dy);
			}
			if(isDangerous()){
				// bluetooth message light on
				
			}else{
				// bluetooth message light off
				
			}
			draw();
			try {
				Thread.sleep(50);
			} catch (Exception ex) {
			}
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.v("Himi", "surfaceChanged");
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		flag = false;
		Log.v("Himi", "surfaceDestroyed");
	}

}

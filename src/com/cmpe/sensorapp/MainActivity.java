package com.cmpe.sensorapp;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, SensorEventListener {

	private Button btn_close;
	private Button btn_enable;
	private EditText et_steps;
	private SensorManager sMgr;
	private Camera camera;
    private boolean isFlashOn;
	private boolean sensorEnabled;
	private Parameters params;
	private long lastUpdate;
	private int count;
	private int stepCount = 0;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Holo);
		setContentView(R.layout.activity_main);
		btn_close = (Button) findViewById(R.id.btn_close);
		btn_enable = (Button) findViewById(R.id.btn_enable);
		et_steps = (EditText) findViewById(R.id.et_step);
		btn_enable.setOnClickListener(this);
		btn_close.setOnClickListener(this);
		sensorEnabled = false;
	}
	

	@Override
	protected void onStart() {
		super.onStart();
		getCamera();
		sMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);	
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.btn_close)
			finish();
		else if(v.getId() == R.id.btn_enable){
			sensorEnabled = true;
		    stepCount = Integer.parseInt(et_steps.getText().toString());
		    Log.i("Step Count", " " + stepCount);
		}
	}
	 
    @Override
    protected void onPause() {
        super.onPause();
        sMgr.unregisterListener(this);
        turnOffFlash();
    }
  
    @Override
    protected void onResume() {
        sMgr.registerListener(this,sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }
 
    @Override
    protected void onStop() {
        super.onStop();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(sensorEnabled){
			if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
				getAccelerometer(event);
		}
	}
	
	private void getAccelerometer(SensorEvent event) {
		float[] values = event.values;
	    // Movement
	    float x = values[0];
	    float y = values[1];
	    float z = values[2];
	    
	    float accelationSquareRoot = (x * x + y * y + z * z)
	            / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
	        long actualTime = System.currentTimeMillis();
	        if (accelationSquareRoot >= 2) //
	        {
	          if (actualTime - lastUpdate < 200) {
	            return;
	          }
	          if(count >= stepCount){
	        	  turnOnFlash();
	        	  return;
	          }
	          lastUpdate = actualTime;
	          Toast.makeText(this, "Step taken...."+ count++ , Toast.LENGTH_SHORT)
	              .show();
	        }
	}
	
	private void getCamera() {
	    if (camera == null) {
	        try {
	            camera = Camera.open();
	            params = camera.getParameters();
	        } catch (RuntimeException e) {
	            Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
	        }
	    }
	}

	private void turnOnFlash() {
	    if (!isFlashOn) {
	        if (camera == null || params == null) {
	            return;
	        }
	        params = camera.getParameters();
	        params.setFlashMode(Parameters.FLASH_MODE_TORCH);
	        camera.setParameters(params);
	        camera.startPreview();
	        isFlashOn = true;
	    }
	}

	private void turnOffFlash() {
	    if (isFlashOn) {
	        if (camera == null || params == null) {
	            return;
	        }
	        params = camera.getParameters();
	        params.setFlashMode(Parameters.FLASH_MODE_OFF);
	        camera.setParameters(params);
	        camera.stopPreview();
	        isFlashOn = false;
	    }
	}
}

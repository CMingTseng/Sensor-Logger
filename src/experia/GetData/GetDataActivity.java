package experia.GetData;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Window;
import java.util.List;

import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.PowerManager;

//加速度センサーの利用
public class GetDataActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;//センサーマネージャ
    private SensorView    sensorView;   //センサービュー
    private Sensor        accelerometer;//加速度せンサー(acc)
    private Sensor        orientation; //回転せンサー(ori)
    private Sensor        magnetic;		//磁界センサー(mag)
    private Sensor        gyroscope;	//ジャイロセンサー(gyr)
    private Sensor        light;		//照度センサー(lig)
    private Sensor        pressure;		//圧力センサー(pre)
    private Sensor        proximity;	//近接センサー(pro)
    private Sensor        gravity;		//重力センサー(gra)
    private Sensor        linearacceleraration;//加速度せンサー(lac)
    private Sensor        rotation;		//回転ベクトルセンサー(rot)
    private Sensor        humidity;		//相対湿度センサー(hum)
    private Sensor        temperature;		//温度センサー(tem)
    private Sensor		  acceleration;    //
    private Sensor        orientationV; //回転せンサー(ori)

    private boolean mIsMagSensor = false;
    private boolean mIsAccSensor = false;

    private static final int MATRIX_SIZE = 16;
    /*回転行列*/
    float[] inR = new float[MATRIX_SIZE];
    float[] outR = new float[MATRIX_SIZE];
    float[] I = new float[MATRIX_SIZE];

    int[] showValues = new int[3];
    float[] orientationValues = new float[3];
    float[] magneticValues = new float[3];
    float[] accelerometerValues = new float[3];

    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    BufferedWriter bw;
    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd_HHmmss");
    SimpleDateFormat sdf2 = new SimpleDateFormat("HHmmss.SSS");
    String dmemo, line;

    //初期化
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);
        sensorView = (SensorView) findViewById(R.id.sensor_view);

        //パワー制御
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "SensorsActivity");
        wakeLock.acquire();

        //センサーマネージャの取得
        sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);

        //センサーの取得
        List<Sensor> list;
        list=sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (list.size()>0) accelerometer=list.get(0);
        list=sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if (list.size()>0) orientation=list.get(0);
        list=sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        if (list.size()>0) magnetic=list.get(0);
        list=sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        if (list.size()>0) gyroscope=list.get(0);
        list=sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        if (list.size()>0) light=list.get(0);
        list=sensorManager.getSensorList(Sensor.TYPE_PRESSURE);
        if (list.size()>0) pressure=list.get(0);
        list=sensorManager.getSensorList(Sensor.TYPE_PROXIMITY);
        if (list.size()>0) proximity=list.get(0);
        /* for 2.3.3 or up */
        list=sensorManager.getSensorList(Sensor.TYPE_GRAVITY);
        if (list.size()>0) gravity=list.get(0);
        list=sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
        if (list.size()>0) linearacceleraration=list.get(0);
        list=sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
        /*if (list.size()>0) rotation=list.get(0);
        list=sensorManager.getSensorList(Sensor.TYPE_RERATIVE_HUMIDITY);*/
        if (list.size()>0) humidity=list.get(0);
        list=sensorManager.getSensorList(Sensor.TYPE_TEMPERATURE);
        if (list.size()>0) temperature=list.get(0);


        try{
        	dmemo = sdf1.format(new Date(System.currentTimeMillis()));
        	File dstFile = new File( Environment.getExternalStorageDirectory().getPath()
                    + "/data/" + "LOG" + dmemo + ".txt");
        	dstFile.getParentFile().mkdir();
        	sensorView.setLine(dstFile.getName());//from getPath()

        	FileOutputStream fos = new FileOutputStream(dstFile, true);
        	OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        	bw = new BufferedWriter(osw);
        	//sensorView.setLine(dmemo);
        } catch (Exception e) {
        	sensorView.setLine("error1 " + e);
        }
    }

    //アプリの開始
    @Override
    protected void onResume() {
        //アプリの開始
        super.onResume();

        //センサーの処理の開始
        if (accelerometer!=null) {
            sensorManager.registerListener(this,
        	    accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (orientation!=null) {
            sensorManager.registerListener(this,
      	        orientation,SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (magnetic!=null) {
            sensorManager.registerListener(this,
      	        magnetic,SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (gyroscope!=null) {
            sensorManager.registerListener(this,
        	    gyroscope,SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (light!=null) {
            sensorManager.registerListener(this,
        	    light,SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (pressure!=null) {
            sensorManager.registerListener(this,
     	    pressure,SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (proximity!=null) {
            sensorManager.registerListener(this,
        	    proximity,SensorManager.SENSOR_DELAY_FASTEST);
       }
        if (gravity!=null) {
            sensorManager.registerListener(this,
        	    gravity,SensorManager.SENSOR_DELAY_FASTEST);
       }
        if (rotation!=null) {
            sensorManager.registerListener(this,
        	    rotation,SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (temperature!=null) {
            sensorManager.registerListener(this,
        	    temperature,SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (linearacceleraration!=null) {
            sensorManager.registerListener(this,
            		linearacceleraration,SensorManager.SENSOR_DELAY_FASTEST);
        }

        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        //センサマネージャヘリスナーを登録
        for(Sensor sensor : sensors){
        	if( sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
        		sensorManager.registerListener(this, sensor,SensorManager.SENSOR_DELAY_NORMAL);
        		mIsMagSensor = true;
        	}
        	if( sensor.getType() == Sensor.TYPE_ACCELEROMETER){
        		sensorManager.registerListener(this, sensor,SensorManager.SENSOR_DELAY_NORMAL);
        		mIsAccSensor = true;
        	}
        }

    }
    
    @Override
    protected void onPause(){
    	super.onPause();
    	sensorManager.unregisterListener(this);
    }

    //アプリの停止
    @Override
    protected void onStop() {
        //センサーの処理の停止
        
        try{
        	bw.close();
        } catch (Exception e) {
        }
        
        sensorManager.unregisterListener(this);
        if (wakeLock.isHeld()) wakeLock.release();

        //アプリの停止
        super.onStop();
    }

    //センサーリスナーの処理
    public void onSensorChanged(SensorEvent event) {
    	int w[] = new int[3];
    	float f;

        //少数2桁切り捨て
        for (int i=0;i<3;i++) {
        	f = event.values[i];
        	if(f > 210000){
        		w[i] = 2100000000;
        	} else if(f < -210000){
        		w[i] = -2100000000;
        	} else {
        		w[i] = (int)(10000.0f * f);
        	}
        }


        dmemo = sdf2.format(new Date(System.currentTimeMillis()));

        //加速度の取得
        if (event.sensor==accelerometer) {
        	//ensorView.setAcceleration(event.values);
        	sensorView.setAcceleration(w);
        	try{
        		bw.write("acc," + dmemo + "," + w[0] + "," + w[1] + "," + w[2]);
        		bw.newLine();
        		bw.flush();
        	} catch (Exception e) {
        		sensorView.setLine("error2 " + e);
        	}
        	accelerometerValues = event.values.clone();
        	mIsAccSensor = true;
        }

        //線形加速度の取得
        if (event.sensor==linearacceleraration) {
        	//ensorView.setAcceleration(event.values);
        	sensorView.setLinearacceleraration(w);
        	try{
        		bw.write("lac," + dmemo + "," + w[0] + "," + w[1] + "," + w[2]);
        		bw.newLine();
        		bw.flush();
        	} catch (Exception e) {
        		sensorView.setLine("error13 " + e);
        	}
        }
        //方向の取得
        if (event.sensor==orientation) {
            //sensorView.setOrientation(event.values);
            sensorView.setOrientation(w);
            try{
        		bw.write("ori," + dmemo + "," + w[0] + "," + w[1] + "," + w[2]);
        		bw.newLine();
        		bw.flush();
        	} catch (Exception e) {
        		sensorView.setLine("error3 " + e);
        	}
        }
        //磁界の取得
        if (event.sensor==magnetic) {
        	sensorView.setMagnetic(w);
        	try{
        		bw.write("mag," + dmemo + "," + w[0] + "," + w[1] + "," + w[2]);
        		bw.newLine();
        		bw.flush();
        	} catch (Exception e) {
        		sensorView.setLine("error4 " + e);
        	}
        	magneticValues = event.values.clone();
        	mIsMagSensor = true;
        }
      //ジャイロの取得
        if (event.sensor==gyroscope) {
        	sensorView.setGyroscope(w);
        	try{
        		bw.write("gyr" + "," + dmemo + "," + w[0] + "," + w[1] + "," + w[2]);
        		bw.newLine();
        		bw.flush();
        	} catch (Exception e) {
        		sensorView.setLine("error5 " + e);
        	}
        }
      //照度の取得
        if (event.sensor==light) {
        	sensorView.setLight(w);
        	try{
        		bw.write("lig" + "," + dmemo + "," + w[0]);
        		bw.newLine();
        		bw.flush();
        	} catch (Exception e) {
        		sensorView.setLine("error6 " + e);
        	}
        }
      //圧力の取得
        if (event.sensor==pressure) {
        	sensorView.setPressure(w);
        	try{
        		bw.write("pre" + "," + dmemo + "," + w[0]);
        		bw.newLine();
        		bw.flush();
        	} catch (Exception e) {
        		sensorView.setLine("error7 " + e);
        	}
        }
      //近接の取得
        if (event.sensor==proximity) {
        	sensorView.setProximity(w);
        	try{
        		bw.write("pro" + "," + dmemo + "," + w[0]);
        		bw.newLine();
        		bw.flush();
        	} catch (Exception e) {
        		sensorView.setLine("error8 " + e);
        	}
        }
      //重力の取得
        if (event.sensor==gravity) {
        	sensorView.setGravity(w);
        	try{
        		bw.write("gra," + dmemo + "," + w[0] + "," + w[1] + "," + w[2]);
        		bw.newLine();
        		bw.flush();
        	} catch (Exception e) {
        		sensorView.setLine("error9 " + e);
        	}
        }
      //方位の取得
       if (event.sensor==rotation) {
        	sensorView.setRotation(w);
        	try{
        		bw.write("rot," + dmemo + "," + w[0] + "," + w[1] + "," + w[2]);
        		bw.newLine();
        		bw.flush();
        	} catch (Exception e) {
        		sensorView.setLine("error10 " + e);
        	}
        }
      //温度の取得
        if (event.sensor==temperature) {
        	sensorView.setTmperature(w);
        	try{
        		bw.write("tem," + dmemo + "," + w[0]);
        		bw.newLine();
        		bw.flush();
        	} catch (Exception e) {
        		sensorView.setLine("error11 " + e);
        	}
        }

        //傾きVの取得
    	if(mIsMagSensor || mIsAccSensor){
    		mIsMagSensor = false;
    		mIsAccSensor = false;

    		SensorManager.getRotationMatrix(inR, I, accelerometerValues, magneticValues);

    		//Activityの表示が縦固定の場合
    		SensorManager.remapCoordinateSystem(inR,SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);
    		SensorManager.getOrientation(outR,orientationValues);
    		
    		for(int i = 0; i < 3; i++){
   // 			showValues[i] = (int)(radianToDegree(orientationValues[i]) * 10000.0);
    			showValues[i] = (int)(orientationValues[i] * 10000.0);
    		}
    		sensorView.setOrientationValues(showValues);
    		
    		try{
    			bw.write("apr," + dmemo + "," + showValues[0] + "," + showValues[1] + "," + showValues[2]);
        		bw.newLine();
        		bw.flush();
        	} catch (Exception e) {
        		sensorView.setLine("error12 " + e);
        	}
    	}

        sensorView.invalidate();

    }
    
    double radianToDegree(float rad){
    	return Math.toDegrees(rad);
    }

    //精度変更イベントの処理
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

/*   int radianToDegree(float rad){
       return (int) Math.floor( Math.toDegrees(rad) );
    }*/
}

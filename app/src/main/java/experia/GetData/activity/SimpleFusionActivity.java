package experia.GetData.activity;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import experia.GetData.R;
import experia.GetData.Util.SimpleFusion;
import experia.GetData.model.SensorData;

public class SimpleFusionActivity extends Activity implements SensorEventListener, View.OnClickListener {

    public static final String TAG = "SimpleFusionActivity";
    private static final double G_TRUE = 9.80665;

    private static final double G_AVG_X = 10.04719;
    private static final double G_AVG_Y = 9.895846;
    private static final double G_AVG_Z = 10.48703;

    public static final double CORG_X = G_TRUE/G_AVG_X;
    public static final double CORG_Y = G_TRUE/G_AVG_Y;
    public static final double CORG_Z = G_TRUE/G_AVG_Z;

    private SensorManager sensorManager;
    private ArrayList<SensorData> mListAcc;
    private ArrayList<SensorData> mListGyro;
    private ArrayList<Double> V;
    private ArrayList<Double> Z;
    private Button btnLoad;
    private Button btnRecord;
    private Button btnStop;
    private Button btnSimleFusion;
    private TextView txtStatus;
    private boolean shouldRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_fusion);

        btnLoad = (Button) findViewById(R.id.btn_load);
        btnRecord = (Button) findViewById(R.id.btn_record);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnSimleFusion = (Button) findViewById(R.id.btn_simple_fusion);
        txtStatus = (TextView) findViewById(R.id.txt_status);

        btnLoad.setOnClickListener(this);
        btnRecord.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnSimleFusion.setOnClickListener(this);

        init();

        //get sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

    }

    private void init() {
        if (mListAcc == null) mListAcc = new ArrayList<SensorData>();
        mListAcc.clear();

        if (mListGyro == null) mListGyro = new ArrayList<SensorData>();
        mListGyro.clear();

        if (V == null) V = new ArrayList<Double>();
        V.clear();

        if (Z == null) Z = new ArrayList<Double>();
        Z.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_simple_fusion, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_load:
                txtStatus.setText("Loading data from file...");
                init();
                loadData();
                break;
            case R.id.btn_record:
                txtStatus.setText("Recording...");
                init();
                shouldRecord = true;
                break;
            case R.id.btn_stop:
                txtStatus.setText("Stopped.");
                shouldRecord = false;
                break;
            case R.id.btn_simple_fusion:
                SimpleFusion.getInstance().setData(mListAcc, mListGyro);

                Vector upNew = new BasicVector();
                for (int i = 100; i< mListAcc.size() - 100; i++) {
                    SensorData sensorData = mListAcc.get(i);
                    sensorData.getAccCalibrated();
                    if (i > 100) {
                        upNew = SimpleFusion.getInstance().getUPnew(upNew, mListAcc.get(i-1), sensorData, mListGyro.get(i));
                    } else {
                        upNew = SimpleFusion.getInstance().getUPnew(null, null, sensorData, mListGyro.get(i));
                    }

                    //Calculate z series
                    double z;
                    double v;
                    if (i == 100) {
                        z = 0;
                        v = 0;
                    } else {
                        double sZt = sensorData.getVector().innerProduct(upNew) + G_TRUE;
                        Log.d(TAG, "series sZt: " + sZt);
                        double time = (sensorData.timestamp - mListAcc.get(i-1).timestamp) /1000000000;
                        v = V.get(V.size()-1) + time*sZt;
                        Log.d(TAG, "series v: " + v);
                        z = Z.get(Z.size()-1) + time*v;
                    }
                    V.add(v);
                    Z.add(z);
                    Log.d(TAG, "upNew:" + upNew.toString());
                    Log.d(TAG, "series Z: " + z);
                }

                txtStatus.setText("Done.");

                break;
            default:
                //
        }
    }

    private void loadData() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("raw.txt")));

            // do reading, usually loop until end of file reading
            String mLine = reader.readLine();
            while (mLine != null) {
                //process line
                if (mLine.contains("Acc Raw:")) {
                    String[] data = mLine.split(" ");
                    SensorData sensorData = new SensorData();
                    sensorData.timestamp = Double.valueOf(data[1]);
                    sensorData.values = new float[]{Float.valueOf(data[4]), Float.valueOf(data[5]), Float.valueOf(data[6])};
                    mListAcc.add(sensorData);
                } else if (mLine.contains("Gyro:")) {
                    String[] data = mLine.split(" ");
                    SensorData sensorData = new SensorData();
                    sensorData.timestamp = Double.valueOf(data[1]);
                    sensorData.values = new float[]{Float.valueOf(data[3]), Float.valueOf(data[4]), Float.valueOf(data[5])};
                    mListGyro.add(sensorData);
                }
                mLine = reader.readLine();

            }
            txtStatus.setText("Data loaded.");
        } catch (IOException e) {
            txtStatus.setText("Error occurred:" + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    txtStatus.setText("Error occurred:" + e.getMessage());
                }
            }
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (shouldRecord) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                SensorData sensorData = getSensorData(event);
                mListAcc.add(sensorData);
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                SensorData sensorData = getSensorData(event);
                mListGyro.add(sensorData);
            }
        }
    }

    private SensorData getSensorData(SensorEvent event) {
        SensorData sensorData = new SensorData();
        sensorData.values = event.values.clone();
        sensorData.timestamp = event.timestamp;
        return sensorData;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

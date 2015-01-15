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

import java.util.ArrayList;

import experia.GetData.R;
import experia.GetData.Util.SimpleFusion;
import experia.GetData.model.SensorData;

public class SimpleFusionActivity extends Activity implements SensorEventListener, View.OnClickListener {

    public static final String TAG = "SimpleFusionActivity";

    private SensorManager sensorManager;
    private ArrayList<SensorData> mListAcc;
    private ArrayList<SensorData> mListGyro;
    private Button btnRecord;
    private Button btnStop;
    private Button btnSimleFusion;
    private TextView txtStatus;
    private boolean shouldRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_fusion);

        btnRecord = (Button) findViewById(R.id.btn_record);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnSimleFusion = (Button) findViewById(R.id.btn_simple_fusion);
        txtStatus = (TextView) findViewById(R.id.txt_status);

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
                    if (i > 100) {
                        upNew = SimpleFusion.getInstance().getUPnew(upNew, mListAcc.get(i-1), mListAcc.get(i), mListGyro.get(i));
                    } else {
                        upNew = SimpleFusion.getInstance().getUPnew(null, null, mListAcc.get(i), mListGyro.get(i));
                    }
                    Log.d(TAG, upNew.toString());
                }

                txtStatus.setText("Done.");

                break;
            default:
                //
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

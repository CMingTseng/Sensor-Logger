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
import android.widget.EditText;
import android.widget.TextView;

import com.badlogic.gdx.math.Quaternion;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import experia.GetData.R;
import experia.GetData.Util.Common;
import experia.GetData.Util.Config;
import experia.GetData.Util.Quest;
import experia.GetData.filter.ExtendedKalmanFilter;
import experia.GetData.filter.LowPassFilter;


public class QuestActivity extends Activity implements SensorEventListener, View.OnClickListener, Quest.OnTaskComplete {

    public static final String TAG = "QuestActivity";

    // Outputs for the acceleration and LPFs
    private float[] acceleration = new float[3];
    private float[] lowPassFilterOutput = new float[3];
    private float[] magnetic = new float[3];
    private LowPassFilter lowPassFilter;
    private SensorManager sensorManager;
    private Button recordBtn;
    private Button computeQuaternionBtn;
    private Button clearBtn;
    private Button kalmanButton;
    private TextView resultTextView;
    private TextView recordingStatus;
    private ArrayList<float[]> accLists = new ArrayList<float[]>();
    private ArrayList<float[]> magneticLists = new ArrayList<float[]>();
    private ArrayList<float[]> gyroLists = new ArrayList<float[]>();
    private ArrayList<Quaternion> quaternions = new ArrayList<Quaternion>();
    private boolean shouldRecordData = false;

    @InjectView(R.id.log_name_set_btn) public Button setLogNameBtn;
    @InjectView(R.id.log_name_edit_text) public EditText logNameEditext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest);
        ButterKnife.inject(this);

        recordBtn = (Button) findViewById(R.id.data_record_button);
        computeQuaternionBtn = (Button) findViewById(R.id.calculate_quaternion_button);
        clearBtn = (Button) findViewById(R.id.button_clear);
        kalmanButton = (Button) findViewById(R.id.kalman_button);
        resultTextView = (TextView) findViewById(R.id.result_textview);
        recordingStatus = (TextView) findViewById(R.id.status);

        recordBtn.setOnClickListener(this);
        computeQuaternionBtn.setOnClickListener(this);
        clearBtn.setOnClickListener(this);
        kalmanButton.setOnClickListener(this);

        //get sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Init LowPassFilter
        lowPassFilter = new LowPassFilter();

        setLogNameBtn.setOnClickListener(this);
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
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quest, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (shouldRecordData) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
                lowPassFilterOutput = lowPassFilter.addSamples(acceleration);
                if (Config.DEBUG) {
                    String log = String.format("Raw: %f %f %f Filtered: %f %f %f", acceleration[0], acceleration[1], acceleration[2], lowPassFilterOutput[0], lowPassFilterOutput[1], lowPassFilterOutput[2]);
                    Log.d(TAG, log);
                }
                float[] acceleration = new float[]{lowPassFilterOutput[0], lowPassFilterOutput[1], lowPassFilterOutput[2]};
                accLists.add(acceleration);
//                Quest.getInstance().addAccelerometer(lowPassFilterOutput);
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetic, 0, event.values.length);
                if (Config.DEBUG) {
                    String log = String.format("Magnetic field: %f %f %f", magnetic[0], magnetic[1], magnetic[2]);
                    Log.d(TAG, log);
                }
                float[] mag = new float[]{magnetic[0], magnetic[1], magnetic[2]};
                magneticLists.add(mag);
//                Quest.getInstance().addMagnetic(magnetic);
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float[] gyro = new float[3];
                System.arraycopy(event.values, 0, gyro, 0, event.values.length);
                gyroLists.add(gyro);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_clear:
                if (resultTextView != null) resultTextView.setText("");
                accLists.clear();
                magneticLists.clear();
                break;
            case R.id.data_record_button:
                //Start to record accelerometer, magnetic, gyro
                shouldRecordData = !shouldRecordData;
                recordingStatus.setText(shouldRecordData ? "Status: Recording" : "Status: Stop");
                break;
            case R.id.calculate_quaternion_button:
                //Calculate quaternion from accLists and magneticLists
                int m = accLists.size();
                int n = magneticLists.size();
                int size = (m < n) ? m : n;
                //Clear all elements of quaternions
                quaternions.clear();
                for (int i = 0; i < size; i++) {
                    Quest.getInstance(this).compute(accLists.get(i), magneticLists.get(i));
                }
                break;
            case R.id.log_name_set_btn:
                if (logNameEditext.getText() != null) {
                    Common.fileName = logNameEditext.getText().toString();
                }
                break;
            case R.id.kalman_button:
                //Kalman filter
                ExtendedKalmanFilter kalmanFilter = new ExtendedKalmanFilter(gyroLists, quaternions);
                kalmanFilter.doKalmanFilter();
            default:
                //Do nothing
        }
    }

    @Override
    public void updateQuaternion(Quaternion quaternion) {
        quaternions.add(quaternion);
    }
}

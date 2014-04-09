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

import experia.GetData.R;
import experia.GetData.filter.LowPassFilter;


public class QuestActivity extends Activity implements SensorEventListener {

    public static final String TAG = "QuestActivity";

    // Outputs for the acceleration and LPFs
    private float[] acceleration = new float[3];
    private float[] lowPassFilterOutput = new float[3];
    private LowPassFilter lowPassFilter;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest);

        //get sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Init LowPassFilter
        lowPassFilter = new LowPassFilter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_FASTEST);
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
        System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
        lowPassFilterOutput = lowPassFilter.addSamples(acceleration);
        String log = String.format("Raw: %f %f %f Filtered: %f %f %f", acceleration[0], acceleration[1], acceleration[2], lowPassFilterOutput[0], lowPassFilterOutput[1], lowPassFilterOutput[2]);
        Log.d(TAG, log);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

package experia.GetData.filter;

import android.util.Log;

import experia.GetData.Util.Config;

/**
 * Created by Le Van Hoang on 2014/04/09.
 */
public class LowPassFilter {

    // Constants for the low-pass filters
    private float timeConstant = 0.18f;
    private float alpha = 0.1f;
    private float dt = 0;

    // Timestamps for the low-pass filters
    private float timestamp = System.nanoTime();
    private float timestampOld = System.nanoTime();

    private int count = 0;

    // Gravity and linear accelerations components for the
    // Wikipedia low-pass filter
    private float[] gravity = new float[]{0, 0, 0};

    // Raw accelerometer data
    private float[] input = new float[]{0, 0, 0};

    /**
     * Add a sample.
     *
     * @param acceleration The acceleration data.
     * @return Returns the output of the filter.
     */
    public float[] addSamples(float[] acceleration) {
        // Get a local copy of the sensor values
        System.arraycopy(acceleration, 0, this.input, 0, acceleration.length);

        timestamp = System.nanoTime();

        // Find the sample period (between updates).
        // Convert from nanoseconds to seconds
        dt = 1 / (count / ((timestamp - timestampOld) / 1000000000.0f));

        // Calculate Wikipedia low-pass alpha
        alpha = dt / (timeConstant + dt);

        if (Config.DEBUG) {
            Log.d("tag", String.valueOf(alpha));
        }
        count++;

        if (count > 5) {
            // Update the Wikipedia filter
            // y[i] = y[i] + alpha * (x[i] - y[i])
            gravity[0] = gravity[0] + alpha * (this.input[0] - gravity[0]);
            gravity[1] = gravity[1] + alpha * (this.input[1] - gravity[1]);
            gravity[2] = gravity[2] + alpha * (this.input[2] - gravity[2]);
        }
        return this.gravity;
    }
}
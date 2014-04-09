package experia.GetData.Util;

import android.util.Log;

import experia.GetData.model.Quaternion;

/**
 * Created by Le Van Hoang on 2014/04/09.
 * This implementation is based on
 */
public class Quest {

    public static final String TAG = "Quest";

    private float[] accelerometer = new float[3];
    private float[] magnetic = new float[3];

    private static Quest ourInstance = new Quest();

    public static Quest getInstance() {
        return ourInstance;
    }

    private Quest() {
    }

    public void addAccelerometer(float[] acc) {
        //Compute Quaternion with latest magnetic field's data
        computeQuaternion(acc, this.magnetic);
        System.arraycopy(acc, 0, this.accelerometer, 0, 3);
    }

    public void addMagnetic(float[] magnetic) {
        //Compute Quaternion wih latest accelerometer's data
        computeQuaternion(this.accelerometer, magnetic);
        System.arraycopy(magnetic, 0, this.magnetic, 0, 3);
    }

    private Quaternion computeQuaternion(float[] acc, float[] magnetic) {
        if (Config.DEBUG) {
            String log = String.format("Quaternion from Acc: %f, %f , %f With Magnetic: %f %f %f", acc[0], acc[1], acc[2], magnetic[0], magnetic[1], magnetic[2]);
            Log.d(TAG, log);
        }

        //Normalied vector
        //Tinh lamda
        //Tinh matrix K
        //Giai matrix

        Quaternion quaternion = new Quaternion(0, 0, 0, 0);
        return quaternion;
    }
}

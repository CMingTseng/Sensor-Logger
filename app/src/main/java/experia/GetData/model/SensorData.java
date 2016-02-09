package experia.GetData.model;

import android.hardware.SensorEvent;

import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

import experia.GetData.Util.SimpleFusion;
import experia.GetData.activity.SimpleFusionActivity;

/**
 * Created by hoang8f on 1/16/15.
 */
public class SensorData {

    public float[] values;
    public double timestamp;

    public Vector getVector() {
        return new BasicVector(new double[]{values[0], values[1], values[2]});
    }

    public void getAccCalibrated() {
        values = new float[]{(float) (SimpleFusionActivity.CORG_X*values[0]), (float) (SimpleFusionActivity.CORG_Y*values[1]), (float) (SimpleFusionActivity.CORG_Z*values[2])};
    }

}

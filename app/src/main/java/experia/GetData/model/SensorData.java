package experia.GetData.model;

import android.hardware.SensorEvent;

import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

/**
 * Created by hoang8f on 1/16/15.
 */
public class SensorData {

    public float[] values;
    public double timestamp;

    public Vector getVector() {
        return new BasicVector(new double[]{values[0], values[1], values[2]});
    }

}

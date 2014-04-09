package experia.GetData.Util;

import android.util.Log;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.dense.BasicVector;

import experia.GetData.model.Quaternion;

/**
 * Created by Le Van Hoang on 2014/04/09.
 * This implementation is based on
 */
public class Quest {

    public static final String TAG = "Quest";

    private float[] accelerometer = new float[3];
    private float[] magnetic = new float[3];

    private double a1 = 0.5;
    private double a2 = 0.5;

    //Gravity
    private final Vector v1 = new BasicVector(new double[]{0.0, 0.0, -1.0});
    //North
    private final Vector v2 = new BasicVector(new double[]{1.0, 0.0, 0.0});

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

        //Normalizes vector
        Vector accVector = new BasicVector(new double[]{acc[0], acc[1], acc[2]});
        Vector magneticVector = new BasicVector(new double[]{magnetic[0], magnetic[1], magnetic[2]});

        double norm1 = accVector.fold(Vectors.mkManhattanNormAccumulator());
        accVector = accVector.divide(norm1);

        double norm2 = magneticVector.fold(Vectors.mkManhattanNormAccumulator());
        magneticVector = magneticVector.divide(norm2);

        if (Config.DEBUG) {
            String log1 = String.format("Norm acc: %f %f %f ", accVector.get(0), accVector.get(1), accVector.get(2));
            String log2 = String.format("Norm magnetic: %f %f %f ", magneticVector.get(0), magneticVector.get(1), magneticVector.get(2));
            Log.d(TAG, log1 + log2);
        }

        //Tinh Cosin theta
        double cos_theta = scalarCrossProduct(v1, v2) * scalarCrossProduct(accVector, magneticVector);
        //Tinh lamda
        //lamda = sqrt(a1^2 + 2a1*a2cos_theta + a2^2)
        double lamda = Math.sqrt(a1*a1 + 2*a1*a2*cos_theta + a2*a2);

        //Tinh matrix K

        //Compute matrix B (3x3)
        Matrix B = accVector.toColumnMatrix().multiply(v1.toRowMatrix()).multiply(a1).add(magneticVector.toColumnMatrix().multiply(v2.toRowMatrix()).multiply(a2));
        Matrix S = B.add(B.transpose());

        //Giai matrix

        Quaternion quaternion = new Quaternion(0, 0, 0, 0);
        return quaternion;
    }

    private double scalarCrossProduct(Vector vector1, Vector vector2) {
        return Math.sqrt(1 - Math.pow(vector1.innerProduct(vector2), 2));
    }
}

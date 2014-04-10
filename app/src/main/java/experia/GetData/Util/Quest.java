package experia.GetData.Util;

import android.util.Log;

import org.la4j.LinearAlgebra;
import org.la4j.linear.LinearSystemSolver;
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

        if (acc[0] == 0 || acc[1] == 0 || acc[2] == 0) return null;
        if (magnetic[0] == 0 || magnetic[1] == 0 || magnetic[2] == 0) return null;

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
        //Tinh lamda_max
        //lamda_max = sqrt(a1^2 + 2a1*a2cos_theta + a2^2)
        double lamda_max = Math.sqrt(a1 * a1 + 2 * a1 * a2 * cos_theta + a2 * a2);

        //Tinh matrix K

        //Compute matrix B (3x3)
        Matrix B = accVector.toColumnMatrix().multiply(v1.toRowMatrix()).multiply(a1).add(magneticVector.toColumnMatrix().multiply(v2.toRowMatrix()).multiply(a2));
        Matrix S = B.add(B.transpose());

        //Compute miu
        double miu = accVector.innerProduct(v1) * a1 + magneticVector.innerProduct(v2) * a2;

        //Identity matrix
        Matrix S1 = S.subtract((new Basic2DMatrix().factory().createIdentityMatrix(3).multiply(miu)));

        Vector Z = crossProduct(accVector, v1).multiply(a1).add(crossProduct(magneticVector, v2).multiply(a2));

//        Matrix K = new Basic2DMatrix().factory().createBlockMatrix(S1, Z.toColumnMatrix(), Z.toRowMatrix(), new Basic2DMatrix().factory().createConstantMatrix(1, 1, miu));
        Matrix K = S1.resize(4,4);
        Vector Z1 = Z.resize(4);
        Z1.set(3, miu);
        K.setRow(3, Z1);
        K.setColumn(3, Z1);

        //Giai matrix
        //K * Quaternion = lamda_max * Quaternion
        // (K - lamda_max * I ) * Quaternion = 0

        //K- lamda_max * I
        Matrix A = K.subtract(new Basic2DMatrix().factory().createIdentityMatrix(4).multiply(lamda_max));
        Matrix Zero = new Basic2DMatrix().factory().createConstantMatrix(4, 1, 0.0);

        LinearSystemSolver linearSystemSolver = A.withSolver(LinearAlgebra.SolverFactory.GAUSSIAN);
        Vector q = linearSystemSolver.solve(new BasicVector().factory().createConstantVector(4, 0.0));

        Quaternion quaternion = new Quaternion(0, 0, 0, 0);
        return quaternion;
    }

    private double scalarCrossProduct(Vector vector1, Vector vector2) {
        return Math.sqrt(1 - Math.pow(vector1.innerProduct(vector2), 2));
    }

    private Vector crossProduct(Vector u, Vector v) {
        double i = u.get(1) * v.get(2) - u.get(2) * v.get(1);
        double j = u.get(0) * v.get(2) - u.get(2) * v.get(0);
        double k = u.get(0) * v.get(1) - u.get(1) * v.get(0);
        return new BasicVector(new double[]{i, j, k});
    }
}

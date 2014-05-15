package experia.GetData.Util;

import android.os.AsyncTask;
import android.util.Log;

import com.badlogic.gdx.math.Quaternion;

import org.la4j.LinearAlgebra;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

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
//        computeQuaternion(acc, this.magnetic);
        new ComputeQuaternion(acc, this.magnetic).execute();
        System.arraycopy(acc, 0, this.accelerometer, 0, 3);
    }

    public void addMagnetic(float[] magnetic) {
        //Compute Quaternion wih latest accelerometer's data
        new ComputeQuaternion(this.accelerometer, magnetic).execute();
        System.arraycopy(magnetic, 0, this.magnetic, 0, 3);
    }

    public void compute(float[] acc, float[] magnetic) {
        //Time 1
        new ComputeQuaternion(acc, magnetic).execute();
        //Time 2
    }

    private class ComputeQuaternion extends AsyncTask<Void, Void, Quaternion> {
        private float[] mAcc;
        private float[] mMagnetic;

        public ComputeQuaternion(float[] acc, float[] magnetic) {
            mAcc = acc;
            mMagnetic = magnetic;
        }


        @Override
        protected Quaternion doInBackground(Void... params) {
            return computeQuaternion(mAcc, mMagnetic);
        }

        @Override
        protected void onPostExecute(Quaternion quaternion) {
            super.onPostExecute(quaternion);
            //Print log

            String log = String.format("Acc: %f, %f , %f Magnetic: %f %f %f Quaternion: %s Yaw: %s Pitch: %s Roll: %s \n", mAcc[0], mAcc[1], mAcc[2], mMagnetic[0], mMagnetic[1], mMagnetic[2], ((quaternion != null) ? quaternion.toString() : "null"), ((quaternion != null) ? quaternion.getYaw() : "null"), ((quaternion != null) ? quaternion.getPitch() : "null"), ((quaternion != null) ? quaternion.getRoll() : "null"));
//            Log.d(TAG, "@@@" + ((quaternion != null) ? "Yaw:" + quaternion.getYaw() + "Pitch:" + quaternion.getPitch() + "Roll:" + quaternion.getRoll() : "null"));
//            Log.d(TAG, "@@@" + log + "Quaternion: " + ((quaternion != null) ? quaternion.toString() : "null"));
            Log.d(TAG, log);
            Common.writeToFile(Common.fileName, log);

            //Export to log file
        }
    }

    private Quaternion computeQuaternion(float[] acc, float[] magnetic) {
        if (Config.DEBUG) {
            String log = String.format("Quaternion from Acc: %f, %f , %f With Magnetic: %f %f %f", acc[0], acc[1], acc[2], magnetic[0], magnetic[1], magnetic[2]);
            Log.d(TAG, log);
        }

        if (acc[0] == 0 || acc[1] == 0 || acc[2] == 0) return null;
        if (magnetic[0] == 0 || magnetic[1] == 0 || magnetic[2] == 0) return null;

        //Normalizes vector
        Vector accVector1 = new BasicVector(new double[]{acc[0], acc[1], acc[2]});
        Vector magneticVector1 = new BasicVector(new double[]{magnetic[0], magnetic[1], magnetic[2]});

//        double norm1 = accVector1.fold(Vectors.mkManhattanNormAccumulator());
//        Vector accVector = accVector1.divide(norm1);
        Vector accVector = accVector1.normalize();

//        double norm2 = magneticVector1.fold(Vectors.mkManhattanNormAccumulator());
//        Vector magneticVector = magneticVector1.divide(norm2);
        Vector magneticVector = magneticVector1.normalize();

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
        Matrix K = S1.resize(4, 4);
        Vector Z1 = Z.resize(4);
        Z1.set(3, miu);
        K.setRow(3, Z1);
        K.setColumn(3, Z1);

        //Convert to Jama Matrix
//        double a[][] = new double[][]{
//                {K.get(0,0) ,K.get(0,1), K.get(0,2), K.get(0,3)},
//                {K.get(1,0) ,K.get(1,1), K.get(1,2), K.get(1,3)},
//                {K.get(2,0) ,K.get(2,1), K.get(2,2), K.get(2,3)},
//                {K.get(3,0) ,K.get(3,1), K.get(3,2), K.get(3,3)}};
//        jama.Matrix KK = new jama.Matrix(a);

        Matrix KK = K.subtract((new Basic2DMatrix().factory().createIdentityMatrix(4).multiply(lamda_max)));

        //Return {U, D, V}
        Matrix[] matrixes = KK.withDecompositor(LinearAlgebra.DecompositorFactory.SVD).decompose();


        //Giai matrix
        //K * Quaternion = lamda_max * Quaternion
        // (K - lamda_max * I ) * Quaternion = 0

        //K- lamda_max * I
//        Matrix A = K.subtract(new Basic2DMatrix().factory().createIdentityMatrix(4).multiply(lamda_max));
//        Matrix Zero = new Basic2DMatrix().factory().createConstantMatrix(4, 1, 0.0);

//        LinearSystemSolver linearSystemSolver = A.withSolver(LinearAlgebra.SolverFactory.GAUSSIAN);
//        Vector q = linearSystemSolver.solve(new BasicVector().factory().createConstantVector(4, 0.0));

        //Find all eigen value and vector
//        Matrix[] matrixes = K.withDecompositor(LinearAlgebra.DecompositorFactory.EIGEN).decompose();

//        Log.i(TAG, "lamda:" + lamda_max);
//        Log.i(TAG, "matrix of vectors:\n" + matrixes[0].toString());
//        Log.i(TAG, "matrix of values:\n" + matrixes[1].toString());

//        double max = matrixes[1].get(0, 0);

        //Find column of V corresponding with Wi = 0;
        int index = 0;
        for (int i = 0; i < 4; i++) {
            if (Math.abs(matrixes[1].get(i, i)) < 0.000001) {
                index = i;
                break;
            }

            //can not find 0 in D matrix
            if (i == 3) {
                Log.d(TAG, "@@@" + matrixes[1].get(0, 0) + " " + matrixes[1].get(1, 1) + " " + matrixes[1].get(2, 2) + " " + matrixes[1].get(3, 3));
                return null;
            }
        }
        Vector q = matrixes[2].getColumn(index);
        return new Quaternion((float)q.get(0), (float)q.get(1), (float)q.get(2), (float)q.get(3));
    }

    private double scalarCrossProduct(Vector vector1, Vector vector2) {
        return Math.sqrt(1 - Math.pow(vector1.innerProduct(vector2), 2));
    }

    private Vector crossProduct(Vector u, Vector v) {
        double i = u.get(1) * v.get(2) - u.get(2) * v.get(1);
        double j = -u.get(0) * v.get(2) + u.get(2) * v.get(0);
        double k = u.get(0) * v.get(1) - u.get(1) * v.get(0);
        return new BasicVector(new double[]{i, j, k});
    }
}

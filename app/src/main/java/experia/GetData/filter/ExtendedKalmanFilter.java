package experia.GetData.filter;

import com.badlogic.gdx.math.Quaternion;

import org.la4j.LinearAlgebra;
import org.la4j.inversion.MatrixInverter;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import java.util.ArrayList;

/**
 * Created by Le Van Hoang on 2014/05/30.
 */
public class ExtendedKalmanFilter {

    private ArrayList<float[]> gyroList;
    private ArrayList<Quaternion> quaternionList;

    private Matrix F_k;

    // H is identity matrix
    private Matrix H_k = new Basic2DMatrix().factory().createIdentityMatrix(7);

    // R_k is diagonal matrix
    // TODO: MARG values, fix this later
    private double r11 = 0.01;
    private double r22 = 0.01;
    private double r33 = 0.01;
    private double r44 = 0.0001;
    private double r55 = 0.0001;
    private double r66 = 0.0001;
    private double r77 = 0.0001;
    private Matrix R_k = new Basic2DMatrix().factory().createMatrix(new double[][]{
            {r11, 0, 0, 0, 0, 0, 0},
            {0, r22, 0, 0, 0, 0, 0},
            {0, 0, r33, 0, 0, 0, 0},
            {0, 0, 0, r44, 0, 0, 0},
            {0, 0, 0, 0, r55, 0, 0},
            {0, 0, 0, 0, 0, r66, 0},
            {0, 0, 0, 0, 0, 0, r77}
    });

    //Q_k: process noise
    //TODO: MARG values, fix this later
    // 0.4 rad^2/s~2
    private double D_i = 0.4;
    // 0.5s
    private double tau_i = 0.5;
    //TODO: deta_t = ? , 20ms = 0.02s, constant ?
    private double delta_t = 0.02;

    private double q11 = (D_i * (1 - Math.exp(-2 * delta_t / tau_i))) / (2 * tau_i);
    private double q22 = q11;
    private double q33 = q11;
    private Matrix Q_k = new Basic2DMatrix().factory().createMatrix(new double[][]{
            {q11, 0, 0, 0, 0, 0, 0},
            {0, q22, 0, 0, 0, 0, 0},
            {0, 0, q33, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0}
    });


    public ExtendedKalmanFilter(ArrayList<float[]> gyroList, ArrayList<Quaternion> quaternionList) {
        this.gyroList = gyroList;
        this.quaternionList = quaternionList;
    }

    //Transition matrix
    public Matrix F_k(Matrix estimate_x_k) {
        //constant variable
        double f11;
        double f22;
        double f33;

        double f41;
        double f42;
        double f43;
        double f44;
        double f45;
        double f46;
        double f47;

        double f51;
        double f52;
        double f53;
        double f54;
        double f55;
        double f56;
        double f57;

        double f61;
        double f62;
        double f63;
        double f64;
        double f65;
        double f66;
        double f67;

        double f71;
        double f72;
        double f73;
        double f74;
        double f75;
        double f76;
        double f77;


        return F_k = new Basic2DMatrix().factory().createMatrix(new double[][]{
                {f11, 0, 0, 0, 0, 0, 0},
                {0, f22, 0, 0, 0, 0, 0},
                {0, 0, f33, 0, 0, 0, 0},
                {f41, f42, f43, f44, f45, f46, f47},
                {f51, f52, f53, f54, f55, f56, f57},
                {f61, f62, f63, f64, f65, f66, f67},
                {f71, f72, f73, f74, f75, f76, f77}
        });
    }

    public void doKalmanFilter() {

        //predict x_i , p_i
        Matrix predict_x_k = null;  //TODO: row matrix ?
        Matrix predict_p_k = null;

        //estimate x_i, p_i
        Matrix estimate_x_k;
        Matrix estimate_p_k;

        //measurement z_k;
        Matrix measure_z_k;

        //Kalman gain
        Matrix K_k;

        //Identity matrix
        Matrix I = new Basic2DMatrix().factory().createIdentityMatrix(7);

        int n = gyroList.size();
        int m = quaternionList.size();
        int size = (m >= n) ? n : m;

        int i = 0;
        while (i < size) {
            if (gyroList.get(i) == null || quaternionList.get(i) == null) {
                i++;
            } else {
                //Init first state
                float[] firstGyro = gyroList.get(i);
                Quaternion firstQuaternion = quaternionList.get(i);
                predict_x_k = new Basic2DMatrix(new double[][]{{(double) firstGyro[0], (double) firstGyro[1], (double) firstGyro[2], firstQuaternion.x, firstQuaternion.y, firstQuaternion.z, firstQuaternion.w}});
                predict_p_k = new Basic2DMatrix().factory().createIdentityMatrix(7);
                break;
            }
        }
        for (; i < size; i++) {
            if (gyroList.get(i) != null && quaternionList.get(i) != null && predict_x_k != null && predict_p_k != null) {

                //Calculate Kalman gain
                Matrix inverse = H_k.multiply(predict_p_k).multiply(H_k.transpose()).add(R_k).withInverter(LinearAlgebra.INVERTER).inverse();
                K_k = predict_p_k.multiply(H_k.transpose()).multiply(inverse);

                //Update equation
                float[] gyro = gyroList.get(i);
                Quaternion quaternion = quaternionList.get(i);
                measure_z_k = new Basic2DMatrix(new double[][]{{(double) gyro[0], (double) gyro[1], (double) gyro[2], quaternion.x, quaternion.y, quaternion.z, quaternion.w}});

                estimate_x_k = predict_x_k.add(K_k.multiply(measure_z_k.subtract(predict_x_k)));
                estimate_p_k = (I.subtract(K_k.multiply(H_k))).multiply(predict_p_k);

                //Projection, predict next values
                predict_x_k = F_k.multiply(estimate_x_k);
                predict_p_k = F_k.multiply(estimate_p_k).multiply(F_k.transpose()).add(F_k);
            }
        }
    }

}

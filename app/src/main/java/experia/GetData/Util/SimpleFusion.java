package experia.GetData.Util;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic1DMatrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.dense.BasicVector;

/**
 * Created by Le Van Hoang on 2014/12/02.
 */
public class SimpleFusion {

    //Algorithm to track vertical motion of a smartphone from simple fusion of accelerometer and gyro measurements.

    private static SimpleFusion ourInstance = new SimpleFusion();

    public static SimpleFusion getInstance() {
        return ourInstance;
    }

    private SimpleFusion() {
    }

    /*
     * Input
     * ACC:  s(t) = (sx, sy, sz)
     * GYRO: om(t) = (omx, omy, omz)
     */
    private Vector s = new BasicVector(new double[]{0, 0, 0});  //s(0)
    private Vector om = new BasicVector(new double[]{0, 0, 0}); //TODO init value ?

    // tau : The time constant will be specified according to results of experimental trial and error test
    private static final int TAU = 10;   //Time in milisecond

    private double getAlpha(int timeOld, int timeNew) {
        int t = (timeNew - timeOld);
        return TAU / (TAU + t);
    }

    private BasicVector getUPg() {
        BasicVector UPg = new BasicVector();
        return UPg;
    }

    private BasicVector getUPa() {
        BasicVector UPa = new BasicVector();
        return UPa;
    }

    private Matrix getRotationMatrix(int told, int tnew, Vector vom) {
        //From wikipedia http://en.wikipedia.org/wiki/Rotation_matrix.
        int dt = (tnew - told)/2;

        double om = Math.sqrt(vom.get(0)*vom.get(0) + vom.get(1)*vom.get(1) + vom.get(2)*vom.get(2));
        double om1 = vom.fold(Vectors.mkManhattanNormAccumulator());

        double theta = dt*om;

        //U is unit vector
        Vector u = vom.divide(om1);

        double ux = u.get(0);
        double uy = u.get(1);
        double uz = u.get(2);

        double cos_theta = Math.cos(theta);
        double sin_theta = Math.sin(theta);

        //calculate the Rotation matrix
        double r11 = cos_theta + ux*ux*(1-cos_theta);
        double r12 = ux*uy*(1- cos_theta) - uz*sin_theta;
        double r13 = ux*uz*(1-cos_theta) + uy*sin_theta;

        double r21 = uy*ux*(1-cos_theta) + uz*sin_theta;
        double r22 = cos_theta + uy*uy*(1- cos_theta);
        double r23 = uy*uz*(1- cos_theta) - ux*sin_theta;

        double r31 = uz*ux*(1-cos_theta) - uy*sin_theta;
        double r32 = uz*uy*(1- cos_theta) + ux*sin_theta;
        double r33 = cos_theta + uz*uz*(1- cos_theta);

        Matrix r = new Basic2DMatrix(new double[][]{
                {r11, r12, r13},
                {r21, r22, r23},
                {r31, r32, r33}}
        );

        return r;
    }

    /*
     * Estimate new orientation
     */
    private Vector getUPnew(int told, int tnew) {
        double alpha = getAlpha(told, tnew);
        return getUPg().multiply(alpha).add(getUPg().multiply(1-alpha));
    }

}

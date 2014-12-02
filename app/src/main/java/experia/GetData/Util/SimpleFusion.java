package experia.GetData.Util;

import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;
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
        BasicVector UPg;

        return UPg;
    }

    private BasicVector getUPa() {
        BasicVector UPa;

        return UPa;
    }

    private Matrix getRotationMatrix(int told, int tnew, Vector om) {
        //From wikipedia http://en.wikipedia.org/wiki/Rotation_matrix.


    }

    /*
     * Estimate new orientation
     */
    private Vector getUPnew() {
        double alpha = getAlpha();
        return getUPg().multiply(alpha).add(getUPg().multiply(1-alpha));
    }




}

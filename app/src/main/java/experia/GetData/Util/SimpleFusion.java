package experia.GetData.Util;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.dense.BasicVector;

import java.util.ArrayList;

import experia.GetData.model.SensorData;

/**
 * Created by Le Van Hoang on 2014/12/02.
 */
public class SimpleFusion {

    /*
     * TODO make some static input, check the filtering
     */

    //Algorithm to track vertical motion of a smartphone from simple fusion of accelerometer and gyro measurements.

    public static final double T = 0.5f; //Window size = 0.5 second

    private ArrayList<SensorData> mListAcc;
    private ArrayList<SensorData> mListGyro;

    private static SimpleFusion ourInstance = new SimpleFusion();

    public static SimpleFusion getInstance() {
        return ourInstance;
    }

    private SimpleFusion() {
    }

    //Set list ACC and GYRO
    public void setData(ArrayList<SensorData> listAccc, ArrayList<SensorData> listGyro) {
        mListAcc = listAccc;
        mListGyro = listGyro;
    }

    /*
     * Input
     * ACC:  s(t) = (sx, sy, sz)
     * GYRO: om(t) = (omx, omy, omz)
     */
    private Vector s = new BasicVector(new double[]{0, 0, 0});  //s(0)
    private Vector om = new BasicVector(new double[]{0, 0, 0}); //TODO init value ?

    // tau : The time constant will be specified according to results of experimental trial and error test
    private static final long TAU = 100;   //Time in milisecond

    private double getAlpha(SensorData eventOld, SensorData eventNew) {
        long t = (eventNew.timestamp - eventOld.timestamp)/1000000;  //Convert to milisecond
        return (double)TAU / (TAU + t);
    }

    private Vector getUPg(Vector upOld, SensorData accOld, SensorData accNew, SensorData gyro) {
        Matrix rotation = getRotationMatrix(accOld, accNew, gyro);
        Vector UPg = rotation.multiply(upOld);
        return UPg;
    }

    // get upA at time event
    private Vector getUPa(SensorData gyro, SensorData acc) {
        //Calulate window size
        ArrayList<SensorData> listWindow = getListEvent(acc);

        BasicVector g = computeG(gyro, acc, listWindow);

        return g.divide(Math.sqrt(g.get(0)*g.get(0) + g.get(1)*g.get(1) + g.get(2)*g.get(2))).multiply(-1);
    }

    private ArrayList<SensorData> getListEvent(SensorData event) {
        SensorData eventStart;
        SensorData eventEnd;

        int position = mListAcc.indexOf(event);
        int start = position;
        int end = position;

        //Backward for 0.5 second
        while ((event.timestamp - mListAcc.get(start).timestamp) < 500000000) {
            start -= 1;
        }

        //Forward for 0.5 second
        while ((mListAcc.get(end).timestamp - event.timestamp) < 500000000) {
            end += 1;
        }

        return new ArrayList<SensorData>(mListAcc.subList(start, end));

    }

    private double computeW(SensorData eventI, SensorData eventJ) {
        double deltaT = (Math.abs(eventJ.timestamp - eventI.timestamp))/1000000000; //Time in second

        return Math.exp(-(deltaT/T)*(deltaT/T));
    }

    private double computeC(SensorData eventI, SensorData eventJ, SensorData accComponent, ArrayList<SensorData> listWindow) {

        double w = computeW(eventI, eventJ);

        double wSum = 0;

        for (SensorData event : listWindow) {
            wSum+= computeW(eventI, event);
        }
        return w/wSum;
    }

    private BasicVector computeG(SensorData eventI, SensorData eventJ, ArrayList<SensorData> listWindow) {
        double g1 = 0;
        double g2 = 0;
        double g3 = 0;
        for (SensorData event : listWindow) {
            double c = computeC(eventI, eventJ, event, listWindow);

            g1 += c*eventJ.values[0];
            g2 += c*eventJ.values[1];
            g3 += c*eventJ.values[2];
        }

        return new BasicVector(new double[]{g1, g2, g3});
    }

    private Matrix getRotationMatrix(SensorData eventOld, SensorData eventNew, SensorData gyro) {

        Vector vom = new BasicVector(new double[]{gyro.values[0], gyro.values[1], gyro.values[2]});

        //From wikipedia http://en.wikipedia.org/wiki/Rotation_matrix.
        double dt = ((eventNew.timestamp - eventOld.timestamp)/2)/1000000000;  //Convert to second

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
    public Vector getUPnew(Vector upOld, SensorData accOld, SensorData accNew, SensorData gyroNew) {
        double alpha = 0;
        if (accOld != null) {
            alpha = getAlpha(accOld, accNew);
            return getUPg(upOld, accOld, accNew, gyroNew).multiply(alpha).add(getUPa(gyroNew, accNew).multiply(1-alpha));
        } else {
            return getUPa(gyroNew, accNew);
        }

    }

}

package experia.GetData.filter;

import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by hoang8f on 5/23/14.
 */
public class MyKalmanFilter extends ExtendedKalmanFilter {


    /**
     * Constructs an extended Kalman filter with no default motion and
     * observation models.
     * @param x the initial state estimate.
     * @param P the initial state covariance.
     */
    public MyKalmanFilter(RealMatrix x, RealMatrix P) {
        super(x, P);
    }

    @Override
    protected RealMatrix F(RealMatrix x) {
        //x la dau vao voi matrix 7 thanh phan
        //Minh chi can tinh toan F
        return null;
    }

    @Override
    protected RealMatrix Q(RealMatrix x) {
        //7x7 Matrix
        //q11 = D1*(1- e^(-2 delta.t/ t1))/2t1
        //Di and ti is experiment value , ti = 0.5s. Di = 0.4
        return null;
    }

    @Override
    protected RealMatrix B(RealMatrix x) {
        // ????
        return null;
    }

    @Override
    protected RealMatrix H(RealMatrix x) {
        //Identity matrix , size ?
        return null;
    }

    @Override
    protected RealMatrix R(RealMatrix x) {
        //Experimental value
        //R11 = R22 = R33 = 0.01
        //R44 = R55 = R66 = 0.0001
        //R is identity matrix ?
        return null;
    }
}

package experia.GetData.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class GraphView extends View {

    Paint mLinePaint;
    ArrayList<int[]> sensorValues = new ArrayList<int[]>();
    Path mXAxisPath = new Path();

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawAxis(canvas, 100);
    }

    private void drawAxis(Canvas canvas, int step) {

        //Line
        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        DashPathEffect dashEffect = new DashPathEffect(new float[]{5, 5}, 1);
        mLinePaint.setPathEffect(dashEffect);
        mLinePaint.setColor(Color.GRAY);
        mLinePaint.setStrokeWidth(2);

        // +3
        canvas.drawLine(0, this.getHeight() / 2 - step * 3, this.getWidth(), this.getHeight() / 2 - step * 3, mLinePaint);
        // +2
        canvas.drawLine(0, this.getHeight() / 2 - step * 2, this.getWidth(), this.getHeight() / 2 - step * 2, mLinePaint);
        // +1
        canvas.drawLine(0, this.getHeight() / 2 - step, this.getWidth(), this.getHeight() / 2 - step, mLinePaint);
        // 0
        canvas.drawLine(0, this.getHeight() / 2, this.getWidth(), this.getHeight() / 2, mLinePaint);
        // -1
        canvas.drawLine(0, this.getHeight() / 2 + step, this.getWidth(), this.getHeight() / 2 + step, mLinePaint);
        // -2
        canvas.drawLine(0, this.getHeight() / 2 + step * 2, this.getWidth(), this.getHeight() / 2 + step * 2, mLinePaint);
        // -3
        canvas.drawLine(0, this.getHeight() / 2 + step * 3, this.getWidth(), this.getHeight() / 2 + step * 3, mLinePaint);

//        canvas.drawLine(this.getWidth()/2, 0, this.getWidth()/2, this.getHeight(), mLinePaint);
        canvas.drawRect(new Rect(2, 2, this.getWidth() - 2, this.getHeight() - 2), mLinePaint);

        //Draw value
        drawPoints(canvas);

    }

    private void drawPoints(Canvas canvas) {
        int size = sensorValues.size();
        float gap = 1f;
        for (int i = (size - 1); i>=0; i --) {
            mLinePaint.setColor(Color.RED);
            canvas.drawPoint((size - i) * gap,this.getHeight()/2 + sensorValues.get(i)[0]/1000, mLinePaint);

            mLinePaint.setColor(Color.GREEN);
            canvas.drawPoint((size - i) * gap,this.getHeight()/2 + sensorValues.get(i)[1]/1000, mLinePaint);

            mLinePaint.setColor(Color.BLUE);
            canvas.drawPoint((size - i) * gap,this.getHeight()/2 + sensorValues.get(i)[2]/1000, mLinePaint);
        }
    }

    public void makePath(int[] values) {
        if (sensorValues.size() == this.getWidth()) {
            try {
            sensorValues.remove(0);
            } catch (Exception e) {

            }
        }
        sensorValues.add(values);

        /*
        mXAxisPath.reset();
        mXAxisPath.moveTo(0, sensorValues.get(sensorValues.size() - 1));

        for (int i = (sensorValues.size() -2); i>=0; i --) {

        }
        */
    }
}

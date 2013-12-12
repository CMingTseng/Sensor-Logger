package experia.GetData;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

//加速度センサーの利用
public class SensorView extends View {
    private int[] acceleration= {-1, -1, -1};	//加速度
    private int[] linearacceleraration= {-1, -1, -1};	//加速度
    private int[] orientation = {-1, -1, -1};	//傾き
    private int[] magnetic= {-1, -1, -1};	//磁界
    private int[] gyroscope= {-1, -1, -1};	//ジャイロ
    private int[] light= {-1};		//ライト
    private int[] pressure= {-1};	//圧力
    private int[] proximity= {-1};	//近接
    private int[] gravity= {-1, -1, -1};	//重力
    private int[] rotation= {-1, -1, -1};	//回転ベクトル
    private int[] temperature= {-1};	//温度
    private int[] orientationValues = {-1, -1, -1};	//傾き

    Paint paint = null;
    String line = "SensorEx>";
    int hi = 28;//28 for xperia, 12 for IDE

    //コンストラクタ

    public SensorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint=new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(hi + 1);
        paint.setColor(Color.WHITE);
    }

    public void setLine(String line) {
       this.line = line;
    }

    //加速度の指定
    public void setAcceleration(int[] acceleration) {
        this.acceleration=acceleration;
    }
    //線形加速度の指定
    public void setLinearacceleraration(int[] linearacceleraration) {
        this.linearacceleraration=linearacceleraration;
    }
    //傾きの指定
    public void setOrientation(int[] orientation) {
        this.orientation=orientation;
    }
    //磁界の指定
    public void setMagnetic(int[] magnetic) {
        this.magnetic=magnetic;
    }
    //ジャイロの指定
    public void setGyroscope(int[] gyroscope) {
        this.gyroscope=gyroscope;
    }
    //照度の指定
    public void setLight(int[] light) {
        this.light=light;
    }
    //圧力の指定
    public void setPressure(int[] pressure) {
        this.pressure=pressure;
    }
    //近接の指定
    public void setProximity(int[] proximity) {
        this.proximity=proximity;
    }
    //重力の指定
    public void setGravity(int[] gravity) {
        this.gravity=gravity;
    }
    //回転ベクトルの指定
    public void setRotation(int[] rotation) {
        this.rotation=rotation;
    }
    //温度の指定
    public void setTmperature(int[] temperature) {
      this.temperature=temperature;
    }
    //傾きVの指定
    public void setOrientationValues(int[] orientationValues) {
      this.orientationValues=orientationValues;
    }

    //描画
    @Override
    protected void onDraw(Canvas canvas) {
        //値の表示
        canvas.drawText(line,0, hi,paint);
        canvas.drawText("X軸加速度:"+acceleration[0],0,hi*2,paint);
        canvas.drawText("Y軸加速度:"+acceleration[1],0,hi*3,paint);
        canvas.drawText("Z軸加速度:"+acceleration[2],0,hi*4,paint);
        canvas.drawText("方位:"    +orientation[0], 0,hi*5,paint);
        canvas.drawText("ピッチ:"   +orientation[1], 0,hi*6,paint);
        canvas.drawText("ロール:"   +orientation[2], 0,hi*7,paint);
        canvas.drawText("X軸磁界:"   +magnetic[0], 0,hi*8,paint);
        canvas.drawText("Y軸磁界:"   +magnetic[1], 0,hi*9,paint);
        canvas.drawText("Z軸磁界:"   +magnetic[2], 0,hi*10,paint);
        canvas.drawText("X軸ジャイロ:"   +gyroscope[0], 0,hi*11,paint);
        canvas.drawText("Y軸ジャイロ:"   +gyroscope[1], 0,hi*12,paint);
        canvas.drawText("Z軸ジャイロ:"   +gyroscope[2], 0,hi*13,paint);
        canvas.drawText("照度:"   +light[0], 0,hi*14,paint);
        canvas.drawText("圧力:"   +pressure[0], 0,hi*15,paint);
        canvas.drawText("近接:"   +proximity[0], 0,hi*16,paint);
        canvas.drawText("X軸重力:"   +gravity[0], 0,hi*17,paint);
        canvas.drawText("Y軸重力:"   +gravity[1], 0,hi*18,paint);
        canvas.drawText("Z軸重力:"   +gravity[2], 0,hi*19,paint);
        canvas.drawText("Y軸回転ベクトル:"   +rotation[1], 0,hi*21,paint);
        canvas.drawText("Z軸回転ベクトル:"   +rotation[2], 0,hi*22,paint);
        canvas.drawText("温度:"   +temperature[0], 0,hi*23,paint);
        canvas.drawText("(線形)X軸加速度:"+linearacceleraration[0],0,hi*24,paint);
        canvas.drawText("(線形)Y軸加速度:"+linearacceleraration[1],0,hi*25,paint);
        canvas.drawText("(線形)Z軸加速度:"+linearacceleraration[2],0,hi*26,paint);
        canvas.drawText("azmuth:"    +orientationValues[0], 0,hi*27,paint);
        canvas.drawText("pitch:"   +orientationValues[1], 0,hi*28,paint);
        canvas.drawText("roll:"   +orientationValues[2], 0,hi*29,paint);

    }
}

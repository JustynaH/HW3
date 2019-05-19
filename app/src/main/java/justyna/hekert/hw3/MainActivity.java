package justyna.hekert.hw3;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    static public SensorManager mSensorManager;
    static Sensor SensorAcc;

    private TextView answerTxtView;
    private ImageView emptyBallImgView;
    private ImageView ballImgView;
    private String[] answersArray;
    private int nAns;

    private int randomAnswer;
    private boolean start = false;

    private int screenWidth;
    private int screenHeight;
    private int imgEdgeSize;
    private boolean layoutReady;
    private ConstraintLayout mainContainer;
    private boolean animFlag = false;

    private long lastUpdate = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        answersArray = getResources().getStringArray(R.array.Answer);
        nAns = answersArray.length;

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){        // Success!
            SensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            answerTxtView = findViewById(R.id.answerTxt);
            emptyBallImgView = findViewById(R.id.emptyBallImg);
            ballImgView = findViewById(R.id.ballImg);

            if(SensorAcc != null){
                emptyBallImgView.setVisibility(View.INVISIBLE);
                answerTxtView.setVisibility(View.INVISIBLE);
                ballImgView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No accelerometer", Toast.LENGTH_SHORT).show();
        }


        layoutReady = false;
        mainContainer = findViewById(R.id.sensor_container);
        mainContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imgEdgeSize = emptyBallImgView.getWidth();
                screenWidth = mainContainer.getWidth();
                screenHeight = mainContainer.getHeight();

                mainContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                layoutReady = true;
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timeMicro;
        if(lastUpdate == -1){
            lastUpdate = event.timestamp;
            timeMicro = 0;
        } else {
            timeMicro = (event.timestamp - lastUpdate)/1000L;
            lastUpdate =event.timestamp;
        }

         if(layoutReady) {
            handleAccelerationSensor(event.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(SensorAcc != null)
            mSensorManager.registerListener(this, SensorAcc, 100000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(SensorAcc != null)
            mSensorManager.unregisterListener(this, SensorAcc);
    }

    private void handleAccelerationSensor (final float sensorValue) {
        if(!animFlag) {
            if(abs(sensorValue) > 3) {
                animFlag = true;
                start = true;

                emptyBallImgView.setVisibility(View.INVISIBLE);
                ballImgView.setVisibility(View.VISIBLE);
                answerTxtView.setVisibility(View.INVISIBLE);

                FlingAnimation flingX = new FlingAnimation(ballImgView, DynamicAnimation.X);

                flingX.setStartVelocity(-1 * sensorValue * screenWidth/2f)
                        .setMinValue(5)
                        .setMaxValue(screenWidth - imgEdgeSize - 5)
                        .setFriction(1f);

                flingX.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean b, float v, float v1) {
                        if(v1 != 0) {
                            final FlingAnimation reflingX = new FlingAnimation(ballImgView, DynamicAnimation.X);

                            reflingX.setStartVelocity(-1 * v1)
                                    .setMinValue(5)
                                    .setMaxValue(screenWidth - imgEdgeSize - 5)
                                    .setFriction(1.25f)
                                    .start();

                            reflingX.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                                @Override
                                public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean b, float v, float v1) {
                                    animFlag = false;
                                    randomAnswer = ((int) abs((sensorValue * 100))) % nAns;
                                }
                            });
                        } else {
                            animFlag = false;
                        }

                    }
                });
                flingX.start();
            }   else if (start) {
                emptyBallImgView.setVisibility(View.VISIBLE);
                ballImgView.setVisibility(View.INVISIBLE);

                answerTxtView.setVisibility(View.VISIBLE);
                TextView valueTextView = findViewById(R.id.answerTxt);
                valueTextView.setText(answersArray[randomAnswer]);
                start = false;
            }
        }
    }


}

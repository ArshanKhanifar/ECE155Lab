package ca.uwaterloo.lab3_206_09;

import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import mapper.InterceptPoint;
import mapper.NavigationalMap;
import mapper.VectorUtils;

/**
 * Created by arshan on 2016-06-08.
 */
public class AccelerometerEventListener extends MainActivity implements SensorEventListener {

    static int StepCount = 0;
    TextView mSteps;
    public int IDLE = 0 ;
    public int TAKING_STEP= 1;
    public int STEP_TAKEN = 2;
    public int SHAKING = 3;
    public int state = IDLE;
    NavigationalMap accelMap;
    
    float valueBefore = (float) 9.8;
    float lowPassBefore = (float) 9.8;
    float difference;
    float prevDifference;

    PointF currentUserPoint = new PointF();
    PointF nextUserPoint = new PointF();
    List<InterceptPoint> goThroughWalls;
    PathFinder pathFinder;



    public AccelerometerEventListener(TextView steps, NavigationalMap map, PathFinder pf){
        mSteps = steps;
        accelMap = map;
        pathFinder = pf;
    }

    public void onAccuracyChanged(Sensor s, int i) {
    }
    @Override
    public void onSensorChanged(SensorEvent se) {

        if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //save the accelerometer values in the data array
            float value = (float) Math.sqrt( se.values[0]*se.values[0] + se.values[1]*se.values[1] + se.values[2]*se.values[2] );

            float alpha = (float) 0.5;

            //High pass filter out[i] = α * in[i] + (1-α) * out[i-1];
            float lowPassValue =  alpha * value + (1 - alpha) * lowPassBefore;


            // VALUE OF A1 and A2
            float A1 = 12.7f;
            float A2 = 8.1f;


            // Making the FSM :
            switch (state){
                case(0): //idle
                    if(lowPassValue>A1){
                        state = TAKING_STEP;
                    }
                    break;
                case(1)://taking step
                    if(lowPassValue<A2){
                        state = STEP_TAKEN;
                    }
                    if(lowPassValue>18){
                        state = SHAKING;
                    }
                    break;
                case(2): // step taken
                    if(pathFinder.destinationPoint!=null && pathFinder.originPoint!=null){
                        takeStep();
                    }
                    state = IDLE;
                    break;
                case(3): // shaking
                    if(lowPassValue<11){
                        state = IDLE;
                    }
            }

            prevDifference = difference;

            float[] magnitudeLow = new float[1];
            magnitudeLow[0] = lowPassValue;

            valueBefore = value;
            lowPassBefore = lowPassValue;
        }
    }
    public void takeStep(){
        //Get the current user point
        currentUserPoint = mv.getUserPoint();

        //Update next user point based on the step they just took
        nextUserPoint.x = currentUserPoint.x + Compass.sineAngle * STEP_SIZE;
        nextUserPoint.y = currentUserPoint.y - Compass.cosineAngle * STEP_SIZE;

        //Calculate intersections between current and next user points
        goThroughWalls = accelMap.calculateIntersections(currentUserPoint, nextUserPoint);

        //If point is on one of the walls, then don't move forward any more!
        if (goThroughWalls.isEmpty()) {
            //No walls in between, so update the point to the new point

            boolean reachedDestination = VectorUtils.distance(pathFinder.destinationPoint,nextUserPoint)<MainActivity.STEP_SIZE;

            int stepsToBreakPoint = pathFinder.stepNumberCalculator(nextUserPoint,pathFinder.getFirstBreakPoint());
            if(stepsToBreakPoint==0 && !reachedDestination){
                float differenceBetweenBreakPointAndDestination = Math.abs(pathFinder.getFirstBreakPoint().x - pathFinder.destinationPoint.x);
                if(differenceBetweenBreakPointAndDestination<2*pathFinder.BABY_STEP){
                    PointF point = new PointF(pathFinder.destinationPoint.x,pathFinder.getFirstBreakPoint().y);
                    nextUserPoint = point;
                }else{
                    nextUserPoint = pathFinder.getFirstBreakPoint();
                }
            }
            mv.setUserPoint(nextUserPoint);
            StepCount++;
            mSteps.setText("Step Count: " + StepCount);
            mv.setUserPath(pathFinder.calculatePath());
            pathFinder.giveNextInstruction(reachedDestination);
        }
    }

}

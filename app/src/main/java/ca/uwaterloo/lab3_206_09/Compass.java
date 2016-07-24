package ca.uwaterloo.lab3_206_09;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arshan on 2016-06-23.
 */
public class Compass extends  MainActivity implements SensorEventListener {
    private float[] gravity = new float[3];
    private float[] magneticField = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];

    public static float sineAngle;
    public static float cosineAngle;

    private static TextView angle;
    private ImageView compass;

    private List<Float> sinList = new ArrayList<>();
    private List<Float> cosList = new ArrayList<>();


    Compass(TextView theAngle, ImageView comps) {
        angle = theAngle;
        compass = comps;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticField = event.values;
        }

        if (magneticField != null && gravity != null) {
            SensorManager.getRotationMatrix(rotationMatrix, null, gravity, magneticField);
            SensorManager.getOrientation(rotationMatrix, orientation);

            sinList.add((float)Math.sin(orientation[0]));
            cosList.add((float)Math.cos(orientation[0]));
            if (sinList.size() > 150) sinList.remove(0);
            if (cosList.size() > 150) cosList.remove(0);
            sineAngle = calculateAverage(sinList);
            cosineAngle = calculateAverage(cosList);



            float AngleToBeShown;
            if(cosineAngle>=0){
                AngleToBeShown = (float) Math.toDegrees(Math.asin(sineAngle));
            }else{
                AngleToBeShown = (float) Math.toDegrees(Math.PI - Math.asin(sineAngle));
            }
            angle.setText("" + Math.floor((double)AngleToBeShown));
            compass.setRotation(-AngleToBeShown);

        }
    }

    private static float calculateAverage(List<Float> marks) {
        float sum = 0;
        if (!marks.isEmpty()) {
            for(int i = 0 ; i < marks.size() ; i++){
              sum+=marks.get(i);
            }
            return sum / (marks.size());
        }
        return sum;
    }

}
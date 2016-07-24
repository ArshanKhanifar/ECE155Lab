package ca.uwaterloo.lab3_206_09;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import mapper.MapLoader;
import mapper.MapView;
import mapper.NavigationalMap;



public class MainActivity extends AppCompatActivity{

    public static MapView mv;
    private TextView angle;
    private ImageView compassImage;
    private EditText stepSize;
    private TextView instructions;

    public static float STEP_SIZE = 0.75f * 3/2 * 17/14 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up the mapview, set height, width and position
        mv = new MapView(getApplicationContext(), 1000, 1000, 40, 40);
        registerForContextMenu(mv);

        LinearLayout layout = ((LinearLayout)findViewById(R.id.parentElement));

        final TextView steps = (TextView) findViewById(R.id.steps);
        instructions = (TextView) findViewById(R.id.Instructions);
        angle = (TextView) findViewById(R.id.angle);
        stepSize = (EditText) findViewById(R.id.stepLength);

        stepSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()>0){
                    STEP_SIZE = Float.parseFloat(s.toString()) * 3/2 * 17/14;
                }
            }
        });


        Button resetSteps = (Button) findViewById(R.id.resetSteps);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); // setting up sensor manager

        //add the map to the view
        final NavigationalMap  map = MapLoader.loadMap(getExternalFilesDir(null),"Lab-room.svg");
        mv.setMap(map);
        layout.addView(mv);

        final PathFinder pFinder = new PathFinder(mv,map, instructions);

        Sensor accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // setting up accelerometer
        final AccelerometerEventListener accelerationListener = new AccelerometerEventListener(steps, map, pFinder);
        sensorManager.registerListener(accelerationListener,accelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);

        compassImage = (ImageView) findViewById(R.id.compass);

        SensorEventListener compass = new Compass(angle,compassImage);
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(compass,magneticSensor,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(compass,accelerationSensor,SensorManager.SENSOR_DELAY_NORMAL);

        Button fakeButton = (Button)findViewById(R.id.fakeStep);
        assert  fakeButton != null;
        fakeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                accelerationListener.takeStep();
            }
        });

        mv.addListener(pFinder);

        assert resetSteps != null;
        resetSteps.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AccelerometerEventListener.StepCount = 0;
                steps.setText("Step Count : 0 ");
            }
        });

    }

    @Override
    public  void  onCreateContextMenu(ContextMenu  menu , View v, ContextMenuInfo  menuInfo) {
        super.onCreateContextMenu(menu , v, menuInfo);
        mv.onCreateContextMenu(menu , v, menuInfo);
    }

    @Override
    public  boolean  onContextItemSelected(MenuItem  item) {
        return super.onContextItemSelected(item) ||  mv.onContextItemSelected(item);
    }


}
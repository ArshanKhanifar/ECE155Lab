package ca.uwaterloo.lab3_206_09;

import android.graphics.PointF;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import mapper.MapView;
import mapper.NavigationalMap;
import mapper.PositionListener;
import mapper.VectorUtils;

/**
 * Created by arshan on 2016-07-08.
 */
public class PathFinder implements PositionListener{
    public final float BABY_STEP = 0.4f;
    private MapView mapView;
    private NavigationalMap map;
    private List<PointF> pathPoints = new ArrayList<>();
    public PointF originPoint;
    public PointF destinationPoint;
    TextView nextInstruction;

    PathFinder(MapView mv,NavigationalMap theMap, TextView instruction){
        mapView = mv;
        map = theMap;
        nextInstruction = instruction;
    }

    @Override
    public void originChanged(MapView source, PointF loc) {

        if (true) { //only set the point if it is in a valid location
            mapView.setOriginPoint(loc);// this is where the user starts to walk
            mapView.setUserPoint(loc);// I guess they're starting to walk from where they start to walk! :|
            originPoint = loc;

            //Calculate path if destination already selected
            if(destinationPoint!=null){
                mapView.setUserPath(calculatePath());
                giveNextInstruction(false);
            }

        } else {
            nextInstruction.setText("Invalid point location! Please select a different point.");
            PointF invalidPoint = new PointF(0,0);
            List<PointF> clearPath = new ArrayList<PointF>();
            clearPath.add(invalidPoint);
            mapView.setUserPath(clearPath);
            mapView.setOriginPoint(invalidPoint);
        }
        Log.d("myActivity" , "x is : " + loc.x);
    }

    @Override
    public void destinationChanged(MapView source, PointF dest) {
        if(true) { //only set the point if it is in a valid location
            mapView.setDestinationPoint(dest); // this is where they put their destination
            destinationPoint = dest;

            //Calculate path if origin already selected
            if(originPoint!=null) {
                mapView.setUserPath(calculatePath());
                giveNextInstruction(false);
            }

        } else {
            nextInstruction.setText("Invalid point location! Please select a different point.");
            PointF invalidPoint = new PointF(0,0);
            List<PointF> clearPath = new ArrayList<PointF>();
            clearPath.add(invalidPoint);
            mapView.setUserPath(clearPath);
            mapView.setDestinationPoint(invalidPoint);
        }
    }
    public void clearPath(){
        pathPoints.clear();
        mapView.setUserPath(pathPoints);
    }

    public List<PointF> calculatePath(){
        pathPoints.clear();
        boolean reachedDest = false;
        pathPoints.add(mapView.getUserPoint());
        while(!reachedDest){
            if(destinationPoint.x>getLastPoint().x){
                while((destinationPoint.x>getLastPoint().x) ){
                    if (canGoRight(getLastPoint())){
                        goRight(getLastPoint());
                    }else{ // detected a wall
                        PointF wallPoint = getLastPoint();
                        while(!canGoRight(getLastPoint())){
                            goUp(getLastPoint());
                            if(!canGoUp(getLastPoint())){
                                pathPoints = pathPoints.subList(0,pathPoints.indexOf(wallPoint) + 1 );
                                while(!canGoRight(getLastPoint())){
                                    goDown(getLastPoint());
                                }
                            }
                        }
                    }
                }
            }else{
                while((destinationPoint.x<getLastPoint().x) ){
                    if (canGoLeft(getLastPoint())){
                        goLeft(getLastPoint());
                    }else{ //detected a wall
                        PointF wallPoint = getLastPoint();
                        while(!canGoLeft(getLastPoint())){
                            goUp(getLastPoint());
                            if(!canGoUp(getLastPoint())){
                                pathPoints = pathPoints.subList(0,pathPoints.indexOf(wallPoint) + 1 );
                                while(!canGoLeft(getLastPoint())){
                                    goDown(getLastPoint());
                                }
                            }
                        }
                    }
                }
            }
            if(destinationPoint.y>getLastPoint().y){
                while((destinationPoint.y>getLastPoint().y)){
                    if(canGoDown(getLastPoint())){
                        goDown(getLastPoint());
                    }else{ // detected a wall
                        PointF wallPoint = getLastPoint();
                        float initialX = wallPoint.x;
                        while(!canGoDown(getLastPoint())){
                            goRight(getLastPoint());
                            if(!canGoRight(getLastPoint())){
                                pathPoints = pathPoints.subList(0,pathPoints.indexOf(wallPoint) + 1);
                                while (!canGoDown(getLastPoint())){
                                    goLeft(getLastPoint());
                                }
                            }
                        }
                        goDown(getLastPoint());
                        if(!canGoLeft(getLastPoint())){
                            while(!canGoLeft(getLastPoint())){
                                goDown(getLastPoint());
                            }
                            while (getLastPoint().x!=initialX){
                                goLeft(getLastPoint());
                            }
                        }else if(!canGoRight(getLastPoint())){
                            while (!canGoRight(getLastPoint())){
                                goDown(getLastPoint());
                            }
                            while (getLastPoint().x!=initialX){
                                goRight(getLastPoint());
                            }
                        }
                    }
                }
            }else {
                while((destinationPoint.y<getLastPoint().y)) {
                    if (canGoUp(getLastPoint())) {
                        goUp(getLastPoint());
                    } else { // detected a wall
                        PointF wallPoint = getLastPoint(); // we save a reference in case the first direction wasn't right
                        float initialX = wallPoint.x;
                        while (!canGoUp(getLastPoint())) {
                            goRight(getLastPoint());
                            if(!canGoRight(getLastPoint())){
                                pathPoints = pathPoints.subList(0,pathPoints.indexOf(wallPoint) + 1); // we choose the other direction
                                while (!canGoUp(getLastPoint())){
                                    goLeft(getLastPoint());
                                }
                            }
                        }
                        goUp(getLastPoint());
                        if(!canGoLeft(getLastPoint())){
                            while (!canGoLeft(getLastPoint())) {
                                goUp(getLastPoint());
                            }
                            while (getLastPoint().x != initialX) {
                                goLeft(getLastPoint());
                            }
                        }else if(!canGoRight(getLastPoint())){
                            while(!canGoRight(getLastPoint())){
                                goUp(getLastPoint());
                            }
                            while (getLastPoint().x!=initialX){
                                goRight(getLastPoint());
                            }
                        }
                    }
                }
            }
            reachedDest = true;
        }
        pathPoints.add(destinationPoint);
        return pathPoints;
    }
    private String getDirection(){
        PointF firstPoint = mapView.getUserPoint();
        PointF secondPoint = pathPoints.get(1);
        if(firstPoint.x-secondPoint.x==0){
            if(firstPoint.y<secondPoint.y){
                return "South";
            }else{
                return "North";
            }
        }else{
            if(firstPoint.x<secondPoint.x){
                return "East";
            }else{
                return "West";
            }
        }
    }
    public PointF getFirstBreakPoint(){
        PointF firstPoint = mapView.getUserPoint();
        PointF secondPoint = pathPoints.get(1);
        if(firstPoint.x-secondPoint.x==0){
            int i = 1;
            while(pathPoints.get(i).x-firstPoint.x==0 && i<pathPoints.size()-2){
                i++;
            }
            return pathPoints.get(i-1);
        }else{
            int i = 1;
            while(pathPoints.get(i).y-firstPoint.y==0 && i<pathPoints.size()-2){
                i++;
            }
            return pathPoints.get(i-1);
        }
    }
    public int stepNumberCalculator(PointF firstPoint, PointF secondPoint){
        return (int) Math.floor(VectorUtils.distance(firstPoint,secondPoint)/MainActivity.STEP_SIZE);
    }

    public void giveNextInstruction(boolean reached){
        if(!reached){
            int numberOfSteps = stepNumberCalculator(mapView.getUserPoint(),getFirstBreakPoint());
            if(numberOfSteps == 0) numberOfSteps = 1;
            nextInstruction.setText("Walk " + numberOfSteps + " steps " + getDirection());
        }else{
            nextInstruction.setText("You've reached your destination!");
        }
    }
    private PointF getLastPoint(){
        return pathPoints.get(pathPoints.size()-1);
    }
    private void goLeft(PointF lastPoint){
        PointF newPoint = new PointF(lastPoint.x-BABY_STEP,lastPoint.y);
        pathPoints.add(newPoint);
    }
    private void goRight(PointF lastPoint){
        PointF newPoint = new PointF(lastPoint.x+BABY_STEP,lastPoint.y);
        pathPoints.add(newPoint);
    }
    private void goUp(PointF lastPoint){
        PointF newPoint = new PointF(lastPoint.x,lastPoint.y-BABY_STEP);
        pathPoints.add(newPoint);
    }
    private void goDown(PointF lastPoint){
        PointF newPoint = new PointF(lastPoint.x,lastPoint.y+BABY_STEP);
        pathPoints.add(newPoint);
    }
    private boolean canGoLeft(PointF currentPoint){
        PointF nextPoint = new PointF(currentPoint.x - BABY_STEP ,currentPoint.y);
        return map.calculateIntersections(currentPoint, nextPoint).size()==0;
    }
    private boolean canGoRight(PointF currentPoint){
        PointF nextPoint = new PointF(currentPoint.x + BABY_STEP ,currentPoint.y);
        return map.calculateIntersections(currentPoint, nextPoint).size()==0;
    }
    private boolean canGoUp(PointF currentPoint){
        PointF nextPoint = new PointF(currentPoint.x,currentPoint.y - BABY_STEP );
        return map.calculateIntersections(currentPoint, nextPoint).size()==0;
    }

    private boolean canGoDown(PointF currentPoint){
        PointF nextPoint = new PointF(currentPoint.x,currentPoint.y + BABY_STEP );
        return map.calculateIntersections(currentPoint, nextPoint).size()==0;
    }



    //Checks if user put destination/origin inside a walled area
    private boolean invalidPointLocation(PointF inputPoint){
        float x = inputPoint.x;
        float y = inputPoint.y;

        //Inside 1st walled block
        if (x>4.343656 && x<6.4144683 && y>5.7849116 && y< 8.322921){
            return true;
        }
        //Inside 2nd walled block
        else if (x>8.422148 && x<10.49296 && y>5.8417325 && y< 8.379741){
            return true;
        }
        //Inside 3rd walled block
        else if (x>12.525892 && x<14.5967045 && y>5.8038516 && y< 8.341861){
            return true;
        }

        //If it's not in any of the blocks, user can set their point there
        return false;

    }
}

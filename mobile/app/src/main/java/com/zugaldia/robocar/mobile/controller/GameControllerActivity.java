package com.zugaldia.robocar.mobile.controller;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.zugaldia.robocar.mobile.R;
import com.zugaldia.robocar.mobile.client.RobocarRestClient;
import com.zugaldia.robocar.mobile.client.RobocarClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;
import timber.log.Timber;

public class GameControllerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.leftButton)
    Button leftButton;

    @BindView(R.id.rightButton)
    Button rightButton;

    @BindView(R.id.leftSpeedTextView)
    TextView leftSpeedTextView;

    @BindView(R.id.rightSpeedTextView)
    TextView rightSpeedTextView;

    @BindView(R.id.upArrowButton)
    ImageButton upArrowButton;

    @BindView(R.id.upLeftArrowButton)
    ImageButton upLeftArrowButton;

    @BindView(R.id.upRightArrowButton)
    ImageButton upRightArrowButton;

    @BindView(R.id.downArrowButton)
    ImageButton downArrowButton;

    @BindView(R.id.leftArrowButton)
    ImageButton leftArrowButton;

    @BindView(R.id.rightArrowButton)
    ImageButton rightArrowButton;

    @BindView(R.id.webserviceUrlTextView)
    TextView webserviceUrlTextView;

    @BindView(R.id.joystickButton)
    ImageButton joystickButton;

    @BindView(R.id.gear1Button)
    RadioButton gear1Button;

    @BindView(R.id.gear2Button)
    RadioButton gear2Button;

    @BindView(R.id.gear3Button)
    RadioButton gear3Button;

    @BindView(R.id.recordToggleButton)
    ToggleButton recordToggleButton;

    @BindView(R.id.autonomousToggleButton)
    ToggleButton autonomousToggleButton;

    int lastLeftSpeed = 0;
    int lastRightSpeed = 0;

    int MIN_SPEED = 56;
    int MAX_SPEED = 255;
    int SPEED_STEPS = 4;
    int MIN_TURN_SPEED = 100;
    int SPEED_FULL = 100;
    int SPEED_LOW = MIN_SPEED;
    int SPEED_CHANGE = (SPEED_FULL - SPEED_LOW)/SPEED_STEPS;

    private InertiaController inertiaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_controller);
        ButterKnife.bind(this);
        initNavigationDrawer();
        inertiaController = new InertiaController();
    }

    @OnTouch({
            R.id.upArrowButton,
            R.id.upLeftArrowButton,
            R.id.upRightArrowButton,
            R.id.downArrowButton,
            R.id.leftArrowButton,
            R.id.rightArrowButton,
            R.id.leftButton,
            R.id.rightButton,
            R.id.joystickButton,
            R.id.gear1Button,
            R.id.gear2Button,
            R.id.recordToggleButton,
            R.id.autonomousToggleButton,
    })
    public boolean onTouch(View v, MotionEvent event) {
        if (v == upArrowButton || v == downArrowButton || v == leftArrowButton || v == rightArrowButton || v == upLeftArrowButton || v == upRightArrowButton)
            return handleArrowButtonEvent(v, event);
        if (v == leftButton || v == rightButton)
            return handleSlideButtonEvent(v, event);
        if(v == joystickButton)
            return handleJoystickButtonEvent(v,event);
        if (v == gear1Button || v == gear2Button || v == gear3Button)
            return handleGearButtonEvent(v,event);
        if (v == recordToggleButton)
            return handleRecordButtonEvent(v,event);
        if (v == autonomousToggleButton)
            return handleDriveButtonEvent(v,event);

        return false;
    }

    private boolean handleDriveButtonEvent(View v, MotionEvent event) {
        try {
            RobocarClient service= new RobocarRestClient("http://"+this.webserviceUrlTextView.getText());
            service.toggleDrive();
        }catch(Exception e) {
            Toast.makeText(this, "Unable to communicate with Robocar: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            Timber.d(e.getMessage());
        }
        //to switch state
        return false;
    }

    private boolean handleRecordButtonEvent(View v, MotionEvent event) {
        try {
            RobocarClient service= new RobocarRestClient("http://"+this.webserviceUrlTextView.getText());
            service.toggleRecord();
        }catch(Exception e) {
            Toast.makeText(this, "Unable to communicate with Robocar: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            Timber.d(e.getMessage());
        }
        return false;
    }

    public boolean handleGearButtonEvent(View v, MotionEvent event){
        if (v == gear1Button){
            SPEED_FULL = 100;
            SPEED_LOW = MIN_SPEED;
        }else if (v == gear2Button){
            SPEED_FULL = 150;
            SPEED_LOW = 95;
        }else{
            SPEED_FULL = MAX_SPEED;
            SPEED_LOW = 140;
        }
        SPEED_CHANGE = (SPEED_FULL - SPEED_LOW)/SPEED_STEPS;
        return false;
    }

    public boolean handleJoystickButtonEvent(View v, MotionEvent event){
        inertiaController.stop();
        boolean buttonPressed = (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN;
        boolean buttonReleased = (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP;
        if(buttonPressed){
            leftArrowButton.setVisibility(View.GONE);
            rightArrowButton.setVisibility(View.GONE);
            upArrowButton.setVisibility(View.GONE);
            downArrowButton.setVisibility(View.GONE);

            joystickButton.setBackgroundResource(R.drawable.button_joystick);
        }

        if(buttonReleased){
            leftArrowButton.setVisibility(View.VISIBLE);
            rightArrowButton.setVisibility(View.VISIBLE);
            upArrowButton.setVisibility(View.VISIBLE);
            downArrowButton.setVisibility(View.VISIBLE);
            setSpeed(0,0);
            joystickButton.setBackgroundResource(0);
            return true;
        }

        float circleRadius = joystickButton.getMeasuredWidth()/2;

        float xSigned = event.getX() - joystickButton.getMeasuredWidth() /2f ;
        float ySigned = joystickButton.getMeasuredHeight() / 2f - event.getY();
        float xUnsigned = Math.abs(xSigned);
        float yUnsigned = Math.abs(ySigned);

        double touchDistanceToCenter = Math.sqrt(xSigned * xSigned + ySigned * ySigned);
        if(touchDistanceToCenter<circleRadius*.8){
            setSpeed(0,0);
            return true;
        }

        float ySign = ySigned > 0 ? 1 : -1;
        float xSign = xSigned > 0 ? 1 : -1;

        boolean isForward = false;
        boolean isBackward = false;
        boolean isLeft=false;
        boolean isRight=false;

        if(ySigned>0) {
            isForward = true;
            isBackward=false;
        }
        else{
            isForward = false;
            isBackward = true;
        }
        if(xSigned>0){
            isRight = true;
            isLeft = false;
        }
        else{
            isRight = false;
            isLeft = true;
        }

        SpeedValueInterpolator svi = new SpeedValueInterpolator()
                .setValueRange(1.2f,6)
                .setSpeedRange(SPEED_LOW,SPEED_FULL)
                .setSpeedStep(10);

        boolean sameDirection = yUnsigned > xUnsigned;

        float m = sameDirection ? yUnsigned / (xUnsigned<1e-6f? 1e-6f:xUnsigned): xUnsigned/(yUnsigned<1e-6f?1e-6f:yUnsigned);
        float lowSpeed = svi.getSpeedForValue(m);

        if(isForward) {
            if (sameDirection) {
                if (isLeft)
                    setSpeed((int) (ySign * lowSpeed), (int) ySign * SPEED_FULL);
                else
                    setSpeed((int) (ySign * SPEED_FULL), (int) (ySign * lowSpeed));
            } else {
                if (isLeft)
                    setSpeed((int) (-ySign * lowSpeed), (int) ySign * SPEED_FULL);
                else
                    setSpeed((int) (ySign * SPEED_FULL), (int) (-ySign * lowSpeed));
            }
        }
        if(isBackward) {
            if (sameDirection) {
                if (isLeft)
                    setSpeed((int) ySign * SPEED_FULL,(int) (ySign * lowSpeed));
                else
                    setSpeed((int) (ySign * lowSpeed),(int) (ySign * SPEED_FULL));
            } else {
                if (isLeft)
                    setSpeed( (int) ySign * SPEED_FULL,(int) (-ySign * lowSpeed));
                else
                    setSpeed( (int) (-ySign * lowSpeed), (int) (ySign * SPEED_FULL));
            }
        }

        inertiaController.start();
        return true;
    }

    boolean isUpArrowPressed = false;
    boolean isUpLeftArrowPressed = false;
    boolean isUpRightArrowPressed = false;
    boolean isDownArrowPressed = false;
    boolean isLeftArrowPressed = false;
    boolean isRightArrowPressed = false;

    private boolean isArrowButtonPressed(boolean currentPressedState, MotionEvent event){
        boolean isPressed = currentPressedState;
        if((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            isPressed = true;
        }
        if((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            isPressed = false;
        }
        return isPressed;
    }

    private void setArrowButtonStates(View v, MotionEvent event){

        if(v == this.upArrowButton)
            this.isUpArrowPressed = isArrowButtonPressed(this.isUpArrowPressed,event);

        if(v == this.upLeftArrowButton)
            this.isUpLeftArrowPressed = isArrowButtonPressed(this.isUpLeftArrowPressed,event);

        if(v == this.upRightArrowButton)
            this.isUpRightArrowPressed = isArrowButtonPressed(this.isUpRightArrowPressed,event);

        if(v == this.downArrowButton)
            this.isDownArrowPressed = isArrowButtonPressed(this.isDownArrowPressed,event);

        if(v == this.leftArrowButton)
            this.isLeftArrowPressed = isArrowButtonPressed(this.isLeftArrowPressed,event);

        if(v == this.rightArrowButton)
            this.isRightArrowPressed = isArrowButtonPressed(this.isRightArrowPressed,event);
    }

    public boolean handleArrowButtonEvent(View v, MotionEvent event) {
        inertiaController.stop();
        setArrowButtonStates(v, event);

        boolean allReleased = !isUpArrowPressed && !isUpLeftArrowPressed && !isUpRightArrowPressed &&
                !isDownArrowPressed && !isLeftArrowPressed && !isRightArrowPressed;

        if (allReleased) {
            //TODO long press handling
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE){
            return true;
        }

        int leftSpeed = lastLeftSpeed;
        int rightSpeed = lastRightSpeed;

        if(isUpArrowPressed){
            if (isLeftArrowPressed) {
                //keep turning while accelerating
                leftSpeed = changeSpeed(lastLeftSpeed, false, SPEED_CHANGE/2);
                rightSpeed = changeSpeed(lastRightSpeed, true, SPEED_CHANGE/2);
            }else if ( isRightArrowPressed){
                leftSpeed = changeSpeed(lastLeftSpeed, true, SPEED_CHANGE/2);
                rightSpeed = changeSpeed(lastRightSpeed, false, SPEED_CHANGE/2);
            }else{
                //pull straight
                if (lastLeftSpeed != lastRightSpeed){
                    //during turning, init to low speed
                    leftSpeed = rightSpeed = SPEED_LOW;
                }else {
                    leftSpeed = rightSpeed = changeSpeed(lastLeftSpeed, true, SPEED_CHANGE);
                }
            }
        }
        else if (isUpLeftArrowPressed){
            leftSpeed = MIN_SPEED;
            if (lastLeftSpeed == lastRightSpeed){
                rightSpeed = MIN_SPEED + SPEED_CHANGE;
            }
            else{
                rightSpeed = changeSpeed(lastRightSpeed, true, SPEED_CHANGE/2);
            }
        }
        else if (isUpRightArrowPressed){
            rightSpeed = MIN_SPEED;
            if (lastLeftSpeed == lastRightSpeed){
                leftSpeed = MIN_SPEED + SPEED_CHANGE;
            }
            else {
                leftSpeed = changeSpeed(lastLeftSpeed, true, SPEED_CHANGE / 2);
            }
            //rightSpeed = changeSpeed(lastRightSpeed, false, SPEED_CHANGE/2);
        }
        else if(isDownArrowPressed){
            if (isLeftArrowPressed) {
                //keep turning while decelerating
                leftSpeed = changeSpeed(lastLeftSpeed, false, SPEED_CHANGE / 2);
                rightSpeed = changeSpeed(lastRightSpeed, true, SPEED_CHANGE / 2);
            }else if (isRightArrowPressed){
                //keep turning while decelerating
                leftSpeed = changeSpeed(lastLeftSpeed, true, SPEED_CHANGE/2);
                rightSpeed = changeSpeed(lastRightSpeed, false, SPEED_CHANGE/2);
            }else{
                //back straight
                if (lastLeftSpeed != lastRightSpeed){
                    leftSpeed = rightSpeed = -SPEED_LOW;
                }else{
                    leftSpeed = rightSpeed = changeSpeed(lastLeftSpeed, false, SPEED_CHANGE);
                }
            }
        }
        else if(isLeftArrowPressed){
            if (lastLeftSpeed == lastRightSpeed){
                //starts to turn, use minimum effective turn speed
                leftSpeed = -MIN_TURN_SPEED;
                rightSpeed = MIN_TURN_SPEED;
            }else{
                leftSpeed = changeSpeed(lastLeftSpeed, false, SPEED_CHANGE);
                rightSpeed = changeSpeed(lastRightSpeed, true, SPEED_CHANGE);
            }
        }
        else if(isRightArrowPressed){
            if (lastLeftSpeed == lastRightSpeed){
                //starts to turn, use minimum effective turn speed
                leftSpeed = MIN_TURN_SPEED;
                rightSpeed = -MIN_TURN_SPEED;
            }else {
                leftSpeed = changeSpeed(lastLeftSpeed, true, SPEED_CHANGE);
                rightSpeed = changeSpeed(lastRightSpeed, false, SPEED_CHANGE);
            }
        }

        leftSpeed = normalizeSpeed(leftSpeed);
        rightSpeed = normalizeSpeed(rightSpeed);

        if(lastLeftSpeed != leftSpeed && lastRightSpeed != rightSpeed)
            setSpeed(leftSpeed,rightSpeed);
        else if(lastLeftSpeed!=leftSpeed)
            setSpeed(leftSpeed,null);
        else if(lastRightSpeed!=rightSpeed)
            setSpeed(null,rightSpeed);

        lastLeftSpeed = leftSpeed;
        lastRightSpeed = rightSpeed;
        inertiaController.start();
        return true;
    }

    private int changeSpeed(int lastSpeed, boolean accelerate, int delta){
        int speed;
        if (accelerate){
            if (lastSpeed >= -SPEED_LOW && lastSpeed < 0){
                speed = 0;
            }else if (lastSpeed >= 0 && lastSpeed < SPEED_LOW){
                speed = SPEED_LOW;
            }else {
                speed = Math.min(SPEED_FULL, lastSpeed + delta);
            }
        }else{
            if (lastSpeed > 0 && lastSpeed <= SPEED_LOW){
                speed = 0;
            }else if (lastSpeed <= 0 && lastSpeed > -SPEED_LOW){
                speed = -SPEED_LOW;
            }else{
                speed =Math.max(-SPEED_FULL, lastSpeed - delta);
            }
        }
        return speed;
    }

    private int getUnsignedSpeed(int speed) {
        int unsignedSpeed = Math.abs(speed);
        if (unsignedSpeed < MIN_SPEED) {
            if (unsignedSpeed < MIN_SPEED/2)
                unsignedSpeed = 0;
            else
                unsignedSpeed = MIN_SPEED;
        }else if (unsignedSpeed > MAX_SPEED){
            unsignedSpeed = MAX_SPEED;
        }
        return unsignedSpeed;
    }

    private int normalizeSpeed(int speed){
        int unsignedSpeed = getUnsignedSpeed(speed);
        return speed>=0?unsignedSpeed:-unsignedSpeed;
    }

    public boolean handleSlideButtonEvent(View v, MotionEvent event) {
        inertiaController.stop();
        boolean buttonReleased = (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP;

        Button button = null;
        TextView textView = null;
        boolean isLeft = false;
        boolean isRight = false;

        if (v == leftButton) {
            button = leftButton;
            textView = leftSpeedTextView;
            isLeft = true;
        }
        if (v == rightButton) {
            button = rightButton;
            textView = rightSpeedTextView;
            isRight = true;
        }

        if (button == null || textView == null)
            return false;

        if (buttonReleased){
            return true;
        }
        int speed = calculateSpeedFromSliderViewTouchEvent(v, event);

        // update only if last speed has changed.
        boolean needsUpdate = (isLeft && lastLeftSpeed != speed) || (isRight && lastRightSpeed != speed);

        if (needsUpdate) {
            setSpeed(isLeft ? speed : null, isRight ? speed : null);

            if (isLeft) lastLeftSpeed = speed;
            if (isRight) lastRightSpeed = speed;
        }
        inertiaController.start();
        return true;
    }

    private int calculateSpeedFromSliderViewTouchEvent(View v, MotionEvent event) {

        float height = v.getHeight();
        float halfHeight = height/2;

        // Calculate y as a number between -SPEED_FULL (at bottom of the button) to SPEED_FULL (at the top of the button)
        // middle=0, top = height, bottom = -height
        float signedValue = height / 2f - event.getY();
        float unsignedValue = Math.abs(signedValue);
        int sign = signedValue < 0 ? -1 : 1;

        SpeedValueInterpolator svi = new SpeedValueInterpolator()
                .setValueRange( halfHeight * 0.1f, halfHeight * 0.8f)
                .setSpeedRange(SPEED_LOW,SPEED_FULL)
                .setSpeedStep(10);
        int speed = (int) svi.getSpeedForValue(unsignedValue);
        return sign * speed;
    }

    private void setSpeed(Integer left, Integer right) {
        if(left!=null)
            this.leftSpeedTextView.setText(""+left);
        if(right!=null)
            this.rightSpeedTextView.setText(""+right);
        try {
            RobocarClient service= new RobocarRestClient("http://"+this.webserviceUrlTextView.getText());
            service.setSpeed(left, right);
        }catch(Exception e) {
            Toast.makeText(this, "Unable to communicate with Robocar: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            Timber.d(e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sp = getSharedPreferences("ui-resources", 0);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("webserviceUrl", webserviceUrlTextView.getText().toString());
        spe.commit();
        inertiaController.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("ui-resources", 0);
        SharedPreferences.Editor spe = sp.edit();
        String baseUrl = sp.getString("webserviceUrl", "");
        if (baseUrl != null && !baseUrl.isEmpty())
            this.webserviceUrlTextView.setText(baseUrl);
        inertiaController.start();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        drawer.closeDrawer(GravityCompat.START);

        int id = item.getItemId();

        new IntentRouter(this)
                .navigateFrom(R.id.game_controller_activity)
                .to(id);

        return true;
    }

    private void initNavigationDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inertiaController.destroy();
    }

    class InertiaController implements Runnable{
        int INTERVAL = 1000; //ms
        private final ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> jobHandle;

        public void start(){
            if (jobHandle != null && !jobHandle.isDone()){
                jobHandle.cancel(true);
            }
            jobHandle =
                    scheduler.scheduleAtFixedRate(this, INTERVAL, INTERVAL, TimeUnit.MILLISECONDS);
        }

        public void run(){
            int delta = 5;//less than SPEED_CHANGE
            int leftSpeed = lastLeftSpeed==0?0:lastLeftSpeed>0?Math.max(0, lastLeftSpeed-delta):Math.min(0, lastLeftSpeed+delta);
            int rightSpeed = lastRightSpeed==0?0:lastRightSpeed>0?Math.max(0, lastRightSpeed-delta):Math.min(0, lastRightSpeed+delta);

            leftSpeed = normalizeSpeed(leftSpeed);
            rightSpeed = normalizeSpeed(rightSpeed);

            if(lastLeftSpeed != leftSpeed || lastRightSpeed != rightSpeed) {
                Timber.d("InertiaController change speed from " + lastLeftSpeed + "/" + lastRightSpeed + " to " + leftSpeed + "/" + rightSpeed);
            }else{
                return;
            }

            if(lastLeftSpeed != leftSpeed && lastRightSpeed != rightSpeed)
                setSpeed(leftSpeed,rightSpeed);
            else if(lastLeftSpeed!=leftSpeed)
                setSpeed(leftSpeed,null);
            else if(lastRightSpeed!=rightSpeed)
                setSpeed(null,rightSpeed);

            lastLeftSpeed = leftSpeed;
            lastRightSpeed = rightSpeed;

        }

        public void stop(){
            if (jobHandle != null){
                jobHandle.cancel(true);
            }

        }

        public void destroy(){
            scheduler.shutdown();
        }
    }
}
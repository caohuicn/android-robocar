package com.zugaldia.robocar.app.manual;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;
import com.zugaldia.robocar.software.webserver.models.RobocarSpeed;

import java.io.IOException;

/**
 * Drives the Robocar using localhost (invoked by the companion app).
 */
public class L298NDriver implements MotorInfo {
    private static final String TAG = "L298NDriver";
    // If speed was set too low, the motor could burn.
    private static final int MIN_SPEED = 56;
    private static final int MAX_SPEED = 255; // Frequency of 50Hz (1000/20)
    private static final double PULSE_PERIOD_MS = 20.0;  // Frequency of 50Hz (1000/20)
    private Pwm lPwm;
    private Pwm rPwm;
    private Gpio lGpio1;
    private Gpio rGpio1;
    private Gpio lGpio2;
    private Gpio rGpio2;
    private double activePulseDuration = 0.0;
    private int leftSpeed;
    private int rightSpeed;


    public L298NDriver() {
        Log.i(TAG, "onCreate");
        try{
            lPwm = PeripheralManager.getInstance().openPwm(BoardDefaults.lPwmPort());
            // Always set frequency and initial duty cycle before enabling PWM
            lPwm.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS);
            lPwm.setPwmDutyCycle(activePulseDuration);
            lPwm.setEnabled(true);
            Log.i(TAG, "set lPwm to 0");

            rPwm = PeripheralManager.getInstance().openPwm(BoardDefaults.rPwmPort());
            // Always set frequency and initial duty cycle before enabling PWM
            rPwm.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS);
            rPwm.setPwmDutyCycle(activePulseDuration);
            rPwm.setEnabled(true);
            Log.i(TAG, "set rPwm to 0");

            lGpio1 = PeripheralManager.getInstance().openGpio(BoardDefaults.lGpio1());
            lGpio1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            lGpio2 = PeripheralManager.getInstance().openGpio(BoardDefaults.lGpio2());
            lGpio2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            rGpio1 = PeripheralManager.getInstance().openGpio(BoardDefaults.rGpio1());
            rGpio1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            rGpio2 = PeripheralManager.getInstance().openGpio(BoardDefaults.rGpio2());
            rGpio2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        }catch (IOException e){
            Log.e(TAG, "failed to initialize L298N driver", e);
        }
    }

    public void changeSpeed(RobocarSpeed speed) {
        try{
            if (speed.getLeft() != null) {
                setLeftSpeed(speed.getLeft());
            }

            if (speed.getRight() != null) {
                setRightSpeed(speed.getRight());
            }

        }catch (IOException e){
            Log.e(TAG, "failed to change speed", e);
        }
    }

    public void setLeftSpeed(int speed) throws IOException {
        if (speed > 0) { // Positive speed motor directions
            lGpio1.setValue(true);
            lGpio2.setValue(false);
        } else { // Negative speed motor directions
            lGpio1.setValue(false);
            lGpio2.setValue(true);
        }
        int unsignedSpeed = getUnsignedSpeed(speed);
        lPwm.setPwmDutyCycle(100 * unsignedSpeed / MAX_SPEED);
        leftSpeed = speed;
        Log.d(TAG, "Set left speed to: " + speed);
    }

    public void setRightSpeed(int speed) throws IOException {
        if (speed > 0) { // Positive speed motor directions
            rGpio1.setValue(true);
            rGpio2.setValue(false);
        } else { // Negative speed motor directions
            rGpio1.setValue(false);
            rGpio2.setValue(true);
        }
        int unsignedSpeed = getUnsignedSpeed(speed);
        rPwm.setPwmDutyCycle(100 * unsignedSpeed / MAX_SPEED);
        rightSpeed = speed;
        Log.d(TAG, "Set right speed to: " + speed);
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

    public void close(){
        try {
            lPwm.setPwmDutyCycle(0);
            rPwm.setPwmDutyCycle(0);
            lPwm.close();
            rPwm.close();
            rGpio1.setValue(false);
            rGpio2.setValue(false);
            lGpio1.setValue(false);
            lGpio2.setValue(false);
            rGpio1.close();
            rGpio2.close();
            lGpio1.close();
            lGpio2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int[] getSpeeds() {
        return new int[]{leftSpeed, leftSpeed, rightSpeed, rightSpeed};
    }
}

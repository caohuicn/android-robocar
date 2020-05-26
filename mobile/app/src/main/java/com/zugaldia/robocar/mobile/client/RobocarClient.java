package com.zugaldia.robocar.mobile.client;

public interface RobocarClient {
    void setSpeed(Integer leftSpeed, Integer rightSpeed);
    void toggleDrive();
    void toggleRecord();
}

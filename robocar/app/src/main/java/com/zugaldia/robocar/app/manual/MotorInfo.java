package com.zugaldia.robocar.app.manual;

public interface MotorInfo {
    /**
     * Returns speed of left front, left back, right front, and right back motor speeds (-255 to 255)
     * @return
     */
    public int[] getSpeeds();
}

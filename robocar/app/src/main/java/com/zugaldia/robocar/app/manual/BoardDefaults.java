package com.zugaldia.robocar.app.manual;

import android.os.Build;

public class BoardDefaults {
    /*
     * Copyright 2017, The Android Open Source Project
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    public static final String DEVICE_IMX7D_PICO = "imx7d_pico";


    /**
     * Return the preferred PWM port for each board.
     */
    public static String lPwmPort() {
        if (Build.DEVICE.equals(DEVICE_IMX7D_PICO))
        {
            return "PWM1";
        }
        throw new RuntimeException("Unknown device" + Build.DEVICE);
    }

    public static String rPwmPort() {
        if (Build.DEVICE.equals(DEVICE_IMX7D_PICO))
        {
            return "PWM2";
        }
        throw new RuntimeException("Unknown device" + Build.DEVICE);
    }

    /**
     * Return the GPIO pin that the LED is connected on.
     * For example, on Intel Edison Arduino breakout, pin "IO13" is connected to an onboard LED
     * that turns on when the GPIO pin is HIGH, and off when low.
     */
    public static String rGpio1() {
        if (Build.DEVICE.equals(DEVICE_IMX7D_PICO))
        {
            return "GPIO2_IO00";

        }
        throw new RuntimeException("Unknown device" + Build.DEVICE);
    }

    public static String rGpio2() {
        if (Build.DEVICE.equals(DEVICE_IMX7D_PICO))
        {
            return "GPIO2_IO05";
        }
        throw new RuntimeException("Unknown device" + Build.DEVICE);
    }

    public static String lGpio1() {
        if (Build.DEVICE.equals(DEVICE_IMX7D_PICO))
        {
            return "GPIO2_IO01";
        }
        throw new RuntimeException("Unknown device" + Build.DEVICE);
    }

    public static String lGpio2() {
        if (Build.DEVICE.equals(DEVICE_IMX7D_PICO))
        {
            return "GPIO2_IO02";
        }
        throw new RuntimeException("Unknown device" + Build.DEVICE);
    }

}

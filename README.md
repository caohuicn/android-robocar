# Voice Controlled Android Things Robocar (L298N Driver)

Major changes in this branch: L298N and voice command support

## Hardware

Nothing expensive. The most expensive one is the Android things board, which I got from Google I/O :)
1. Android things board (NXP i.MX7D)
1. A robot car chassis. I used [this one](https://www.amazon.com/ELEGOO-Tracking-Ultrasonic-Intelligent-Educational/dp/B07KPZ8RSZ/ref=sr_1_1_sspa?dchild=1&keywords=elegoo+car&qid=1590463135&sr=8-1-spons&psc=1&spLa=ZW5jcnlwdGVkUXVhbGlmaWVyPUEyRlM3R0pXNkpPMkVCJmVuY3J5cHRlZElkPUEwMTgyMTI1M0pNN0JOMU9UV0FBVyZlbmNyeXB0ZWRBZElkPUEwNjAxNDI3MlhZUjlCSlJVOEk4MCZ3aWRnZXROYW1lPXNwX2F0ZiZhY3Rpb249Y2xpY2tSZWRpcmVjdCZkb05vdExvZ0NsaWNrPXRydWU=)
1. L298N motor driver board (included in the above robocar package)
1. Power source for DC motor (included in the above robocar package)
1. Power source for Android Things (I used my cellphone power bank)
1. Microphone and speaker ( I simply used a phone headset(CTIA plug))
1. Some pieces of wood and glue
![car2](https://user-images.githubusercontent.com/6988741/82860596-fe974700-9ece-11ea-9ae9-0d8e43967c90.jpg)

You can ignore the PCA9685 board (not wired, not used).

## L298N Motor Driver
Both Antonio Zugaldia's and Google's android things robocar use Adafruit stepper & DC Motor hat as the motor controller. Since the robot car kit I ordered already has L298N motor driver, one less thing to buy.
Check [L298NDriver.java](https://github.com/caohuicn/android-robocar/blob/voice/robocar/app/src/main/java/com/zugaldia/robocar/app/manual/L298NDriver.java). 
### Wiring
Headset & power bank are removed to show the wiring:
![car](https://user-images.githubusercontent.com/6988741/82862215-f5f53f80-9ed3-11ea-81d2-a5111afe463a.jpg)
I'm using pins 12(PWM1)/14(Ground)/29(GPIO2_IO01)/31(GPIO2_IO02)/33(PWM2)/35(GPIO2_IO00)/37(GPIO2_IO05).
See https://developer.android.com/things/hardware/imx7d. Make sure to use the ground pin. Basically PWM controls the motor speed, and 2 GPIOs together control the motor current direction so that the wheels can move forward and backward.

## Voice Control
Speech recognition engine is using Pocketsphinx on Android(https://github.com/cmusphinx/pocketsphinx-android-demo). 
```
        recognizer.addKeyphraseSearch(WAKEUP_SEARCH, ACTIVATION_KEYPHRASE);
        File actionGrammar = new File(assetsDir, "commands.gram");
        recognizer.addKeywordSearch(ACTION_SEARCH, actionGrammar);
        // For continuous recognition, keyword search is preferred to grammar search
```
Activation keyphrase is used so that the car will not move by accident. My son likes to call it "Mr. crazy car" :). Once it's activated, it'll be ready to take the following commands(in commands.gram):
```
forward /1e-15/
backward /1e-17/
reverse /1e-17/
left /1e-15/
right /1e-15/
good job /1e-30/
stop /1e-20/
```
"stop" (to stop the car) and "good job" (to stop the car and de-activate the command mode) are given a higher sensitivity level to avoid having to screaming at the car to make it stop, given the motor noise and the mediocre microphone.

Have fun!
# References

1. [Donkey car](https://www.donkeycar.com/)
1. [Android things robocar](https://github.com/androidthings/robocar)
1. [Build your own (small) Autonomous Robocar](https://blog.mapbox.com/build-your-own-small-autonomous-robocar-41ae74927f55)
1. [Android Things - Control your devices through voice with USB Audio support](http://nilhcem.com/android-things/control-your-devices-through-voice-with-usb-audio-support)
1. [Arduino DC Motor Control Tutorial – L298N | PWM | H-Bridge](https://howtomechatronics.com/tutorials/arduino/arduino-dc-motor-control-tutorial-l298n-pwm-h-bridge/)

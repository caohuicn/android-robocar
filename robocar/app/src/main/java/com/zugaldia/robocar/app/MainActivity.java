package com.zugaldia.robocar.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.zugaldia.robocar.app.autonomous.CameraDriver;
import com.zugaldia.robocar.app.autonomous.TensorFlowTrainer;
import com.zugaldia.robocar.app.manual.L298NDriver;
import com.zugaldia.robocar.app.speech.PocketSphinx;
import com.zugaldia.robocar.app.speech.TtsSpeaker;
import com.zugaldia.robocar.software.webserver.HTTPRequestListener;
import com.zugaldia.robocar.software.webserver.LocalWebServer;
import com.zugaldia.robocar.software.webserver.models.RobocarMove;
import com.zugaldia.robocar.software.webserver.models.RobocarResponse;
import com.zugaldia.robocar.software.webserver.models.RobocarSpeed;
import com.zugaldia.robocar.software.webserver.models.RobocarStatus;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements HTTPRequestListener, TtsSpeaker.Listener, PocketSphinx.Listener {

    private static final int SPEECH_REQUEST_CODE = 1111;
    private L298NDriver l298NDriver;

    private CameraDriver cameraDriver;
    private TensorFlowTrainer tensorFlowTrainer;

    // I2C Name
    public static final String I2C_DEVICE_NAME = "I2C1";
    // Adafruit Motor Hat
    private static final int MOTOR_HAT_I2C_ADDRESS = 0x60;
    private LocalWebServer localWebServer;

    private enum State {
        INITIALIZING,
        LISTENING_TO_KEYPHRASE,
        CONFIRMING_KEYPHRASE,
        LISTENING_TO_ACTION,
        CONFIRMING_ACTION,
        CONFIRM_STOP_ACTION,
        TIMEOUT
    }

    private TtsSpeaker tts;
    private PocketSphinx pocketsphinx;
    private State state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Local web server (for LocalhostDriver)
        setupWebServer();

        // Manual drivers (always available)
        l298NDriver = new L298NDriver();
        tts = new TtsSpeaker(this, this);
    }


    private void setupWebServer() {
        localWebServer = new LocalWebServer(this);
        try {
            localWebServer.start();
            Timber.i("LocalWebServer listening on " + LocalWebServer.getIpAddress(this) + ":" + localWebServer.getListeningPort());
        } catch (IOException e) {
            Timber.e(e, "Failed to start local web server.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        l298NDriver.close();
        localWebServer.stop();
        tts.onDestroy();
        pocketsphinx.onDestroy();
    }


    /*
     * Implement RequestListener (web server)
     */

    @Override
    public void onRequest(NanoHTTPD.IHTTPSession session) {
        LocalWebServer.logSession(session);
    }

    @Override
    public RobocarStatus onStatus() {
        return new RobocarStatus(200, "OK");
    }

    @Override
    public RobocarResponse onMove(RobocarMove move) {
        return new RobocarResponse(200, "TODO");
    }

    @Override
    public RobocarResponse onSpeed(RobocarSpeed speed) {
        if (speed == null) {
            return new RobocarResponse(400, "Bad Request");
        }

        l298NDriver.changeSpeed(speed);
        return new RobocarResponse(200, "OK. Speed set to " + speed);
    }

    @Override
    public RobocarResponse onDrive() {
//        if (cameraDriver == null) {
//            cameraDriver = new CameraDriver(this, motorHat);
//            cameraDriver.start();
//        }else{
//            cameraDriver.stop();
//            cameraDriver = null;
//        }
        return new RobocarResponse(200, "OK. ");
    }

    @Override
    public RobocarResponse onRecord() {
        String message = "OK. ";
        if (tensorFlowTrainer == null) {
            tensorFlowTrainer = new TensorFlowTrainer(this, l298NDriver);
            tensorFlowTrainer.startSession();
            message += "Recording started.";
        } else {
            tensorFlowTrainer.endSession();
            tensorFlowTrainer = null;
            message += "Recording ended.";
        }
        return new RobocarResponse(200, message);
    }

    @Override
    public void onTtsInitialized() {
        // There's no runtime permissions on Android Things.
        // Otherwise, we would first have to ask for the Manifest.permission.RECORD_AUDIO
        pocketsphinx = new PocketSphinx(this, this);
    }

    @Override
    public void onTtsSpoken() {
        updateRecognizerState();
    }

    private void updateRecognizerState() {
        switch (state) {
            case INITIALIZING:
            case CONFIRM_STOP_ACTION:
            case TIMEOUT:
                state = State.LISTENING_TO_KEYPHRASE;
                pocketsphinx.startListeningToActivationPhrase();
                break;
            case CONFIRMING_ACTION:
                //restart action search without having to activate
            case CONFIRMING_KEYPHRASE:
                state = State.LISTENING_TO_ACTION;
                pocketsphinx.startListeningToAction();
                break;
        }
    }

    @Override
    public void onSpeechRecognizerReady() {
        state = State.INITIALIZING;
        tts.say("I'm ready!");
        Log.d("MainActivity", "I'm ready");
    }

    @Override
    public void onActivationPhraseDetected() {
        state = State.CONFIRMING_KEYPHRASE;
        tts.say("Yup?");
    }

    private static final int SPEED_NORMAL = 70;
    private static final int SPEED_TURNING_INSIDE = 50;
    private static final int SPEED_TURNING_OUTSIDE = 150;

    @Override
    public void onTextRecognized(String recognizedText) {
        Log.d("MainActivity", recognizedText);
        state = State.CONFIRMING_ACTION;
        String input = recognizedText == null ? "" : recognizedText.trim();
        RobocarSpeed speed = new RobocarSpeed();
        String answer;
        //test for "stop" first, in case we receive "left right stop"
        if (input.contains("stop")) {
            answer = "Stopping";
            speed.setLeft(0);
            speed.setRight(0);
        } else if (input.contains("good job")) {
            answer = "Thank you!";
            speed.setLeft(0);
            speed.setRight(0);
            state = State.CONFIRM_STOP_ACTION;
        } else if (input.contains("forward")) {
            answer = "Moving";
            speed.setLeft(SPEED_NORMAL);
            speed.setRight(SPEED_NORMAL);
        } else if (input.contains("backward") || input.contains("reverse")) {
            answer = "Backing";
            speed.setLeft(-SPEED_NORMAL);
            speed.setRight(-SPEED_NORMAL);
        } else if (input.contains("left")) {
            answer = "Turning";
            speed.setLeft(SPEED_TURNING_INSIDE);
            speed.setRight(SPEED_TURNING_OUTSIDE);
        } else if (input.contains("right")) {
            answer = "Turning";
            speed.setLeft(SPEED_TURNING_OUTSIDE);
            speed.setRight(SPEED_TURNING_INSIDE);
        } else {
            answer = "Sorry, I didn't understand you.";
        }
        //better not let tts repeat the same keywords to avoid recognizing it again
        tts.say(answer);
        l298NDriver.changeSpeed(speed);
    }

    @Override
    public void onTimeout() {
        state = State.TIMEOUT;
        tts.say("Timeout! Talk to you later.");
    }
}

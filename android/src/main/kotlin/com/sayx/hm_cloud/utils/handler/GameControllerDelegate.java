package com.sayx.hm_cloud.utils.handler;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.json.JSONException;
import org.json.JSONObject;

public interface GameControllerDelegate {

    static final int KEY_BASE = 1000;

    public static final int THUMBSTICK_LEFT_X = KEY_BASE;
    public static final int THUMBSTICK_LEFT_Y = KEY_BASE + 1;
    public static final int THUMBSTICK_RIGHT_X = KEY_BASE + 2;
    public static final int THUMBSTICK_RIGHT_Y = KEY_BASE + 3;

    public static final int BUTTON_A = 0x2000;
    public static final int BUTTON_B = 0x1000;
    public static final int BUTTON_C = KEY_BASE + 6;
    public static final int BUTTON_X = 0x8000;
    public static final int BUTTON_Y = 0x4000;
    public static final int BUTTON_Z = KEY_BASE + 9;

    public static final int BUTTON_DPAD_UP = 0x0001;
    public static final int BUTTON_DPAD_DOWN = 0x0002;
    public static final int BUTTON_DPAD_LEFT = 0x0004;
    public static final int BUTTON_DPAD_RIGHT = 0x0008;
    public static final int BUTTON_DPAD_CENTER = KEY_BASE + 14;

    // LB
    public static final int BUTTON_LEFT_SHOULDER = 0x0100;
    // RB
    public static final int BUTTON_RIGHT_SHOULDER = 0x0200;

    public static final int BUTTON_LEFT_TRIGGER = 0x00FF;
    public static final int BUTTON_RIGHT_TRIGGER = 0x00FF;

    public static final int BUTTON_LEFT_THUMBSTICK = 0x0040;
    public static final int BUTTON_RIGHT_THUMBSTICK = 0x0080;

    public static final int BUTTON_START = 0x0010;
    public static final int BUTTON_SELECT = 0x0020;

    void onCreate(Context context);

    void onPause();

    void onResume();

    void onDestroy();

    boolean dispatchKeyEvent(KeyEvent event);

    boolean dispatchGenericMotionEvent(MotionEvent event);

    void setControllerEventListener(ControllerEventListener listener);

    void onInputDeviceAdded(int deviceId);

    void onInputDeviceChanged(int deviceId);

    void onInputDeviceRemoved(int deviceId);

    public class ControllerEventListener {

        /**
         * 手柄事件
         * 主要是已经封装好了的手柄事件
         *
         * @param handledEvent
         */
        public void onEvent(String handledEvent) {

        }

        public void onButtonEvent(String vendorName, int controller, int button, boolean isPressed, float value, boolean isAnalog) {

        }

        public void onAxisEvent(String vendorName, int controller, int axisID, float value, boolean isAnalog) {

        }

        public void onControllerInput(String vendorName,
                                      int controller, int buttonFlags, float leftTrigger, float rightTrigger,
                                      float leftStickX, float leftStickY,
                                      float rightStickX, float rightStickY) {

        }

        public void onConnected(String vendorName, int controller) {

        }

        public void onDisconnected(String vendorName, int controller) {

        }
    }


    static JSONObject gamePadControlEvent(int buttons, int leftTrigger, int rightTrigger, int thumbLX, int thumbLY, int thumbRX, int thumbRY) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cmd", "game_pad_control");
            JSONObject bodyJSONObject = new JSONObject();
            bodyJSONObject.put("buttons", buttons);
            bodyJSONObject.put("left_trigger", leftTrigger);
            bodyJSONObject.put("right_trigger", rightTrigger);
            bodyJSONObject.put("thumb_LX", thumbLX);
            bodyJSONObject.put("thumb_LY", thumbLY);
            bodyJSONObject.put("thumb_RX", thumbRX);
            bodyJSONObject.put("thumb_RY", thumbRY);
            jsonObject.put("body", bodyJSONObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}

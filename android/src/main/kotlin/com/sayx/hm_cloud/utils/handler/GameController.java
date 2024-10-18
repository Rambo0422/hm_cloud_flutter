package com.sayx.hm_cloud.utils.handler;

import android.content.Context;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class GameController implements GameControllerDelegate {

    private final SparseIntArray mKeyMap;
    private final SparseArray<String> mGameController = new SparseArray<>();

    private float mOldLeftThumbstickX = 0.0f;
    private float mOldLeftThumbstickY = 0.0f;
    private float mOldRightThumbstickX = 0.0f;
    private float mOldRightThumbstickY = 0.0f;

    private float mOldLeftTrigger = 0.0f;
    private float mOldRightTrigger = 0.0f;
    private float mOldThrottle = 0.0f;
    private float mOldBrake = 0.0f;
    private float mOldGas = 0.0f;

    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int AXIS_Z = 11;
    private static final int AXIS_RZ = 14;
    private static final int AXIS_LTRIGGER = 17;
    private static final int AXIS_RTRIGGER = 18;
    public static final int AXIS_GAS = 22;
    private static final int AXIS_BRAKE = 23;
    private static final int AXIS_THROTTLE = 19;

    private int buttonFlags = 0;

    public GameController() {
        mKeyMap = new SparseIntArray(25);
        mKeyMap.put(KeyEvent.KEYCODE_BUTTON_A, BUTTON_A);
        mKeyMap.put(KeyEvent.KEYCODE_BUTTON_B, BUTTON_B);
        mKeyMap.put(KeyEvent.KEYCODE_BUTTON_X, BUTTON_X);
        mKeyMap.put(KeyEvent.KEYCODE_BUTTON_Y, BUTTON_Y);

        mKeyMap.put(KeyEvent.KEYCODE_DPAD_UP, BUTTON_DPAD_UP);
        mKeyMap.put(KeyEvent.KEYCODE_DPAD_DOWN, BUTTON_DPAD_DOWN);
        mKeyMap.put(KeyEvent.KEYCODE_DPAD_LEFT, BUTTON_DPAD_LEFT);
        mKeyMap.put(KeyEvent.KEYCODE_DPAD_RIGHT, BUTTON_DPAD_RIGHT);
        mKeyMap.put(KeyEvent.KEYCODE_DPAD_CENTER, BUTTON_DPAD_CENTER);

        // 左边摇杆按下
        mKeyMap.put(KeyEvent.KEYCODE_BUTTON_THUMBL, BUTTON_LEFT_THUMBSTICK);

        // 右边摇杆按下
        mKeyMap.put(KeyEvent.KEYCODE_BUTTON_THUMBR, BUTTON_RIGHT_THUMBSTICK);

        // LB
        mKeyMap.put(KeyEvent.KEYCODE_BUTTON_L1, BUTTON_LEFT_SHOULDER);
        // RB
        mKeyMap.put(KeyEvent.KEYCODE_BUTTON_R1, BUTTON_RIGHT_SHOULDER);

        mKeyMap.put(KeyEvent.KEYCODE_BUTTON_START, BUTTON_START);
        mKeyMap.put(KeyEvent.KEYCODE_BUTTON_SELECT, BUTTON_SELECT);

        mKeyMap.put(KeyEvent.KEYCODE_BUTTON_L2, BUTTON_LEFT_TRIGGER);
        mKeyMap.put(KeyEvent.KEYCODE_BUTTON_R2, BUTTON_RIGHT_TRIGGER);
    }

    @Override
    public void onCreate(Context context) {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onDestroy() {
        mControllerEventListener = null;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = false;

        if (mControllerEventListener != null) {
            int eventSource = event.getSource();
            int keyCode = event.getKeyCode();
            keyCode = handleFlipFaceButtons(keyCode);

            int controllerKey = mKeyMap.get(keyCode);
            if (controllerKey != 0) {
                if (((eventSource & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                        || ((eventSource & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                    int deviceId = event.getDeviceId();
                    String deviceName = event.getDevice().getName();

                    if (mGameController.get(deviceId) == null) {
                        gatherControllers(mGameController);
                        mGameController.append(deviceId, deviceName);
                    }

                    int action = event.getAction();
                    handled = true;


                    if (keyCode == KeyEvent.KEYCODE_BUTTON_L2 || keyCode == KeyEvent.KEYCODE_BUTTON_R2) {
                        if (action == KeyEvent.ACTION_DOWN) {
                            if (keyCode == KeyEvent.KEYCODE_BUTTON_L2) {
                                mOldLeftTrigger = controllerKey;
                            } else if (keyCode == KeyEvent.KEYCODE_BUTTON_R2) {
                                mOldRightTrigger = controllerKey;
                            }

                        } else if (action == KeyEvent.ACTION_UP) {
                            if (keyCode == KeyEvent.KEYCODE_BUTTON_L2) {
                                mOldLeftTrigger = 0;
                            } else if (keyCode == KeyEvent.KEYCODE_BUTTON_R2) {
                                mOldRightTrigger = 0;
                            }
                        }
                    } else {
                        if (action == KeyEvent.ACTION_DOWN) {
                            buttonFlags |= controllerKey;
                            mControllerEventListener.onButtonEvent(deviceName, deviceId, buttonFlags, true, 1.0f, false);
                        } else if (action == KeyEvent.ACTION_UP) {
                            buttonFlags &= ~controllerKey;
                            mControllerEventListener.onButtonEvent(deviceName, deviceId, buttonFlags, false, 0.0f, false);
                        }
                    }

                    mControllerEventListener.onControllerInput(
                            deviceName,
                            deviceId,
                            buttonFlags,
                            mOldLeftTrigger,
                            mOldRightTrigger,
                            mOldLeftThumbstickX,
                            mOldLeftThumbstickY,
                            mOldRightThumbstickX,
                            mOldRightThumbstickY
                    );
                }
            }
        }

        return handled;
    }

    /**
     * 翻转按键
     *
     * @param keyCode
     * @return
     */
    private int handleFlipFaceButtons(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_A:
                return KeyEvent.KEYCODE_BUTTON_B;
            case KeyEvent.KEYCODE_BUTTON_B:
                return KeyEvent.KEYCODE_BUTTON_A;
            case KeyEvent.KEYCODE_BUTTON_X:
                return KeyEvent.KEYCODE_BUTTON_Y;
            case KeyEvent.KEYCODE_BUTTON_Y:
                return KeyEvent.KEYCODE_BUTTON_X;
            default:
                return keyCode;
        }
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        boolean handled = false;
        if (mControllerEventListener != null) {
            int eventSource = event.getSource();
            if (((eventSource & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((eventSource & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int deviceId = event.getDeviceId();
                    String deviceName = event.getDevice().getName();
                    if (mGameController.get(deviceId) == null) {
                        gatherControllers(mGameController);
                        mGameController.append(deviceId, deviceName);
                    }

                    float oldRightTrigger = mOldRightTrigger;
                    float rtValue = event.getAxisValue(MotionEvent.AXIS_RTRIGGER) * 0xFF;
                    if (Float.compare(rtValue, oldRightTrigger) != 0) {
                        mOldRightTrigger = rtValue;
                        handled = true;
                    }

                    float oldLeftTrigger = mOldLeftTrigger;
                    float ltValue = event.getAxisValue(MotionEvent.AXIS_LTRIGGER) * 0xFF;
                    if (Float.compare(ltValue, oldLeftTrigger) != 0) {
                        mOldLeftTrigger = ltValue;
                        handled = true;
                    }

                    float oldLeftThumbstickX = mOldLeftThumbstickX;
                    float oldLeftThumbstickY = mOldLeftThumbstickY;

                    float newAXIS_LX = event.getAxisValue(AXIS_X);
                    if (Float.compare(newAXIS_LX, oldLeftThumbstickX) != 0) {
                        mControllerEventListener.onAxisEvent(deviceName, deviceId, THUMBSTICK_LEFT_X, newAXIS_LX, true);
                        oldLeftThumbstickX = newAXIS_LX;
                        handled = true;
                    }

                    // 如果是负数，转成正数，如果是正数，转成附属
                    float newAXIS_LY = event.getAxisValue(AXIS_Y) * -1f;
                    if (Float.compare(newAXIS_LY, oldLeftThumbstickY) != 0) {
                        mControllerEventListener.onAxisEvent(deviceName, deviceId, THUMBSTICK_LEFT_Y, newAXIS_LY, true);
                        oldLeftThumbstickY = newAXIS_LY;
                        handled = true;
                    }

                    float oldRightThumbstickX = mOldRightThumbstickX;
                    float oldRightThumbstickY = mOldRightThumbstickY;

                    float newAXIS_RX = event.getAxisValue(AXIS_Z);
                    if (Float.compare(newAXIS_RX, oldRightThumbstickX) != 0) {
                        mControllerEventListener.onAxisEvent(deviceName, deviceId, THUMBSTICK_RIGHT_X, newAXIS_RX, true);
                        oldRightThumbstickX = newAXIS_RX;
                        handled = true;
                    }

                    float newAXIS_RY = event.getAxisValue(AXIS_RZ) * -1f;
                    if (Float.compare(newAXIS_RY, oldRightThumbstickY) != 0) {
                        mControllerEventListener.onAxisEvent(deviceName, deviceId, THUMBSTICK_RIGHT_Y, newAXIS_RY, true);
                        oldRightThumbstickY = newAXIS_RY;
                        handled = true;
                    }

                    float newAXIS_BRAKE = event.getAxisValue(AXIS_BRAKE);
                    if (Float.compare(newAXIS_BRAKE, mOldBrake) != 0) {
                        mControllerEventListener.onAxisEvent(deviceName, deviceId, BUTTON_LEFT_TRIGGER, newAXIS_BRAKE, true);
                        mOldBrake = newAXIS_BRAKE;
                        handled = true;
                    }

                    float newAXIS_THROTTLE = event.getAxisValue(AXIS_THROTTLE);
                    if (Float.compare(newAXIS_THROTTLE, mOldThrottle) != 0) {
                        mControllerEventListener.onAxisEvent(deviceName, deviceId, BUTTON_RIGHT_TRIGGER, newAXIS_THROTTLE, true);
                        mOldThrottle = newAXIS_THROTTLE;
                        handled = true;
                    }

                    float newAXIS_GAS = event.getAxisValue(AXIS_GAS);
                    if (Float.compare(newAXIS_GAS, mOldGas) != 0) {
                        mControllerEventListener.onAxisEvent(deviceName, deviceId, BUTTON_RIGHT_TRIGGER, newAXIS_GAS, true);
                        mOldGas = newAXIS_GAS;
                        handled = true;
                    }

                    float hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
                    float hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);

                    boolean buttonAxisHafIsPressed = false;

                    // 临时Flag
                    int tempButtonFlags = buttonFlags;

                    int KEYCODE_DPAD_LEFT_VALUE = mKeyMap.get(KeyEvent.KEYCODE_DPAD_LEFT);
                    int KEYCODE_DPAD_RIGHT_VALUE = mKeyMap.get(KeyEvent.KEYCODE_DPAD_RIGHT);
                    buttonFlags &= ~(KEYCODE_DPAD_LEFT_VALUE | KEYCODE_DPAD_RIGHT_VALUE);
                    if (hatX != 0f) {
                        buttonAxisHafIsPressed = true;
                        if (hatX < -0.5) {
                            buttonFlags |= KEYCODE_DPAD_LEFT_VALUE;
                        } else if (hatX > 0.5) {
                            buttonFlags |= KEYCODE_DPAD_RIGHT_VALUE;
                        }
                    }

                    int KEYCODE_DPAD_UP_VALUE = mKeyMap.get(KeyEvent.KEYCODE_DPAD_UP);
                    int KEYCODE_DPAD_DOWN_VALUE = mKeyMap.get(KeyEvent.KEYCODE_DPAD_DOWN);
                    buttonFlags &= ~(KEYCODE_DPAD_UP_VALUE | KEYCODE_DPAD_DOWN_VALUE);
                    if (hatY != 0f) {
                        buttonAxisHafIsPressed = true;
                        if (hatY < -0.5) {
                            buttonFlags |= KEYCODE_DPAD_UP_VALUE;
                        } else if (hatY > 0.5) {
                            buttonFlags |= KEYCODE_DPAD_DOWN_VALUE;
                        }
                    }

                    // 说明按键值有改变
                    if (tempButtonFlags != buttonFlags) {
                        mControllerEventListener.onButtonEvent(deviceName, deviceId, buttonFlags, buttonAxisHafIsPressed, 1.0f, false);
                    }

                    if (oldLeftThumbstickX != mOldLeftThumbstickX ||
                            oldLeftThumbstickY != mOldLeftThumbstickY ||
                            oldRightThumbstickX != mOldRightThumbstickX ||
                            oldRightThumbstickY != mOldRightThumbstickY||
                            handled) {

                        mOldLeftThumbstickX = oldLeftThumbstickX;
                        mOldLeftThumbstickY = oldLeftThumbstickY;
                        mOldRightThumbstickX = oldRightThumbstickX;
                        mOldRightThumbstickY = oldRightThumbstickY;

                        mControllerEventListener.onControllerInput(
                                deviceName,
                                deviceId,
                                buttonFlags,
                                mOldLeftTrigger,
                                mOldRightTrigger,
                                mOldLeftThumbstickX,
                                mOldLeftThumbstickY,
                                mOldRightThumbstickX,
                                mOldRightThumbstickY
                        );
                    }
                }
            }
        }

        return handled;
    }

    private ControllerEventListener mControllerEventListener;

    @Override
    public void setControllerEventListener(ControllerEventListener listener) {
        mControllerEventListener = listener;
    }

    private void gatherControllers(SparseArray<String> controllers) {
        int controllerCount = controllers.size();
        for (int i = 0; i < controllerCount; i++) {
            try {
                int controllerDeviceId = controllers.keyAt(i);
                InputDevice device = InputDevice.getDevice(controllerDeviceId);
                if (device == null) {
                    mControllerEventListener.onDisconnected(controllers.get(controllerDeviceId), controllerDeviceId);
                    controllers.delete(controllerDeviceId);
                }
            } catch (Exception e) {
                int controllerDeviceId = controllers.keyAt(i);
                mControllerEventListener.onDisconnected(controllers.get(controllerDeviceId), controllerDeviceId);
                controllers.delete(controllerDeviceId);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onInputDeviceAdded(int deviceId) {
        try {
            InputDevice device = InputDevice.getDevice(deviceId);
            int deviceSource = device.getSources();

            if (((deviceSource & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((deviceSource & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                String deviceName = device.getName();
                mGameController.append(deviceId, deviceName);
                mControllerEventListener.onConnected(deviceName, deviceId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInputDeviceChanged(int deviceId) {
        gatherControllers(mGameController);
    }

    @Override
    public void onInputDeviceRemoved(int deviceId) {
        if (mGameController.get(deviceId) != null) {
            mControllerEventListener.onDisconnected(mGameController.get(deviceId), deviceId);
            mGameController.delete(deviceId);
        }
    }
}

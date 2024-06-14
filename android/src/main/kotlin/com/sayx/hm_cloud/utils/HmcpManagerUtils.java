package com.sayx.hm_cloud.utils;

import android.content.Context;
import android.os.Bundle;

import com.haima.hmcp.BuildConfig;
import com.haima.hmcp.Constants;
import com.haima.hmcp.HmcpManager;
import com.haima.hmcp.listeners.OnInitCallBackListener;

public class HmcpManagerUtils {

    public static void init(Context context, String accessKeyId, String channel, OnInitCallBackListener mOnInitCallBackListener) {
        Bundle bundle = new Bundle();
        bundle.putString(HmcpManager.ACCESS_KEY_ID, accessKeyId);
        bundle.putString(HmcpManager.CHANNEL_ID, channel);

        Constants.IS_DEBUG = true;
        Constants.IS_ERROR = true;
        Constants.IS_INFO = true;

        HmcpManager.getInstance().init(bundle, context, mOnInitCallBackListener);
    }
}

package com.sayx.hm_cloud;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.BinaryMessenger;

public class AppFlutterActivity extends FlutterActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GameManager.INSTANCE.setFlutterActivity(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String route = extras.getString("route");
            Bundle arguments = extras.getBundle("arguments");
            LogUtils.v("route:" + route + ", arguments:" + arguments);
            GameManager.INSTANCE.openFlutterPage(route, bundleToMap(arguments));
        }
    }

    public Map<String, Object> bundleToMap(@Nullable Bundle bundle) {
        Map<String, Object> map = new HashMap<>();
        if (bundle == null) {
            return map;
        }
        for (String key : bundle.keySet()) {
            map.put(key, bundle.get(key));
        }
        return map;
    }


    @Nullable
    @Override
    public FlutterEngine provideFlutterEngine(@NonNull Context context) {
        return GameManager.flutterEngine;
    }
}
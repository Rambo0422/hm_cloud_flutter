package com.sayx.hm_cloud;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.BinaryMessenger;

public class AppFlutterActivity extends FlutterActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String route = extras.getString("route");
            Bundle arguments = extras.getBundle("arguments");
            LogUtils.v("route:" + route + ", arguments:" + arguments);
            // 通过通信通道，让Flutter打开指定页面
            GameManager.openFlutterPage(route, arguments);
        }
    }

    @Nullable
    @Override
    public FlutterEngine provideFlutterEngine(@NonNull Context context) {
        return GameManager.flutterEngine;
    }
}

package com.tencent.effectiveanimation.sample;

import android.util.Log;

public class TestReflect {

    public static final String TAG = TestReflect.class.getSimpleName();

    private String name;
    protected TestReflect(String name) {
        this.name = name;
    }

    public void outPut() {
        Log.e(TAG, name);
    }
}

package com.example.voltorbflipmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import java.util.List;


public class Utilities {

    public static void tryCatch(Runnable function, ExceptionHandler fallback) {
        try {
            function.run();
        }
        catch(Exception e) {
            fallback.handle(e);
        }
    }

    public static void logExecutionTime(Runnable task, String taskName) {
        long startTime = System.currentTimeMillis();
        task.run();
        long endTime = System.currentTimeMillis();
        Log.d(taskName, "Execution time: " + (endTime - startTime) + "ms");
    }


    public static void delayedHandler(Runnable task, long delay) {
            new Handler(Looper.getMainLooper()).postDelayed(task, delay);
    }


}



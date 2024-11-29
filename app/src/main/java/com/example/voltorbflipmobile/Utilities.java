package com.example.voltorbflipmobile;

import static com.example.voltorbflipmobile.Utilities.ColorTypes.*;
import static com.example.voltorbflipmobile.Utilities.SoundEffects.*;
import static com.example.voltorbflipmobile.Utilities.ImageTypes.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Utilities {

    /*
    Shared Tables:
        - ANIMATION_TABLE -> Stores the original animation frames for the tiles
        - DECODED_ANIM_TABLE -> Stores the decoded animation frames for the tiles
        - COLOR_TABLE -> Stores the color values for the ColorTypes enum
        - SOUND_TABLE -> Stores the sound values for the SoundEffects enum
        - IMAGE_TABLE -> Stores the image values for the ImageTypes enum (the tileTypes)

    Methods:
        - loadUtilityFiles(Context context) -> just for storing all of the data at start

        - playSound(SoundEffects soundKey) -> Plays the sounds from the SoundEffects enum

        - createExecutorService (int threads) -> Creates a thread pool with the given number of threads

        - Logs:
            - logDebug(String msg) -> logs something with the debug tag attached
            - logError(String msg) -> logs something with the error tag attached

        - tryCatch(Runnable function, ExceptionHandler fallback)
            -> use it for making smaller try-catch blocks

        - logExecutionTime(Runnable task, String taskName)
            -> Wrap it around code to time how long it'll take

        - delayedHandler(Runnable task, long delay) -> Use it to make a delayed action without the
            jargon
     */

    // ================================================================
    //                        Constants
    // ================================================================
    public static final String DEBUG_TAG = "Debugging Purposes";
    public static final String ERROR_TAG = "Error";
    public static final String SUCCESS_TAG = "Success!";

    public static final long ANIMATION_DELAY = 200L;
    public static final long ANIMATION_DURATION = 500L;

    // ================================================================
    //                        Enums
    // ================================================================
    public enum ColorTypes { ROSE, MINT, TEAL, WISTERA, GOLD }

    public enum SoundEffects { EXPLOSION_SFX, FLIP_TILE_SFX, INCREASE_POINT_SFX, LEVEL_COMPLETE_SFX, STORING_POINTS_SFX }

    public enum ImageTypes { VOLTORB, ONE, TWO, THREE, BACK_TILE, MINI_VOLTORB }

    public enum TileTypes { VOLTORB, ONE, TWO, THREE }

    // ================================================================
    //                        Sound Effects
    // ================================================================
    static AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
    public static final SoundPool soundpool = new SoundPool.Builder().setMaxStreams(10).setAudioAttributes(audioAttributes).build();

    // ================================================================
    //                        Shared Tables
    // ================================================================
    public static final HashMap<String, int[]> ANIMATION_TABLE = new HashMap<>();
    public static final HashMap<ColorTypes, int[]> COLOR_TABLE = new HashMap<>();
    public static final HashMap<Integer, Integer> SOUND_TABLE = new HashMap<>();
    public static final HashMap<Integer, List<Bitmap>> DECODED_ANIM_TABLE = new HashMap<>();
    public static final HashMap<Integer, Bitmap> IMAGE_TABLE = new HashMap<>();


    // ================================================================
    //                        Creation Method
    // ================================================================

    public static void loadUtilityFiles(Context context) {
        ExecutorService backgroundThread = createExecutorService(3);

        CountDownLatch latch = new CountDownLatch(1);

        try {
            backgroundThread.submit( () -> {

                SOUND_TABLE.put(EXPLOSION_SFX.ordinal(),          soundpool.load(context, R.raw.explosion_sfx, 1));
                SOUND_TABLE.put(INCREASE_POINT_SFX.ordinal(),     soundpool.load(context, R.raw.increase_point_sfx, 1));
                SOUND_TABLE.put(FLIP_TILE_SFX.ordinal(),          soundpool.load(context, R.raw.flip_tile_sfx, 1));
                SOUND_TABLE.put(STORING_POINTS_SFX.ordinal(),     soundpool.load(context, R.raw.storing_points_sfx, 1));
                SOUND_TABLE.put(LEVEL_COMPLETE_SFX.ordinal(),     soundpool.load(context, R.raw.level_complete_sfx, 1));

                COLOR_TABLE.put(ROSE,       new int[]{220, 117, 143});
                COLOR_TABLE.put(MINT,       new int[]{0, 204, 163});
                COLOR_TABLE.put(TEAL,       new int[]{0, 128, 128});
                COLOR_TABLE.put(WISTERA,    new int[]{180, 160, 229});
                COLOR_TABLE.put(GOLD,       new int[]{243, 176, 43});


                ANIMATION_TABLE.put("points", new int[]{
                        R.drawable.animation_points_0,
                        R.drawable.animation_points_1,
                        R.drawable.animation_points_2,
                        R.drawable.animation_points_3
                });

                ANIMATION_TABLE.put("explosion", new int[]{
                        R.drawable.animation_explosion_0_final,
                        R.drawable.animation_explosion_1_final,
                        R.drawable.animation_explosion_2_final,
                        R.drawable.animation_explosion_3_final,
                        R.drawable.animation_explosion_4_final,
                        R.drawable.animation_explosion_5_final,
                        R.drawable.animation_explosion_6_final,
                        R.drawable.animation_explosion_7_final,
                        R.drawable.animation_explosion_8_final
                });

                List<Bitmap> decodedPointFrames = new ArrayList<>();
                List<Bitmap> decodedExplosionFrames = new ArrayList<>();

                for (int id = 0; id < Objects.requireNonNull(ANIMATION_TABLE.get("explosion")).length; id++) {
                    int[] explosionIds = Objects.requireNonNull(ANIMATION_TABLE.get("explosion"));
                    int[] pointIds = Objects.requireNonNull(ANIMATION_TABLE.get("points"));

                    if (id < Objects.requireNonNull(ANIMATION_TABLE.get("points")).length) {
                        decodedPointFrames.add(BitmapFactory.decodeResource(context.getResources(), pointIds[id]));
                    }
                    decodedExplosionFrames.add(BitmapFactory.decodeResource(context.getResources(), explosionIds[id]));
                }

                DECODED_ANIM_TABLE.put(0, decodedExplosionFrames);
                DECODED_ANIM_TABLE.put(1, decodedPointFrames);

            });

            backgroundThread.submit(() -> {
                IMAGE_TABLE.put(VOLTORB.ordinal(),          BitmapFactory.decodeResource(context.getResources(), R.drawable.voltorb));
                IMAGE_TABLE.put(ONE.ordinal(),              BitmapFactory.decodeResource(context.getResources(), R.drawable.one));
                IMAGE_TABLE.put(TWO.ordinal(),              BitmapFactory.decodeResource(context.getResources(), R.drawable.two));
                IMAGE_TABLE.put(THREE.ordinal(),            BitmapFactory.decodeResource(context.getResources(), R.drawable.three));
                IMAGE_TABLE.put(BACK_TILE.ordinal(),        BitmapFactory.decodeResource(context.getResources(), R.drawable.big_back_of_tile));
                IMAGE_TABLE.put(MINI_VOLTORB.ordinal(),     BitmapFactory.decodeResource(context.getResources(), R.drawable.voltorb_mini));

                latch.countDown();
            });


            latch.await();
            delayedHandler(() -> Log.d(SUCCESS_TAG, "All files loaded successfully!"), 1000);

        }

        catch (Exception e) {
            Log.d(ERROR_TAG, "Error: " + e.getMessage());
        }

    }

    // ================================================================
    //                        Utility Methods
    // ================================================================


    public static void playSound(SoundEffects soundKey) {
        try {
            if (soundKey == LEVEL_COMPLETE_SFX) {
                soundpool.play(Objects.requireNonNull(SOUND_TABLE.get(soundKey.ordinal())), 2, 2, 0, 0, 1);
                return;
            }
            soundpool.play(Objects.requireNonNull( SOUND_TABLE.get(soundKey.ordinal()) ), 1, 1, 0, 0, 1);

        }
        catch (Exception e) {
            Log.d(ERROR_TAG, "Error: " + e.getMessage());
        }
    }


    public static ExecutorService createExecutorService(int threads) {
        return Executors.newFixedThreadPool(threads);
    }

    public static void logDebug(String message) {
        Log.d(DEBUG_TAG, message);
    }
    public static void logError(String message) {
        Log.d(ERROR_TAG, message);
    }

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



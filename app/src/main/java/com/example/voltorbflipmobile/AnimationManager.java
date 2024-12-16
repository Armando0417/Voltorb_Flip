package com.example.voltorbflipmobile;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.List;

public class AnimationManager {
    private static AnimationManager instance;

    private WeakReference<FrameLayout> overlayLayout;


    private AnimationManager() {}

    public static synchronized AnimationManager getInstance() {
        if (instance == null) {
            instance = new AnimationManager();
        }
        return instance;
    }

    public void setOverlayLayout (FrameLayout _overlayLayout) {
        this.overlayLayout = new WeakReference<>(_overlayLayout);
    }

    @NonNull
    private FrameLayout.LayoutParams getLayoutParams(View tileView, Tiles.gameTile currentTile, int frameIndex) {
        int width;
        int height;

        if (currentTile.getNumericValue() > 0) {
            width = tileView.getWidth() * 2;
            height = tileView.getHeight() * 2;
            return new FrameLayout.LayoutParams(width, height);
        }

        if (currentTile.getNumericValue() == 0 && frameIndex >= 3) {
            width = tileView.getWidth() * 3;
            height = tileView.getHeight() * 3;
        }

        else {
            width = tileView.getWidth();
            height = tileView.getHeight();
        }
        return new FrameLayout.LayoutParams (width, height);
    }


    public void triggerAnimation(Context context, View tileView, Tiles.gameTile currentTile, Game_Manager _gm, Fragment _currFragment) {
        if (overlayLayout == null) {
            throw new IllegalStateException("Overlay layout not set or has been garbage collected bruh");
        }

        Game_Manager.isInteractionAllowed = false;
        long overlayDuration = 70L;

        int[] location = new int[2];
        tileView.getLocationOnScreen(location);
        int tileX = location[0];
        int tileY = location[1];

        int[] overlayLocation = new int[2];
        overlayLayout.get().getLocationOnScreen(overlayLocation);
        int tileRelativeX = tileX - overlayLocation[0];
        int tileRelativeY = tileY - overlayLocation[1];


        List<Bitmap> animationFrames = (currentTile.getNumericValue() == 0)
                ? Utilities.DECODED_ANIM_TABLE.get(0)
                : Utilities.DECODED_ANIM_TABLE.get(1);


        ImageView animationView = new ImageView(context);
        overlayLayout.get().setVisibility(ImageView.VISIBLE);
        overlayLayout.get().addView(animationView);

        assert animationFrames != null;

        ValueAnimator animator = ValueAnimator.ofInt(0, animationFrames.size() - 1);
        animator.setDuration(animationFrames.size() * overlayDuration);

        animator.addUpdateListener(animation -> {
            int frameIndex = (int) animation.getAnimatedValue();

            Bitmap frame = animationFrames.get(frameIndex);
            FrameLayout.LayoutParams params = getLayoutParams(tileView, currentTile, frameIndex);


            int offSetX = (params.width - tileView.getWidth()) / 2;
            int offSetY = (params.height - tileView.getHeight()) / 2;

            params.leftMargin = tileRelativeX - offSetX;
            params.topMargin = tileRelativeY - offSetY;

            animationView.setLayoutParams(params);
            animationView.setImageBitmap(frame);

        });

        animator.start();

        if (currentTile.getNumericValue() == 0) {
            Utilities.delayedHandler( () -> Utilities.playSound(Utilities.SoundEffects.EXPLOSION_SFX), 300);

            if (_currFragment instanceof SecondFragment) {
                SecondFragment currFragment = (SecondFragment) _currFragment;
                currFragment.gameCompleted(false);
            }

        }
        else {
            Utilities.delayedHandler( () -> Game_Manager.isInteractionAllowed = true, 100);
        }

        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                overlayLayout.get().removeView(animationView);


                if (_gm != null && _currFragment instanceof SecondFragment) {

                    Game_Manager.gameBoard.updateBoard(currentTile.getRowCol().first, currentTile.getRowCol().second);
                    if (_gm.isScoreUpdated()) {
                        Utilities.playSound(Utilities.SoundEffects.INCREASE_POINT_SFX);
                    }
                    _gm.scoreUpdated = false;
                    Game_Manager.isInteractionAllowed = true;

                    Utilities.delayedHandler(() -> {

                        if (_gm.verifyWin()) {
                            Utilities.playSound(Utilities.SoundEffects.LEVEL_COMPLETE_SFX);
                            Game_Manager.isInteractionAllowed = false;

                            SecondFragment currFragment = (SecondFragment) _currFragment;
                            currFragment.gameCompleted(true);

                        }

                    }, Utilities.ANIMATION_DELAY);


                }
            }

        });

    }




}

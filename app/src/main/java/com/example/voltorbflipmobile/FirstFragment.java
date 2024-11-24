package com.example.voltorbflipmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.voltorbflipmobile.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private FrameLayout animationOverlay;
    HashMap<String, View> tileTable;

    HashMap<Integer, Bitmap> imageTable;
    ArrayList<Integer> explosionAnimations;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);


        loadFiles();
        prepareViews();

        return binding.getRoot();

    }

    private void flipTile(ImageView backImage, ImageView frontImage) {
        backImage.animate().rotationY(90).setDuration(200).withEndAction(() -> {
            backImage.setVisibility(View.GONE);
            frontImage.setVisibility(View.VISIBLE);
            frontImage.setRotationY(90);
            frontImage.animate().rotationY(0).setDuration(150);
        }).start();
        Utilities.playSound(Utilities.SoundEffects.FLIP_TILE_SFX);
    }


    private void loadFiles() {
        imageTable = new HashMap<>();
        explosionAnimations = new ArrayList<>();

        animationOverlay = binding.animationOverlay;

        imageTable.put(0, BitmapFactory.decodeResource(getResources(), R.drawable.voltorb));
        imageTable.put(1, BitmapFactory.decodeResource(getResources(), R.drawable.one));
        imageTable.put(2, BitmapFactory.decodeResource(getResources(), R.drawable.two));
        imageTable.put(3, BitmapFactory.decodeResource(getResources(), R.drawable.three));
        imageTable.put(4, BitmapFactory.decodeResource(getResources(), R.drawable.big_back_of_tile));

        explosionAnimations.add(R.drawable.animation_explosion_0_final);
        explosionAnimations.add(R.drawable.animation_explosion_1_final);
        explosionAnimations.add(R.drawable.animation_explosion_2_final);
        explosionAnimations.add(R.drawable.animation_explosion_3_final);
        explosionAnimations.add(R.drawable.animation_explosion_4_final);
        explosionAnimations.add(R.drawable.animation_explosion_5_final);
        explosionAnimations.add(R.drawable.animation_explosion_6_final);
        explosionAnimations.add(R.drawable.animation_explosion_7_final);
        explosionAnimations.add(R.drawable.animation_explosion_8_final);


        ArrayList<Integer> explosionIds = explosionAnimations;

        List<Bitmap> decodedExplosionFrames = new ArrayList<>();

        for (Integer explosionId : explosionIds) {
            Bitmap decodedFrame = BitmapFactory.decodeResource(getResources(), explosionId);
            decodedExplosionFrames.add(decodedFrame);
        }


    }

    private void prepareViews() {

        GridLayout gridLayout = binding.grid;

        View tile1 = gridLayout.findViewById(R.id.anim_tile1);
        View tile2 = gridLayout.findViewById(R.id.anim_tile2);
        View tile3 = gridLayout.findViewById(R.id.anim_tile3);
        View tileExp = gridLayout.findViewById(R.id.anim_tileExp);


        ImageView tile1Back = tile1.findViewById(R.id.back_image);
        ImageView tile2Back = tile2.findViewById(R.id.back_image);
        ImageView tile3Back = tile3.findViewById(R.id.back_image);
        ImageView tileExpBack = tileExp.findViewById(R.id.back_image);

        ImageView tile1Front = tile1.findViewById(R.id.front_image);
        ImageView tile2Front = tile2.findViewById(R.id.front_image);
        ImageView tile3Front = tile3.findViewById(R.id.front_image);
        ImageView tileExpFront = tileExp.findViewById(R.id.front_image);


        tile1Back.setImageResource(R.drawable.big_back_of_tile);
        tile2Back.setImageResource(R.drawable.big_back_of_tile);
        tile3Back.setImageResource(R.drawable.big_back_of_tile);
        tileExpBack.setImageResource(R.drawable.big_back_of_tile);

        tile1Front.setImageResource(R.drawable.one);
        tile2Front.setImageResource(R.drawable.two);
        tile3Front.setImageResource(R.drawable.three);
        tileExpFront.setImageResource(R.drawable.voltorb);

        tile1Back.setVisibility(View.GONE);
        tile2Back.setVisibility(View.GONE);
        tile3Back.setVisibility(View.GONE);
        tileExpBack.setVisibility(View.GONE);
        tile1Front.setVisibility(View.GONE);
        tile2Front.setVisibility(View.GONE);
        tile3Front.setVisibility(View.GONE);
        tileExpFront.setVisibility(View.GONE);

        tileTable = new HashMap<>();

        tileTable.put("tile1Back", tile1Back);
        tileTable.put("tile2Back", tile2Back);
        tileTable.put("tile3Back", tile3Back);
        tileTable.put("tileExpBack", tileExpBack);
        tileTable.put("tile1Front", tile1Front);
        tileTable.put("tile2Front", tile2Front);
        tileTable.put("tile3Front", tile3Front);
        tileTable.put("tileExpFront", tileExpFront);


}



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Utilities.delayedHandler(() -> {
            tileTable.get("tile1Back").setVisibility(View.VISIBLE);
            Utilities.playSound(Utilities.SoundEffects.INCREASE_POINT_SFX);
        }, 500);

        Utilities.delayedHandler(() -> {
            tileTable.get("tile2Back").setVisibility(View.VISIBLE);
            Utilities.playSound(Utilities.SoundEffects.INCREASE_POINT_SFX);

        }, 1000);

        Utilities.delayedHandler(() -> {
            tileTable.get("tile3Back").setVisibility(View.VISIBLE);
            Utilities.playSound(Utilities.SoundEffects.INCREASE_POINT_SFX);
        }, 1500);

        Utilities.delayedHandler(() -> {
            tileTable.get("tileExpBack").setVisibility(View.VISIBLE);
            Utilities.playSound(Utilities.SoundEffects.INCREASE_POINT_SFX);
        }, 2000);

        Utilities.delayedHandler(() -> {
            flipTile((ImageView) tileTable.get("tile1Back"), (ImageView) tileTable.get("tile1Front"));
        }, 2500);

        Utilities.delayedHandler(() -> {
            flipTile((ImageView) tileTable.get("tile2Back"), (ImageView) tileTable.get("tile2Front"));
        }, 3000);

        Utilities.delayedHandler(() -> {
            flipTile((ImageView) tileTable.get("tile3Back"), (ImageView) tileTable.get("tile3Front"));
        }, 3500);

        Utilities.delayedHandler(() -> {
            flipTile((ImageView) tileTable.get("tileExpBack"), (ImageView) tileTable.get("tileExpFront"));
            playOverlayAnimationSingleTile(explosionAnimations, tileTable.get("tileExpFront"));
        }, 4000);

        Utilities.delayedHandler(() -> {
            binding.startButton.setAlpha(0f);
            binding.startButton.setVisibility(View.VISIBLE);
            binding.startButton.animate().alpha(1f).setDuration(500).start();

            binding.gameTitleText.setAlpha(0f);
            binding.gameTitleText.setVisibility(View.VISIBLE);
            binding.gameTitleText.animate().alpha(1f).setDuration(500).start();

            binding.gameTitleShadow.setAlpha(0f);
            binding.gameTitleShadow.setVisibility(View.VISIBLE);
            binding.gameTitleShadow.animate().alpha(1f).setDuration(500).start();
        }, 4500);


        Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 0; i < 200; i++) {
            int delay = 250 * (i + 1);
            handler.postDelayed(() -> {
                float currentTranslationX = binding.gameTitleShadow.getTranslationX();
                binding.gameTitleShadow.setTranslationX(currentTranslationX * -1);
            }, delay);
        }


        binding.startButton.setOnClickListener(v -> {
                handler.removeCallbacksAndMessages(null);
                    Utilities.playSound(Utilities.SoundEffects.STORING_POINTS_SFX);
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                }
        );



    }

    public void playOverlayAnimationSingleTile(ArrayList<Integer> animationFrames, View tileView) {

        // Get the tile's position on the screen
        int[] location = new int[2];
        tileView.getLocationOnScreen(location);
        int tileX = location[0];
        int tileY = location[1];

        // Get the overlay's position
        int[] overlayLocation = new int[2];
        animationOverlay.getLocationOnScreen(overlayLocation);

        int tileRelativeX = tileX - overlayLocation[0];
        int tileRelativeY = tileY - overlayLocation[1];

        // Preloaded animation frames
        if (animationFrames == null || animationFrames.isEmpty()) {
            Log.d("DEBUG", "No preloaded frames available.");
            return;
        }

        requireActivity().runOnUiThread(() -> {
            // Create ImageView for animation
            ImageView animationView = new ImageView(requireContext());
            animationOverlay.setVisibility(View.VISIBLE);
            animationOverlay.addView(animationView);

            // Create a ValueAnimator to control the frame changes
            ValueAnimator animator = ValueAnimator.ofInt(0, animationFrames.size() - 1);
            animator.setDuration(animationFrames.size() * 200L);
            animator.addUpdateListener(animation -> {
                int frameIndex = (int) animation.getAnimatedValue();
                int currentBitmap = animationFrames.get(frameIndex);

                // Update layout params to match tile position and keep animation centered
                int width, height;
                if (frameIndex >= 3){
                    width = tileView.getWidth() * 3;
                    height = tileView.getHeight() * 3;
                }
                else {
                    width = tileView.getWidth();
                    height = tileView.getHeight();
                }
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);

                int offsetX = (params.width - tileView.getWidth()) / 2;
                int offsetY = (params.height - tileView.getHeight()) / 2;
                params.leftMargin = tileRelativeX - offsetX + 6;
                params.topMargin = tileRelativeY - offsetY + 5;

                animationView.setLayoutParams(params);

                animationView.setImageResource(currentBitmap);
            });
            animator.start();

            Utilities.playSound(Utilities.SoundEffects.EXPLOSION_SFX);

//            soundpool.play(1, 1, 1, 0, 0, 1);
            // Add animation end listener
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animationOverlay.removeView(animationView);


                }
            });
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
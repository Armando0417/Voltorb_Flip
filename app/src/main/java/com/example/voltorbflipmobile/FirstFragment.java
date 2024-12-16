package com.example.voltorbflipmobile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.voltorbflipmobile.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {
    private FragmentFirstBinding binding;
    List<Tiles.gameTile> tiles = new ArrayList<>();
    AnimationManager animationManager = AnimationManager.getInstance();


    @Override
    public View onCreateView (
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);

        int tileSize =  getResources().getDisplayMetrics().widthPixels / 10;

        Tiles.gameTile tile1 = new Tiles.gameTile   ( Utilities.TileTypes.ONE, new Pair<> (0, 0), tileSize, tileSize);
        Tiles.gameTile tile2 = new Tiles.gameTile   ( Utilities.TileTypes.TWO, new Pair<> (0, 1), tileSize, tileSize);
        Tiles.gameTile tile3 = new Tiles.gameTile   ( Utilities.TileTypes.THREE, new Pair<> (0, 2), tileSize, tileSize);
        Tiles.gameTile tileExp = new Tiles.gameTile ( Utilities.TileTypes.VOLTORB, new Pair<> (1, 1), tileSize, tileSize);

        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        tiles.add(tileExp);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridLayout gridLayout = binding.grid;

        View tile1 =    gridLayout.findViewById(R.id.anim_tile1);
        View tile2 =    gridLayout.findViewById(R.id.anim_tile2);
        View tile3 =    gridLayout.findViewById(R.id.anim_tile3);
        View tileExp =  gridLayout.findViewById(R.id.anim_tileExp);

        TileAdapter.GameTileHolder holder1 = new TileAdapter.GameTileHolder(tile1);
        TileAdapter.GameTileHolder holder2 = new TileAdapter.GameTileHolder(tile2);
        TileAdapter.GameTileHolder holder3 = new TileAdapter.GameTileHolder(tile3);
        TileAdapter.GameTileHolder holderExp = new TileAdapter.GameTileHolder(tileExp);

        holder1.bind(     tiles.get(0),     this);
        holder2.bind(     tiles.get(1),     this);
        holder3.bind(     tiles.get(2),     this);
        holderExp.bind(   tiles.get(3),     this);

        tile1.findViewById(R.id.back_image).setVisibility(View.INVISIBLE);
        tile2.findViewById(R.id.back_image).setVisibility(View.INVISIBLE);
        tile3.findViewById(R.id.back_image).setVisibility(View.INVISIBLE);
        tileExp.findViewById(R.id.back_image).setVisibility(View.INVISIBLE);

        Utilities.delayedHandler( () -> {
            tile1.findViewById(R.id.back_image).setVisibility(View.VISIBLE);
            Utilities.playSound(Utilities.SoundEffects.INCREASE_POINT_SFX);
        }, 500);

        Utilities.delayedHandler( () -> {
           tile2.findViewById(R.id.back_image).setVisibility(View.VISIBLE);
            Utilities.playSound(Utilities.SoundEffects.INCREASE_POINT_SFX);
        }, 1000);

        Utilities.delayedHandler( () -> {
            tile3.findViewById(R.id.back_image).setVisibility(View.VISIBLE);
            Utilities.playSound(Utilities.SoundEffects.INCREASE_POINT_SFX);
        }, 1500);

        Utilities.delayedHandler( () -> {
            tileExp.findViewById(R.id.back_image).setVisibility(View.VISIBLE);
            Utilities.playSound(Utilities.SoundEffects.INCREASE_POINT_SFX);
        }, 2000);


        animationManager.setOverlayLayout(binding.animationOverlay);


        Utilities.delayedHandler(() -> {
            holder1.flipTileUP(1);
        }, 2500);
            Utilities.delayedHandler(() -> {
                holder1.updateTileImage();
                tile1.findViewById(R.id.front_image).setVisibility(View.VISIBLE);
            }, 2700);

            Utilities.delayedHandler(() -> {
                holder2.flipTileUP(2);
            }, 3000);
            Utilities.delayedHandler(() -> {
                holder2.updateTileImage();
                tile2.findViewById(R.id.front_image).setVisibility(View.VISIBLE);
            }, 3200);

            Utilities.delayedHandler(() -> {
                holder3.flipTileUP(3);
            }, 3500);
            Utilities.delayedHandler(() -> {
                holder3.updateTileImage();
                tile3.findViewById(R.id.front_image).setVisibility(View.VISIBLE);
            }, 3700);

            Utilities.delayedHandler(() -> {
                holderExp.flipTileUP(0);
            }, 4000);
            Utilities.delayedHandler(() -> {
                holderExp.updateTileImage();
                tileExp.findViewById(R.id.front_image).setVisibility(View.VISIBLE);
            }, 4200);

            Utilities.delayedHandler(() -> {
                animationManager.triggerAnimation(requireContext(), tileExp, tiles.get(3), null, this);

            }, 4300);

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

    @Override
    public void onPause() {
        super.onPause();
        // Stop the music when exiting the fragment
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.stopMusic();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
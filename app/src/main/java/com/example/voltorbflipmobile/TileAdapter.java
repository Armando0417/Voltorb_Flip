package com.example.voltorbflipmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.widget.ImageView;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class TileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<ArrayList<Tiles.Tile>> board;

    private static final int GAME_TILE = 0;
    private static final int INFO_TILE = 1;
    public final Fragment fragment;

    public TileAdapter(ArrayList<ArrayList<Tiles.Tile>> board, Fragment _fragment) {
        this.board = board;
        this.fragment = _fragment;
    }

    @Override
    public int getItemViewType(int position) {
        int row = position / board.size();
        int col = position % board.size();

        if (board.get(row).get(col) instanceof Tiles.gameTile)
            return GAME_TILE;
        else
            return INFO_TILE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == GAME_TILE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_gametile, parent, false);
            return new GameTileHolder(view);
        }
        else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_infotile, parent, false);
            return new CustomInfoHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int row = position / board.size();
        int col = position % board.size();

        if (position == board.size() * board.size() - 1) {
            return;
        }

        if (holder instanceof GameTileHolder) {
            ((GameTileHolder) holder).bind((Tiles.gameTile) board.get(row).get(col), fragment);
        }
        else {
            if (holder instanceof CustomInfoHolder) {

                ((CustomInfoHolder) holder).bind((Tiles.infoTile) board.get(row).get(col)); // Bind infoTile data
            } else {
                Log.d("TileAdapter", "Invalid view holder type");
            }
        }
    }

    @Override
    public int getItemCount() {
        return board.size() * board.size() - 1; // Total number of tiles
    }



    /*-------------------------

        Game Tile View Holder

    -------------------------*/

    public static class GameTileHolder extends RecyclerView.ViewHolder {
        public final ImageView frontImage;
        public final ImageView backImage;
        public boolean isFlippedUp = false;
        public boolean isFlippedDown = true;
        private Tiles.gameTile currTile;

        public GameTileHolder(@NonNull View itemView) {
            super(itemView);
            frontImage = itemView.findViewById(R.id.front_image);
            backImage = itemView.findViewById(R.id.back_image);

        }

        public void bind(Tiles.gameTile tile, Fragment currFragment) {
            currTile = tile;
            backImage.setImageBitmap(tile.getBackImage());
            frontImage.setImageBitmap(tile.getFrontImage());
            int[] animationFrames = tile.getAnimationFrames();


            if (currFragment instanceof SecondFragment) {

                backImage.setOnClickListener(v -> {
                    if (Game_Manager.isInteractionAllowed) {
                        Game_Manager.isInteractionAllowed = false;

                        flipTileUP( tile.getType().ordinal() );

                        Utilities.delayedHandler( () -> {
                            Utilities.tryCatch( () -> {
                                SecondFragment fragment = (SecondFragment) currFragment;
                                fragment.playOverlayAnimation(animationFrames, itemView, tile);
                            }, Handlers.RUNTIME_EXCEPTION);

                        }, 200);
                    }

                    else {
                        Utilities.logError("Interaction is not allowed yet.");
                    }
                });
            }
        }

        public void flipTileUP (Integer tileVal) {

            LinkedList<Bitmap> correspondingSequence = Utilities.CORRESPONDING_FLIP_TABLE.get(tileVal);
            assert correspondingSequence != null;

            ValueAnimator animator = ValueAnimator.ofInt(0, correspondingSequence.size() - 1);

                animator.setDuration(correspondingSequence.size() * 35L);

                animator.addUpdateListener(animation -> {
                    int frameIndex = (int) animation.getAnimatedValue();

                backImage.setImageBitmap(correspondingSequence.get(frameIndex));
            });

            animator.start();

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!Game_Manager.gameFinishedState)
                        Utilities.playSound(Utilities.SoundEffects.FLIP_TILE_SFX);

                    backImage.setImageBitmap(currTile.getBackImage());
                    backImage.setVisibility(View.INVISIBLE);
                    frontImage.setVisibility(View.VISIBLE);

                    isFlippedUp = true;
                    isFlippedDown = false;

                    updateTileImage();
                }
            });

        }

        public void flipTileDOWN (Integer tileVal) {
            backImage.setImageBitmap(currTile.getBackImage());

            LinkedList<Bitmap> correspondingSequence = Utilities.CORRESPONDING_FLIP_TABLE.get(tileVal);

            assert correspondingSequence != null;
                LinkedList<Bitmap> reversedSequence = new LinkedList<>(correspondingSequence);

            Collections.reverse(reversedSequence);

            ValueAnimator animator = ValueAnimator.ofInt(0, reversedSequence.size() - 1);

            animator.setDuration(reversedSequence.size() * 20L);

            animator.addUpdateListener(animation -> {
                int frameIndex = (int) animation.getAnimatedValue();
                frontImage.setImageBitmap(reversedSequence.get(frameIndex));
            });

            animator.start();
            if (!Game_Manager.gameFinishedState)
                Utilities.playSound(Utilities.SoundEffects.FLIP_TILE_SFX);
            isFlippedUp = false;
            isFlippedDown = true;

            Utilities.delayedHandler( () -> {
                frontImage.setImageBitmap(currTile.getFrontImage());
                frontImage.setVisibility(View.INVISIBLE);
                backImage.setVisibility(View.VISIBLE);

            }, Utilities.FLIPPING_DELAY);
        }

        public void updateTileImage() {
            if (isFlippedDown) {
                frontImage.setImageBitmap(currTile.getFrontImage());
                frontImage.setVisibility(View.INVISIBLE);
                backImage.setVisibility(View.VISIBLE);
            }
            else {
                backImage.setImageBitmap(currTile.getBackImage());
                backImage.setVisibility(View.INVISIBLE);
                frontImage.setVisibility(View.VISIBLE);
            }
        }



        public void flipUp() {
            backImage.animate().rotationY(180).setDuration(200).withEndAction(() -> {
                backImage.setVisibility(View.GONE);
                frontImage.setVisibility(View.VISIBLE);
                frontImage.setRotation(0);
                frontImage.animate().rotationY(0).setDuration(150).withEndAction(() -> {
                });
            }).start();
            isFlippedUp = true;
            isFlippedDown = false;
            if (!Game_Manager.gameFinishedState)
                Utilities.playSound(Utilities.SoundEffects.FLIP_TILE_SFX);
        }

        public void flipDown() {
            frontImage.animate().rotationY(180).setDuration(200).withEndAction(() -> {
                frontImage.setVisibility(View.GONE);
                backImage.setVisibility(View.VISIBLE);
                backImage.setRotation(0);

            }).start();
            isFlippedUp = false;
            isFlippedDown = true;
            if (!Game_Manager.gameFinishedState)
                Utilities.playSound(Utilities.SoundEffects.FLIP_TILE_SFX);
        }

    }


    /*-------------------------

        Info Tile View Holder

    -------------------------*/
    public static class CustomInfoHolder extends RecyclerView.ViewHolder {
        TextView pointCountView;
        TextView bombCountView;
        ImageView miniVoltorb;
        ConstraintLayout tileBackground;


        public CustomInfoHolder(@NonNull View itemView) {
            super(itemView);
            pointCountView = itemView.findViewById(R.id.total_points);
            bombCountView = itemView.findViewById(R.id.total_bombs);
//            container = itemView.findViewById(R.id.tile_background);
            tileBackground = itemView.findViewById(R.id.tile_background);

            miniVoltorb = itemView.findViewById(R.id.mini_voltorb);
        }

        public void bind(Tiles.infoTile tile) {
            pointCountView.setText(String.valueOf(tile.getTotalPoints()));

            bombCountView.setText(String.valueOf(tile.getTotalBombs()));

            miniVoltorb.setImageBitmap(tile.getMiniVoltorb());
            miniVoltorb.bringToFront();


            try {
                tileBackground.setBackgroundColor(tile.getColor());
//                tileBackground.setBackgroundColor(Color.TRANSPARENT);
            }
            catch (Exception e) {
                Log.d(Utilities.DEBUG_TAG, "Error: " + e.getMessage());
            }

        }


    }

}
package com.example.voltorbflipmobile;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class TileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<ArrayList<SecondFragment.Tile>> board;

    private static final int GAME_TILE = 0;
    private static final int INFO_TILE = 1;
    public final SecondFragment fragment;

    public TileAdapter(ArrayList<ArrayList<SecondFragment.Tile>> board, SecondFragment fragment) {
        this.board = board;
        this.fragment = fragment;
    }

    @Override
    public int getItemViewType(int position) {
        int row = position / board.size();
        int col = position % board.size();
        if (board.get(row).get(col) instanceof SecondFragment.gameTile) {
            return GAME_TILE;
        } else {
            return INFO_TILE;
        }
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
            ((GameTileHolder) holder).bind((SecondFragment.gameTile) board.get(row).get(col), fragment);
        }
        else {
            if (holder instanceof CustomInfoHolder) {

                ((CustomInfoHolder) holder).bind((SecondFragment.infoTile) board.get(row).get(col)); // Bind infoTile data
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

    public class GameTileHolder extends RecyclerView.ViewHolder {
        private final ImageView frontImage;
        private final ImageView backImage;

        private final ImageView currentFrame;


        private boolean isFlipped = false;
        private boolean isAnimating = false;



        public GameTileHolder(@NonNull View itemView) {
            super(itemView);
            frontImage = itemView.findViewById(R.id.front_image);
            backImage = itemView.findViewById(R.id.back_image);
            currentFrame = itemView.findViewById(R.id.animation_frame);

        }

        public void bind(SecondFragment.gameTile tile, SecondFragment currFragment) {
            backImage.setImageBitmap(tile.getBackImage());
            frontImage.setImageBitmap(tile.getFrontImage());
            int[] animationFrames = tile.getAnimationFrames();


//            currentFrame.setScaleType(ImageView.ScaleType.FIT_CENTER);


            backImage.setOnClickListener(v -> {
                if (Game_Manager.isLosingState) {
                    Log.d(SecondFragment.DEBUG_TAG, "Lose animation in progress. No action allowed.");
                    return;
                }

                else if (Game_Manager.isWinningState) {
                    Log.d(SecondFragment.DEBUG_TAG, "Win animation in progress. No action allowed.");
                    return;
                }

                else if (Game_Manager.isTimerRunning()) {
                    Log.d(SecondFragment.DEBUG_TAG, "Tile Clicked and Animation Not Started");
                }
                else {
                    Log.d(SecondFragment.DEBUG_TAG, "Tile Clicked and Animation Started");
                    isAnimating = true;

                    Game_Manager.startCountdownTimer();

                    flipUp();


                    if (!Game_Manager.isLosingState) {

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            try {
                                currFragment.playOverlayAnimation(animationFrames, itemView, tile);
                            }
                            catch (Exception e) {
                                Log.d(SecondFragment.ERROR_TAG, "Error: " + e.getMessage());
                            }
                            finally {
                                Log.d(SecondFragment.DEBUG_TAG, "Animation Ended");
                                currFragment.isAnimating = false;
                                isAnimating = false;
                            }
                        }, 200);
                    }
                }
            });
        }


        public void flipUp() {
            backImage.animate().rotationY(180).setDuration(200).withEndAction(() -> {
                backImage.setVisibility(View.GONE);
                frontImage.setVisibility(View.VISIBLE);
                frontImage.setRotation(0);
                frontImage.animate().rotationY(0).setDuration(150).withEndAction(() -> {
                    isFlipped = true;
                });
            }).start();
            fragment.playSound(fragment.flipTileSfx);
        }

        public void flipDown() {
            backImage.animate().rotationY(180).setDuration(200).withEndAction(() -> {
                backImage.setVisibility(View.VISIBLE);
                frontImage.setVisibility(View.GONE);
                frontImage.setRotation(0);
                frontImage.animate().rotationY(0).setDuration(150).withEndAction(() -> {
                    isFlipped = true;
                });
            }).start();
            fragment.playSound(fragment.flipTileSfx);
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

        public void bind(SecondFragment.infoTile tile) {
            pointCountView.setText(String.valueOf(tile.getTotalPoints()));

            bombCountView.setText(String.valueOf(tile.getTotalBombs()));

            miniVoltorb.setImageBitmap(tile.getMiniVoltorb());
            miniVoltorb.bringToFront();


            try {
                tileBackground.setBackgroundColor(tile.getColor());
//                tileBackground.setBackgroundColor(Color.TRANSPARENT);
            }
            catch (Exception e) {
                Log.d(SecondFragment.DEBUG_TAG, "Error: " + e.getMessage());
            }

        }


    }

}
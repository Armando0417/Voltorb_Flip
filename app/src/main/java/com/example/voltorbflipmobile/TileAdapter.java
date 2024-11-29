package com.example.voltorbflipmobile;

import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.autofill.AutofillId;
import android.widget.ImageView;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class TileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<ArrayList<Tiles.Tile>> board;

    private static final int GAME_TILE = 0;
    private static final int INFO_TILE = 1;
    public final SecondFragment fragment;

    public TileAdapter(ArrayList<ArrayList<Tiles.Tile>> board, SecondFragment fragment) {
        this.board = board;
        this.fragment = fragment;
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
        private final ImageView frontImage;
        private final ImageView backImage;
        public boolean isFlippedUp = false;
        public boolean isFlippedDown = true;

        public GameTileHolder(@NonNull View itemView) {
            super(itemView);
            frontImage = itemView.findViewById(R.id.front_image);
            backImage = itemView.findViewById(R.id.back_image);

        }

        public void bind(Tiles.gameTile tile, SecondFragment currFragment) {
            backImage.setImageBitmap(tile.getBackImage());
            frontImage.setImageBitmap(tile.getFrontImage());
            int[] animationFrames = tile.getAnimationFrames();

            if (Game_Manager.isInteractionAllowed) {

            backImage.setOnClickListener(v -> {
                Game_Manager.isInteractionAllowed = false;
                flipUp();

                Utilities.delayedHandler( () -> {
                    try {
                        currFragment.playOverlayAnimation(animationFrames, itemView, tile);
                    }
                    catch (Exception e) {
                        Utilities.logError("Error in overlay animation: " + e);
                    }
                    finally {
                        Utilities.logDebug("Animation Ended. Re-enabling interactions.");
                    }
                }, Utilities.ANIMATION_DURATION);

            });
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
package com.example.voltorbflipmobile;

import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;






public class TileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<ArrayList<SecondFragment.Tile>> board;

    private static final int GAME_TILE = 0;
    private static final int INFO_TILE = 1;
    private final SecondFragment fragment;

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gametile_layout, parent, false);
            return new GameTileHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.infotile_layout, parent, false);
            return new customInfoHolder(view);
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
            if (holder instanceof customInfoHolder) {

                ((customInfoHolder) holder).bind((SecondFragment.infoTile) board.get(row).get(col)); // Bind infoTile data
            }
            else {
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

        private final ImageView currentFrame;

        private boolean isFlipped = false;


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

            currentFrame.setScaleType(ImageView.ScaleType.FIT_CENTER);

            backImage.setOnClickListener(v -> {
                if (!isFlipped) {
                    flipTile();
                    try {
                        currFragment.playOverlayAnimation(animationFrames, itemView, tile);

                    } catch (Exception e) {
                        Log.d(SecondFragment.ERROR_TAG, "Error: " + e.getMessage());
                    }
                }
            });
        }

        private void flipTile() {
            backImage.animate().rotationY(180).setDuration(200).withEndAction(() -> {
                backImage.setVisibility(View.GONE);
                frontImage.setVisibility(View.VISIBLE);
                frontImage.setRotation(0);
                frontImage.animate().rotationY(0).setDuration(200);
                isFlipped = true;

            }).start();
        }

    }



    /*-------------------------

        Info Tile View Holder

    -------------------------*/
    public static class customInfoHolder extends RecyclerView.ViewHolder {
        TextView pointCountView;
        TextView bombCountView;
        LinearLayout container;
        ImageView miniVoltorb;

        public customInfoHolder(@NonNull View itemView) {
            super(itemView);
            pointCountView = itemView.findViewById(R.id.total_points);
            bombCountView = itemView.findViewById(R.id.total_bombs);
            container = itemView.findViewById(R.id.tile_background);
            miniVoltorb = itemView.findViewById(R.id.mini_voltorb);
        }

        public void bind(SecondFragment.infoTile tile) {
            pointCountView = itemView.findViewById(R.id.total_points);
            pointCountView.setText(String.valueOf(tile.getTotalPoints()));

            bombCountView = itemView.findViewById(R.id.total_bombs);
            bombCountView.setText(String.valueOf(tile.getTotalBombs()));

            miniVoltorb.setImageBitmap(tile.getMiniVoltorb());
            miniVoltorb.bringToFront();


            Log.d(SecondFragment.DEBUG_TAG, "Extracted Color" + tile.getColor());
            try {
                container.setBackgroundColor(tile.getColor());
            } catch (Exception e) {
                Log.d(SecondFragment.DEBUG_TAG, "Error: " + e.getMessage());
            }

        }


    }

}
package com.example.voltorbflipmobile;

import android.graphics.Color;
import android.os.Build;
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

    public TileAdapter(ArrayList<ArrayList<SecondFragment.Tile>> board) {
        this.board = board;
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
            ((GameTileHolder) holder).bind((SecondFragment.gameTile) board.get(row).get(col));
//            Log.d("TileAdapter", "Created game tile at position: " + position);
        } else {
            if (holder instanceof customInfoHolder) {
//                Log.d("TileAdapter", "It is a info tile at position: " + position);
                ((customInfoHolder) holder).bind((SecondFragment.infoTile) board.get(row).get(col)); // Bind infoTile data
            } else {
                Log.d("TileAdapter", "Invalid view holder type");
            }
//            Log.d("TileAdapter", "Created info tile at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return board.size() * board.size() - 1; // Total number of tiles
    }


    /*-------------------------

            Game Tile View

     -------------------------*/
    public static class customGameTileView extends View {

        private Bitmap frontImage, backImage;
        boolean isFlipped = false;
        boolean isFlipping = false;

        Float rotationAngle = 0.0f;

        ArrayList<Bitmap> animationFrames;

        public customGameTileView(Context context) {
            super(context);
        }

        public customGameTileView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }


        public void loadImages(Bitmap _front, Bitmap _back) {
            frontImage = _front;
            backImage = _back;

            float width = getWidth();
            float height = getHeight();

            // Scale down the bitmap to fit
            float scale = Math.min(width / (float) frontImage.getWidth(),
                    height / (float) frontImage.getHeight());

        }

        public void tileClicked() {
            if (isFlipped) return;
            isFlipping = true;
            rotationAngle = 0.0f;
//            Log.d("GameTileViewer", "Tile clicked");
        }

        public void update() {
            if (isFlipped) return;

            if (isFlipping) {
                rotationAngle += 10;

                if (rotationAngle >= 180) {
                    isFlipping = false;
                    isFlipped = true;
                    rotationAngle = 180.0f;
                }


            }
        }


        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            this.update();

            // Verify that images are loaded
            if (frontImage == null || backImage == null) return;

            // Save the current canvas state
            canvas.save();

            float width = getWidth();
            float height = getHeight();

            // Scale down the bitmap to fit
            float scale = Math.min(width / (float) frontImage.getWidth(),
                    height / (float) frontImage.getHeight());

            // Set the scale for the canvas
            canvas.scale(scale, scale); // Scale the canvas to fit the images

            // Translate to the center of the view
            canvas.translate(width / (2 * scale), height / (2 * scale));

            // Apply rotation
            canvas.rotate(rotationAngle + 180);

            // Draw the appropriate image
            if (rotationAngle < 90 || rotationAngle > 270) {
                // Draw back image
                canvas.drawBitmap(backImage, (float) -backImage.getWidth() / 2, (float) -backImage.getHeight() / 2, null);
                invalidate();
            } else {
                // Draw front image
                canvas.drawBitmap(frontImage, (float) -frontImage.getWidth() / 2, (float) -frontImage.getHeight() / 2, null);
                invalidate();
            }

            // Restore the canvas state
            canvas.restore();


        }


        // End of customGameTileViewer class
    }


    /*-------------------------

        Game Tile View Holder

    -------------------------*/

//    public static class GameTileViewHolder extends RecyclerView.ViewHolder {
//        public customGameTileView tileView;
//
//        public GameTileViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tileView = itemView.findViewById(R.id.customGameTileViewer);
//        }
//
//        public void bind(SecondFragment.gameTile tile) {
//            tileView.loadImages(tile.getFrontImage(), tile.getBackImage());
//            tileView.setOnClickListener(v -> tileView.tileClicked());
//        }
//    }


    public static class GameTileHolder extends RecyclerView.ViewHolder {
        private ImageView frontImage;
        private ImageView backImage;



        private boolean isFlipped = false;


        public GameTileHolder(@NonNull View itemView) {
            super(itemView);
            frontImage = itemView.findViewById(R.id.front_image);
            backImage = itemView.findViewById(R.id.back_image);
        }

        public void bind(SecondFragment.gameTile tile) {
            backImage.setImageBitmap(tile.getBackImage());
            frontImage.setImageBitmap(tile.getFrontImage());
            backImage.setOnClickListener(v -> {
                if (!isFlipped) {
                    flipTile();
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
                int color = tile.getColor();
                container.setBackgroundColor(tile.getColor());
            } catch (Exception e) {
                Log.d(SecondFragment.DEBUG_TAG, "Error: " + e.getMessage());
            }

        }


    }

}
package com.example.voltorbflipmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voltorbflipmobile.databinding.FragmentSecondBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SecondFragment extends Fragment {





    // ================================================================
    //                      Interface Tile
    // ================================================================

    public interface Tile {
        int getWidth();
        int getHeight();
        Pair<Integer, Integer> getPosition();
        Pair<Integer, Integer> getRowCol();


    }


    // ================================================================
    //                      Class gameTile
    // ================================================================

    public class gameTile implements Tile {
        tileTypes value;
        Pair<Integer, Integer> position;
        Pair<Integer, Integer> row_col; // row index 0, col index 1
        int width, height;
        Float rotationAngle;
        boolean flipped, isFlipping = false;
        Bitmap frontImage, backImage;
        public int[] horizPipeColor, vertPipeColor;




        int[] animationFrames;

        gameTile(tileTypes _value, Pair<Integer, Integer> _position, int _width, int _height) {
            this.value = _value;
            this.position = _position;
            this.width = _width;
            this.height = _height;
            backImage = imageTable.get(4);
            horizPipeColor = new int[3];
            vertPipeColor = new int[3];

            horizPipeColor[0] = 0;
            horizPipeColor[1] = 0;
            horizPipeColor[2] = 0;

            vertPipeColor[0] = 0;
            vertPipeColor[1] = 0;
            vertPipeColor[2] = 0;




        }
        void set_row_col(int _row, int _col) {
            row_col = new Pair<>(_row, _col);
        }

    // Getters
        public Pair<Integer, Integer> get_row_col() {
            return row_col;
        }
        @Override
        public int getWidth() {
            return this.width;
        }
        @Override
        public int getHeight() {
            return this.height;
        }
        @Override
        public Pair<Integer, Integer> getPosition() {
            return this.position;
        }
        @Override
        public Pair<Integer, Integer> getRowCol() {
            return this.row_col;
        }
        Integer getNumericValue() {
            return value.ordinal();
        }
        tileTypes getType() {
            return value;
        }

        void update() {
            if (isFlipping) {
                rotationAngle += 10;
                Log.d("Rotation Angle changing", String.valueOf(rotationAngle));
                if (rotationAngle >= 180) {
                    Log.d("Rotation Angle Finished", "");
                    rotationAngle = 180.0F;
                    isFlipping = false;
                    flipped = true;
                }
            }
        }

        Bitmap getFrontImage() {
            return frontImage;
        }
        Bitmap getBackImage() {
            return backImage;
        }

        int[] getAnimationFrames() {
            return animationFrames;
        }


        void setValueImage(tileTypes _value) {
            value = _value;

            switch (value){
                case VOLTORB:
                    frontImage = imageTable.get(0);
                    break;
                case ONE:
                    frontImage = imageTable.get(1);
                    break;
                case TWO:
                    frontImage = imageTable.get(2);
                    break;
                case THREE:
                    frontImage = imageTable.get(3);
                    break;
                default:
                    frontImage = imageTable.get(4);
                    break;
            }

            if (value.ordinal() > 0) {
                animationFrames = animationTable.get("points");
            }
            else {
                animationFrames = animationTable.get("explosion");
            }

        }

        public int getHorizColor() {
            return Color.rgb(horizPipeColor[0], horizPipeColor[1], horizPipeColor[2]);
        }
        public int getVertColor() {
            return Color.rgb(vertPipeColor[0], vertPipeColor[1], vertPipeColor[2]);
        }




    }

    public class infoTile implements Tile {

        private int totalPoints;
        private int totalBombs;
        private final int width, height;

        boolean markCol;
        Pair<Integer, Integer> position;
        Pair<Integer, Integer> row_col; // row index 0, col index 1>

        Bitmap miniVoltorb;

        int[] tileColor;


        infoTile(Pair<Integer, Integer> _position, int _width, int _height, boolean _markCol) {
            this.position = _position;
            this.width = _width;
            this.height = _height;
            this.markCol = _markCol;

            miniVoltorb = imageTable.get(5);
        }

        void set_row_col(int _row, int _col) {
            row_col = new Pair<>(_row, _col);
            setColor();
        }

        void setColor() {
            int relevantValue;
            if (markCol) {
                relevantValue = row_col.second;
            }
            else {
                relevantValue = row_col.first;
            }

            switch (relevantValue) {
                case 0:
                    tileColor = colorTable.get(colorTypes.ROSE);
                    break;
                case 1:
                    tileColor = colorTable.get(colorTypes.MINT);
                    break;
                case 2:
                    tileColor = colorTable.get(colorTypes.LAKE_BLUE);
                    break;
                case 3:
                    tileColor = colorTable.get(colorTypes.WISTERA);
                    break;
                default:
                    tileColor = colorTable.get(colorTypes.GOLD);
                    break;
            }

        }


        int getTotalPoints() {
            return totalPoints;
        }

        int getTotalBombs() {
            return totalBombs;
        }

        void tally_points_bombs(ArrayList<ArrayList<Tile>> _board) {
            if (_board.size() < BOARD_SIZE || _board.get(0).size() < BOARD_SIZE) {
                throw new IllegalArgumentException("Board size is smaller than expected.");
            }

            if (markCol) {
                int myCol = row_col.second;
                for (int currRow = 0; currRow < BOARD_SIZE; currRow++) {
                    try {
                        gameTile currTile = (gameTile) _board.get(currRow).get(myCol);
                        if (currTile.getNumericValue() > 0) {
                            totalPoints += currTile.getNumericValue();
                            currTile.vertPipeColor = tileColor;
                        }
                        else {
                            totalBombs++;
                        }
                    } catch (Exception e) {
                        Log.d("Type Mismatch", "The tile at position [" + currRow + ", " + myCol + "] is not of type gameTile");
                    }
                }
            }

            else {
                int myRow = row_col.first;
                for (int currCol = 0; currCol < BOARD_SIZE; currCol++) {
                    try {

                        gameTile currTile = (gameTile) _board.get(myRow).get(currCol);
                        if (currTile.getNumericValue() > 0) {
                            totalPoints += currTile.getNumericValue();
                            currTile.horizPipeColor = tileColor;
                        }
                        else {
                            totalBombs++;
                        }
                    }
                    catch (Exception e) {
                        Log.d("Type Mismatch", "The tile at position [" + myRow + ", " + currCol + "] is not of type gameTile");
                    }
                }
            }
        }


        @Override
        public int getWidth() {
            return this.width;
        }

        @Override
        public int getHeight() {
            return this.height;
        }

        @Override
        public Pair<Integer, Integer> getPosition() {
            return this.position;
        }

        @Override
        public Pair<Integer, Integer> getRowCol() {
            return this.row_col;
        }

        public Bitmap getMiniVoltorb() {
            return miniVoltorb;
        }

        public int getColor() {
            return Color.rgb(tileColor[0], tileColor[1], tileColor[2]);
        }





        //end of class infoTile
    }


    public class VerticalSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private final int verticalSpaceHeight;
        private final int spanCount;

        public VerticalSpacingItemDecoration(int verticalSpaceHeight, int spanCount) {
            this.verticalSpaceHeight = verticalSpaceHeight;
            this.spanCount = spanCount;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            // Apply spacing only if the item is not in the first row
            if (position >= spanCount) {
                outRect.top = verticalSpaceHeight;
            }
        }
    }





    // ================================================================
    //                Continuation of Fragment Class
    // ================================================================

    //



    public final static String  DEBUG_TAG = "Debugging Purposes";
    public final static String  ERROR_TAG = "Error";

    public final static String SUCCESS_TAG = "Success!";

    public enum tileTypes {
        VOLTORB, ONE, TWO, THREE
    }

    public enum colorTypes {
        ROSE, MINT, LAKE_BLUE, WISTERA, GOLD
    }

    private final HashMap<String, int[] > animationTable = new HashMap<>();


    private FrameLayout animationOverlay;


    private FragmentSecondBinding binding;


    private final HashMap<Integer, Bitmap> imageTable = new HashMap<>();
    private final HashMap<colorTypes, int[]> colorTable = new HashMap<>();

    private final HashMap<Integer, List<Bitmap>> decodedAnimTable = new HashMap<>();

    ExecutorService executorService = Executors.newFixedThreadPool(3);

    private ArrayList<ArrayList<Tile>> finalBoard;
    public Game_Manager gm;

    public boolean isAnimating = false;
    private static final Integer BOARD_SIZE = 5;
    private static final Integer TOTAL_SIZE = 6;



    // ================================================================
    //                          Music Related
    // ================================================================

    public final HashMap<Integer, Integer> soundTable = new HashMap<>();
    private SoundPool soundpool;
    public final Integer flipTileSfx = 1;
    public final Integer increasePointSfx = 2;
    public final Integer explosionSfx = 3;

    public final Integer storingPointsSfx = 4;
    public final Integer levelCompleteSfx = 5;


    // ================================================================
    //                     Second Fragment Methods
    // ================================================================

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        try {
            animationOverlay = view.findViewById(R.id.animation_overlay);
        }
        catch (Exception e) {
            Log.d(ERROR_TAG, "Error: " + e.getMessage());
        }


        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();


        soundpool = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build();


        Log.d(DEBUG_TAG, "Finished Loading File method successfully");

        loadFilesWithServiceThreading();

        createGrid(screenWidth);

        gm = new Game_Manager(finalBoard);
        gm.countBoard();
        gm.showTileCount();

        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity != null) {
            mainActivity.startMusic();
        }


        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 6));
        recyclerView.addItemDecoration(new VerticalSpacingItemDecoration(30, 6));


        // Make sure gameBoard is not null and has items
        if (finalBoard != null && !finalBoard.isEmpty()) {
            TileAdapter adapter = new TileAdapter(finalBoard, this);
            recyclerView.setAdapter(adapter);
        }

        else {
            Log.e("SecondFragment", "Game board is empty or null.");
        }

        // Update the game Tiles
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {

                if (finalBoard.get(row).get(col) instanceof gameTile) {
                    ((gameTile) finalBoard.get(row).get(col)).update();
                }
            }
        }

        // Position connectors after RecyclerView layout is complete
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();




            /*
                The method should place all of the column connectors and row connectors in their respective spots in the screen.

                Goal: place the connector pieces in the correct spots to make the grid look good enough

                Process:
                    1) We need to get the 4 tiles of each side.
                        ! i -> [0, 5)
                        * the top tile (used for the columns)       -> row: [0] col: [i]
                        * The bottom tile (used for the columns)    -> row: [4] col: [i]
                        * The left Tile (used for the rows)         -> row: [i] col: [0]
                        *The right Tile (used for the rows)         -> row: [i] col: [4]

                   2) Calculate the distance between the two opposing tiles
                        * Width will be between the left and right tiles
                            ? right tile - left tile + tile width/2
                        * Height will be between the top and bottom tiles
                            ? bottom tile - top tile + tile height/2


                   3) Find the correct position for the rows and columns (center them on their respective
                        position)

                   4) Get the connector views (the current horizontal and vertical connectors)
                    * The horizontal rows and vertical columns go from 0 -> 4
                    ! you will be iterating through them

                   5) Set the correct width OR height (depending on horizontal or vertical (respectively) )

                   6) Set the correct position (x, y)

             */

                int gridWidth = recyclerView.getWidth();
                int gridHeight = recyclerView.getHeight();
                int tileWidth = gridWidth / 6; // Assuming 6 columns
                int tileHeight = gridHeight / 6; // Assuming 6 rows

                int startTileLeft = 0;
                int startTileRight = 5;

                int startTileTop = 0;
                int startTileBot = 30;


                // Position the connectors (vertical and horizontal)
                for (int i = 0; i < 5; i++) {

                    //Getting all 4 tiles
                    View leftSideTile = Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(startTileLeft)).itemView;
                    View RightSideTile = Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(startTileRight)).itemView;
                    View topTile = Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(startTileTop)).itemView;
                    View botTile = Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(startTileBot)).itemView;





                    // Get positions of all tiles
                    int[] locationLeft = new int[2];
                    int[] locationRight = new int[2];
                    int[] locationTop = new int[2];
                    int[] locationBot = new int[2];

                    leftSideTile.getLocationOnScreen(locationLeft);
                    RightSideTile.getLocationOnScreen(locationRight);
                    topTile.getLocationOnScreen(locationTop);
                    botTile.getLocationOnScreen(locationBot);

                    // Get overlay's position
                    int[] overlayLocation = new int[2];
                    animationOverlay.getLocationOnScreen(overlayLocation);

                    // Calculate relative positions for all tiles

// Left Tile (already done in your original code)
                    int leftRelativeX = locationLeft[0] - overlayLocation[0];
                    int leftRelativeY = locationLeft[1] - overlayLocation[1];

// Right Tile
                    int rightRelativeX = locationRight[0] - overlayLocation[0];
                    int rightRelativeY = locationRight[1] - overlayLocation[1];

// Top Tile
                    int topRelativeX = locationTop[0] - overlayLocation[0];
                    int topRelativeY = locationTop[1] - overlayLocation[1];

// Bottom Tile
                    int botRelativeX = locationBot[0] - overlayLocation[0];
                    int botRelativeY = locationBot[1] - overlayLocation[1];


                    Log.d(DEBUG_TAG, "Current Tiles being checked: " + startTileLeft + ", " + startTileRight + ", " + startTileTop + ", " + startTileBot);

                    startTileLeft += 6;
                    startTileRight += 6;
                    startTileTop++;
                    startTileBot++;


                    View horizontalConnector = view.findViewById(getResources().getIdentifier("horizontal_connector_" + i, "id", requireContext().getPackageName()));
                    View verticalConnector = view.findViewById(getResources().getIdentifier("vertical_connector_" + i, "id", requireContext().getPackageName()));


// Calculate distances using relative positions
                    Log.d(DEBUG_TAG, "Gonna check for: " + rightRelativeX + " - " + leftRelativeX);
                    int horizontalDistance = Math.abs(rightRelativeX - leftRelativeX + tileWidth/4);
                    int verticalDistance = Math.abs(botRelativeY - topRelativeY + tileHeight/4);

// ... (connector retrieval code) ...

                    if (horizontalConnector != null) {
                        // Use relative positions for horizontal connector placement
                        horizontalConnector.setLayoutParams(new FrameLayout.LayoutParams(horizontalDistance, 20));
                        horizontalConnector.setX(leftRelativeX  + (float) tileWidth/4);
                        horizontalConnector.setY(rightRelativeY + (float) tileHeight/5);
                        Log.d(DEBUG_TAG, "Placed Horizontal Connector #" + i + " at " + horizontalConnector.getX() + ", " + horizontalConnector.getY());
                    }

                    if (verticalConnector != null) {
                        // Use relative positions for vertical connector placement
                        verticalConnector.setLayoutParams(new FrameLayout.LayoutParams(20, verticalDistance));
                        verticalConnector.setX(botRelativeX + (float) tileWidth / 3);
                        verticalConnector.setY(topRelativeY + (float) tileHeight / 6);
                        Log.d(DEBUG_TAG, "Placed Vertical Connector #" + i + " at " + verticalConnector.getX() + ", " + verticalConnector.getY());
                    }


//                    int horizontalDistance = Math.abs(locationRight[0] - locationLeft[0]) + tileWidth / 2;
//                    int verticalDistance = Math.abs(locationBot[1] - locationTop[1]) + tileHeight / 2;




                    /*
                        Possible optimization, preload all of the connectors into an array and then iterate through it to assign each position

                     */

//
//
//                    if (horizontalConnector != null) {
//                        float horizontalPosition = topTile.getX();
//                        horizontalConnector.setLayoutParams(new FrameLayout.LayoutParams(horizontalDistance, 20));
//                        horizontalConnector.setX(horizontalPosition + (float) tileWidth / 2);
//                        horizontalConnector.setY((float) locationLeft[1] + (float) tileHeight /2);
//                        Log.d(DEBUG_TAG, "Placed Horizontal Connector #" + i + " at " + horizontalConnector.getX() + ", " + horizontalConnector.getY());
//                    }
//                    if (verticalConnector != null) {
//                        float verticalPosition = leftSideTile.getY();
//                        verticalConnector.setLayoutParams(new ViewGroup.LayoutParams(20, verticalDistance));
//                        verticalConnector.setX((float) locationTop[0] + (float) tileWidth / 2);
//                        verticalConnector.setY( verticalPosition + (float) tileHeight / 2);
//                        Log.d(DEBUG_TAG, "Placed Vertical Connector #" + i + " at " + verticalConnector.getX() + ", " + verticalConnector.getY());
//                    }
//
//
//                }

                }

                // Remove the listener after the first layout pass
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });
    }




    // ================================================================
    // Region              Auxiliary Methods
    // ================================================================
        public void playSound(Integer soundKey) {
            try {
                soundpool.play(soundTable.get(soundKey), 1, 1, 0, 0, 1);
            }
            catch (Exception e) {
                Log.d(ERROR_TAG, "Error: " + e.getMessage());
            }
        }

    @NonNull
    private static FrameLayout.LayoutParams getLayoutParams(View tileView, gameTile currentTile, int frameIndex) {
        int width;
        int height;

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


    private void loadFilesWithServiceThreading() {
            CountDownLatch latch = new CountDownLatch(1);

            try {
                executorService.submit(() -> {
                    imageTable.put(0, BitmapFactory.decodeResource(getResources(), R.drawable.voltorb));
                    imageTable.put(1, BitmapFactory.decodeResource(getResources(), R.drawable.one));
                    imageTable.put(2, BitmapFactory.decodeResource(getResources(), R.drawable.two));
                    imageTable.put(3, BitmapFactory.decodeResource(getResources(), R.drawable.three));
                    imageTable.put(4, BitmapFactory.decodeResource(getResources(), R.drawable.big_back_of_tile));
                    imageTable.put(5, BitmapFactory.decodeResource(getResources(), R.drawable.voltorb_mini));

                    latch.countDown();
                });

                executorService.submit(() -> {
                    animationTable.put("points", new int[]{
                            R.drawable.animation_points_0,
                            R.drawable.animation_points_1,
                            R.drawable.animation_points_2,
                            R.drawable.animation_points_3
                    });
                    animationTable.put("explosion", new int[]{
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
                    for (int frameResId : Objects.requireNonNull(animationTable.get("points"))) {
                        decodedPointFrames.add(BitmapFactory.decodeResource(getResources(), frameResId));
                    }
                    decodedAnimTable.put(1, decodedPointFrames);
                    List<Bitmap> decodedExplosionFrames = new ArrayList<>();
                    for (int frameResId : Objects.requireNonNull(animationTable.get("explosion"))) {
                        decodedExplosionFrames.add(BitmapFactory.decodeResource(getResources(), frameResId));
                    }
                    decodedAnimTable.put(0, decodedExplosionFrames);
                });

                executorService.submit(() -> {
                    soundTable.put(explosionSfx, soundpool.load(getContext(), R.raw.explosion_sfx, 1));
                    soundTable.put(increasePointSfx, soundpool.load(getContext(), R.raw.increase_point_sfx, 1));
                    soundTable.put(flipTileSfx, soundpool.load(getContext(), R.raw.flip_tile_sfx, 1));
                    soundTable.put(storingPointsSfx, soundpool.load(getContext(), R.raw.storing_points_sfx, 1));
                    soundTable.put(levelCompleteSfx, soundpool.load(getContext(), R.raw.level_complete_sfx, 1));

                    // Loading color types
                    colorTable.put(colorTypes.ROSE, new int[]{220, 117, 143});
                    colorTable.put(colorTypes.MINT, new int[]{0, 204, 163});
                    colorTable.put(colorTypes.LAKE_BLUE, new int[]{119, 141, 169});
                    colorTable.put(colorTypes.WISTERA, new int[]{180, 160, 229});
                    colorTable.put(colorTypes.GOLD, new int[]{243, 176, 43});
                });

            latch.await();

            } catch (Exception e) {
                Log.d(ERROR_TAG, "Error: " + e.getMessage());
            }
            finally {
                Log.d(SUCCESS_TAG, "Images loaded successfully");
            }
        }


    public void playOverlayAnimation(int[] animationFrames, View tileView, gameTile currentTile) {
            if (isAnimating) {
                Log.d("DEBUG", "Animation is already running.");
                return;
            }
            isAnimating = true;

            // Get tile's position on the screen
            int[] location = new int[2];
            tileView.getLocationOnScreen(location);
            int tileX = location[0];
            int tileY = location[1];

            int[] overlayLocation = new int[2];
            animationOverlay.getLocationOnScreen(overlayLocation);
            int tileRelativeX = tileX - overlayLocation[0];
            int tileRelativeY = tileY - overlayLocation[1];

            // No need for CountDownLatch here as you're directly using the shared bitmaps
            List<Bitmap> preloadedFrames = (currentTile.getNumericValue() == 0)
                    ? decodedAnimTable.get(0)
                    : decodedAnimTable.get(1);

            requireActivity().runOnUiThread(() -> {
                if (preloadedFrames == null || preloadedFrames.isEmpty()) {
                    Log.d("DEBUG", "No preloaded frames available.");
                    isAnimating = false;
                    return;
                }

                // Create ImageView for animation
                ImageView animationView = new ImageView(requireContext());
                animationOverlay.setVisibility(View.VISIBLE);
                animationOverlay.addView(animationView);

                // Create a ValueAnimator to control the frame changes
                ValueAnimator animator = ValueAnimator.ofInt(0, animationFrames.length - 1);
                animator.setDuration(animationFrames.length * 100L);
                animator.addUpdateListener(animation -> {
                    int frameIndex = (int) animation.getAnimatedValue();
                    Bitmap currentBitmap = preloadedFrames.get(frameIndex);

                    FrameLayout.LayoutParams params = getLayoutParams(tileView, currentTile, frameIndex);

                    // Calculate new center position to keep animation centered
                        int offsetX = (params.width - tileView.getWidth()) / 2;
                        int offsetY = (params.height - tileView.getHeight()) / 2;
                        params.leftMargin = tileRelativeX - offsetX;
                        params.topMargin = tileRelativeY - offsetY;
                        animationView.setLayoutParams(params);

                    animationView.setLayoutParams(params);

                    // Set the preloaded bitmap for the current frame
                    animationView.setImageBitmap(currentBitmap);
                });
                animator.start();
                if (currentTile.getNumericValue() == 0) {
                    Log.d("DEBUG", "Explosion Sound Here!");
                    playSound(explosionSfx);
                }
                else {
                    playSound(increasePointSfx);
                }
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animationOverlay.removeView(animationView);

                        gm.updateBoard(currentTile);
                        if (gm.verifyLoss(currentTile)) {
                            Log.d(DEBUG_TAG, "Game Over Works!");
                        }
                        if (gm.verifyWin()) {
                            Log.d(DEBUG_TAG, "Win Works!");
                            playSound(levelCompleteSfx);
                        }

                    }
                });
            });
        }


    private void createGrid(int screenWidth) {
        int tileDimensions = screenWidth / 10;

        ArrayList<ArrayList<Integer>> testBoard = testBoardGenerator();

        // Create the grid
        for (int row = 0; row < TOTAL_SIZE; row++) {
            for (int col = 0; col < TOTAL_SIZE; col++) {
                int xPos = col * tileDimensions;
                int yPos = row * tileDimensions;

                if (row == BOARD_SIZE && col == BOARD_SIZE) {
                    continue;
                }

                if (row == BOARD_SIZE) {
                    finalBoard.get(row).add(col,
                            new infoTile(
                                    new Pair<>(row, col),               // row, col
                                    tileDimensions, tileDimensions,     // width, height
                                    true                               // mark the column
                            ));
                    ((infoTile) finalBoard.get(row).get(col)).set_row_col(row, col);
                } else if (col == BOARD_SIZE) {
                    finalBoard.get(row).add(col,
                            new infoTile(
                                    new Pair<>(row, col),               // row, col
                                    tileDimensions, tileDimensions,     // width, height
                                    false                                // mark the column
                            ));
                    ((infoTile) finalBoard.get(row).get(col)).set_row_col(row, col);
                } else {
                    finalBoard.get(row).add(col,
                            new gameTile(
                                    tileTypes.values()[testBoard.get(row).get(col)], // Tile type
                                    new Pair<>(row, col),                            // row & col
                                    tileDimensions, tileDimensions                   // width & height
                            ));

                    gameTile currTile = (gameTile) finalBoard.get(row).get(col);
                    currTile.setValueImage(tileTypes.values()[testBoard.get(row).get(col)]);
                }
            }
        }
            populateInfoTiles();

        }



        private ArrayList<ArrayList<Integer>> testBoardGenerator() {
            finalBoard = new ArrayList<>(TOTAL_SIZE);
            for (int i = 0; i < TOTAL_SIZE; i++) {
                finalBoard.add(new ArrayList<>(TOTAL_SIZE));
            }

        /*
            Test Board Layout:
                0 3 1 3 2 0
                0 0 0 0 2 0
                0 2 1 3 0 0
                0 0 1 3 0 0
                1 2 3 2 1 0
                0 0 0 0 0 _
         */

            ArrayList<ArrayList<Integer>> testBoard = new ArrayList<>();
            testBoard.add(new ArrayList<>(Arrays.asList(0, 3, 1, 3, 2, 0)));
            testBoard.add(new ArrayList<>(Arrays.asList(0, 0, 0, 0, 2, 0)));
            testBoard.add(new ArrayList<>(Arrays.asList(0, 2, 1, 3, 0, 0)));
            testBoard.add(new ArrayList<>(Arrays.asList(0, 0, 1, 3, 0, 0)));
            testBoard.add(new ArrayList<>(Arrays.asList(1, 2, 3, 2, 1, 0)));
            testBoard.add(new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0)));

            return testBoard;
        }


        private void populateInfoTiles() {
            for (int row = 0; row < TOTAL_SIZE; row++) {
                for (int col = 0; col < TOTAL_SIZE; col++) {
                    if (row == BOARD_SIZE && col == BOARD_SIZE) {
                        continue;
                    }
                    if (finalBoard.get(row).get(col) instanceof infoTile) {
                        ((infoTile) finalBoard.get(row).get(col)).tally_points_bombs(finalBoard);
                    }
                }
            }
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
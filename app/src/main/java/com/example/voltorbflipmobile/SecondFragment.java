package com.example.voltorbflipmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Grid;
import androidx.core.content.ContextCompat;
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
                    tileColor = colorTable.get(colorTypes.GOLD);
                    break;
                case 1:
                    tileColor = colorTable.get(colorTypes.ROSE);
                    break;
                case 2:
                    tileColor = colorTable.get(colorTypes.MINT);
                    break;
                case 3:
                    tileColor = colorTable.get(colorTypes.TEAL);
                    break;
                default:
                    tileColor = colorTable.get(colorTypes.WISTERA);
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

        public colorTypes getColorType() {
            return colorTypes.values()[row_col.first - 1];
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


    public static class VerticalSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private final int verticalSpaceHeight;
        private final int spanCount;

        public VerticalSpacingItemDecoration(int verticalSpaceHeight, int spanCount) {
            this.verticalSpaceHeight = verticalSpaceHeight;
            this.spanCount = spanCount;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            // Apply spacing only if the item is not in the first row
            if (position >= spanCount) {
                outRect.top = verticalSpaceHeight;
            }
        }
    }


    static class NonScrollableGridLayoutManager extends GridLayoutManager {

        public NonScrollableGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        @Override
        public boolean canScrollVertically() {
            return false; // Disable vertical scrolling
        }

        @Override
        public boolean canScrollHorizontally() {
            return false; // Disable horizontal scrolling
        }
    }


    // ================================================================
    //                Continuation of Fragment Class
    // ================================================================

    public final static String  DEBUG_TAG = "Debugging Purposes";
    public final static String  ERROR_TAG = "Error";

    public final static String SUCCESS_TAG = "Success!";


    // Region ======== Enums =========
    public enum tileTypes {
        VOLTORB, ONE, TWO, THREE
    }

    public enum colorTypes {
        ROSE, MINT, TEAL, WISTERA, GOLD
    }


    // Region ======== Image Tables ========
    private final HashMap<String, int[] > animationTable = new HashMap<>();

    private final HashMap<Integer, Bitmap> imageTable = new HashMap<>();
    private final HashMap<colorTypes, int[]> colorTable = new HashMap<>();

    private final HashMap<Integer, List<Bitmap>> decodedAnimTable = new HashMap<>();



    // Region ======== Utilities ========
    private FrameLayout animationOverlay;

    private FragmentSecondBinding binding;
    ProgressBar loadingIndicator;
    public Game_Manager gm;

    ExecutorService executorService = Executors.newFixedThreadPool(3);

    private ArrayList<ArrayList<Tile>> finalBoard;
    private ArrayList<Tile> flattenedBoard;

    public boolean isAnimating = false;
    private static final Integer BOARD_SIZE = 5;
    private static final Integer TOTAL_SIZE = 6;



    // Region ======== Sound Tables ========

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


        loadFilesWithServiceThreading();

        createGrid(screenWidth);


        executorService.submit(() -> {
            gm = new Game_Manager(finalBoard, binding.getRoot().getRootView().findViewById(R.id.scoreboard));

            flattenedBoard = new ArrayList<>();
            flattenBoard();

        });

        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity != null) {
            mainActivity.startMusic();
        }


        return binding.getRoot();
    }

//    public class GridBoxItemDecoration extends RecyclerView.ItemDecoration {
//        private final int borderThickness;
//        private final Paint paint;
//
//        public GridBoxItemDecoration(int color, int thickness) {
//            this.borderThickness = thickness;
//            this.paint = new Paint();
//            this.paint.setColor(color);
//            this.paint.setStyle(Paint.Style.STROKE);
//            this.paint.setStrokeWidth(thickness);
//        }
//
//        @Override
//        public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//            int childCount = parent.getChildCount();
//
//            float parentLeft = parent.getLeft();
//            float parentTop = parent.getTop();
//            float parentRight = parent.getRight();
//            float parentBottom = parent.getBottom();
//            canvas.drawRect(parentLeft, parentTop, parentRight, parentBottom, paint);
//
//            for (int i = 0; i < childCount; i++) {
//                View child = parent.getChildAt(i);
//
//                // Get the item's bounds
//                float left = child.getLeft();
//                float top = child.getTop();
//                float right = child.getRight() - 1;
//                float bottom = child.getBottom() - 1;
//
//                // Draw a box around the item
//                canvas.drawRect(left, top, right, bottom, paint);
////                canvas.drawRect(parent.getLeft(), parent.getTop(), parent.getBottom(), parent.getRight(), paint);
//            }
//        }
//    }
//
//



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = binding.recyclerView;

        NonScrollableGridLayoutManager layoutManager = new NonScrollableGridLayoutManager(requireContext(), 6);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new VerticalSpacingItemDecoration(30, 6));

//        int dividerColor = Color.LTGRAY; // Choose a color for your dividers
//        int dividerThickness = 2; // Adjust thickness (in pixels) as needed
//        recyclerView.addItemDecoration(new GridBoxItemDecoration(dividerColor, dividerThickness));

        // Make sure gameBoard is not null and has items
        if (finalBoard != null && !finalBoard.isEmpty()) {
            TileAdapter adapter = new TileAdapter(finalBoard, this);
            recyclerView.setAdapter(adapter);
        }

        else {
            Log.e("SecondFragment", "Game board is empty or null.");
        }

        // Position connectors after RecyclerView layout is complete

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                CountDownLatch latch = new CountDownLatch(2);

                int gridWidth = recyclerView.getWidth();
                int gridHeight = recyclerView.getHeight();
                int tileWidth = gridWidth / 6;
                int tileHeight = gridHeight / 6;

                Log.d("Grid width and height are: ", gridWidth + " " + gridHeight);

                int startTileLeft = 0;
                int startTileRight = 5;

                int startTileTop = 0;
                int startTileBot = 30;


                // Position the connectors (vertical and horizontal)
                for (int i = 0; i < 5; i++) {

                    //? Gradient Inset Rectangle
                    GradientDrawable horizOuterRect = new GradientDrawable();
                    horizOuterRect.setShape(GradientDrawable.RECTANGLE);
                    horizOuterRect.setColor(ContextCompat.getColor(getContext(), R.color.white));
                    horizOuterRect.setSize(0, 0);


                    GradientDrawable vertOuterRect = new GradientDrawable();
                    vertOuterRect.setShape(GradientDrawable.RECTANGLE);
                    vertOuterRect.setColor(ContextCompat.getColor(getContext(), R.color.white));
                    vertOuterRect.setSize(0, 0);


                    //? Inner Rectangle
                    GradientDrawable innerVertRect = new GradientDrawable();
                    innerVertRect.setShape(GradientDrawable.RECTANGLE);

                    GradientDrawable innerHorizRect = new GradientDrawable();
                    innerHorizRect.setShape(GradientDrawable.RECTANGLE);


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

                    // Region Calculate relative positions for all tiles

                    // Right Tile
                    int leftRelativeX = locationLeft[0] - overlayLocation[0];
                    int rightRelativeY = locationRight[1] - overlayLocation[1];

                    Log.d(DEBUG_TAG, "The actual tile position for left is " + (locationLeft[1] - overlayLocation[1]) );
                    // Bottom Tile
                        int botRelativeX = locationBot[0] - overlayLocation[0];

                // Calculate distance between tiles
                    int horizDist = Math.abs(locationRight[0] - (locationLeft[0] + leftSideTile.getWidth()) - 10);
                    int vertDist = Math.abs( locationBot[1] - (locationTop[1] + topTile.getHeight()) - 10);

                // Fetching both Tile Colors
                    infoTile currHorizTile = null;
                    infoTile currVertTile = null;
                    if (startTileBot != 35) {
                        currVertTile = (infoTile) flattenedBoard.get(startTileBot);
                        currHorizTile = (infoTile) flattenedBoard.get(startTileRight);
                    }

                    int currRowColor = (currHorizTile != null) ? currHorizTile.getColor() : Color.YELLOW;
                    int currColColor =  (currVertTile != null) ? currVertTile.getColor() :Color.YELLOW;

                        startTileLeft += 6;
                        startTileRight += 6;
                        startTileTop++;
                        startTileBot++;

                        float horizTopOffset = (float) (15.0/100.0);
                        float horizBottomOffset = (float) (15.0/100.0);
                        float horizLeftOffset = (float) (1.5/100.0);
                        float horizRightOffset = (float) (1.5/100.0);

                        int horizTopActualOffset = (int) (20 * horizTopOffset);
                        int horizBottomActualOffset = (int) (20 * horizBottomOffset);
                        int horizLeftActualOffset = (int) (horizDist  * horizLeftOffset);
                        int horizRightActualOffset = (int) (horizDist  * horizRightOffset);

                        float vertTopOffset = (float) (1.5 / 100.0);
                        float vertBottomOffset = (float) (1.5 / 100.0);
                        float vertLeftOffset = (float) (15.0 / 100.0);
                        float vertRightOffset = (float) (15.0 / 100.0);

                        int vertTopActualOffset = (int) (vertDist * vertTopOffset);
                        int vertBottomActualOffset = (int) (vertDist * vertBottomOffset);
                        int vertLeftActualOffset = (int) (20 * vertLeftOffset);
                        int vertRightActualOffset = (int) (20 * vertRightOffset);

                // Horizontal Thread
                    int finalI = i;
                    executorService.submit(() -> {
                        innerHorizRect.setColor(currRowColor);
                        InsetDrawable horizontalInset = new InsetDrawable(innerHorizRect, horizLeftActualOffset, horizTopActualOffset, horizRightActualOffset, horizBottomActualOffset);
                        LayerDrawable horizontalLayers = new LayerDrawable(new Drawable[]{horizOuterRect, horizontalInset});

                        View horizontalConnector = view.findViewById(getResources().getIdentifier("horizontal_connector_" + finalI, "id", requireContext().getPackageName()));

                        if (horizontalConnector != null) {
                            requireActivity().runOnUiThread(() -> {
                                horizontalConnector.setLayoutParams(new FrameLayout.LayoutParams(horizDist + 10, 20));
                                horizontalConnector.setX((float) locationLeft[0] + leftSideTile.getWidth() );
                                horizontalConnector.setY(leftSideTile.getY() + ((float) leftSideTile.getHeight() /2 ) + 11);

                                horizontalConnector.setBackground(horizontalLayers);
                            });
                        }
                        latch.countDown();
                    });

                // Vertical Thread
                    int finalI1 = i;
                    executorService.submit(() -> {
                        innerVertRect.setColor(currColColor);
                        InsetDrawable verticalInset = new InsetDrawable(innerVertRect, vertLeftActualOffset, vertTopActualOffset, vertRightActualOffset, vertBottomActualOffset);
                        LayerDrawable verticalLayers = new LayerDrawable(new Drawable[]{vertOuterRect, verticalInset});

                        View verticalConnector = view.findViewById(getResources().getIdentifier("vertical_connector_" + finalI1, "id", requireContext().getPackageName()));

                        if (verticalConnector != null) {
                            requireActivity().runOnUiThread(() -> {
                                verticalConnector.setLayoutParams(new FrameLayout.LayoutParams(20, vertDist + 10));
                                verticalConnector.setX((float) locationTop[0] + (float) topTile.getWidth() / 2 - 10);

                                verticalConnector.setY((float) (locationTop[1] - locationTop[1] / 2.5) - 10);
                                verticalConnector.setBackground(verticalLayers);

                            });
                        }

                        latch.countDown();
                    });




            }

                try {
                    latch.await();
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    try {
                        loadingIndicator.setVisibility(View.GONE);
                    }
                    catch (Exception e) {
                        Log.d(ERROR_TAG, "Error: " + e.getMessage());
                    }
                    View grid = binding.getRoot().getRootView().findViewById(R.id.game_grid);
                    grid.setVisibility(View.VISIBLE);

                    grid.setAlpha(0f);
                    grid.animate().alpha(1f).setDuration(10).start();

                    recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Log.d(DEBUG_TAG, "Layout Complete");
                }
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
                    List<Bitmap> decodedExplosionFrames = new ArrayList<>();

                    for (int id = 0; id < Objects.requireNonNull(animationTable.get("explosion")).length; id++) {
                        int[] explosionIds = Objects.requireNonNull(animationTable.get("explosion"));
                        int[] pointIds = Objects.requireNonNull(animationTable.get("points"));

                        if (id < Objects.requireNonNull(animationTable.get("points")).length) {
                            decodedPointFrames.add(BitmapFactory.decodeResource(getResources(), pointIds[id]));
                        }

                        decodedExplosionFrames.add(BitmapFactory.decodeResource(getResources(), explosionIds[id]));
                    }

                    decodedAnimTable.put(0, decodedExplosionFrames);
                    decodedAnimTable.put(1, decodedPointFrames);

                });

                executorService.submit(() -> {
                    soundTable.put(explosionSfx, soundpool.load(getContext(), R.raw.explosion_sfx, 1));
                    soundTable.put(increasePointSfx, soundpool.load(getContext(), R.raw.increase_point_sfx, 1));
                    soundTable.put(flipTileSfx, soundpool.load(getContext(), R.raw.flip_tile_sfx, 1));
                    soundTable.put(storingPointsSfx, soundpool.load(getContext(), R.raw.storing_points_sfx, 1));
                    soundTable.put(levelCompleteSfx, soundpool.load(getContext(), R.raw.level_complete_sfx, 1));

                    colorTable.put(colorTypes.ROSE, new int[]{220, 117, 143});
                    colorTable.put(colorTypes.MINT, new int[]{0, 204, 163});
                    colorTable.put(colorTypes.TEAL, new int[]{0, 128, 128});
                    colorTable.put(colorTypes.WISTERA, new int[]{180, 160, 229});
                    colorTable.put(colorTypes.GOLD, new int[]{243, 176, 43});
                });

            latch.await();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Code to display the board or transition to the next screen
                    Log.d(DEBUG_TAG, "Board is now fully loaded and displayed.");
                }, 500); // 500ms delay

            }
            catch (Exception e) {
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
                    playSound(explosionSfx);
                    Log.d(DEBUG_TAG, "Explosion went off");
                }
                else {
                    playSound(increasePointSfx);
                }
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animationOverlay.removeView(animationView);

                        gm.updateBoard(currentTile);


                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (Game_Manager.verifyLoss(currentTile)) {
                                Log.d(DEBUG_TAG, "Lose Works!");
//                                triggerLoseAnimation();
                            }
                            if (gm.verifyWin()) {
                                Log.d(DEBUG_TAG, "Win Works!");
                                playSound(levelCompleteSfx);
                            }
                        }, 500);

                    }
                });
            });
        }

    public void triggerLoseAnimation() {
        RecyclerView recView = binding.recyclerView;

        final int TOTAL_ROWS = 6;        // Total rows in the overall grid
        final int TOTAL_COLUMNS = 6;    // Total columns in the overall grid
        final int FLIP_ROWS = 5;        // Rows to flip
        final int FLIP_COLUMNS = 5;     // Columns to flip

        for (int col = 0; col < FLIP_COLUMNS; col++) {
            int finalCol = col;

            // Delay flipping for each column
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                for (int row = 0; row < FLIP_ROWS; row++) {
                    // Calculate the position in the RecyclerView
                    int position = (row * TOTAL_COLUMNS) + finalCol;

                    // Get the ViewHolder for the current position
                    RecyclerView.ViewHolder viewHolder = recView.findViewHolderForAdapterPosition(position);

                    if (viewHolder instanceof TileAdapter.GameTileHolder) {
                        TileAdapter.GameTileHolder tileHolder = (TileAdapter.GameTileHolder) viewHolder;


                        tileHolder.flipUp();
                    }
                }
            }, col * 500);
        }
    }



    private void createGrid(int screenWidth) {
        int tileDimensions = screenWidth / 10;

        ArrayList<ArrayList<Integer>> testBoard = testBoardGenerator();

        // Create the grid
        for (int row = 0; row < TOTAL_SIZE; row++) {
            for (int col = 0; col < TOTAL_SIZE; col++) {

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
                }
                else if (col == BOARD_SIZE) {
                    finalBoard.get(row).add(col,
                            new infoTile(
                                    new Pair<>(row, col),               // row, col
                                    tileDimensions, tileDimensions,     // width, height
                                    false                                // mark the column
                            ));
                    ((infoTile) finalBoard.get(row).get(col)).set_row_col(row, col);
                }
                else {
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
            Log.d(DEBUG_TAG, "Finished Create grid with a size of " + finalBoard.size() + "x" + finalBoard.get(0).size());
        }


        private void flattenBoard() {
            for (ArrayList<Tile> row : finalBoard) {
                Log.d(DEBUG_TAG, "Size of final board is " + finalBoard.size());
                Log.d(DEBUG_TAG, "row size is: " + row.size());
                for (Tile curr : row) {
                    if (curr != null) {
                        flattenedBoard.add(curr);
                    }
                }

                }
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
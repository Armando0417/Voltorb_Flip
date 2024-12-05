package com.example.voltorbflipmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voltorbflipmobile.databinding.FragmentSecondBinding;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;


public class SecondFragment extends Fragment {

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

    public class GridBoxItemDecoration extends RecyclerView.ItemDecoration {
        private final int borderThickness;
        private final Paint paint;

        public GridBoxItemDecoration(int color, int thickness) {
            this.borderThickness = thickness;
            this.paint = new Paint();
            this.paint.setColor(color);
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setStrokeWidth(thickness);
        }

        @Override
        public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int childCount = parent.getChildCount();

            float parentLeft = parent.getLeft();
            float parentTop = parent.getTop();
            float parentRight = parent.getRight();
            float parentBottom = parent.getBottom();
            canvas.drawRect(parentLeft, parentTop, parentRight, parentBottom, paint);

            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                // Get the item's bounds
                float left = child.getLeft();
                float top = child.getTop();
                float right = child.getRight() - 1;
                float bottom = child.getBottom() - 1;

                // Draw a box around the item
                canvas.drawRect(left, top, right, bottom, paint);
//                canvas.drawRect(parent.getLeft(), parent.getTop(), parent.getBottom(), parent.getRight(), paint);
            }
        }
    }


    public static class SharedViewModel extends ViewModel {
        private final MutableLiveData<Integer> levelNumber = new MutableLiveData<>(0);

        public LiveData<Integer> getLevelNumber() {
            return levelNumber;
        }

        public void setLevelNumber(int level) {
            levelNumber.setValue(level);
        }
    }


    // ================================================================
    //                Continuation of Fragment Class
    // ================================================================

    // Region ======== Miscellaneous Variables ========

    private FrameLayout animationOverlay;

    private FragmentSecondBinding binding;

    ExecutorService backgroundExecutor = Utilities.createExecutorService(3);

    public boolean isAnimating = false;

    public Game_Manager gm;

    public FrameLayout stateLayout;
    public  TextView[] loseText = new TextView[2];

    public boolean startLoseAnimation = false;

    private static final int[] verticalIDs = {
            R.id.vertical_connector_0,
            R.id.vertical_connector_1,
            R.id.vertical_connector_2,
            R.id.vertical_connector_3,
            R.id.vertical_connector_4,
    };
    private static final int[] horizontalIDs = {
            R.id.horizontal_connector_0,
            R.id.horizontal_connector_1,
            R.id.horizontal_connector_2,
            R.id.horizontal_connector_3,
            R.id.horizontal_connector_4
    };

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

        stateLayout = view.findViewById(R.id.next_state_trigger);
        loseText[0] = view.findViewById(R.id.you_lose_text_background);
        loseText[1] = view.findViewById(R.id.you_lose_text_foreground);

        Utilities.tryCatch(() -> {
            animationOverlay = view.findViewById(R.id.animation_overlay);
        }, Handlers.GENERAL_EXCEPTION);

        gm = new Game_Manager(binding.getRoot().getRootView().findViewById(R.id.scoreboard), getResources().getDisplayMetrics().widthPixels);

        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity != null)
            mainActivity.startMusic();


        Utilities.tryCatch( () -> {
            SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            model.setLevelNumber( gm.getCurrentLevel() );

        }, Handlers.GENERAL_EXCEPTION);



        return binding.getRoot();
    }



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = binding.recyclerView;

        NonScrollableGridLayoutManager layoutManager = new NonScrollableGridLayoutManager(requireContext(), 6);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new VerticalSpacingItemDecoration(30, 6));

        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        viewModel.getLevelNumber().observe(getViewLifecycleOwner(), levelNum -> {
            if (getActivity() != null) {
                Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).setTitle("Level " + levelNum);
            }
        });


//        int dividerColor = Color.LTGRAY; // Choose a color for your dividers
//        int dividerThickness = 2; // Adjust thickness (in pixels) as needed
//        recyclerView.addItemDecoration(new GridBoxItemDecoration(dividerColor, dividerThickness));

        // Make sure tiles is not null and has items
        if (gm.getGameBoard().getBoardAsList() != null && !gm.getGameBoard().getBoardAsList().isEmpty()) {
            TileAdapter adapter = new TileAdapter(gm.getGameBoard().getBoardAsList(), this);
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
                        horizOuterRect.setColor(ContextCompat.getColor(requireContext(), R.color.white));
                        horizOuterRect.setSize(0, 0);

                    GradientDrawable vertOuterRect = new GradientDrawable();
                        vertOuterRect.setShape(GradientDrawable.RECTANGLE);
                        vertOuterRect.setColor(ContextCompat.getColor(requireContext(), R.color.white));
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


                    // Bottom Tile
                        int botRelativeX = locationBot[0] - overlayLocation[0];

                    // Calculate distance between tiles
                        int horizDist = Math.abs(locationRight[0] - (locationLeft[0] + leftSideTile.getWidth()) - 10);
                        int vertDist = Math.abs( locationBot[1] - (locationTop[1] + topTile.getHeight()) - 10);

                    // Fetching both Tile Colors
                        Tiles.infoTile currHorizTile = null;
                        Tiles.infoTile currVertTile = null;
                        if (startTileBot != 35) {
                            currVertTile = (Tiles.infoTile) gm.getGameBoard().getFlattenedBoard().get(startTileBot);
                            currHorizTile = (Tiles.infoTile) gm.getGameBoard().getFlattenedBoard().get(startTileRight);
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
                    backgroundExecutor.submit(() -> {
                        innerHorizRect.setColor(currRowColor);
                        InsetDrawable horizontalInset = new InsetDrawable(innerHorizRect, horizLeftActualOffset, horizTopActualOffset, horizRightActualOffset, horizBottomActualOffset);
                        LayerDrawable horizontalLayers = new LayerDrawable(new Drawable[]{horizOuterRect, horizontalInset});

//                        View horizontalConnector = view.findViewById(getResources().getIdentifier("horizontal_connector_" + finalI, "id", requireContext().getPackageName()));
                            View horizontalConnector = view.findViewById(horizontalIDs[finalI]);


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
                    backgroundExecutor.submit(() -> {
                        innerVertRect.setColor(currColColor);
                        InsetDrawable verticalInset = new InsetDrawable(innerVertRect, vertLeftActualOffset, vertTopActualOffset, vertRightActualOffset, vertBottomActualOffset);
                        LayerDrawable verticalLayers = new LayerDrawable(new Drawable[]{vertOuterRect, verticalInset});

//                        View verticalConnector = view.findViewById(getResources().getIdentifier("vertical_connector_" + finalI1, "id", requireContext().getPackageName()));

                        View verticalConnector = view.findViewById(verticalIDs[finalI1]);

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
                    View grid = binding.getRoot().getRootView().findViewById(R.id.game_grid);
                    grid.setVisibility(View.VISIBLE);

                    grid.setAlpha(0f);
                    grid.animate().alpha(1f).setDuration(10).start();

                    recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Log.d(Utilities.SUCCESS_TAG, "Layout Complete");
                }
            }
        });
    }

    // ================================================================
    // Region              Auxiliary Methods
    // ================================================================

    @NonNull
    private static FrameLayout.LayoutParams getLayoutParams(View tileView, Tiles.gameTile currentTile, int frameIndex) {
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

    public void playOverlayAnimation(int[] animationFrames, View tileView, Tiles.gameTile currentTile) {
            Game_Manager.isInteractionAllowed = false;
            long overlayDuration = 70L;

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
                    ? Utilities.DECODED_ANIM_TABLE.get(0)
                    : Utilities.DECODED_ANIM_TABLE.get(1);

            requireActivity().runOnUiThread(() -> {
                if (preloadedFrames == null || preloadedFrames.isEmpty()) {
                    Utilities.logError("No preloaded frames loaded, Cancelling animations");
                    isAnimating = false;
                    return;
                }

                // Create ImageView for animation
                ImageView animationView = new ImageView(requireContext());
                animationOverlay.setVisibility(View.VISIBLE);
                animationOverlay.addView(animationView);

                // Create a ValueAnimator to control the frame changes
                ValueAnimator animator = ValueAnimator.ofInt(0, animationFrames.length - 1);

                animator.setDuration(animationFrames.length * overlayDuration);


                boolean isPoints = !(currentTile.getNumericValue() == 0);
                int animationDelay = isPoints ? 400 : 900;

                animator.addUpdateListener(animation -> {
                    int frameIndex = (int) animation.getAnimatedValue();

                    Bitmap currentBitmap = preloadedFrames.get(frameIndex);

                    FrameLayout.LayoutParams params = getLayoutParams(tileView, currentTile, frameIndex);

                    int offsetX = (params.width - tileView.getWidth()) / 2;
                    int offsetY = (params.height - tileView.getHeight()) / 2;
                    params.leftMargin = tileRelativeX - offsetX;
                    params.topMargin = tileRelativeY - offsetY;

                    animationView.setLayoutParams(params);

                    animationView.setImageBitmap(currentBitmap);
                });

                animator.start();

                if (currentTile.getNumericValue() == 0) {
                    Utilities.delayedHandler( () -> Utilities.playSound(Utilities.SoundEffects.EXPLOSION_SFX), 300);
                    Utilities.logDebug("Loss condition has been met, Triggering lose animation.");

                    Utilities.delayedHandler( () -> {
                        stateLayout.setVisibility(View.VISIBLE);
                        stateLayout.setBackgroundColor(Color.argb(126, 210, 0, 0) );
                        loseText[0].setVisibility(View.VISIBLE);
                        loseText[0].setText(R.string.you_lose_text);
                        loseText[1].setVisibility(View.VISIBLE);
                        loseText[1].setText(R.string.you_lose_text);

                        stateLayout.setOnClickListener(v -> {
                            stateLayout.setVisibility(View.GONE);

                            triggerFlipAllAnim();
                        });

                    }, animationDelay);
                }

                else {
                    Utilities.playSound(Utilities.SoundEffects.INCREASE_POINT_SFX);
                    Utilities.delayedHandler( () -> Game_Manager.isInteractionAllowed = true, 100);

                }

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animationOverlay.removeView(animationView);

                        gm.getGameBoard().updateBoard(currentTile.getRowCol().first, currentTile.getRowCol().second);

                        Utilities.delayedHandler( () -> {

                             if (gm.verifyWin()) {
                                Utilities.logDebug("Win condition has been met, Triggering win animation.");
                                Utilities.playSound(Utilities.SoundEffects.LEVEL_COMPLETE_SFX);

                                 Game_Manager.isInteractionAllowed = false;

                                 Utilities.delayedHandler( () -> {
                                     stateLayout.setVisibility(View.VISIBLE);
                                     stateLayout.setBackgroundColor(Color.argb(126, 0, 210, 0) );
                                     loseText[0].setVisibility(View.VISIBLE);
                                     loseText[0].setText(R.string.you_win);
                                     loseText[1].setVisibility(View.VISIBLE);
                                     loseText[1].setText(R.string.you_win);

                                     stateLayout.setOnClickListener(v -> {
                                         stateLayout.setVisibility(View.GONE);
                                         triggerFlipAllAnim();
                                         try {
                                             SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
                                             Integer currLevel = model.getLevelNumber().getValue();
                                             if (currLevel != null) {
                                                 gm.levelUp();
                                                 model.setLevelNumber( gm.getCurrentLevel() );
                                             }
                                         }
                                         catch (Exception e) {
                                             Utilities.logError("Failure to update level number");
                                         }

                                     });

                                 }, animationDelay);

                                //TODO ADD A DISPLAY BUTTON TO MOVE TO A NEW LEVEL (SCORE GETS STORED AND GENERATE NEW BOARD)
                            }
                        }, Utilities.ANIMATION_DELAY);
                    }
                });
            });
        }


    public void triggerFlipAllAnim() {
        Game_Manager.gameFinishedState = true;
        Utilities.delayedHandler(() -> {
            showAllTiles();
            Utilities.delayedHandler( this::flipAllDown, Utilities.ANIMATION_DURATION);
        }, Utilities.ANIMATION_DURATION);

    }

    public void flipAllDown() {
        RecyclerView recView = binding.recyclerView;

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
                        if (tileHolder.isFlippedUp) {

                            Tiles.gameTile currTile = (Tiles.gameTile) gm.getGameBoard().getFlattenedBoard().get(position);
                            if (currTile != null) {
                                tileHolder.flipTileDOWN ( currTile.getType().ordinal() );
                            }
                            else {
                                Utilities.logError("Tile is null");
                            }

                        }
                        Game_Manager.isWinningState = false;
                    }
                }
                if (Game_Manager.gameFinishedState)
                    Utilities.playSound(Utilities.SoundEffects.FLIP_TILE_SFX);

            }, col * 240);
        }

        Utilities.delayedHandler( () -> {
            gm.resetBoard();
            Game_Manager.isInteractionAllowed = true;
            Game_Manager.gameFinishedState = false;
            RecyclerView recyclerView = binding.recyclerView;
            TileAdapter adapter = new TileAdapter(gm.getGameBoard().getBoardAsList(), this);
            recyclerView.setAdapter(adapter);

        }, 1100);
    }


    public void showAllTiles() {
        RecyclerView recView = binding.recyclerView;

        final int TOTAL_COLUMNS = 6;    // Total columns in the overall grid
        final int FLIP_ROWS = 5;        // Rows to flip
        final int FLIP_COLUMNS = 5;     // Columns to flip

        for (int col = 0; col < FLIP_COLUMNS; col++) {

            for (int row = 0; row < FLIP_ROWS; row++) {
                int position = (row * TOTAL_COLUMNS) + col;

                // Get the ViewHolder for the current position
                RecyclerView.ViewHolder viewHolder = recView.findViewHolderForAdapterPosition(position);

                if (viewHolder instanceof TileAdapter.GameTileHolder) {

                    TileAdapter.GameTileHolder tileHolder = (TileAdapter.GameTileHolder) viewHolder;
                    if (tileHolder.isFlippedDown) {
                        Tiles.gameTile currTile = (Tiles.gameTile) gm.getGameBoard().getFlattenedBoard().get(position);

                        if (currTile != null) {
                            tileHolder.flipTileUP ( currTile.getType().ordinal() );
                        }
                        else {
                            Utilities.logError("Tile is null");
                        }
                    }
                    Game_Manager.isWinningState = false;
                    }
                }
            }
            if (Game_Manager.gameFinishedState)
                Utilities.playSound(Utilities.SoundEffects.FLIP_TILE_SFX);

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
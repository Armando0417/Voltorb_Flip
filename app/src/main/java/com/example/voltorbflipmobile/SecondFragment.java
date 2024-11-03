package com.example.voltorbflipmobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voltorbflipmobile.databinding.FragmentSecondBinding;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;


public class SecondFragment extends Fragment {

    /*
        TAGS
     */
    public final static String  DEBUG_TAG = "Debugging Purposes";
    public final static String  ERROR_TAG = "Error";

    enum tileTypes {
        VOLTORB, ONE, TWO, THREE
    }

    enum colorTypes {
        ROSE, MINT, LAKE_BLUE, WISTERA, GOLD
    }

    private final int[] Colors = {
            Color.RED,
            Color.GREEN,
            Color.CYAN,
            Color.MAGENTA,
            Color.YELLOW
    };

    private final HashMap<String, int[] > animationTable = new HashMap<>();


    // ================================================================
    //                      Interface Tile
    // ================================================================

    public interface Tile {
        int getWidth();
        int getHeight();
        Pair<Integer, Integer> getPosition();
        Pair<Integer, Integer> getRowCol();

    } //================================================================


    // ================================================================
    //                      Class gameTile
    // ================================================================

    public class gameTile implements Tile {
        tileTypes value;
        Pair<Integer, Integer> position;
        Pair<Integer, Integer> row_col; // row index 0, col index 1
        int width, height;
        Float rotationAngle;
        boolean flipped, playAnimation, isFlipping = false;
        Bitmap frontImage, backImage;
        int[] animationFrames;

        gameTile(tileTypes _value, Pair<Integer, Integer> _position, int _width, int _height) {
            this.value = _value;
            this.position = _position;
            this.width = _width;
            this.height = _height;
            backImage = imageTable.get(4);
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


//            if (value == tileTypes.VOLTORB) {
//                frontImage = imageTable.get(0);
//            }
//            else if (value == tileTypes.ONE) {
//                frontImage = imageTable.get(1);
//            }
//            else if (value == tileTypes.TWO) {
//                frontImage = imageTable.get(2);
//            }
//            else if (value == tileTypes.THREE) {
//                frontImage = imageTable.get(3);
//            }
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
            int relevantValue = -1;
            if (markCol) {
                relevantValue = row_col.second;
            }
            else {
                relevantValue = row_col.first;
            }
            Log.d(DEBUG_TAG, "Value of relevantValue:" + relevantValue);
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

        /*
                    { {0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5} },

                    { {1, 0}, {1, 1}, {1, 2}, {1, 3}, {1, 4}, {1, 5} },

                    { {2, 0}, {2, 1}, {2, 2}, {2, 3}, {2, 4}, {2, 5} },

                    { {3, 0}, {3, 1}, {3, 2}, {3, 3}, {3, 4}, {3, 5} },

                    { {4, 0}, {4, 1}, {4, 2}, {4, 3}, {4, 4}, {4, 5} },

                    { {5, 0}, {5, 1}, {5, 2}, {5, 3}, {5, 4}, {5, 5} }


         */

       /*
            Test Board Layout:
            0 3 1 3 2 0
            0 0 0 0 2 0
            0 2 1 3 0 0
            0 0 1 3 0 0
            1 2 3 2 1 0
            0 0 0 0 0 _
         */

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
                        Log.d(DEBUG_TAG, "Verifying tile at position [" + myRow + ", " + currCol + "]");

                        gameTile currTile = (gameTile) _board.get(myRow).get(currCol);
                        if (currTile.getNumericValue() > 0) {
                            totalPoints += currTile.getNumericValue();
                        }
                        else {
                            totalBombs++;
                        }
                    } catch (Exception e) {
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



    /*

        Continuation of Second Fragment

     */
    private FragmentSecondBinding binding;

    private RecyclerView recyclerView;

    private final HashMap<Integer, Bitmap> imageTable = new HashMap<>();
    private final HashMap<colorTypes, int[]> colorTable = new HashMap<>();

    private ArrayList<ArrayList<Tile>> finalBoard;

    private static final Integer BOARD_SIZE = 5;
    private static final Integer TOTAL_SIZE = 6;



    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        imageTable.put(0, BitmapFactory.decodeResource(getResources(), R.drawable.voltorb));
        imageTable.put(1, BitmapFactory.decodeResource(getResources(), R.drawable.one));
        imageTable.put(2, BitmapFactory.decodeResource(getResources(), R.drawable.two));
        imageTable.put(3, BitmapFactory.decodeResource(getResources(), R.drawable.three));

        imageTable.put(4, BitmapFactory.decodeResource(getResources(), R.drawable.big_back_of_tile));

        imageTable.put(5, BitmapFactory.decodeResource(getResources(), R.drawable.voltorb_mini));

//        imageTable.put(-1, BitmapFactory.decodeResource(getResources(), R.drawable.nullImage));


        animationTable.put("points", new int[]{R.drawable.animation_points_0, R.drawable.animation_points_1, R.drawable.animation_points_2, R.drawable.animation_points_3});
        animationTable.put("explosion", new int[]{R.drawable.animation_explosion_0_final, R.drawable.animation_explosion_1_final, R.drawable.animation_explosion_2_final, R.drawable.animation_explosion_3_final,
                                                    R.drawable.animation_explosion_4_final, R.drawable.animation_explosion_5_final, R.drawable.animation_explosion_6_final, R.drawable.animation_explosion_7_final,
                                                    R.drawable.animation_explosion_8_final});


        colorTable.put(colorTypes.ROSE, new int[]{220, 117, 143});
        colorTable.put(colorTypes.MINT, new int[]{0, 204, 163});
        colorTable.put(colorTypes.LAKE_BLUE, new int[]{119, 141, 169});
        colorTable.put(colorTypes.WISTERA, new int[]{180, 160, 229});
        colorTable.put(colorTypes.GOLD, new int[]{243, 176, 43});


        createGrid(screenWidth);


        return binding.getRoot();
    }

    private void createGrid(int screenWidth) {


        int tileDimensions = screenWidth / 10;

        int boardSize = 0;
        finalBoard = new ArrayList<>(TOTAL_SIZE);

        for (int i = 0; i < TOTAL_SIZE; i++) {
            finalBoard.add(new ArrayList<>(TOTAL_SIZE));
            boardSize += TOTAL_SIZE;
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

        /*
            For debugging Purposes:
                int[][][] grid = {
                    // Row 0
                    { {0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5} },
                    // Row 1
                    { {1, 0}, {1, 1}, {1, 2}, {1, 3}, {1, 4}, {1, 5} },
                    // Row 2
                    { {2, 0}, {2, 1}, {2, 2}, {2, 3}, {2, 4}, {2, 5} },
                    // Row 3
                    { {3, 0}, {3, 1}, {3, 2}, {3, 3}, {3, 4}, {3, 5} },
                    // Row 4
                    { {4, 0}, {4, 1}, {4, 2}, {4, 3}, {4, 4}, {4, 5} },
                    // Row 5
                    { {5, 0}, {5, 1}, {5, 2}, {5, 3}, {5, 4}, {5, 5} }
                };

         */

        ArrayList<ArrayList<Integer>> testBoard = new ArrayList<>();
        testBoard.add(new ArrayList<>(Arrays.asList(0, 3, 1, 3, 2, 0)));
        testBoard.add(new ArrayList<>(Arrays.asList(0, 0, 0, 0, 2, 0)));
        testBoard.add(new ArrayList<>(Arrays.asList(0, 2, 1, 3, 0, 0)));
        testBoard.add(new ArrayList<>(Arrays.asList(0, 0, 1, 3, 0, 0)));
        testBoard.add(new ArrayList<>(Arrays.asList(1, 2, 3, 2, 1, 0)));
        testBoard.add(new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0)));

        // Create the grid
        int totalGameTileCount = 0;
        int totalInfoTileCount = 0;

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
                    totalInfoTileCount++;
                }
                else if (col == BOARD_SIZE) {
                    finalBoard.get(row).add(col,
                            new infoTile(
                                    new Pair<>(row, col),               // row, col
                                    tileDimensions, tileDimensions,     // width, height
                                    false                                // mark the column
                            ));
                    ((infoTile) finalBoard.get(row).get(col)).set_row_col(row, col);
                    totalInfoTileCount++;
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

                    totalGameTileCount++;
                }
            }
            //End of Creating the board
        }


            for (int row = 0; row < TOTAL_SIZE; row++) {
                for (int col = 0; col < TOTAL_SIZE; col++) {
                    if (row == BOARD_SIZE && col == BOARD_SIZE) {
                        continue;
                    }
                    if (finalBoard.get(row).get(col) instanceof infoTile) {
                        ((infoTile) finalBoard.get(row).get(col)).tally_points_bombs(finalBoard);
//                        Log.d("Total Points", String.valueOf(((infoTile) finalBoard.get(row).get(col)).getTotalPoints()));
                    }
                }
            }


        }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 6));

        // Make sure gameBoard is not null and has items

        if (finalBoard != null && !finalBoard.isEmpty()) {
          TileAdapter adapter = new TileAdapter(finalBoard);
          recyclerView.setAdapter(adapter);
        }

        else {

            // Log an error or handle it accordingly
            Log.e("SecondFragment", "Game board is empty or null.");

        }
        // Update the game Tiles
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {

                if ( finalBoard.get(row).get(col) instanceof gameTile) {
                    ( (gameTile) finalBoard.get(row).get(col)).update();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
package com.example.voltorbflipmobile;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

public class Tiles {
    public interface Tile {
        int getWidth();
        int getHeight();
        Pair<Integer, Integer> getPosition();
        Pair<Integer, Integer> getRowCol();

    }

    // ================================================================
    //                      Class gameTile
    // ================================================================

    public static class gameTile implements Tiles.Tile {
        Utilities.TileTypes value;
        Pair<Integer, Integer> position;
        Pair<Integer, Integer> row_col; // row index 0, col index 1
        int width, height;
        Float rotationAngle;
        boolean flipped, isFlipping = false;
        Bitmap frontImage, backImage;
        public int[] horizPipeColor, vertPipeColor;

        int[] animationFrames;

        gameTile(Utilities.TileTypes _value, Pair<Integer, Integer> _position, int _width, int _height) {
            this.value = _value;
            this.row_col = _position;
            this.width = _width;
            this.height = _height;
            backImage = Utilities.IMAGE_TABLE.get(4);
            horizPipeColor = new int[3];
            vertPipeColor = new int[3];
            setValueImage(value);
        }
        // Getters
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
        Utilities.TileTypes getType() {
            return value;
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


        void setValueImage(Utilities.TileTypes _value) {
            value = _value;

            switch (value){
                case VOLTORB:
                    frontImage = Utilities.IMAGE_TABLE.get(0);
                    break;
                case ONE:
                    frontImage = Utilities.IMAGE_TABLE.get(1);
                    break;
                case TWO:
                    frontImage = Utilities.IMAGE_TABLE.get(2);
                    break;
                case THREE:
                    frontImage = Utilities.IMAGE_TABLE.get(3);
                    break;
                default:
                    frontImage = Utilities.IMAGE_TABLE.get(4);
                    break;
            }

            if (value.ordinal() > 0) {
                animationFrames = Utilities.ANIMATION_TABLE.get("points");
            }
            else {
                animationFrames = Utilities.ANIMATION_TABLE.get("explosion");
            }

        }
        public int getHorizColor() {
            return Color.rgb(horizPipeColor[0], horizPipeColor[1], horizPipeColor[2]);
        }
        public int getVertColor() {
            return Color.rgb(vertPipeColor[0], vertPipeColor[1], vertPipeColor[2]);
        }


        void activateOverlayAnimation() {

        }











    }

    public static class infoTile implements Tiles.Tile {

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

            miniVoltorb = Utilities.IMAGE_TABLE.get(5);
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
                    tileColor = Utilities.COLOR_TABLE.get(Utilities.ColorTypes.GOLD);
                    break;
                case 1:
                    tileColor = Utilities.COLOR_TABLE.get(Utilities.ColorTypes.ROSE);
                    break;
                case 2:
                    tileColor = Utilities.COLOR_TABLE.get(Utilities.ColorTypes.MINT);
                    break;
                case 3:
                    tileColor = Utilities.COLOR_TABLE.get(Utilities.ColorTypes.TEAL);
                    break;
                default:
                    tileColor = Utilities.COLOR_TABLE.get(Utilities.ColorTypes.WISTERA);
                    break;
            }

        }

        int getTotalPoints() {
            return totalPoints;
        }

        int getTotalBombs() {
            return totalBombs;
        }

        void tally_points_bombs(ArrayList<ArrayList<Tiles.Tile>> _board) {
            if (_board.size() < Game_Manager.BOARD_SIZE || _board.get(0).size() < Game_Manager.BOARD_SIZE) {
                throw new IllegalArgumentException("Board size is smaller than expected.");
            }

            if (markCol) {
                int myCol = row_col.second;
                for (int currRow = 0; currRow < Game_Manager.BOARD_SIZE; currRow++) {
                    try {
                        Tiles.gameTile currTile = (Tiles.gameTile) _board.get(currRow).get(myCol);
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
                for (int currCol = 0; currCol < Game_Manager.BOARD_SIZE; currCol++) {
                    try {

                        Tiles.gameTile currTile = (Tiles.gameTile) _board.get(myRow).get(currCol);
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

        public Utilities.ColorTypes getColorType() {
            return Utilities.ColorTypes.values()[row_col.first - 1];
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




}

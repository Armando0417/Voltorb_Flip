package com.example.voltorbflipmobile;

import android.os.CountDownTimer;

import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;

import com.example.voltorbflipmobile.Tiles.*;

public class Game_Manager {
    public static final int BOARD_SIZE = 6;
    // ================================================================
    //                     Board State Class
    // ================================================================

    public class Board {
        public static final int BOARD_SIZE = 6;
        public static final int GRID_SIZE = 5;
        public static final int MAX_TILE_VALUE = 4;

        ArrayList<ArrayList<Tile>> tiles;
        ArrayList<Tiles.Tile> flattenedBoard;

        HashMap<Utilities.TileTypes, Integer> tileFrequencies;

        boolean boardLost = false;
        boolean boardCompleted = false;


        public Board() {
            tileFrequencies = new HashMap<>();
            flattenedBoard = new ArrayList<>();
            tiles = new ArrayList<>();

            generateNewGrid();
            populateInfoTiles();
            countBoard();
            flattenBoard();
        }

        private void flattenBoard() {
            for (ArrayList<Tile> row : tiles) {
                for (Tile curr : row) {
                    if (curr != null) {
                        flattenedBoard.add(curr);
                    }
                }
            }
        }

        public ArrayList<Tile> getFlattenedBoard() {
            return flattenedBoard;
        }
        public ArrayList<ArrayList<Tile>> getBoardAsList() {
            return tiles;
        }



        private void createGameBoard(ArrayList<ArrayList<Integer>> initialGridValues) {

            int tileSize = screenWidth / 10;

            this.tiles = new ArrayList<>(BOARD_SIZE);
            for (int i = 0; i < BOARD_SIZE; i++) {
                this.tiles.add(new ArrayList<>(BOARD_SIZE));
            }

            // Create the board
            for (int rowIndex = 0; rowIndex < BOARD_SIZE; rowIndex++) {
                for (int columnIndex = 0; columnIndex < BOARD_SIZE; columnIndex++) {

                    if (rowIndex == GRID_SIZE && columnIndex == GRID_SIZE) {
                        continue;
                    }

                    if (rowIndex == GRID_SIZE) {
                        this.tiles.get(rowIndex).add(columnIndex,
                                (Tile) new infoTile(
                                       new Pair<>(rowIndex, columnIndex),               // rowIndex, columnIndex
                                        tileSize, tileSize,     // width, height
                                       true                               // mark the column
                               ));
                        ((Tiles.infoTile) this.tiles.get(rowIndex).get(columnIndex)).set_row_col(rowIndex, columnIndex);
                    }
                    else if (columnIndex == GRID_SIZE) {
                        this.tiles.get(rowIndex).add(columnIndex,
                                (Tile) new infoTile(
                                        new Pair<>(rowIndex, columnIndex),               // rowIndex, columnIndex
                                        tileSize, tileSize,     // width, height
                                        false                                // mark the column
                                ));
                        ((Tiles.infoTile) this.tiles.get(rowIndex).get(columnIndex)).set_row_col(rowIndex, columnIndex);
                    }
                    else {
                        this.tiles.get(rowIndex).add(columnIndex,
                                (Tile) new Tiles.gameTile(
                                        Utilities.TileTypes.values()[initialGridValues.get(rowIndex).get(columnIndex)],    // Tile type
                                        new Pair<>(rowIndex, columnIndex),                                       // rowIndex & columnIndex
                                        tileSize, tileSize                              // width & height
                                ));

                        Tiles.gameTile currentTile = (Tiles.gameTile) this.tiles.get(rowIndex).get(columnIndex);

                        currentTile.setValueImage(Utilities.TileTypes.values()[initialGridValues.get(rowIndex).get(columnIndex)]);
                    }
                }
            }
            populateInfoTiles();
            Log.d(Utilities.DEBUG_TAG, "Finished Create grid with a size of " + this.tiles.size() + "x" + this.tiles.get(0).size());

        }

        private void populateInfoTiles() {
            for (int rowIndex = 0; rowIndex < BOARD_SIZE; rowIndex++) {
                for (int columnIndex = 0; columnIndex < BOARD_SIZE; columnIndex++) {
                    if (rowIndex == GRID_SIZE && columnIndex == GRID_SIZE) {
                        continue;
                    }
                    if (this.tiles.get(rowIndex).get(columnIndex) instanceof Tiles.infoTile) {
                        ((Tiles.infoTile) this.tiles.get(rowIndex).get(columnIndex)).tally_points_bombs(this.tiles);
                    }
                }
            }
        }

        public void countBoard() {
            for (ArrayList<Tiles.Tile> row : tiles) {
                for (Tiles.Tile tile : row) {
                    if (tile instanceof Tiles.gameTile) {
                        Tiles.gameTile currTile = (Tiles.gameTile) tile;
                        tileFrequencies.merge(currTile.getType(), 1, Integer::sum);
//                        tileFrequencies.compute(currTile.getType(), (k, v) -> v == null ? 1 : v + 1);
                    }
                }
            }
        }


        private void generateNewGrid() {
            ArrayList<ArrayList<Integer>> initialGridValues = new ArrayList<>();

            for (int rowIndex = 0; rowIndex < BOARD_SIZE; rowIndex++) {
                ArrayList<Integer> newRow = new ArrayList<>();

                for (int columnIndex = 0; columnIndex < BOARD_SIZE; columnIndex++) {
                    if (rowIndex == GRID_SIZE && columnIndex == GRID_SIZE)
                        continue;

                    Random randomGenerator = new Random();

                    int randomTileValue = randomGenerator.nextInt(MAX_TILE_VALUE);

                    newRow.add(randomTileValue);

                    if (rowIndex == GRID_SIZE || columnIndex == GRID_SIZE)
                        newRow.set(columnIndex, 0);
                }
                initialGridValues.add(newRow);
            }

            initialGridValues = verifyGrid(initialGridValues);
            Log.d(Utilities.DEBUG_TAG, initialGridValues.toString());
            createGameBoard(initialGridValues);

        }


        public void updateBoard(int row, int col) {
            Tiles.gameTile currTile = Objects.requireNonNull((Tiles.gameTile) this.tiles.get(row).get(col));

            if (currTile.getNumericValue() > 0
                    && Objects.requireNonNull( tileFrequencies.get(currTile.getType()) )  > 0) {

//                tileFrequencies.compute(currTile.getType(), (k, v) -> v == null ? -1 : v - 1);
                tileFrequencies.merge(currTile.getType(), 1, Integer::sum);

                if (Objects.requireNonNull( tileFrequencies.get(currTile.getType()) ) == -1 )
                    Utilities.logError("Tile Counter for " + currTile.getType().toString() + " is -1! ");

                updateScore (currTile.getNumericValue() );
            }

            else {
                Utilities.delayedHandler(
                        () -> boardLost = true
                , 500);
            }

        }

        public boolean verifyWin() {
            int totalTwo = Objects.requireNonNull(tileFrequencies.get(Utilities.TileTypes.TWO));
            int totalThree = Objects.requireNonNull(tileFrequencies.get(Utilities.TileTypes.THREE));

            return totalTwo == 0 && totalThree == 0;
        }



        private ArrayList<ArrayList<Integer>> verifyGrid(ArrayList<ArrayList<Integer>> initialGridValues) {
            ArrayList<ArrayList<Integer>> validGrid = new ArrayList<>();

            HashMap<Integer, Integer> totalTileFrequencies = new HashMap<>();
            Queue<Integer> rowsToRegen = new LinkedList<>();



            for (int rowIndex = 0; rowIndex < GRID_SIZE; rowIndex++) {
                HashMap<Integer, Integer> rowTileFrequencies = new HashMap<>();

                for (int columnIndex = 0; columnIndex < GRID_SIZE; columnIndex++) {

                    rowTileFrequencies.merge(initialGridValues.get(rowIndex).get(columnIndex), 1, Integer::sum);
                    totalTileFrequencies.merge(initialGridValues.get(rowIndex).get(columnIndex), 1, Integer::sum);

                }
                if (rowTileFrequencies.getOrDefault(0, 0) > 4 ||
                        rowTileFrequencies.getOrDefault(1, 0) > 4 ||
                        rowTileFrequencies.getOrDefault(2, 0) > 4 ||
                        rowTileFrequencies.getOrDefault(3, 0) > 4) {
                    rowsToRegen.add(rowIndex);
                }

            }
            if (!rowsToRegen.isEmpty()) {
                validGrid = regenerateInvalidRows(initialGridValues, rowsToRegen);
            }
            else {
                validGrid = initialGridValues;
            }
            return validGrid;
        }

        private ArrayList<ArrayList<Integer>> regenerateInvalidRows(ArrayList<ArrayList<Integer>> initialGridValues, Queue<Integer> rowsToRegen) {
            while (!rowsToRegen.isEmpty()) {
                int currentRow = Objects.requireNonNull(rowsToRegen.poll());

                for (int cellIndex = 0; cellIndex < BOARD_SIZE; cellIndex++) {
                    if (cellIndex == GRID_SIZE) {
                        initialGridValues.get(currentRow).set(cellIndex, 0);
                    }
                    else {
                        Random randomGenerator = new Random();
                        int randomTileValue = randomGenerator.nextInt(MAX_TILE_VALUE);
                        initialGridValues.get(currentRow).set(cellIndex, randomTileValue);
                    }
                }
            }
            return initialGridValues;
        }
    }


//============================================================================================================


    // ================================================================
    //                     Game Manager Methods
    // ================================================================


    static Board gameBoard;
    HashMap<Utilities.TileTypes, Integer> tileCounter;

    static HashMap<Integer, TextView> scoreMap;
    static String scoreText;
    static int currScore;
    public View game_boardView;
    public final int screenWidth;

    private static CountDownTimer countDownTimer;
    public static boolean isTimerRunning = false;
    public static boolean isLosingState = false;
    public static boolean isWinningState = false;

    public static void startCountdownTimer() {
        countDownTimer = new CountDownTimer(350, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(Utilities.DEBUG_TAG, "Timer Ticked");
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
            }
        };
        isTimerRunning = true;
        countDownTimer.start();
    }

    private void cancelCountdownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public static boolean isTimerRunning() {
        return isTimerRunning;
    }

    public Game_Manager(View _gboard, int _screenWidth) {
        this.game_boardView = _gboard;
        scoreMap = new HashMap<>();
        scoreText = "";
        currScore = 0;
        this.screenWidth = _screenWidth;
        gameBoard = new Board();
        loadScoreViews();

    }

    public void resetBoard() {
        scoreText = "";
        currScore = 0;
        gameBoard = new Board();
    }

    public Board getGameBoard() {
        return gameBoard;
    }


//    public static ArrayList<ArrayList<Integer>> generateNewBoard() {
//
//        ArrayList<ArrayList<Integer>> newBoard = new ArrayList<>();
//
//        final int BOARD_SIZE = 6;
//
//        for (int row = 0; row < BOARD_SIZE; row++) {
//            ArrayList<Integer> newRow = new ArrayList<>();
//
//            for (int col = 0; col < BOARD_SIZE; col++) {
//                if (row == 5 && col == 5)
//                    continue;
//
//                Random RandomNumGenerator = new Random();
//
//                int randomVal = RandomNumGenerator.nextInt(4);
//
//                newRow.add(randomVal);
//
//                if (row == 5 || col == 5)
//                    newRow.set(col, 0);
//            }
//            newBoard.add(newRow);
//        }
//
//        return new ArrayList<>(verifyBoard(newBoard));
//
//    }
//
//    private static ArrayList<ArrayList<Integer>> verifyBoard(ArrayList<ArrayList<Integer>> newBoard) {
//        ArrayList<ArrayList<Integer>> fixedBoard = new ArrayList<>();
//
//        HashMap<Integer, Integer> freqBoard = new HashMap<>();
//        Queue<Integer> rowsToCheck = new LinkedList<>();
//
//        final int GridSize = 5;
//
//        for (int row = 0; row < GridSize; row++) {
//            HashMap<Integer, Integer> freqRow = new HashMap<>();
//
//            for (int col = 0; col < GridSize; col++) {
//                if (freqRow.get(newBoard.get(row).get(col)) != null) {
//                    freqRow.compute(newBoard.get(row).get(col), (k, v) -> v == null ? 1 : v + 1);
//                }
//                if (freqBoard.get(newBoard.get(row).get(col)) != null) {
//                    freqBoard.compute(newBoard.get(row).get(col), (k, v) -> v == null ? 1 : v + 1);
//                }
//                freqRow.putIfAbsent(newBoard.get(row).get(col), 1);
//                freqBoard.putIfAbsent(newBoard.get(row).get(col), 1);
//
//
//
//            }
//            if (freqRow.getOrDefault(0, 0) > 4 ||
//                    freqRow.getOrDefault(1, 0) > 4 ||
//                    freqRow.getOrDefault(2, 0) > 4 ||
//                    freqRow.getOrDefault(3, 0) > 4) {
//                rowsToCheck.add(row);
//            }
//
//
//        }
//        fixedBoard = regenerateBoard(newBoard, rowsToCheck);
//
//        return fixedBoard;
//    }
//
//    private static ArrayList<ArrayList<Integer>> regenerateBoard(ArrayList<ArrayList<Integer>> newBoard, Queue<Integer> rowsToCheck) {
//        ArrayList<ArrayList<Integer>> fixedBoard = new ArrayList<>();
//        if (rowsToCheck.isEmpty()) {
//            fixedBoard = newBoard;
//            return fixedBoard;
//        }
//        while (!rowsToCheck.isEmpty()) {
//            int currRow = Objects.requireNonNull(rowsToCheck.poll());
//
//            for (int cell = 0; cell < 6; cell++) {
//                if (cell == 5) {
//                    fixedBoard.get(currRow).set(cell, 0);
//                }
//                else {
//                    Random RandomNumGenerator = new Random();
//                    int randomVal = RandomNumGenerator.nextInt(4);
//                    fixedBoard.get(currRow).set(cell, randomVal);
//                }
//            }
//        }
//        return fixedBoard;
//    }

    public void loadScoreViews() {
        try {
            scoreMap.put(0, game_boardView.findViewById(R.id.score10k));
            scoreMap.put(1, game_boardView.findViewById(R.id.score1k));
            scoreMap.put(2, game_boardView.findViewById(R.id.score100));
            scoreMap.put(3, game_boardView.findViewById(R.id.score10s));
            scoreMap.put(4, game_boardView.findViewById(R.id.score1s));
        }
        catch (Exception e) {
            Log.d(Utilities.ERROR_TAG, "Loaded wrong");
        }

    }

    public void prepareNumber(int num) {
        currScore = num * currScore;
        if (currScore == 0) {
            currScore = num;
        }

        scoreText = String.valueOf(currScore);

        StringBuilder strBuilder = new StringBuilder(scoreText);

        if (scoreText.length() < 5) {
            while (strBuilder.length() < 5) {
                strBuilder.insert(0, "0");
            }
            scoreText = strBuilder.toString();
        }
        if (scoreText.length() >= 6)
            scoreText = "99999";

    }


//    public void countBoard() {
//        for (ArrayList<SecondFragment.Tile> row : board) {
//            for (SecondFragment.Tile tile : row) {
//                if (tile instanceof SecondFragment.gameTile) {
//                    SecondFragment.gameTile currTile = (SecondFragment.gameTile) tile;
//
//                    tileCounter.compute(currTile.getType(), (k, v) -> v == null ? 1 : v + 1);
//                }
//            }
//        }
//    }

//    public void updateBoard(SecondFragment.gameTile currTile) {
//        if (currTile == null) {
//            Log.d(SecondFragment.ERROR_TAG, "Tile couldn't be updated since it doesn't exist");
//            return;
//        }
//
//        if (currTile.getNumericValue() > 0
//                && Objects.requireNonNull( tileCounter.get(currTile.getType()) )  > 0)
//        {
//            tileCounter.compute(currTile.getType(), (k, v) -> v == null ? -1 : v - 1);
//
//            if (Objects.requireNonNull( tileCounter.get(currTile.getType()) ) == -1 )
//                Utilities.logError("Tile Counter for " + currTile.getType().toString() + " is -1! ");
//
//            prepareNumber(currTile.getNumericValue());
//
//            for (int i = 0; i < 5; i++) {
//                int finalI = i;
//                Utilities.tryCatch(
//                    () -> {
//                            TextView currDigit = Objects.requireNonNull ( (TextView) scoreMap.get(finalI) );
//                            currDigit.setText(String.valueOf(scoreText.charAt(finalI)));
//                        },
//                    Handlers.NULL_POINTER_EXCEPTION
//                );
//
//            }
//        }
//    }

    private void updateScore(Integer numericValue) {
        prepareNumber(numericValue);

        for (int i = 0; i < 5; i++) {
            int finalI = i;
            Utilities.tryCatch(
                    () -> {
                        TextView currDigit = Objects.requireNonNull ( (TextView) scoreMap.get(finalI) );
                        currDigit.setText(String.valueOf(scoreText.charAt(finalI)));
                    },
                    Handlers.NULL_POINTER_EXCEPTION
            );

        }


    }


//    public Boolean verifyLoss(SecondFragment.gameTile currTile) {
//        if (currTile == null) {
//            Log.d(SecondFragment.ERROR_TAG, "Tile couldn't be extracted since it doesn't exist");
//            return false;
//        }
//        return currTile.getNumericValue() == 0;
//    }
//
//
//
//    public Boolean verifyWin() {
//        int totalTwo = Objects.requireNonNull(tileCounter.get(SecondFragment.tileTypes.TWO));
//        int totalThree = Objects.requireNonNull(tileCounter.get(SecondFragment.tileTypes.THREE));
//
//        isWinningState = totalTwo == 0 && totalThree == 0;
//
//        return isWinningState;
//    }


}

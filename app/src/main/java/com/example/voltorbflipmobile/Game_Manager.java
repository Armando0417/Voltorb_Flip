package com.example.voltorbflipmobile;

import static java.lang.Math.max;

import android.os.CountDownTimer;

import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;

import com.example.voltorbflipmobile.Tiles.*;

public class Game_Manager {


    //Region DEBUG BOARD:
    boolean useDebugBoard = false;


    // ================================================================
    //                     Board State Class
    // ================================================================

    public class Board {
        public static final int BOARD_SIZE = 6;
        public static final int GRID_SIZE = 5;
        public static final int MAX_TILE_VALUE = 4;
        private int currentLevel = -1;

        ArrayList<ArrayList<Tile>> tiles;

        ArrayList<Tiles.Tile> flattenedBoard;

        HashMap<Utilities.TileTypes, Integer> tileFrequencies;
        HashMap<Integer, Integer> initialWeights;
        ArrayList<Pair<Integer, Double> > cumulative_probabilities;

        boolean boardLost = false;
        boolean boardCompleted = false;


        public Board(int level) {
            tileFrequencies = new HashMap<>();
            flattenedBoard = new ArrayList<>();
            initialWeights = new HashMap<>();

            tiles = new ArrayList<>();
            cumulative_probabilities = new ArrayList<>();

            boardLost = false;
            boardCompleted = false;

            currentLevel = level;

            prepareWeights();
            generateWeightedGrid();
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


        private void prepareWeights() {
            if (currentLevel < 0) {
                Utilities.logError( "Level was not loaded properly ");
                return;
            }
            initialWeights.put ( 0,        5 + currentLevel * 2            );
            initialWeights.put ( 1,        max(16 - currentLevel * 2, 8)   );
            initialWeights.put ( 2,        max(6 - currentLevel, 2)        );
            initialWeights.put ( 3,        max(3 - currentLevel, 2)        );

            HashMap<Integer, Double> normalizedWeights = new HashMap<>( normalizeWeights(initialWeights) );
            cumulative_probabilities = generate_cumulative_probabilities(normalizedWeights);
            return;
        }

        private ArrayList< Pair<Integer, Double> > generate_cumulative_probabilities(HashMap<Integer, Double> normalized) {
            ArrayList<Pair<Integer, Double> > cumulative_probabilities = new ArrayList<>();
            double cumulative_prob = 0;
            for (Map.Entry<Integer, Double> pair : normalized.entrySet()) {
                cumulative_prob += pair.getValue();
                cumulative_probabilities.add( new Pair<>(pair.getKey(), cumulative_prob) );
            }
            return cumulative_probabilities;
        }

        private HashMap<Integer, Double> normalizeWeights(HashMap<Integer, Integer> weights) {
            HashMap<Integer, Double> normalizedWeights = new HashMap<>();
            double total = 0;
            for (Map.Entry<Integer, Integer> pair : weights.entrySet()) {
                total += pair.getValue();
            }
            for (HashMap.Entry<Integer, Integer> pair : weights.entrySet()) {
                normalizedWeights.put(pair.getKey(), pair.getValue() / total);
            }
            return normalizedWeights;
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

                    if ( (rowIndex == GRID_SIZE) && (columnIndex == GRID_SIZE) ) {
                        continue;
                    }

                    if (rowIndex == GRID_SIZE) {
                        this.tiles.get(rowIndex).add(columnIndex,
                                (Tile) new infoTile(
                                       new Pair<>(rowIndex, columnIndex),               // rowIndex, columnIndex
                                        tileSize, tileSize,                             // width, height
                                       true                                             // mark the column
                               ));
                        ((Tiles.infoTile) this.tiles.get(rowIndex).get(columnIndex)).set_row_col(rowIndex, columnIndex);
                    }
                    else if (columnIndex == GRID_SIZE) {
                        this.tiles.get(rowIndex).add(columnIndex,
                                (Tile) new infoTile(
                                        new Pair<>(rowIndex, columnIndex),                  // rowIndex, columnIndex
                                        tileSize, tileSize,                                 // width, height
                                        false                                               // mark the column
                                ));
                        ((Tiles.infoTile) this.tiles.get(rowIndex).get(columnIndex)).set_row_col(rowIndex, columnIndex);
                    }
                    else {
                        this.tiles.get(rowIndex).add(columnIndex,
                                (Tile) new Tiles.gameTile(
                                        Utilities.TileTypes.values()[initialGridValues.get(rowIndex).get(columnIndex)],     // Tile type
                                        new Pair<>(rowIndex, columnIndex),                                                  // rowIndex & columnIndex
                                        tileSize, tileSize                                                                  // width & height
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
            tileFrequencies.put(Utilities.TileTypes.VOLTORB, 0);
            tileFrequencies.put(Utilities.TileTypes.ONE, 0);
            tileFrequencies.put(Utilities.TileTypes.TWO, 0);
            tileFrequencies.put(Utilities.TileTypes.THREE, 0);


            for (ArrayList<Tiles.Tile> row : tiles) {
                for (Tiles.Tile tile : row) {
                    if (tile instanceof Tiles.gameTile) {
                        Tiles.gameTile currTile = (Tiles.gameTile) tile;
                        tileFrequencies.merge(currTile.getType(), 1, Integer::sum);
                    }
                }
            }
        }


        private void generateNewGrid() {
            ArrayList<ArrayList<Integer>> initialGridValues = new ArrayList<>();

            for (int rowIndex = 0; rowIndex < BOARD_SIZE; rowIndex++) {
                final ArrayList<Integer> newRow = getNewRow(rowIndex);
                initialGridValues.add(newRow);
            }

            initialGridValues = verifyGrid(initialGridValues);

            if (useDebugBoard) 
                createGameBoard(testBoardGenerator());

            else
                createGameBoard(initialGridValues);
        }

        private void generateWeightedGrid() {
            ArrayList<ArrayList<Integer>> initialGridValues = new ArrayList<>();

            for (int rowIndex = 0; rowIndex < BOARD_SIZE; rowIndex++) {
                final ArrayList<Integer> newRow = getWeightedRow(rowIndex);
                initialGridValues.add(newRow);
            }

            if (useDebugBoard)
                createGameBoard(testBoardGenerator());

            else
                createGameBoard(initialGridValues);
        }


        @NonNull
        private ArrayList<Integer> getNewRow(int rowIndex) {
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
            return newRow;
        }
        private ArrayList<Integer> getWeightedRow(int rowIndex) {
            ArrayList<Integer> newRow = new ArrayList<>();

            for (int columnIndex = 0; columnIndex < BOARD_SIZE; columnIndex++) {
                if (rowIndex == GRID_SIZE && columnIndex == GRID_SIZE)
                    continue;

                Random randomGenerator = new Random();

                int randomTileValue = pickValue( randomGenerator.nextDouble() );

                if (randomTileValue == -1 ){
                    newRow.add(0);
                }
                newRow.add(randomTileValue);



                if (rowIndex == GRID_SIZE || columnIndex == GRID_SIZE)
                    newRow.set(columnIndex, 0);
            }
            return newRow;
        }

        private Integer pickValue (Double randomValue) {
            for (Pair< Integer, Double> pair : cumulative_probabilities) {
                if (randomValue <= pair.second) {
                    return pair.first;
                }
            }
            return -1;
        }



        private ArrayList<ArrayList<Integer>> testBoardGenerator() {

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


        public void updateBoard(int row, int col) {
            Tiles.gameTile currTile = Objects.requireNonNull((Tiles.gameTile) this.tiles.get(row).get(col));

            if (currTile.getNumericValue() > 0
                    && Objects.requireNonNull( tileFrequencies.get(currTile.getType()) )  > 0) {

                tileFrequencies.merge(currTile.getType(), 1, (key, decrease) -> key - decrease);

                if (Objects.requireNonNull( tileFrequencies.get(currTile.getType()) ) == -1 )
                    Utilities.logError("Tile Counter for " + currTile.getType().toString() + " is -1! ");

                updateScore (currTile.getNumericValue() );
                Utilities.delayedHandler( () -> {
                    boardCompleted = verifyWin();
                }, 200);
            }

            else {
                Utilities.delayedHandler(
                        () -> boardLost = true
                , 100);
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
                if (    rowTileFrequencies.getOrDefault(0, 0) > 4 ||
                        rowTileFrequencies.getOrDefault(1, 0) > 4 ||
                        rowTileFrequencies.getOrDefault(2, 0) > 4 ||
                        rowTileFrequencies.getOrDefault(3, 0) > 4)
                {
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

    static HashMap<Integer, TextView> scoreMap;
    static String scoreText;
    static int currScore;
    public View score_boardView;
    public final int screenWidth;
    public static final int BOARD_SIZE = 6;

    public static boolean isWinningState = false;

    public static boolean isInteractionAllowed = true;

    public static boolean gameFinishedState = false;

    public int levelNumber = 1;
    public boolean scoreUpdated = false;

    public Game_Manager(View _score_board, int _screenWidth) {
        this.score_boardView = _score_board;
        scoreMap = new HashMap<>();

        levelNumber = 1;
        scoreText = "";
        currScore = 0;
        this.screenWidth = _screenWidth;

        gameBoard = new Board(levelNumber);
        isInteractionAllowed = true;
        loadScoreViews();

    }

    public void levelUp() {
        levelNumber++;
    }

    public void resetBoard() {
        scoreText = "";
        currScore = 0;
        gameBoard = new Board(levelNumber);
        printCurrentBoard();
        isInteractionAllowed = true;
        scoreMap.forEach( (key, value) -> {
            value.setText("0");
        });
    }

    public Board getGameBoard() {
        return gameBoard;
    }
    public boolean isScoreUpdated() {
        return scoreUpdated;
    }
    public int getCurrentLevel() {
        return levelNumber;
    }

    public static void printCurrentBoard() {
        for (ArrayList<Tiles.Tile> row : gameBoard.tiles) {
            StringBuilder currRow = new StringBuilder();

            currRow.append(" [");

            for (Tiles.Tile cell : row) {
                if (cell instanceof Tiles.gameTile) {
                    Tiles.gameTile currTile = (Tiles.gameTile) cell;
                    currRow.append(currTile.getNumericValue());
                }
                else {
                    currRow.append("X");
                }
                currRow.append(", ");
            }
            currRow.delete(currRow.length() - 2, currRow.length());
            currRow.append("]");

            Log.d(Utilities.DEBUG_TAG, currRow.toString());
        }
    }


public void loadScoreViews() {

        Utilities.tryCatch( () -> {
            scoreMap.put(0, score_boardView.findViewById(R.id.score10k));
            scoreMap.put(1, score_boardView.findViewById(R.id.score1k));
            scoreMap.put(2, score_boardView.findViewById(R.id.score100));
            scoreMap.put(3, score_boardView.findViewById(R.id.score10s));
            scoreMap.put(4, score_boardView.findViewById(R.id.score1s));
        },
            Handlers.GENERAL_EXCEPTION);

    }

    public void prepareNumber(int num) {
        int prevScore = currScore;
        currScore = num * currScore;

        if (currScore == 0) {
            currScore = num;
        }
        scoreUpdated = currScore != prevScore;

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

    public boolean verifyLoss() {
        return gameBoard.boardLost;
    }
    public boolean verifyWin() {
        return gameBoard.boardCompleted;
    }

}

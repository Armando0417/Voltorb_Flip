package com.example.voltorbflipmobile;

import android.os.CountDownTimer;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Game_Manager {

    ArrayList<ArrayList<SecondFragment.Tile>> board;
    HashMap<SecondFragment.tileTypes, Integer> tileCounter;

    HashMap<Integer, TextView> scoreMap;
    String scoreText;
    int currScore;

    public View game_board;

    private static CountDownTimer countDownTimer;
    public static boolean isTimerRunning = false;
    public static boolean isLosingState = false;
    public static boolean isWinningState = false;

    public static void startCountdownTimer() {
        countDownTimer = new CountDownTimer(350, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(SecondFragment.DEBUG_TAG, "Timer Ticked");
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



    public Game_Manager(ArrayList<ArrayList<SecondFragment.Tile>> _board, View _gboard) {
        this.board = _board;
        this.game_board = _gboard;
        this.tileCounter = new HashMap<>();
        this.scoreMap = new HashMap<>();
        this.scoreText = "";
        this.currScore = 0;

        countBoard();
        loadScoreViews();

    }

    public void loadScoreViews() {
        try {
            scoreMap.put(0, game_board.findViewById(R.id.score10k));
            scoreMap.put(1, game_board.findViewById(R.id.score1k));
            scoreMap.put(2, game_board.findViewById(R.id.score100));
            scoreMap.put(3, game_board.findViewById(R.id.score10s));
            scoreMap.put(4, game_board.findViewById(R.id.score1s));
        }
        catch (Exception e) {
            Log.d(SecondFragment.ERROR_TAG, "Loaded wrong");
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


    public void countBoard() {
        for (ArrayList<SecondFragment.Tile> row : board) {
            for (SecondFragment.Tile tile : row) {
                if (tile instanceof SecondFragment.gameTile) {
                    SecondFragment.gameTile currTile = (SecondFragment.gameTile) tile;

                    tileCounter.compute(currTile.getType(), (k, v) -> v == null ? 1 : v + 1);
                }
            }
        }
    }

    public void updateBoard(SecondFragment.gameTile currTile) {
        if (currTile == null) {
            Log.d(SecondFragment.ERROR_TAG, "Tile couldn't be updated since it doesn't exist");
            return;
        }

        if (currTile.getNumericValue() > 0
                && Objects.requireNonNull( tileCounter.get(currTile.getType()) )  > 0)
        {
            tileCounter.compute(currTile.getType(), (k, v) -> v == null ? -1 : v - 1);

            if (Objects.requireNonNull( tileCounter.get(currTile.getType()) ) == -1 )
                Utilities.logError("Tile Counter for " + currTile.getType().toString() + " is -1! ");

            prepareNumber(currTile.getNumericValue());

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
    }


    public static Boolean verifyLoss(SecondFragment.gameTile currTile) {
        if (currTile == null) {
            Log.d(SecondFragment.ERROR_TAG, "Tile couldn't be extracted since it doesn't exist");
            return false;
        }
        return currTile.getNumericValue() == 0;
    }

    public Boolean verifyWin() {
        int totalTwo = Objects.requireNonNull(tileCounter.get(SecondFragment.tileTypes.TWO));
        int totalThree = Objects.requireNonNull(tileCounter.get(SecondFragment.tileTypes.THREE));

        isWinningState = totalTwo == 0 && totalThree == 0;

        return isWinningState;
    }


}

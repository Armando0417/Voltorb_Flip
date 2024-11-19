package com.example.voltorbflipmobile;

import android.os.CountDownTimer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
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
    public boolean isWinningState = false;

    public static void startCountdownTimer() {
        countDownTimer = new CountDownTimer(500, 1000) {
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


    private void prepareNumber(int num) {

        currScore = num * currScore;
        if (currScore == 0) {
            currScore = num;
        }

        scoreText = String.valueOf(currScore);

        if (scoreText.length() < 5) {
            while (scoreText.length() < 5) {
                scoreText = "0" + scoreText;
            }
        }
        Log.d(SecondFragment.DEBUG_TAG, "Final Number is: " + scoreText);
    }


    public void countBoard() {
        for (ArrayList<SecondFragment.Tile> row : board) {
            for (SecondFragment.Tile tile : row) {
                if (tile instanceof SecondFragment.gameTile) {
                    SecondFragment.gameTile currTile = (SecondFragment.gameTile) tile;

                    // Increment the count for the tile type in tileCounter
                    tileCounter.put(currTile.getType(), tileCounter.getOrDefault(currTile.getType(), 0) + 1);
                }
            }
        }
    }

    public void updateBoard(SecondFragment.gameTile currTile) {
        if (currTile == null) {
            Log.d(SecondFragment.ERROR_TAG, "Tile couldn't be updated since it doesn't exist");
            return;
        }

        if (currTile.getNumericValue() > 0 && tileCounter.getOrDefault(currTile.getType(), 0) > 0) {
            tileCounter.put(currTile.getType(), tileCounter.get(currTile.getType()) - 1);


            prepareNumber(currTile.getNumericValue());

            for (int i = 0; i < 5; i++) {
                try {
                    TextView currDigit = (TextView) scoreMap.get(i);
                    currDigit.setText(String.valueOf(scoreText.charAt(i)));
//                    scoreMap.get(i).setText(scoreText.charAt(i));
                }
                catch (Exception e) {
                    Log.d(SecondFragment.ERROR_TAG, "Uhhh we passed it wrong");
                    Log.d(SecondFragment.ERROR_TAG, e.getMessage());
                }
            }

        }
    }





    public static Boolean verifyLoss(SecondFragment.gameTile currTile) {
        if (currTile == null) {
            Log.d(SecondFragment.ERROR_TAG, "Tile couldn't be extracted since it doesn't exist");
            return false;
        }
        if (currTile.getNumericValue() == 0) {
            return true;
        }
        return false;
    }

    public Boolean verifyWin() {
        return (tileCounter.getOrDefault(SecondFragment.tileTypes.ONE, 0) == 0)
                && (tileCounter.getOrDefault(SecondFragment.tileTypes.TWO, 0) == 0)
                && (tileCounter.getOrDefault(SecondFragment.tileTypes.THREE, 0) == 0);
    }


}

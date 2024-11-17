package com.example.voltorbflipmobile;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Game_Manager {

    ArrayList<ArrayList<SecondFragment.Tile>> board;
    HashMap<SecondFragment.tileTypes, Integer> tileCounter;

    public Game_Manager(ArrayList<ArrayList<SecondFragment.Tile>> _board) {
        this.board = _board;
        this.tileCounter = new HashMap<>();
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
        // Use currTile.getType() as the key, not currTile itself
        if (currTile.getNumericValue() > 0 && tileCounter.getOrDefault(currTile.getType(), 0) > 0) {
            tileCounter.put(currTile.getType(), tileCounter.get(currTile.getType()) - 1);
        }
    }

    public Boolean verifyLoss(SecondFragment.gameTile currTile) {
        if (currTile == null) {
            Log.d(SecondFragment.ERROR_TAG, "Tile couldn't be extracted since it doesn't exist");
            return false;
        }
        return currTile.getNumericValue() == 0;
    }

    public Boolean verifyWin() {
        return (tileCounter.getOrDefault(SecondFragment.tileTypes.ONE, 0) == 0)
                && (tileCounter.getOrDefault(SecondFragment.tileTypes.TWO, 0) == 0)
                && (tileCounter.getOrDefault(SecondFragment.tileTypes.THREE, 0) == 0);
    }

    public void showTileCount() {
        for (Map.Entry<SecondFragment.tileTypes, Integer> entry : tileCounter.entrySet()) {
                Log.d("Tile Count", entry.getKey() + ": " + entry.getValue());
        }
    }
}

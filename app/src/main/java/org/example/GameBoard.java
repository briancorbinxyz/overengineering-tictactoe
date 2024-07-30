package org.example;

public interface GameBoard {

    String toString();

    boolean isValidMove(int location);

    boolean hasChain(String playerMarker);

    boolean hasMovesAvailable();

    GameBoard withMove(String playerMarker, int location);

    int getDimension();

    String[] getContent();
}

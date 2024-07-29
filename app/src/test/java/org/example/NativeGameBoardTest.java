package org.example;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.invoke.MethodHandles;

import org.testng.annotations.Test;

public class NativeGameBoardTest {

    private static final Logger log = System.getLogger(MethodHandles.lookup().lookupClass().getName());

    @Test public void should_load_library() {
        printSysemProperties();
        GameBoard gameBoard = new NativeGameBoard();
    }

	private void printSysemProperties() {
        System.getProperties().forEach((k, v) -> log.log(Level.INFO, k + " = " + v));
	}
}

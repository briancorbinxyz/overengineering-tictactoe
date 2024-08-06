package org.example;

import static org.testng.Assert.assertNotNull;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.invoke.MethodHandles;
import org.testng.annotations.Test;

public class GameBoardNativeImplTest {

    private static final Logger log =
            System.getLogger(MethodHandles.lookup().lookupClass().getName());

    @Test
    public void should_load_library() {
        printEnvironmentVariables();
        printSystemProperties();
        GameBoard gameBoard = new GameBoardNativeImpl();
        assertNotNull(gameBoard);
    }

    private void printSystemProperties() {
        System.getProperties().forEach((k, v) -> log.log(Level.INFO, k + " = " + v));
    }

    private void printEnvironmentVariables() {
        System.getenv().forEach((k, v) -> log.log(Level.INFO, k + " = " + v));
    }
}

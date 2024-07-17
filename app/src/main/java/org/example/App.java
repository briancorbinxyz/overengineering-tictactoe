/*
 * This source file was generated by the Gradle 'init' task
 */
package org.example;

import java.io.File;

/**
 * A simple java tic-tac-toe game.
 */
public class App {
    
    private final Game game;

    /**
     * Constructs a new instance of the App class with a default Game instance.
     */
    public App() {
        this.game = new Game();
    }

    /**
     * Constructs a new instance of the App class with a Game instance loaded from the specified file.
     *
     * @param gameFile the file containing the saved game state to load
     * @throws Exception if there is an error loading the game state from the file
     */
    public App(File gameFile) throws Exception {
        this.game = Game.from(gameFile);
    }

    /**
     * Runs the game.
     * @throws Exception
     */
    public void run() throws Exception {
        game.play();
    }

    /**
     * Returns a greeting message for the Tic-Tac-Toe game.
     * @return the greeting message
     */
    public String getGreeting() {
        return "Welcome to Tic-Tac-Toe!";
    }

    /**
     * The main entry point for the Tic-Tac-Toe application.
     * 
     * If command-line arguments are provided, it will load a saved game state from the specified file.
     * Otherwise, it will start a new game.
     */
    public static void main(String[] args) throws Exception {
        App app;
        if (args.length > 0) {
            app = new App(new File(args[0]));
        } else {
            app = new App();
        }
        System.out.println(app.getGreeting());
        app.run();
    }
}

/*
 * This source file was generated by the Gradle 'init' task
 */
package org.example;

import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import org.example.bot.BotStrategy;
import org.example.bot.BotStrategyConfig;

/** A simple java tic-tac-toe game. */
public class App {

  private static final Logger log = System.getLogger(App.class.getName());

  /**
   * Runs the game.
   *
   * @throws Exception if there is an error whilst playing the game
   */
  public void run() throws Exception {
    var game = newStandardGameMCTS();
    game.play();
    game.close();
  }

  private Game newStandardGame() {
    return new Game(
        3,
        false,
        new PlayerNode.Local<>("X", new HumanPlayer()),
        new PlayerNode.Local<>("O", new BotPlayer(BotStrategy.ALPHABETA)));
  }

  private Game newStandardGameMaxN() {
    return new Game(
        3,
        false,
        new PlayerNode.Local<>("X", new HumanPlayer()),
        new PlayerNode.Local<>("O", new BotPlayer(BotStrategy.MAXN)));
  }

  private Game newStandardGameParanoid() {
    return new Game(
        3,
        false,
        new PlayerNode.Local<>("X", new HumanPlayer()),
        new PlayerNode.Local<>("O", new BotPlayer(BotStrategy.PARANOID)));
  }

  private Game newStandardGameMCTS() {
    return new Game(
        3,
        false,
        new PlayerNode.Local<>("X", new HumanPlayer()),
        new PlayerNode.Local<>("O", new BotPlayer(BotStrategy.MCTS)));
  }

  private Game newLargeStandardGame() {
    return new Game(
        4,
        false,
        new PlayerNode.Local<>("X", new HumanPlayer()),
        new PlayerNode.Local<>(
            "O",
            new BotPlayer(
                BotStrategy.alphabeta(BotStrategyConfig.newBuilder().maxDepth(4).build()))));
  }

  private Game newMultiplayerGameMCTS() {
    return new Game(
        10,
        false,
        new PlayerNode.Local<>("X", new HumanPlayer()),
        new PlayerNode.Local<>("O", new BotPlayer(BotStrategy.MCTS)),
        new PlayerNode.Local<>("Y", new BotPlayer(BotStrategy.MCTS)));
  }

  private Game newMultiplayerGameMaxN() {
    // slow!
    return new Game(
        5,
        false,
        new PlayerNode.Local<>("X", new HumanPlayer()),
        new PlayerNode.Local<>(
            "O",
            new BotPlayer(BotStrategy.maxn(BotStrategyConfig.newBuilder().maxDepth(3).build()))),
        new PlayerNode.Local<>(
            "Y",
            new BotPlayer(BotStrategy.maxn(BotStrategyConfig.newBuilder().maxDepth(3).build()))));
  }

  private Game newMultiplayerGameParanoid() {
    // slow!
    return new Game(
        10,
        false,
        new PlayerNode.Local<>("X", new HumanPlayer()),
        new PlayerNode.Local<>(
            "O",
            new BotPlayer(
                BotStrategy.paranoid(BotStrategyConfig.newBuilder().maxDepth(2).build()))),
        new PlayerNode.Local<>(
            "Y",
            new BotPlayer(
                BotStrategy.paranoid(BotStrategyConfig.newBuilder().maxDepth(2).build()))));
  }

  /**
   * Runs the game from the specified file.
   *
   * @param gameFile the file containing the saved game state to load
   * @throws Exception if there is an error whilst playing the game or loading the game state from
   *     the file
   */
  public void runFrom(File gameFile) throws Exception {
    var game = Game.from(gameFile);
    game.play();
  }

  /**
   * Returns a greeting message for the Tic-Tac-Toe game.
   *
   * @return the greeting message
   */
  public String getGreeting() {
    return "Welcome to Tic-Tac-Toe!";
  }

  /**
   * The main entry point for the Tic-Tac-Toe application.
   *
   * <p>If command-line arguments are provided, it will load a saved game state from the specified
   * file. Otherwise, it will start a new game.
   */
  public static void main(String[] args) throws Exception {
    App app = new App();
    log.log(Level.INFO, () -> app.getGreeting());
    if (args.length > 0) {
      app.runFrom(new File(args[0]));
    } else {
      app.run();
    }
  }
}

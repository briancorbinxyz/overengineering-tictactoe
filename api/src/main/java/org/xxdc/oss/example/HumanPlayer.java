package org.xxdc.oss.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Scanner;

/**
 * Represents a human player in the game. The human player interacts with the game by providing
 * their next move through the console.
 */
public record HumanPlayer() implements Player, Serializable {

  private static final Logger log = System.getLogger(HumanPlayer.class.getName());

  private static final long serialVersionUID = 1L;

  @Override
  public int nextMove(GameState state) {
    int move;
    var io = System.console() != null ? new ConsoleInput() : new ScannerInput();
    do {
      log.log(
          Level.INFO,
          "Player {0}: choose an available location from [0-{1}]: ",
          state.currentPlayer(),
          (state.board().dimension() * state.board().dimension() - 1));
      try {
        var msg = io.readLine();
        move = Integer.parseInt(msg);
      } catch (NumberFormatException e) {
        // expected if user enters non-integer carry on.
        log.log(Level.TRACE, "Invalid location: {0}", e.getMessage(), e);
        move = -1;
      }
    } while (!state.board().isValidMove(move));
    return move;
  }

  /** Represents an input source that can be used to read a line of text. */
  public static sealed interface Input {
    /**
     * Reads a line of text from the input source.
     *
     * @return the line of text read from the input source.
     */
    String readLine();
  }

  /**
   * An implementation of the {@link Input} interface that reads a line of text from the system
   * console.
   */
  static final class ConsoleInput implements Input {
    @Override
    public String readLine() {
      return System.console().readLine();
    }
  }

  /**
   * An implementation of the {@link Input} interface that reads a line of text from the system
   * input stream, using a {@link Scanner} that closes the underlying input stream when the JVM
   * thread exits. This is useful for ensuring that the input stream is properly closed, even if an
   * exception is thrown or the method is interrupted.
   */
  static final class ScannerInput implements Input {
    @Override
    public String readLine() {
      try (Scanner scanner = new Scanner(new CloseOnExitInputStream(System.in))) {
        return scanner.nextLine();
      }
    }
  }

  /**
   * An {@link InputStream} implementation that closes the underlying input stream when the JVM
   * thread exits. This is useful for ensuring that the input stream is properly closed, even if an
   * exception is thrown or the method is interrupted.
   */
  static class CloseOnExitInputStream extends InputStream {

    private final InputStream in;

    /**
     * An {@link InputStream} implementation that closes the underlying input stream when the JVM
     * thread exits. This is useful for ensuring that the input stream is properly closed, even if
     * an exception is thrown or the method is interrupted.
     */
    public CloseOnExitInputStream(InputStream in) {
      this.in = in;
    }

    @Override
    public void close() {
      // no op
    }

    @Override
    public int read() throws IOException {
      return in.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      return in.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
      return in.read(b);
    }
  }
}

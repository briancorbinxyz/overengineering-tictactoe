package org.xxdc.oss.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * Provides methods for saving and loading a {@link Game} object to/from a file.
 *
 * <p>The {@link #saveTo(File, Game)} method writes the provided {@link Game} object to the
 * specified file. The {@link #loadFrom(File)} method reads a {@link Game} object from the specified
 * file and returns it.
 *
 * <p>The {@link GamePersistenceFilter} class is used to filter the objects that can be loaded from
 * the file, rejecting any loaded classes with more than 1000 object references to prevent
 * deserialization attacks.
 */
public class GamePersistence {

  private static final Logger log = System.getLogger(GamePersistence.class.getName());

  /**
   * Saves the provided {@link Game} object to the specified file.
   *
   * @param gameFile the file to save the game state to
   * @param game the {@link Game} object to save
   * @throws IOException if an I/O error occurs while writing the game state to the file
   */
  public void saveTo(File gameFile, Game game) throws IOException {
    try (FileOutputStream os = new FileOutputStream(gameFile);
        ObjectOutputStream o = new ObjectOutputStream(os)) {
      o.writeObject(game);
    }
    log.log(Level.DEBUG, "Saved to game state to: {0}", gameFile);
  }

  /**
   * Reads a {@link Game} object from the specified file and returns it.
   *
   * @param gameFile the file to load the game state from
   * @return the {@link Game} object read from the file
   * @throws IOException if an I/O error occurs while reading the game state from the file
   * @throws ClassNotFoundException if the {@link Game} class cannot be found
   */
  public Game loadFrom(File gameFile) throws IOException, ClassNotFoundException {
    try (FileInputStream is = new FileInputStream(gameFile);
        ObjectInputStream o = new ObjectInputStream(is)) {
      o.setObjectInputFilter(new GamePersistenceFilter());
      return Game.class.cast(o.readObject());
    }
  }

  /**
   * A filter that rejects any loaded classes with more than 1000 object references to prevent
   * deserialization attacks.
   */
  private static class GamePersistenceFilter implements ObjectInputFilter {
    // OVER-ENGINEER Reject any loaded classes games > 1000 object references
    private static final long MAX_REFERENCES = 1000;

    /**
     * Checks the input and returns a status indicating whether the input should be allowed or
     * rejected.
     *
     * @param filterInfo the input filter information
     * @return the status of the input, either {@link Status#ALLOWED}, {@link Status#REJECTED}, or
     *     {@link Status#UNDECIDED}
     */
    public Status checkInput(FilterInfo filterInfo) {
      return switch (filterInfo) {
        case FilterInfo fi when fi.references() > MAX_REFERENCES -> Status.REJECTED;
        case FilterInfo fi when fi.serialClass() != null -> Status.ALLOWED;
        default -> Status.UNDECIDED;
      };
    }
  }
}

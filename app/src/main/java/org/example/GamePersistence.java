package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

    public void saveTo(File gameFile, Game game) throws IOException {
        try (FileOutputStream os = new FileOutputStream(gameFile);
                ObjectOutputStream o = new ObjectOutputStream(os)) {
            o.writeObject(game);
        }
        System.out.println("[Saved to " + gameFile + "]");
    }

    public Game loadFrom(File gameFile) throws IOException, ClassNotFoundException {
        try (FileInputStream is = new FileInputStream(gameFile);
                ObjectInputStream o = new ObjectInputStream(is)) {
            o.setObjectInputFilter(new GamePersistenceFilter());
            return Game.class.cast(o.readObject());
        }
    }

    private static class GamePersistenceFilter implements ObjectInputFilter {
        // OVER-ENGINEER Reject any loaded classes games > 1000 object references
        private static final long MAX_REFERENCES = 1000;

        public Status checkInput(FilterInfo filterInfo) {
            return switch (filterInfo) {
                case FilterInfo fi when fi.references() > MAX_REFERENCES -> Status.REJECTED;
                case FilterInfo fi when fi.serialClass() != null -> Status.ALLOWED;
                default -> Status.UNDECIDED;
            };
        }
    }
}

package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GamePersistence {

    public void saveTo(File gameFile, Game game) throws IOException {
        try (
            FileOutputStream os = new FileOutputStream(gameFile);
            ObjectOutputStream o = new ObjectOutputStream(os)
            ) {
            o.writeObject(game);
        }
        System.out.println("[Saved to " + gameFile + "]");
    }

    public Game loadFrom(File gameFile) throws IOException, ClassNotFoundException {
        try (
            FileInputStream is = new FileInputStream(gameFile);
            ObjectInputStream o = new ObjectInputStream(is)
        ) {
            o.setObjectInputFilter(new GamePersistenceFilter());
            return Game.class.cast(o.readObject()); 
        }
    }

    private static class GamePersistenceFilter implements ObjectInputFilter {

        // OVER-ENGINEER Reject any loaded classes games > 1000 object references
        private static final long MAX_REFERENCES = 1000;

        public Status checkInput(FilterInfo filterInfo) {
            if (filterInfo.references() > MAX_REFERENCES) {
                return Status.REJECTED;
            }
            if (null != filterInfo.serialClass()) {
                return Status.ALLOWED;
            }
            return Status.UNDECIDED;
        }

    }

}

package org.xxdc.oss.example.interop;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.Cleaner;
import org.xxdc.oss.example.GameBoard;
import org.xxdc.oss.example.interop.loader.NativeLoader;

/**
 * Provides a Java wrapper around a native TicTacToe library. This class handles loading the native
 * library, initializing the required methods, and creating instances of {@link GameBoard} that
 * represent the game state.
 */
public final class TicTacToeLibrary {

  private static final Logger log =
      System.getLogger(MethodHandles.lookup().lookupClass().getName());

  static final String LIBRARY_NAME = "xxdc_oss_tictactoe";

  private final Arena arena = Arena.ofAuto();

  private final Linker linker = Linker.nativeLinker();

  private final Cleaner cleaner = Cleaner.create();

  private SymbolLookup libTicTacToe;

  private MethodHandle version;
  private MethodHandle versionString;

  /** Initializes the native TicTacToe library and prepares it for use. */
  public TicTacToeLibrary() {
    initLibrary();
  }

  /**
   * Creates a new {@link GameBoard} instance with the specified dimension.
   *
   * @param dimension the dimension of the game board (e.g. 3 for a 3x3 board)
   * @return a new {@link GameBoard} instance representing the game board
   */
  public GameBoard newGameBoard(int dimension) {
    return new TicTacToeGameBoard(dimension, libTicTacToe, cleaner);
  }

  private void initLibrary() {
    try {
      libTicTacToe = NativeLoader.loadLibrary(LIBRARY_NAME, arena);
      initLibraryMethods();
      logVersion(version);
      logVersionString(versionString);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private void initLibraryMethods() {
    version =
        libTicTacToe
            .find("version")
            .map(
                m ->
                    linker.downcallHandle(
                        m,
                        FunctionDescriptor.of(
                            ValueLayout.JAVA_LONG,
                            new MemoryLayout[] {ValueLayout.ADDRESS, ValueLayout.JAVA_LONG})))
            .orElseThrow(() -> new IllegalStateException("Unable to find method version"));
    versionString =
        libTicTacToe
            .find("version_string")
            .map(m -> linker.downcallHandle(m, FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)))
            .orElseThrow(() -> new IllegalStateException("Unable to find method version_string"));
  }

  private void logVersionString(MethodHandle versionString)
      throws NoSuchMethodException, IllegalAccessException, Throwable {
    // Create a method handle for our local callback
    MethodHandle callback =
        MethodHandles.lookup()
            .findStatic(
                TicTacToeLibrary.class,
                "logVersionString",
                MethodType.methodType(void.class, MemorySegment.class, int.class));
    FunctionDescriptor callbackDesc =
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
    MemorySegment callbackStub = linker.upcallStub(callback, callbackDesc, arena);
    versionString.invokeExact(callbackStub);
  }

  @SuppressWarnings("unused")
  private static void logVersionString(MemorySegment version, int length) {
    MemorySegment ptr = version.reinterpret(length);
    log.log(Level.DEBUG, "Version = {0}", ptr.getString(0));
  }

  private void logVersion(MethodHandle version) throws Throwable {
    // First, call with null pointer to get required length
    long requiredLength = (long) version.invoke(MemorySegment.NULL, 0L);
    if (requiredLength <= 0) {
      throw new RuntimeException("Failed to get required buffer length");
    }

    MemorySegment buffer = arena.allocate(requiredLength);
    long written = (long) version.invoke(buffer, requiredLength);
    if (written < 0) {
      throw new RuntimeException("Buffer too small");
    } else if (written != requiredLength) {
      throw new RuntimeException("Unexpected number of bytes written");
    }
    log.log(Level.DEBUG, "Version = {0}", buffer.getString(0));
  }
}

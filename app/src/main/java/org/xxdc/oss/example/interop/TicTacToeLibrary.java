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

public final class TicTacToeLibrary {

  private static final Logger log =
      System.getLogger(MethodHandles.lookup().lookupClass().getName());

  static final String LIBRARY_NAME = "tictactoe";

  private final Arena arena = Arena.ofAuto();

  private final Linker linker = Linker.nativeLinker();

  private final Cleaner cleaner = Cleaner.create();

  private SymbolLookup libTicTacToe;

  private MethodHandle version;
  private MethodHandle versionString;

  public TicTacToeLibrary() {
    initLibrary();
  }

  public GameBoard newGameBoard(int dimension) {
    return new TicTacToeGameBoard(dimension, libTicTacToe, cleaner);
  }

  private String platformLibraryName() {
    return System.mapLibraryName(LIBRARY_NAME);
  }

  private void initLibrary() {
    libTicTacToe = SymbolLookup.libraryLookup(platformLibraryName(), arena);
    initLibraryMethods();
    try {
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

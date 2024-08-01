package org.example;

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

/**
 * Implements the GameBoard interface using a native library. This class manages the lifetime of the
 * native library resources and provides methods to interact with the native library.
 */
public class GameBoardNativeImpl implements GameBoard {

    // Bound to GameBoardNativeImpl lifetime, Not explicitly closeable, Accessible from any thread
    // https://docs.oracle.com/en%2Fjava%2Fjavase%2F22%2Fdocs%2Fapi%2F%2F/java.base/java/lang/foreign/Arena.html
    private static final Logger log =
            System.getLogger(MethodHandles.lookup().lookupClass().getName());

    private final TicTacToeLibrary library;

    public GameBoardNativeImpl() {
        library = new TicTacToeLibrary();
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toString'");
    }

    @Override
    public boolean isValidMove(int location) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isValidMove'");
    }

    @Override
    public boolean hasChain(String playerMarker) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasChain'");
    }

    @Override
    public boolean hasMovesAvailable() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasMovesAvailable'");
    }

    @Override
    public GameBoard withMove(String playerMarker, int location) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withMove'");
    }

    @Override
    public int getDimension() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDimension'");
    }

	@Override
	public String asJsonString() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'asJsonString'");
	}

    private static final class TicTacToeLibrary {

        static final String LIBRARY_NAME = "tictactoe";

        private final Arena arena = Arena.ofAuto();

        private final Linker linker = Linker.nativeLinker();

        public TicTacToeLibrary() {
            initLibrary();
        }

        private String platformLibraryName() {
            return System.mapLibraryName("tictactoe");
        }

        private void initLibrary() {
            var libTicTacToe = SymbolLookup.libraryLookup(platformLibraryName(), arena);
            var version =
                    foreignMethod(
                            libTicTacToe,
                            "version",
                            ValueLayout.JAVA_LONG,
                            ValueLayout.ADDRESS,
                            ValueLayout.JAVA_LONG);
            var versionString =
                    foreignMethod(
                            libTicTacToe,
                            "versionString",
                            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
            try {
                logVersion(version);
                logVersionString(versionString);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        private void logVersionString(MethodHandle versionString)
                throws NoSuchMethodException, IllegalAccessException, Throwable {
            // Create a method handle for our local callback
            MethodHandle callback =
                    MethodHandles.lookup()
                            .findStatic(
                                    GameBoardNativeImpl.class,
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
            log.log(Level.INFO, "Version = {0}", ptr.getString(0));
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
            log.log(Level.INFO, "Version = {0}", buffer.getString(0));
        }

        private MethodHandle foreignMethod(
                SymbolLookup library, String methodName, FunctionDescriptor methodSig) {
            return library.find(methodName)
                    .map(
                            methodAddress -> {
                                return linker.downcallHandle(methodAddress, methodSig);
                            })
                    .orElseThrow(
                            () -> new IllegalStateException("Unable to find method " + methodName));
        }

        private MethodHandle foreignMethod(
                SymbolLookup library,
                String methodName,
                MemoryLayout returnType,
                MemoryLayout... parameterTypes) {
            return library.find(methodName)
                    .map(
                            methodAddress -> {
                                var methodSignature = FunctionDescriptor.of(returnType, parameterTypes);
                                return linker.downcallHandle(methodAddress, methodSignature);
                            })
                    .orElseThrow(
                            () -> new IllegalStateException("Unable to find method " + methodName));
        }

    }
}

package org.xxdc.oss.example.interop.loader;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;
import java.nio.file.Files;

/**
 * Provides a utility to load a native library from the classpath and return a {@link SymbolLookup} for accessing its symbols.
 * The library is first extracted to a temporary file before being loaded.
 */
public class NativeLoader {

  private NativeLoader() {}

  /**
   * Loads a native library from the classpath and returns a {@link SymbolLookup} for accessing its symbols.
   * The library is first extracted to a temporary file before being loaded.
   *
   * @param libraryName the name of the native library to load
   * @param arena the {@link Arena} to use for the symbol lookup
   * @return a {@link SymbolLookup} for the loaded native library
   * @throws IllegalArgumentException if an {@link IOException} occurs while extracting the library to a temporary file
   */
  public static SymbolLookup loadLibrary(String libraryName, Arena arena) {
    try {
      var platformLibraryName = platformLibraryName(libraryName);
      return fromClassPathToTemp(platformLibraryName, arena);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static String platformLibraryName(String libraryName) {
    return System.mapLibraryName(libraryName);
  }

  private static SymbolLookup fromClassPathToTemp(String platformLibraryName, Arena arena)
      throws IOException {
    var resourceLookupString = "/" + platformLibraryName;
    var tempResourceFile = Files.createTempFile(platformLibraryName, ".tmp");
    try (var inputStream =
            NativeLoader.class.getResourceAsStream(resourceLookupString);
        OutputStream outputStream = Files.newOutputStream(tempResourceFile)) {
      inputStream.transferTo(outputStream);
      return SymbolLookup.libraryLookup(tempResourceFile, arena);
    }
  }
}

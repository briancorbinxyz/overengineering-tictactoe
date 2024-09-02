package org.xxdc.oss.example.interop.loader;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;
import java.nio.file.Files;

public class NativeLoader {

  public static String NATIVE_ROOT = "/native/";

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
    var resourceLookupString = NATIVE_ROOT + platformLibraryName;
    var tempResourceFile = Files.createTempFile(platformLibraryName, ".tmp");
    try (var inputStream = NativeLoader.class.getResourceAsStream(resourceLookupString);
        OutputStream outputStream = Files.newOutputStream(tempResourceFile)) {
      inputStream.transferTo(outputStream);
      return SymbolLookup.libraryLookup(tempResourceFile, arena);
    }
  }
}

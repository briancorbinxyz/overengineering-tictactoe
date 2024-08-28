package org.xxdc.oss.example;

/// This interface defines a contract for classes that can be serialized to JSON strings.
/// Any class that implements this interface must provide an implementation of the `asJsonString()`
/// method, which returns the JSON representation of the object.
public interface JsonSerializable {

  /// Returns the JSON representation of the object.
  /// @return the JSON string representation of the object.
  String asJsonString();
}

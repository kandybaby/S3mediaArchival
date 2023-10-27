package com.example.mediaarchival.deserializers;

import com.example.mediaarchival.enums.ArchivedStatus;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

/**
 * Custom deserializer for converting JSON strings into {@link ArchivedStatus} enums.
 */
public class ArchivedStatusDeserializer extends JsonDeserializer<ArchivedStatus> {

  /**
   * Deserializes the JSON string to an {@link ArchivedStatus} enum.
   *
   * @param p the JsonParser
   * @param ctxt the DeserializationContext
   * @return the deserialized {@link ArchivedStatus} enum
   * @throws IOException if an input/output error occurs
   */
  @Override
  public ArchivedStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String value = p.readValueAs(String.class);
    try {
      return ArchivedStatus.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException ex) {
      return ArchivedStatus.NOT_ARCHIVED;
    }
  }
}

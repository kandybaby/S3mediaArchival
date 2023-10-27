package com.example.mediaarchival.deserializers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.example.mediaarchival.enums.ArchivedStatus;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ArchivedStatusDeserializerTest {

  private ArchivedStatusDeserializer deserializer;

  @Mock private JsonParser jsonParser;

  @Mock private DeserializationContext deserializationContext;

  private ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    deserializer = new ArchivedStatusDeserializer();
    objectMapper = new ObjectMapper();
  }

  @Test
  public void ArchivedStatusDeserializer_testDeserialize_ValidValue() throws IOException {
    // Mock the JSON parser to provide a valid string value
    when(jsonParser.readValueAs(String.class)).thenReturn("ARCHIVED");

    ArchivedStatus result = deserializer.deserialize(jsonParser, deserializationContext);

    assertEquals(ArchivedStatus.ARCHIVED, result);
  }

  @Test
  public void ArchivedStatusDeserializer_testDeserialize_InvalidValue() throws IOException {
    // Mock the JSON parser to provide an invalid string value
    when(jsonParser.readValueAs(String.class)).thenReturn("INVALID_TYPE");

    ArchivedStatus result = deserializer.deserialize(jsonParser, deserializationContext);

    // Ensure that it falls back to the default value (OTHER in this case)
    assertEquals(ArchivedStatus.NOT_ARCHIVED, result);
  }
}

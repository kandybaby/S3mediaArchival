package com.example.mediaarchival.deserializers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.example.mediaarchival.enums.MediaCategory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MediaCategoryDeserializerTest {

  private MediaCategoryDeserializer deserializer;

  @Mock private JsonParser jsonParser;

  @Mock private DeserializationContext deserializationContext;

  private ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    deserializer = new MediaCategoryDeserializer();
    objectMapper = new ObjectMapper();
  }

  @Test
  public void MediaCategoryDeserializer_testDeserialize_ValidValue() throws IOException {
    when(jsonParser.readValueAs(String.class)).thenReturn("TV");

    MediaCategory result = deserializer.deserialize(jsonParser, deserializationContext);

    assertEquals(MediaCategory.TV, result);
  }

  @Test
  public void MediaCategoryDeserializer_testDeserialize_InvalidValue() throws IOException {
    when(jsonParser.readValueAs(String.class)).thenReturn("INVALID_TYPE");

    MediaCategory result = deserializer.deserialize(jsonParser, deserializationContext);

    assertEquals(MediaCategory.OTHER, result);
  }
}

package com.example.mediaarchival.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.example.mediaarchival.enums.ArchivedStatus;
import com.example.mediaarchival.enums.MediaCategory;
import com.example.mediaarchival.models.LibraryModel;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.LibraryRepository;
import com.example.mediaarchival.repositories.MediaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.StorageClass;

public class LibraryControllerTest {
  private MockMvc mockMvc;

  @Mock private LibraryRepository libraryRepository;

  @Mock private MediaRepository mediaRepository;

  @Mock private S3Client s3Client;

  @Mock private JmsTemplate jmsTemplate;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    LibraryController libraryController = new LibraryController(libraryRepository, mediaRepository, s3Client, jmsTemplate);
    mockMvc = MockMvcBuilders.standaloneSetup(libraryController).build();
  }

  @Test
  void testGetAllLibraries() throws Exception {
    // Arrange
    List<LibraryModel> libraries = new ArrayList<>();
    libraries.add(new LibraryModel());
    libraries.add(new LibraryModel());
    when(libraryRepository.findAll()).thenReturn(libraries);

    // Act
    RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/libraries");
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    verify(libraryRepository, times(1)).findAll();
  }

  @Test
  void testGetLibraryById() throws Exception {
    // Arrange
    Long libraryId = 1L;
    LibraryModel library = new LibraryModel();
    when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));

    // Act
    RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/libraries/{id}", libraryId);
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    verify(libraryRepository, times(1)).findById(libraryId);
  }

  @Test
  void testGetLibraryById_NotFound() throws Exception {
    Long libraryId = 999L; // Non-existent ID
    when(libraryRepository.findById(libraryId)).thenReturn(Optional.empty());

    RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/libraries/{id}", libraryId);
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }


  @Test
  void testCreateLibrary() throws Exception {
    // Arrange
    LibraryModel library = new LibraryModel();
    library.setName("Test Library");
    library.setPath("/test/path");
    library.setCategory(MediaCategory.TV);
    library.setBucketName("Bucket");
    library.setStorageClass(StorageClass.STANDARD);
    when(libraryRepository.save(any(LibraryModel.class))).thenReturn(library);

    ObjectMapper objectMapper = new ObjectMapper();
    String requestBody = objectMapper.writeValueAsString(library);

    // Act
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/api/libraries")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

    MvcResult result = mockMvc.perform(requestBuilder).andReturn();
    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.CREATED.value());
    verify(libraryRepository, times(1)).save(any(LibraryModel.class));
  }

  @Test
  void testCreateLibrary_DuplicateNameOrPath() throws Exception {
    LibraryModel library = new LibraryModel();
    library.setName("Existing Library");
    library.setPath("/existing/path");
    when(libraryRepository.findByName(library.getName())).thenReturn(Optional.of(library));
    when(libraryRepository.findByPath(library.getPath())).thenReturn(Optional.of(library));

    ObjectMapper objectMapper = new ObjectMapper();
    String requestBody = objectMapper.writeValueAsString(library);

    RequestBuilder requestBuilder =
            MockMvcRequestBuilders.post("/api/libraries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody);

    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void testDeleteLibrary_NotFound() throws Exception {
    Long libraryId = 999L; // Non-existent ID
    when(libraryRepository.findById(libraryId)).thenReturn(Optional.empty());

    RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/api/libraries/{id}", libraryId);
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }


  @Test
  void testDeleteLibrary() throws Exception {
    // Arrange
    Long libraryId = 1L;
    LibraryModel existingLibrary = new LibraryModel();
    existingLibrary.setId(libraryId);
    when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(existingLibrary));

    // Act
    RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/api/libraries/{id}", libraryId);
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    verify(libraryRepository, times(1)).findById(libraryId);
    verify(mediaRepository, times(1)).findByLibraryId(libraryId);
    verify(mediaRepository, times(1)).deleteAll(anyList());
    verify(libraryRepository, times(1)).delete(existingLibrary);
  }

  @Test
  void testScanLibrary_NotFound() throws Exception {
    Long libraryId = 999L; // Non-existent ID
    when(libraryRepository.findById(libraryId)).thenReturn(Optional.empty());

    RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/libraries/{id}/scan", libraryId);
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void testScanLibrary_Successful() throws Exception {
    Long libraryId = 1L;
    LibraryModel library = new LibraryModel();
    library.setId(libraryId);
    when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));
    when(mediaRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());

    RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/libraries/{id}/scan", libraryId);
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    verify(libraryRepository, times(1)).findById(libraryId);
    verify(jmsTemplate, times(1)).convertAndSend(eq("libraryScanQueue"), eq(libraryId));
  }

  @Test
  void testSynchronizeMedia_NotFound() throws Exception {
    Long libraryId = 999L; // Non-existent ID
    when(libraryRepository.findById(libraryId)).thenReturn(Optional.empty());

    RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/libraries/{id}/synchronize", libraryId);
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void testSynchronizeMedia_Successful() throws Exception {
    Long libraryId = 1L;
    LibraryModel library = new LibraryModel();
    library.setId(libraryId);
    when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));
    when(mediaRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());

    RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/libraries/{id}/synchronize", libraryId);
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    verify(libraryRepository, times(1)).findById(libraryId);
    verify(jmsTemplate, times(1)).convertAndSend(eq("librarySyncQueue"), eq(libraryId));
  }




  @Test
  void testArchiveLibraryMediaObjects() throws Exception {
    // Arrange
    Long libraryId = 1L;
    MediaModel media1 = new MediaModel();
    media1.setPath("test1");
    MediaModel media2 = new MediaModel();
    media2.setPath("test2");

    List<MediaModel> mediaObjects = Arrays.asList(media1, media2);

    when(mediaRepository.findByLibraryIdAndArchivedStatusIn(eq(libraryId), anyList()))
        .thenReturn(mediaObjects);

    // Act
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/api/libraries/{id}/archive", libraryId);
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    verify(mediaRepository, times(1)).findByLibraryIdAndArchivedStatusIn(eq(libraryId), anyList());
    verify(mediaRepository, times(2)).save(any(MediaModel.class)); // Two media objects are saved
    verify(jmsTemplate, times(2))
        .convertAndSend(
            eq("archivingQueue"), any(String.class)); // Two media objects are sent to the queue
  }
}

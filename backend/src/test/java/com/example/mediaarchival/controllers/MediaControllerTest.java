package com.example.mediaarchival.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatReflectiveOperationException;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mediaarchival.models.LibraryModel;
import com.example.mediaarchival.models.MediaModel;
import com.example.mediaarchival.repositories.MediaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import software.amazon.awssdk.services.s3.model.StorageClass;

public class MediaControllerTest {
  private MockMvc mockMvc;

  @Mock private MediaRepository mediaRepository;

  @Mock private JmsTemplate jmsTemplate;

  private MediaController mediaController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    mediaController = new MediaController(mediaRepository, jmsTemplate);
    mockMvc = MockMvcBuilders.standaloneSetup(mediaController).build();
  }

  @Test
  void testGetAllMedias_noErrors() throws Exception {
    // Arrange
    List<MediaModel> mediaList = new ArrayList<>();
    mediaList.add(new MediaModel());
    mediaList.add(new MediaModel());
    // Mock Page for paging parameters
    Page<MediaModel> page = new PageImpl<>(mediaList);

    when(mediaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

    // Act
    RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/media-objects");
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    verify(mediaRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  void testGetAllMedias_WithParameters() throws Exception {
    // Arrange
    Page<MediaModel> page = new PageImpl<>(new ArrayList<>());
    when(mediaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

    // Act
    RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/media-objects")
            .param("archivedStatus", "ARCHIVED")
            .param("search", "test")
            .param("libraryId", "1")
            .param("isRecovering", "true")
            .param("isArchiving", "false")
            .param("page", "1")
            .param("size", "5")
            .param("sortBy", "name")
            .param("sortDirection", "desc");
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    verify(mediaRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
  }


  @Test
  void testGetMediaById_ExistentId() throws Exception {
    // Arrange
    Long mediaId = 1L;
    MediaModel media = new MediaModel();
    when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(media));

    // Act
    RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/media-objects/{id}", mediaId);
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    verify(mediaRepository, times(1)).findById(mediaId);
  }

  @Test
  void testGetMediaById_NonExistentId() throws Exception {
    // Arrange
    Long nonExistentId = 999L;
    when(mediaRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act
    RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/media-objects/{id}", nonExistentId);
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    verify(mediaRepository, times(1)).findById(nonExistentId);
  }

  @Test
  void testDeleteMedia_ExistentId() throws Exception {
    // Arrange
    Long mediaId = 1L;
    MediaModel existingMedia = new MediaModel();
    existingMedia.setId(mediaId);
    when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(existingMedia));

    // Act
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.delete("/api/media-objects/{id}", mediaId);
    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    verify(mediaRepository, times(1)).findById(mediaId);
    verify(mediaRepository, times(1)).delete(existingMedia);
  }

  @Test
  void testDeleteMedia_NonExistentId() throws Exception {
    // Arrange
    Long nonExistentId = 999L;
    when(mediaRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act & Assert
    RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/api/media-objects/{id}", nonExistentId);

    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }


  @Test
  void testArchiveMediaObjects_success() throws Exception {
    // Arrange
    List<String> paths = Arrays.asList("path1", "path2");
    MediaModel media1 = new MediaModel();
    media1.setPath("path1");
    MediaModel media2 = new MediaModel();
    media2.setPath("path2");

    when(mediaRepository.findByPath("path1")).thenReturn(media1);
    when(mediaRepository.findByPath("path2")).thenReturn(media2);

    ObjectMapper objectMapper = new ObjectMapper();
    String requestBody = objectMapper.writeValueAsString(paths);

    // Act
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post(
                "/api/media-objects/archive") // Assuming your controller has the mapping "/media"
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody);

    MvcResult result = mockMvc.perform(requestBuilder).andReturn();

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    verify(mediaRepository, times(1)).findByPath("path1");
    verify(mediaRepository, times(1)).findByPath("path2");
    verify(jmsTemplate, times(1)).convertAndSend("archivingQueue", "path1");
    verify(jmsTemplate, times(1)).convertAndSend("archivingQueue", "path2");
  }
  @Test
  void PrepareMediaObjectsForDownload_success() throws Exception {
    // Arrange
    List<String> paths = Arrays.asList("path1", "path2");
    LibraryModel library = new LibraryModel();
    library.setStorageClass(StorageClass.GLACIER);
    MediaModel media1 = new MediaModel();
    media1.setPath("path1");
    MediaModel media2 = new MediaModel();
    media2.setPath("path2");
    media1.setLibrary(library);
    media2.setLibrary(library);
    when(mediaRepository.findByPath("path1")).thenReturn(media1);
    when(mediaRepository.findByPath("path2")).thenReturn(media2);

    ObjectMapper objectMapper = new ObjectMapper();
    String requestBody = objectMapper.writeValueAsString(paths);

    // Act
    mockMvc.perform(MockMvcRequestBuilders.post("/api/media-objects/prepare-download")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isOk());

    // Assert
    verify(mediaRepository, times(1)).findByPath("path1");
    verify(mediaRepository, times(1)).findByPath("path2");
    verify(jmsTemplate, times(2)).convertAndSend(anyString(), anyString());
  }

  @Test
  void CancelJobs_success() throws Exception {
    // Arrange
    List<Long> ids = Arrays.asList(1L, 2L);
    MediaModel media1 = new MediaModel();
    MediaModel media2 = new MediaModel();
    media1.setArchiving(true);
    media2.setArchiving(true);
    when(mediaRepository.findById(1L)).thenReturn(Optional.of(media1));
    when(mediaRepository.findById(2L)).thenReturn(Optional.of(media2));

    ObjectMapper objectMapper = new ObjectMapper();
    String requestBody = objectMapper.writeValueAsString(ids);

    // Act
    mockMvc.perform(MockMvcRequestBuilders.post("/api/media-objects/cancel-job")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isOk());

    // Assert
    verify(mediaRepository, times(1)).findById(1L);
    verify(mediaRepository, times(1)).findById(2L);
    verify(mediaRepository, times(2)).save(any(MediaModel.class));
  }

  @Test
  void CancelAllArchiveJobs_success() throws Exception {
    // Arrange
    List<MediaModel> medias = Arrays.asList(new MediaModel(), new MediaModel());
    when(mediaRepository.findByIsArchiving(true)).thenReturn(medias);

    // Act
    mockMvc.perform(MockMvcRequestBuilders.post("/api/media-objects/cancel-all-archive-jobs"))
            .andExpect(status().isOk());

    // Assert
    verify(mediaRepository, times(1)).findByIsArchiving(true);
    verify(mediaRepository, times(2)).save(any(MediaModel.class));
  }

  @Test
  void testClearAllFinishedDownloads_success() throws Exception {
    // Arrange
    List<MediaModel> successes = Arrays.asList(new MediaModel(), new MediaModel());
    when(mediaRepository.findByDownloadSuccess(true)).thenReturn(successes);

    // Act
    mockMvc.perform(MockMvcRequestBuilders.post("/api/media-objects/clear-all-finished"))
            .andExpect(status().isOk());

    // Assert
    verify(mediaRepository, times(1)).findByDownloadSuccess(true);
    verify(mediaRepository, times(2)).save(any(MediaModel.class));
  }


}

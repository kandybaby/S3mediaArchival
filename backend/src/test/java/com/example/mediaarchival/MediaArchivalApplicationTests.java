package com.example.mediaarchival;

import com.example.mediaarchival.consumers.MediaObjectTransferListenerTest;
import com.example.mediaarchival.controllers.LibraryControllerTest;
import com.example.mediaarchival.controllers.MediaControllerTest;
import com.example.mediaarchival.controllers.UserControllerTest;
import com.example.mediaarchival.converters.StringToArchivedStatusConverterTest;
import com.example.mediaarchival.converters.StringToMediaTypeConverterTest;
import com.example.mediaarchival.deserializers.ArchivedStatusDeserializerTest;
import com.example.mediaarchival.deserializers.MediaCategoryDeserializerTest;
import com.example.mediaarchival.filters.JwtValidationFilterTest;
import com.example.mediaarchival.tasks.S3CleanupTaskTest;
import com.example.mediaarchival.tasks.StartupResetTasksTest;
import com.example.mediaarchival.utils.TarUtilsTest;
import com.example.mediaarchival.utils.TokenUtilsTest;
import com.example.mediaarchival.tasks.RestoreCheckerTest;
import com.example.mediaarchival.utils.DirectoryUtilsTest;
import com.example.mediaarchival.consumers.LibraryUpdateConsumerTest;
import com.example.mediaarchival.consumers.RestoreConsumerTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MediaArchivalApplicationTests {

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class MediaControllerTests extends MediaControllerTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class LibraryControllerTests extends LibraryControllerTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class JwtValidationFilterTests extends JwtValidationFilterTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class StringToMediaTypeConverterTests extends StringToMediaTypeConverterTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class MediaCategoryDeserializerTests extends MediaCategoryDeserializerTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class StringToArchivedStatusConverterTests extends StringToArchivedStatusConverterTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class ArchivedStatusDeserializerTests extends ArchivedStatusDeserializerTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class UserControllerTests extends UserControllerTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class TokenUtilsTests extends TokenUtilsTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class TarUtilsTests extends TarUtilsTest {}
  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class MediaObjectTransferListenerTests extends MediaObjectTransferListenerTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_METHOD)
  class S3CleanupTaskTests extends S3CleanupTaskTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_METHOD)
  class RestoreCheckerTests extends RestoreCheckerTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class StartupResetTasksTests extends StartupResetTasksTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class DirectoryUtilsTests extends DirectoryUtilsTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_METHOD)
  class LibraryUpdateConsumerTests extends LibraryUpdateConsumerTest {}

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_METHOD)
  class RestoreConsumerTests extends RestoreConsumerTest {}

  @Test
  void contextLoads() {}
}

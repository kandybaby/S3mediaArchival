package com.example.mediaarchival.models;

import com.example.mediaarchival.deserializers.MediaCategoryDeserializer;
import com.example.mediaarchival.enums.MediaCategory;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import software.amazon.awssdk.services.s3.model.StorageClass;

/**
 * Entity representing a media library in the media archival system.
 * The library represents a collection of media that all share some
 * common form of file organization. For example, a movie library,
 * a TV library, or a music library
 */
@Entity
public class LibraryModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String path;

  @JsonDeserialize(using = MediaCategoryDeserializer.class)
  private MediaCategory category;

  private StorageClass storageClass;

  private String bucketName;

  private boolean isUpdating;

  /**
   * Gets the unique identifier for the library.
   *
   * @return the library ID.
   */
  public Long getId() {
    return id;
  }

  /**
   * Sets the unique identifier for the library.
   *
   * @param id the ID to set for the library.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Gets the name of the library.
   *
   * @return the library name.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the library.
   *
   * @param name the name to set for the library.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the path where the media for this library is stored.
   *
   * @return the storage path.
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the path where the media for this library is stored.
   *
   * @param path the storage path to set.
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Gets the category of the media in the library.
   *
   * @return the media category.
   */
  public MediaCategory getCategory() {
    return category;
  }

  /**
   * Sets the category of the media in the library.
   *
   * @param category the media category to set.
   */
  public void setCategory(MediaCategory category) {
    this.category = category;
  }

  /**
   * Gets the storage class used for the media in this library on S3.
   *
   * @return the S3 storage class.
   */
  public StorageClass getStorageClass() {
    return storageClass;
  }

  /**
   * Sets the storage class used for the media in this library on S3.
   *
   * @param storageClass the S3 storage class to set.
   */
  public void setStorageClass(StorageClass storageClass) {
    this.storageClass = storageClass;
  }

  /**
   * Gets the name of the S3 bucket where the media for this library is stored.
   *
   * @return the S3 bucket name.
   */
  public String getBucketName() {
    return bucketName;
  }

  /**
   * Sets the name of the S3 bucket where the media for this library is stored.
   *
   * @param bucketName the S3 bucket name to set.
   */
  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  /**
   * Checks if the library is currently being updated.
   *
   * @return true if the library is updating, false otherwise.
   */
  public boolean isUpdating() {
    return isUpdating;
  }

  /**
   * Sets the updating status of the library.
   *
   * @param isUpdating the updating status to set.
   */
  public void setUpdating(boolean isUpdating) {
    this.isUpdating = isUpdating;
  }
}

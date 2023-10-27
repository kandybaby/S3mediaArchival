package com.example.mediaarchival.models;

import com.example.mediaarchival.deserializers.ArchivedStatusDeserializer;
import com.example.mediaarchival.enums.ArchivedStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

@Entity
public class MediaModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Column(unique = true)
  private String path;

  @JsonDeserialize(using = ArchivedStatusDeserializer.class)
  private ArchivedStatus archivedStatus;

  private Instant dateArchived;
  private Instant dateLastModified;

  // Indicates that an object is in the process of being uploaded
  private boolean isArchiving;

  // Indicates that the object is being tarred as part of an upload
  private boolean isTarring;

  private int uploadProgress;

  // Indicates that the object is in the process of being downloaded
  private boolean isRecovering;

  // Indicates that the object is in the process of being restored to S3 from Glacier
  private boolean isRestoring;

  // Indicates that the object has been restored from S3
  private boolean isRestored;

  private int downloadProgress;

  // Indicates that a download job has completed with success or failure. Null means no
  // job has been completed.
  private Boolean downloadSuccess;

  private boolean isJobCancelled;

  private long size;

  @ManyToOne private LibraryModel library;

  /**
   * Gets the unique identifier for the media.
   * @return the unique identifier
   */
  public Long getId() {
    return id;
  }

  /**
   * Sets the unique identifier for the media.
   * @param id the unique identifier to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Gets the name of the media.
   * @return the media name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the media.
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the file path of the media.
   * @return the file path
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the file path of the media.
   * @param path the file path to set
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Gets the archived status of the media.
   * @return the archived status
   */
  public ArchivedStatus getArchivedStatus() {
    return archivedStatus;
  }

  /**
   * Sets the archived status of the media.
   * @param archivedStatus the archived status to set
   */
  public void setArchivedStatus(ArchivedStatus archivedStatus) {
    this.archivedStatus = archivedStatus;
  }

  /**
   * Gets the date when the media was archived.
   * @return the date archived
   */
  public Instant getDateArchived() {
    return dateArchived;
  }

  /**
   * Sets the date when the media was archived.
   * @param dateArchived the date archived to set
   */
  public void setDateArchived(Instant dateArchived) {
    this.dateArchived = dateArchived;
  }

  /**
   * Gets the date when the media was last modified.
   * @return the date last modified
   */
  public Instant getDateLastModified() {
    return dateLastModified;
  }

  /**
   * Sets the date when the media was last modified.
   * @param dateLastModified the date last modified to set
   */
  public void setDateLastModified(Instant dateLastModified) {
    this.dateLastModified = dateLastModified;
  }

  /**
   * Gets the associated library for the media.
   * @return the library
   */
  public LibraryModel getLibrary() {
    return library;
  }

  /**
   * Sets the associated library for the media.
   * @param library the library to set
   */
  public void setLibrary(LibraryModel library) {
    this.library = library;
  }

  /**
   * Checks if the media is currently being archived.
   * @return true if archiving, otherwise false
   */
  public boolean isArchiving() {
    return isArchiving;
  }

  /**
   * Sets the media's archiving status.
   * @param isArchiving the archiving status to set
   */
  public void setArchiving(boolean isArchiving) {
    this.isArchiving = isArchiving;
  }

  /**
   * Checks if the media is currently being tarred.
   * @return true if tarring, otherwise false
   */
  public boolean isTarring() {
    return isTarring;
  }

  /**
   * Sets the media's tarring status.
   * @param isTarring the tarring status to set
   */
  public void setTarring(boolean isTarring) {
    this.isTarring = isTarring;
  }

  /**
   * Gets the current upload progress percentage.
   * @return the upload progress
   */
  public int getUploadProgress() {
    return uploadProgress;
  }

  /**
   * Sets the current upload progress percentage.
   * @param uploadProgress the upload progress to set
   */
  public void setUploadProgress(int uploadProgress) {
    this.uploadProgress = uploadProgress;
  }

  /**
   * Checks if the media is currently being restored.
   * @return true if restoring, otherwise false
   */
  public boolean isRestoring() {
    return isRestoring;
  }

  /**
   * Sets the media's restoring status.
   * @param retrieving the restoring status to set
   */
  public void setRestoring(boolean retrieving) {
    isRestoring = retrieving;
  }

  /**
   * Checks if the media has been restored.
   * @return true if restored, otherwise false
   */
  public boolean isRestored() {
    return isRestored;
  }

  /**
   * Sets the media's restored status.
   * @param restored the restored status to set
   */
  public void setRestored(boolean restored) {
    isRestored = restored;
  }

  /**
   * Gets the current download progress percentage.
   * @return the download progress
   */
  public int getDownloadProgress() {
    return downloadProgress;
  }

  /**
   * Sets the current download progress percentage.
   * @param downloadProgress the download progress to set
   */
  public void setDownloadProgress(int downloadProgress) {
    this.downloadProgress = downloadProgress;
  }

  /**
   * Checks if the media's job is cancelled.
   * @return true if the job is cancelled, otherwise false
   */
  public boolean isJobCancelled() {
    return isJobCancelled;
  }

  /**
   * Sets the media's job cancellation status.
   * @param jobCancelled the job cancellation status to set
   */
  public void setJobCancelled(boolean jobCancelled) {
    isJobCancelled = jobCancelled;
  }

  /**
   * Gets the size of the media file.
   * @return the size of the file
   */
  public long getSize() {
    return size;
  }

  /**
   * Sets the size of the media file.
   * @param size the size to set
   */
  public void setSize(long size) {
    this.size = size;
  }

  /**
   * Checks if the media is currently being recovered.
   * @return true if recovering, otherwise false
   */
  public boolean isRecovering() {
    return isRecovering;
  }

  /**
   * Sets the media's recovering status.
   * @param isRecovering the recovering status to set
   */
  public void setRecovering(boolean isRecovering) {
    this.isRecovering = isRecovering;
  }

  /**
   * Gets the success status of the last download job.
   * @return true if successful, false if failed, null if no job has been completed
   */
  public Boolean getDownloadSuccess() {
    return downloadSuccess;
  }

  /**
   * Sets the success status of the last download job.
   * @param downloadFinished the status to set, true for success, false for failure, null if no job
   */
  public void setDownloadSuccess(Boolean downloadFinished) {
    this.downloadSuccess = downloadFinished;
  }

}

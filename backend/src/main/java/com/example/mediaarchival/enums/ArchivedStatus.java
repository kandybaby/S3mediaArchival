package com.example.mediaarchival.enums;

/**
 * Represents the archival status of a media item.
 */
public enum ArchivedStatus {
  /**
   * The media item has been archived.
   */
  ARCHIVED,

  /**
   * The media item is not archived.
   */
  NOT_ARCHIVED,

  /**
   * The media item is archived but the content is out of date.
   */
  OUT_OF_DATE,
}

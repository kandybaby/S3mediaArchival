package com.example.mediaarchival.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for accessing environment variables related to the media archival application,
 * with default values provided when the variables are not set.
 */
public class EnvUtils {

  private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");
  /**
   * Retrieves the temporary directory path from the environment,
   * or provides a default if not set.
   *
   * @return the temporary directory path as a String.
   */
  public static String getTempDirectory() {
    return System.getenv("TEMP_DIRECTORY") != null ? System.getenv("TEMP_DIRECTORY") : "/temporaryDirectory";
  }

  /**
   * Retrieves the download directory path from the environment,
   * or provides a default if not set.
   *
   * @return the download directory path as a String.
   */
  public static String getDownloadDirectory() {
    return System.getenv("DOWNLOAD_DIRECTORY") != null
            ? System.getenv("DOWNLOAD_DIRECTORY")
            : "/downloadDirectory";
  }

  /**
   * Retrieves the AWS region from the environment,
   * or provides a default if not set.
   *
   * @return the AWS region as a String.
   */
  public static String getAWSRegion() {
    return System.getenv("AWS_REGION") != null ? System.getenv("AWS_REGION") : "us-east-1";
  }

  /**
   * Retrieves the AWS access key ID from the environment,
   * or provides a default if not set.
   *
   * @return the AWS access key ID as a String.
   */
  public static String getAWSAccessKey() {
    return System.getenv("AWS_ACCESS_KEY_ID") != null
            ? System.getenv("AWS_ACCESS_KEY_ID")
            : "default_access_key_id";
  }

  /**
   * Retrieves the AWS secret access key from the environment,
   * or provides a default if not set.
   *
   * @return the AWS secret access key as a String.
   */
  public static String getAWSSecretKey() {
    return System.getenv("AWS_SECRET_ACCESS_KEY") != null
            ? System.getenv("AWS_SECRET_ACCESS_KEY")
            : "default_secret_access_key";
  }

  /**
   * Retrieves the data transfer throughput value from the environment,
   * or provides a default if not set. Throughput is GbPS
   *
   * @return the data transfer throughput as a Double.
   * @throws NumberFormatException if the environment variable is set but cannot be parsed as a Double.
   */
  public static Double getTransferThroughput() {
    String throughputValue = System.getenv("TRANSFER_THROUGHPUT");
    if (throughputValue != null) {
      try {
        return Double.parseDouble(throughputValue);
      } catch (NumberFormatException e) {
        errorLogger.error("Invalid format for TRANSFER_THROUGHPUT: " + throughputValue);
        return 0.5;
      }
    }
    return 0.5;
  }
}

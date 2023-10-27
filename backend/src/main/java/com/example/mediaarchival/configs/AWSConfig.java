package com.example.mediaarchival.configs;

import static software.amazon.awssdk.transfer.s3.SizeConstant.MB;

import com.example.mediaarchival.utils.EnvUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

/**
 * Configuration class for AWS services, primarily for the S3 client setup.
 * This class provides beans for AWS credentials and S3 client configurations.
 */
@Configuration
public class AWSConfig {

  /**
   * Provides AWS credentials.
   *
   * @return An AwsCredentialsProvider.
   */
  @Bean
  public AwsCredentialsProvider awsCredentialsProvider() {
    String secretKey = EnvUtils.getAWSSecretKey();
    String accessKey = EnvUtils.getAWSAccessKey();
    AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
    return StaticCredentialsProvider.create(awsCredentials);
  }

  /**
   * Creates an S3AsyncClient for asynchronous operations.
   *
   * @param credentialsProvider Provider for AWS credentials.
   * @return An S3AsyncClient.
   */
  @Bean
  public S3AsyncClient s3AsyncClient(AwsCredentialsProvider credentialsProvider) {
    Region region = Region.of(EnvUtils.getAWSRegion());
    return S3AsyncClient.crtBuilder()
        .credentialsProvider(credentialsProvider)
        .region(region)
        .targetThroughputInGbps(EnvUtils.getTransferThroughput())
        .minimumPartSizeInBytes(8 * MB)
        .build();
  }

  /**
   * Creates an Apache HTTP client for the S3 client.
   *
   * @return An instance of SdkHttpClient.
   */
  @Bean
  public SdkHttpClient apacheHttpClient() {
    return ApacheHttpClient.builder().build();
  }

  /**
   * Creates an S3Client for synchronous operations.
   *
   * @param credentialsProvider Provider for AWS credentials.
   * @param apacheHttpClient The Apache HTTP client.
   * @return An S3Client.
   */
  @Bean
  public S3Client s3Client(
      AwsCredentialsProvider credentialsProvider, SdkHttpClient apacheHttpClient) {
    Region region = Region.of(EnvUtils.getAWSRegion());
    return S3Client.builder()
        .httpClient(apacheHttpClient)
        .credentialsProvider(credentialsProvider)
        .region(region)
        .build();
  }

  /**
   * Creates an S3TransferManager for managing file transfers.
   *
   * @param s3AsyncClient The S3AsyncClient.
   * @return An S3TransferManager.
   */
  @Bean
  public S3TransferManager s3TransferManager(S3AsyncClient s3AsyncClient) {
    return S3TransferManager.builder().s3Client(s3AsyncClient).build();
  }
}

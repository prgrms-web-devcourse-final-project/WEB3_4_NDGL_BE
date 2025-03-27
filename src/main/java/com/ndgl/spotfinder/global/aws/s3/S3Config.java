package com.ndgl.spotfinder.global.aws.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

	@Value("${spring.cloud.aws.credentials.access-key}")
	private String accessKey;

	@Value("${spring.cloud.aws.credentials.secret-key}")
	private String secretKey;

	@Value("${spring.cloud.aws.region.static}")
	private Region region;

	@Bean
	public AwsCredentialsProvider awsCredentialsProvider() {
		return StaticCredentialsProvider.create(
			AwsBasicCredentials.create(accessKey, secretKey)
		);
	}

	@Bean
	public S3Presigner s3Presigner(AwsCredentialsProvider credentialsProvider) {
		return S3Presigner.builder()
			.credentialsProvider(credentialsProvider)
			.region(region)
			.build();
	}

	@Bean
	public S3Client s3Client(AwsCredentialsProvider credentialsProvider) {
		return S3Client.builder()
			.credentialsProvider(credentialsProvider)
			.region(region)
			.build();
	}
}

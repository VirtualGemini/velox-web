package com.velox.framework.file.support.client.s3;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.velox.framework.file.common.provider.FileProviderConstants;
import com.velox.framework.file.common.web.FileWebConstants;
import com.velox.framework.file.spi.client.AbstractFileClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.net.URL;
import java.time.Duration;

public class S3FileClient extends AbstractFileClient<S3FileClientConfig> {

    private S3Client client;
    private S3Presigner presigner;

    public S3FileClient(String id, S3FileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        if (StrUtil.isEmpty(config.getDomain())) {
            config.setDomain(buildDomain());
        }
        String regionStr = resolveRegion();
        Region region = Region.of(regionStr);
        AwsCredentialsProvider credentialsProvider = buildCredentialsProvider();
        URI endpoint = URI.create(buildEndpoint());
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(Boolean.TRUE.equals(config.getEnablePathStyleAccess()))
                .chunkedEncodingEnabled(false)
                .build();
        client = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .endpointOverride(endpoint)
                .serviceConfiguration(serviceConfiguration)
                .build();
        presigner = S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .endpointOverride(endpoint)
                .serviceConfiguration(serviceConfiguration)
                .build();
    }

    @Override
    protected String doUpload(byte[] content, String path, String type) {
        String key = resolveKey(path);
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(key)
                .contentType(type)
                .contentLength((long) content.length)
                .build();
        client.putObject(putRequest, RequestBody.fromBytes(content));
        return presignGetUrl(key, null);
    }

    @Override
    protected void doDelete(String path) {
        String key = resolveKey(path);
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(config.getBucket())
                .key(key)
                .build();
        client.deleteObject(deleteRequest);
    }

    @Override
    protected byte[] doGetContent(String path) {
        String key = resolveKey(path);
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(config.getBucket())
                .key(key)
                .build();
        return IoUtil.readBytes(client.getObject(getRequest));
    }

    @Override
    protected String doPresignPutUrl(String path) {
        String key = resolveKey(path);
        return presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(FileProviderConstants.S3_DEFAULT_EXPIRATION)
                .putObjectRequest(b -> b.bucket(config.getBucket()).key(key)).build())
                .url().toString();
    }

    @Override
    protected String doPresignGetUrl(String url, Integer expirationSeconds) {
        String path = StrUtil.removePrefix(url, config.getDomain() + FileWebConstants.URL_PATH_SEPARATOR);
        path = decodeUtf8(removeUrlQuery(path));

        if (!BooleanUtil.isFalse(config.getEnablePublicAccess())) {
            return config.getDomain() + FileWebConstants.URL_PATH_SEPARATOR + path;
        }

        String finalPath = path;
        Duration expiration = expirationSeconds != null
                ? Duration.ofSeconds(expirationSeconds)
                : FileProviderConstants.S3_DEFAULT_EXPIRATION;
        URL signedUrl = presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(b -> b.bucket(config.getBucket()).key(finalPath)).build())
                .url();
        return signedUrl.toString();
    }

    private String buildDomain() {
        if (HttpUtil.isHttp(config.getEndpoint()) || HttpUtil.isHttps(config.getEndpoint())) {
            return config.getEndpoint() + FileWebConstants.URL_PATH_SEPARATOR + config.getBucket();
        }
        return FileProviderConstants.HTTPS_PREFIX
                + config.getBucket()
                + FileWebConstants.DOT
                + config.getEndpoint();
    }

    private String buildEndpoint() {
        if (HttpUtil.isHttp(config.getEndpoint()) || HttpUtil.isHttps(config.getEndpoint())) {
            return config.getEndpoint();
        }
        return FileProviderConstants.HTTPS_PREFIX + config.getEndpoint();
    }

    private String resolveRegion() {
        if (StrUtil.isNotEmpty(config.getRegion())) {
            return config.getRegion();
        }
        String endpoint = config.getEndpoint();
        if (StrUtil.isEmpty(endpoint)) {
            return FileProviderConstants.DEFAULT_AWS_REGION;
        }
        String host = endpoint;
        if (HttpUtil.isHttp(endpoint) || HttpUtil.isHttps(endpoint)) {
            try {
                host = URI.create(endpoint).getHost();
            } catch (Exception e) {
                return FileProviderConstants.DEFAULT_AWS_REGION;
            }
        }
        if (StrUtil.isEmpty(host)) {
            return FileProviderConstants.DEFAULT_AWS_REGION;
        }
        if (host.contains(FileProviderConstants.AWS_HOST_SUFFIX.substring(1))) {
            if (host.startsWith(FileProviderConstants.S3_HOST_PREFIX) && host.contains(FileProviderConstants.AWS_HOST_SUFFIX)) {
                String regionPart = host.substring(
                        FileProviderConstants.S3_HOST_PREFIX.length(),
                        host.indexOf(FileProviderConstants.AWS_HOST_SUFFIX)
                );
                if (StrUtil.isNotEmpty(regionPart) && !regionPart.equals(FileProviderConstants.S3_ACCELERATE_REGION)) {
                    return regionPart;
                }
            }
            return FileProviderConstants.DEFAULT_AWS_REGION;
        }
        if (host.contains(S3FileClientConfig.ENDPOINT_ALIYUN)) {
            if (host.startsWith(FileProviderConstants.OSS_HOST_PREFIX)
                    && host.contains(FileWebConstants.DOT + S3FileClientConfig.ENDPOINT_ALIYUN)) {
                String regionPart = host.substring(
                        FileProviderConstants.OSS_HOST_PREFIX.length(),
                        host.indexOf(FileWebConstants.DOT + S3FileClientConfig.ENDPOINT_ALIYUN)
                );
                if (StrUtil.isNotEmpty(regionPart)) {
                    return regionPart;
                }
            }
        }
        if (host.contains(S3FileClientConfig.ENDPOINT_TENCENT)) {
            if (host.startsWith(FileProviderConstants.COS_HOST_PREFIX)
                    && host.contains(FileWebConstants.DOT + S3FileClientConfig.ENDPOINT_TENCENT)) {
                String regionPart = host.substring(
                        FileProviderConstants.COS_HOST_PREFIX.length(),
                        host.indexOf(FileWebConstants.DOT + S3FileClientConfig.ENDPOINT_TENCENT)
                );
                if (StrUtil.isNotEmpty(regionPart)) {
                    return regionPart;
                }
            }
        }
        return FileProviderConstants.DEFAULT_AWS_REGION;
    }

    private AwsCredentialsProvider buildCredentialsProvider() {
        if (StrUtil.isNotEmpty(config.getSessionToken())) {
            return StaticCredentialsProvider.create(AwsSessionCredentials.create(
                    config.getAccessKey(),
                    config.getAccessSecret(),
                    config.getSessionToken()));
        }
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(config.getAccessKey(), config.getAccessSecret()));
    }

    private String resolveKey(String path) {
        String normalizedPath = StrUtil.removePrefix(path, StrUtil.SLASH);
        if (StrUtil.isEmpty(config.getBasePath())) {
            return normalizedPath;
        }
        return StrUtil.removeSuffix(config.getBasePath(), StrUtil.SLASH)
                + StrUtil.SLASH
                + normalizedPath;
    }

    private static String decodeUtf8(String str) {
        try {
            return java.net.URLDecoder.decode(str, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return str;
        }
    }

    private static String removeUrlQuery(String url) {
        int queryIndex = url.indexOf(FileWebConstants.QUESTION_MARK);
        return queryIndex >= 0 ? url.substring(0, queryIndex) : url;
    }
}

package com.velox.framework.file.autoconfigure;

import com.velox.framework.file.api.client.FileClientFactory;
import com.velox.framework.file.api.diagnostics.FileFailureReasonResolver;
import com.velox.framework.file.common.prefix.FilePropertyPrefixes;
import com.velox.framework.file.common.storage.FileStorageCodes;
import com.velox.framework.file.core.client.DefaultFileClientManager;
import com.velox.framework.file.noop.DisabledFileClientManager;
import com.velox.framework.file.properties.VeloxFileProperties;
import com.velox.framework.file.spi.client.FileClientManager;
import com.velox.framework.file.spi.client.FileClientTypeRegistration;
import com.velox.framework.file.support.client.FileClientTypeRegistry;
import com.velox.framework.file.support.diagnostics.DefaultFileFailureReasonResolver;
import com.velox.framework.file.support.client.ftp.FtpFileClient;
import com.velox.framework.file.support.client.ftp.FtpFileClientConfig;
import com.velox.framework.file.support.client.local.LocalFileClient;
import com.velox.framework.file.support.client.local.LocalFileClientConfig;
import com.velox.framework.file.support.client.s3.S3FileClient;
import com.velox.framework.file.support.client.s3.S3FileClientConfig;
import com.velox.framework.file.support.client.sftp.SftpFileClient;
import com.velox.framework.file.support.client.sftp.SftpFileClientConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@EnableConfigurationProperties(VeloxFileProperties.class)
public class VeloxFileAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FileClientTypeRegistry fileClientTypeRegistry(ObjectProvider<FileClientTypeRegistration> registrations) {
        return new FileClientTypeRegistry(registrations.orderedStream().toList());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = FilePropertyPrefixes.FILE,
            name = FilePropertyPrefixes.ENABLED,
            havingValue = FilePropertyPrefixes.TRUE,
            matchIfMissing = true
    )
    public FileClientManager fileClientManager(FileClientTypeRegistry fileClientTypeRegistry) {
        return new DefaultFileClientManager(fileClientTypeRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = FilePropertyPrefixes.FILE,
            name = FilePropertyPrefixes.ENABLED,
            havingValue = FilePropertyPrefixes.FALSE
    )
    public FileClientManager disabledFileClientManager(FileClientTypeRegistry fileClientTypeRegistry) {
        return new DisabledFileClientManager(fileClientTypeRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public FileClientFactory fileClientFactory(FileClientManager fileClientManager) {
        return fileClientManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public FileFailureReasonResolver fileFailureReasonResolver() {
        return new DefaultFileFailureReasonResolver();
    }

    @Bean
    @ConditionalOnMissingBean(name = FileAutoConfigurationConstants.LOCAL_REGISTRATION_BEAN_NAME)
    public FileClientTypeRegistration localFileClientTypeRegistration() {
        return FileClientTypeRegistration.builtIn(FileStorageCodes.LOCAL, LocalFileClientConfig.class,
                (configId, config) -> new LocalFileClient(configId, (LocalFileClientConfig) config));
    }

    @Bean
    @ConditionalOnMissingBean(name = FileAutoConfigurationConstants.FTP_REGISTRATION_BEAN_NAME)
    @ConditionalOnClass(name = {
            FileAutoConfigurationConstants.FTP_CLASS_NAME,
            FileAutoConfigurationConstants.FTP_CLIENT_CLASS_NAME
    })
    public FileClientTypeRegistration ftpFileClientTypeRegistration() {
        return FileClientTypeRegistration.builtIn(FileStorageCodes.FTP, FtpFileClientConfig.class,
                (configId, config) -> new FtpFileClient(configId, (FtpFileClientConfig) config));
    }

    @Bean
    @ConditionalOnMissingBean(name = FileAutoConfigurationConstants.SFTP_REGISTRATION_BEAN_NAME)
    @ConditionalOnClass(name = FileAutoConfigurationConstants.SFTP_CLASS_NAME)
    public FileClientTypeRegistration sftpFileClientTypeRegistration() {
        return FileClientTypeRegistration.builtIn(FileStorageCodes.SFTP, SftpFileClientConfig.class,
                (configId, config) -> new SftpFileClient(configId, (SftpFileClientConfig) config));
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = {
            FileAutoConfigurationConstants.S3_CLIENT_CLASS_NAME,
            FileAutoConfigurationConstants.S3_PRESIGNER_CLASS_NAME
    })
    static class S3FileClientRegistrationConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = FileAutoConfigurationConstants.S3_REGISTRATION_BEAN_NAME)
        public FileClientTypeRegistration s3FileClientTypeRegistration() {
            return FileClientTypeRegistration.builtIn(FileStorageCodes.S3, S3FileClientConfig.class,
                    (configId, config) -> new S3FileClient(configId, (S3FileClientConfig) config));
        }
    }
}

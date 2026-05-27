package com.velox.framework.file.autoconfigure;

public final class FileAutoConfigurationConstants {

    public static final String LOCAL_REGISTRATION_BEAN_NAME = "localFileClientTypeRegistration";
    public static final String FTP_REGISTRATION_BEAN_NAME = "ftpFileClientTypeRegistration";
    public static final String SFTP_REGISTRATION_BEAN_NAME = "sftpFileClientTypeRegistration";
    public static final String S3_REGISTRATION_BEAN_NAME = "s3FileClientTypeRegistration";

    public static final String FTP_CLASS_NAME = "cn.hutool.extra.ftp.Ftp";
    public static final String FTP_CLIENT_CLASS_NAME = "org.apache.commons.net.ftp.FTPClient";
    public static final String SFTP_CLASS_NAME = "cn.hutool.extra.ssh.Sftp";
    public static final String S3_CLIENT_CLASS_NAME = "software.amazon.awssdk.services.s3.S3Client";
    public static final String S3_PRESIGNER_CLASS_NAME = "software.amazon.awssdk.services.s3.presigner.S3Presigner";

    private FileAutoConfigurationConstants() {
    }
}

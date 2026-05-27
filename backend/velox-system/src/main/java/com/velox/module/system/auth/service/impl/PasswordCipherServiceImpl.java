package com.velox.module.system.auth.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.PBKDF2;
import com.velox.framework.security.properties.SecurityProperties;
import com.velox.module.system.auth.service.PasswordCipherService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PasswordCipherServiceImpl implements PasswordCipherService {

    private static final Pattern PASSWORD_PREFIX_PATTERN = Pattern.compile("^\\{([a-zA-Z0-9_-]+)}(.+)$");
    private static final Pattern PBKDF2_HASH_PATTERN = Pattern.compile("^(\\d+)\\$(\\d+)\\$([0-9a-fA-F]+)\\$([0-9a-fA-F]+)$");
    private static final String ALGORITHM_MD5 = "md5";
    private static final String ALGORITHM_BCRYPT = "bcrypt";
    private static final String ALGORITHM_PBKDF2_SHA512 = "pbkdf2_sha512";
    private static final String PBKDF2_JDK_ALGORITHM = "PBKDF2WithHmacSHA512";

    private final SecurityProperties securityProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordCipherServiceImpl(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        validateAlgorithm(normalizeAlgorithm(securityProperties.getPassword().getAlgorithm()));
    }

    @Override
    public String encode(String rawPassword) {
        String algorithm = normalizeAlgorithm(securityProperties.getPassword().getAlgorithm());
        return "{" + algorithm + "}" + encode(rawPassword, algorithm);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(encodedPassword)) {
            return false;
        }
        PasswordValue passwordValue = resolvePassword(encodedPassword);
        return matches(rawPassword, passwordValue.hash(), passwordValue.algorithm());
    }

    @Override
    public boolean needsUpgrade(String encodedPassword) {
        if (!securityProperties.getPassword().isUpgradeOnLogin()) {
            return false;
        }
        String activeAlgorithm = normalizeAlgorithm(securityProperties.getPassword().getAlgorithm());
        PasswordValue passwordValue = resolvePassword(encodedPassword);
        if (!activeAlgorithm.equals(passwordValue.algorithm())) {
            return true;
        }
        if (ALGORITHM_PBKDF2_SHA512.equals(activeAlgorithm)) {
            return needsUpgradePbkdf2(passwordValue.hash());
        }
        return false;
    }

    private PasswordValue resolvePassword(String encodedPassword) {
        Matcher matcher = PASSWORD_PREFIX_PATTERN.matcher(encodedPassword);
        if (matcher.matches()) {
            String algorithm = normalizeAlgorithm(matcher.group(1));
            validateAlgorithm(algorithm);
            return new PasswordValue(algorithm, matcher.group(2));
        }
        return new PasswordValue(ALGORITHM_MD5, encodedPassword);
    }

    private String encode(String rawPassword, String algorithm) {
        return switch (algorithm) {
            case ALGORITHM_MD5 -> DigestUtil.md5Hex(rawPassword);
            case ALGORITHM_BCRYPT -> BCrypt.hashpw(rawPassword, BCrypt.gensalt(securityProperties.getPassword().getBcryptStrength()));
            case ALGORITHM_PBKDF2_SHA512 -> encodePbkdf2(rawPassword);
            default -> throw new IllegalStateException("Unsupported password algorithm: " + algorithm);
        };
    }

    private boolean matches(String rawPassword, String encodedPassword, String algorithm) {
        return switch (algorithm) {
            case ALGORITHM_MD5 -> DigestUtil.md5Hex(rawPassword).equalsIgnoreCase(encodedPassword);
            case ALGORITHM_BCRYPT -> BCrypt.checkpw(rawPassword, encodedPassword);
            case ALGORITHM_PBKDF2_SHA512 -> matchesPbkdf2(rawPassword, encodedPassword);
            default -> throw new IllegalStateException("Unsupported password algorithm: " + algorithm);
        };
    }

    private String normalizeAlgorithm(String algorithm) {
        return StringUtils.hasText(algorithm) ? algorithm.trim().toLowerCase(Locale.ROOT) : ALGORITHM_BCRYPT;
    }

    private void validateAlgorithm(String algorithm) {
        if (!ALGORITHM_MD5.equals(algorithm)
                && !ALGORITHM_BCRYPT.equals(algorithm)
                && !ALGORITHM_PBKDF2_SHA512.equals(algorithm)) {
            throw new IllegalStateException(
                    "Unsupported password algorithm: " + algorithm + ", available: pbkdf2_sha512,bcrypt,md5");
        }
    }

    private String encodePbkdf2(String rawPassword) {
        SecurityProperties.Password passwordConfig = securityProperties.getPassword();
        int iterations = passwordConfig.getPbkdf2Iterations();
        int keyLength = passwordConfig.getPbkdf2KeyLength();
        int saltLength = passwordConfig.getPbkdf2SaltLength();
        validatePbkdf2Config(iterations, keyLength, saltLength);

        byte[] salt = new byte[saltLength];
        secureRandom.nextBytes(salt);

        PBKDF2 pbkdf2 = new PBKDF2(PBKDF2_JDK_ALGORITHM, keyLength, iterations);
        String saltHex = toHex(salt);
        String hashHex = pbkdf2.encryptHex(rawPassword.toCharArray(), salt);
        return iterations + "$" + keyLength + "$" + saltHex + "$" + hashHex;
    }

    private boolean matchesPbkdf2(String rawPassword, String encodedPassword) {
        Matcher matcher = PBKDF2_HASH_PATTERN.matcher(encodedPassword);
        if (!matcher.matches()) {
            return false;
        }
        int iterations = parsePositiveInt(matcher.group(1), "pbkdf2 iterations");
        int keyLength = parsePositiveInt(matcher.group(2), "pbkdf2 keyLength");
        String saltHex = matcher.group(3);
        String expectedHash = matcher.group(4);
        byte[] salt = fromHex(saltHex);

        PBKDF2 pbkdf2 = new PBKDF2(PBKDF2_JDK_ALGORITHM, keyLength, iterations);
        String actualHash = pbkdf2.encryptHex(rawPassword.toCharArray(), salt);
        return actualHash.equalsIgnoreCase(expectedHash);
    }

    private boolean needsUpgradePbkdf2(String encodedPassword) {
        Matcher matcher = PBKDF2_HASH_PATTERN.matcher(encodedPassword);
        if (!matcher.matches()) {
            return true;
        }
        int storedIterations = parsePositiveInt(matcher.group(1), "pbkdf2 iterations");
        int storedKeyLength = parsePositiveInt(matcher.group(2), "pbkdf2 keyLength");
        int targetIterations = securityProperties.getPassword().getPbkdf2Iterations();
        int targetKeyLength = securityProperties.getPassword().getPbkdf2KeyLength();
        return storedIterations < targetIterations || storedKeyLength != targetKeyLength;
    }

    private int parsePositiveInt(String value, String field) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new IllegalStateException("Invalid " + field + ": " + value);
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Invalid " + field + ": " + value, ex);
        }
    }

    private void validatePbkdf2Config(int iterations, int keyLength, int saltLength) {
        if (iterations < 10000) {
            throw new IllegalStateException("pbkdf2Iterations must be >= 10000");
        }
        if (keyLength < 128) {
            throw new IllegalStateException("pbkdf2KeyLength must be >= 128");
        }
        if (saltLength < 16) {
            throw new IllegalStateException("pbkdf2SaltLength must be >= 16");
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private byte[] fromHex(String hex) {
        int len = hex.length();
        if ((len & 1) != 0) {
            throw new IllegalStateException("Invalid hex length: " + len);
        }
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(hex.charAt(i), 16);
            int lo = Character.digit(hex.charAt(i + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalStateException("Invalid hex value: " + hex);
            }
            result[i / 2] = (byte) ((hi << 4) + lo);
        }
        return result;
    }

    private record PasswordValue(String algorithm, String hash) {
    }
}

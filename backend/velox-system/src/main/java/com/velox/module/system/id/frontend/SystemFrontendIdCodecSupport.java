package com.velox.module.system.id.frontend;

import com.velox.framework.id.exception.VeloxIdGeneratorException;
import com.velox.framework.id.properties.VeloxIdProperties;
import com.velox.framework.id.support.codec.Base62IdCodec;
import com.velox.module.system.id.frontend.SystemFrontendIdProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
@Component
public class SystemFrontendIdCodecSupport {

    private static final int MIN_SNOWFLAKE_LENGTH = 15;

    private final VeloxIdProperties idProperties;
    private final SystemFrontendIdProperties frontendIdProperties;
    private final Base62IdCodec base62IdCodec = new Base62IdCodec();

    public SystemFrontendIdCodecSupport(
            VeloxIdProperties idProperties,
            SystemFrontendIdProperties frontendIdProperties
    ) {
        this.idProperties = idProperties;
        this.frontendIdProperties = frontendIdProperties;
    }

    public boolean isEnabled() {
        return idProperties.isEnabled() && frontendIdProperties.isBase62();
    }

    public boolean isIdentifierName(String name) {
        String normalized = normalizeIdentifierName(name);
        if (normalized.isEmpty()) {
            return false;
        }
        return normalized.equals("id")
                || normalized.equals("ids")
                || normalized.equals("createby")
                || normalized.equals("updateby")
                || normalized.equals("createdby")
                || normalized.equals("updatedby")
                || normalized.endsWith("id")
                || normalized.endsWith("ids");
    }

    public String encodeIdentifier(String value) {
        String normalized = normalizeValue(value);
        if (normalized == null || !isEnabled()) {
            return normalized;
        }
        if (!isSnowflake(normalized)) {
            return normalized;
        }
        return base62IdCodec.encode(normalized);
    }

    public String decodeIdentifier(String value) {
        String normalized = normalizeValue(value);
        if (normalized == null || !isEnabled()) {
            return normalized;
        }
        if (isSnowflake(normalized) || isSequenceString(normalized)) {
            return normalized;
        }
        try {
            String decoded = base62IdCodec.decode(normalized);
            return isSnowflake(decoded) ? decoded : normalized;
        } catch (VeloxIdGeneratorException exception) {
            return normalized;
        }
    }

    public List<String> encodeIdentifiers(Collection<String> values) {
        List<String> encoded = new ArrayList<>();
        if (values == null) {
            return encoded;
        }
        for (String value : values) {
            encoded.add(encodeIdentifier(value));
        }
        return encoded;
    }

    public List<String> decodeIdentifiers(Collection<String> values) {
        List<String> decoded = new ArrayList<>();
        if (values == null) {
            return decoded;
        }
        for (String value : values) {
            decoded.add(decodeIdentifier(value));
        }
        return decoded;
    }

    private String normalizeIdentifierName(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder(name.length());
        for (int index = 0; index < name.length(); index++) {
            char current = Character.toLowerCase(name.charAt(index));
            if ((current >= 'a' && current <= 'z') || (current >= '0' && current <= '9')) {
                builder.append(current);
            }
        }
        return builder.toString();
    }

    private String normalizeValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean isSequenceString(String value) {
        return isNumeric(value) && !isSnowflake(value);
    }

    private boolean isSnowflake(String value) {
        if (!isNumeric(value) || value.length() < MIN_SNOWFLAKE_LENGTH) {
            return false;
        }
        try {
            long id = Long.parseLong(value);
            return id > 0 && (id >> 22) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private boolean isNumeric(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            if (!Character.isDigit(value.charAt(index))) {
                return false;
            }
        }
        return true;
    }

}

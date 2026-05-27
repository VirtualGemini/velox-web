package com.velox.framework.file.support.client;

import com.velox.framework.file.common.message.FileCommonMessages;
import com.velox.framework.file.exception.FileClientException;
import com.velox.framework.file.spi.client.FileClientTypeRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileClientTypeRegistry {

    private static final Logger log = LoggerFactory.getLogger(FileClientTypeRegistry.class);

    private final Map<Integer, FileClientTypeRegistration> registrations;

    public FileClientTypeRegistry(Collection<FileClientTypeRegistration> registrations) {
        this.registrations = new LinkedHashMap<>();
        for (FileClientTypeRegistration registration : registrations) {
            FileClientTypeRegistration previous = this.registrations.get(registration.storage());
            if (previous == null) {
                this.registrations.put(registration.storage(), registration);
                continue;
            }
            if (previous.builtIn() && !registration.builtIn()) {
                this.registrations.put(registration.storage(), registration);
                log.info(FileCommonMessages.FILE_CLIENT_REGISTRATION_OVERRIDDEN,
                        registration.storage(),
                        previous.configClass().getName(),
                        registration.configClass().getName());
                continue;
            }
            if (!previous.builtIn() && registration.builtIn()) {
                log.info(FileCommonMessages.FILE_CLIENT_REGISTRATION_IGNORED,
                        registration.storage(),
                        registration.configClass().getName(),
                        previous.configClass().getName());
                continue;
            }
            throw FileClientException.registrationConflict(registration.storage());
        }
    }

    public FileClientTypeRegistration get(Integer storage) {
        return registrations.get(storage);
    }

    public FileClientTypeRegistration require(Integer storage) {
        FileClientTypeRegistration registration = get(storage);
        if (registration == null) {
            throw FileClientException.unsupportedStorage(storage);
        }
        return registration;
    }

    public List<Integer> storageTypes() {
        return List.copyOf(registrations.keySet());
    }
}

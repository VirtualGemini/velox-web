package com.velox.module.system.user.security.service;

import com.velox.module.system.user.security.dto.EmailRebindCommand;
import com.velox.module.system.user.security.dto.EmailRebindSendCodeCommand;
import com.velox.module.system.user.security.dto.LoginMethodsUpdateCommand;
import com.velox.module.system.user.security.dto.MfaEmailUpdateCommand;
import com.velox.module.system.user.security.dto.SecurityStatusDTO;

public interface AccountSecurityService {

    SecurityStatusDTO getStatus();

    void sendEmailRebindCode(EmailRebindSendCodeCommand command);

    Boolean rebindEmail(EmailRebindCommand command);

    Boolean updateLoginMethods(LoginMethodsUpdateCommand command);

    void sendMfaEmailCode();

    Boolean updateMfaEmail(MfaEmailUpdateCommand command);
}

package com.velox.module.system.user.security.service;

import com.velox.module.system.user.security.dto.EmailRebindCommand;
import com.velox.module.system.user.security.dto.EmailRebindProofDTO;
import com.velox.module.system.user.security.dto.EmailRebindProofVerifyCommand;
import com.velox.module.system.user.security.dto.EmailRebindSendCodeCommand;
import com.velox.module.system.user.security.dto.LoginMethodsUpdateCommand;
import com.velox.module.system.user.security.dto.MfaEmailUpdateCommand;
import com.velox.module.system.user.security.dto.MfaTotpDisableCommand;
import com.velox.module.system.user.security.dto.MfaTotpEnableCommand;
import com.velox.module.system.user.security.dto.MfaTotpProvisionDTO;
import com.velox.module.system.user.security.dto.SecurityStatusDTO;

public interface AccountSecurityService {

    SecurityStatusDTO getStatus();

    void sendEmailRebindProofCode();

    EmailRebindProofDTO verifyEmailRebindProof(EmailRebindProofVerifyCommand command);

    void sendEmailRebindCode(EmailRebindSendCodeCommand command);

    Boolean rebindEmail(EmailRebindCommand command);

    Boolean updateLoginMethods(LoginMethodsUpdateCommand command);

    void sendMfaEmailCode();

    Boolean updateMfaEmail(MfaEmailUpdateCommand command);

    MfaTotpProvisionDTO provisionMfaTotp();

    Boolean enableMfaTotp(MfaTotpEnableCommand command);

    Boolean disableMfaTotp(MfaTotpDisableCommand command);
}

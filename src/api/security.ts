import request from '@/utils/http'

export function fetchSecurityStatus() {
  return request.get<Api.User.Security.SecurityStatus>({
    url: '/api/user/security/status'
  })
}

export function fetchSendEmailRebindProofCode() {
  return request.post<void>({
    url: '/api/user/security/email/rebind/proof/send-code'
  })
}

export function fetchVerifyEmailRebindProof(data: Api.User.Security.EmailRebindProofVerifyCommand) {
  return request.post<Api.User.Security.EmailRebindProof>({
    url: '/api/user/security/email/rebind/proof/verify',
    data
  })
}

export function fetchSendEmailRebindCode(data: Api.User.Security.EmailRebindSendCodeCommand) {
  return request.post<void>({
    url: '/api/user/security/email/rebind/send-code',
    data
  })
}

export function fetchRebindEmail(data: Api.User.Security.EmailRebindCommand) {
  return request.put<boolean>({
    url: '/api/user/security/email/rebind',
    data
  })
}

export function fetchUpdateLoginMethods(data: Api.User.Security.LoginMethodsUpdateCommand) {
  return request.put<boolean>({
    url: '/api/user/security/login-methods',
    data
  })
}

export function fetchSendMfaEmailCode() {
  return request.post<void>({
    url: '/api/user/security/mfa/email/send-code'
  })
}

export function fetchUpdateMfaEmail(data: Api.User.Security.MfaEmailUpdateCommand) {
  return request.put<boolean>({
    url: '/api/user/security/mfa/email',
    data
  })
}

export function fetchProvisionMfaTotp() {
  return request.post<Api.User.Security.MfaTotpProvision>({
    url: '/api/user/security/mfa/totp/provision'
  })
}

export function fetchEnableMfaTotp(data: Api.User.Security.MfaTotpEnableCommand) {
  return request.put<boolean>({
    url: '/api/user/security/mfa/totp/enable',
    data
  })
}

export function fetchDisableMfaTotp(data: Api.User.Security.MfaTotpDisableCommand) {
  return request.put<boolean>({
    url: '/api/user/security/mfa/totp/disable',
    data
  })
}

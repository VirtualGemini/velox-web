import request from '@/utils/http'

/**
 * 登录
 * @param params 登录参数
 * @returns 登录响应
 */
export function fetchLogin(params: Api.Auth.LoginParams) {
  return request.post<Api.Auth.LoginResponse>({
    url: '/api/auth/login',
    params
    // showSuccessMessage: true // 显示成功消息
    // showErrorMessage: false // 不显示错误消息
  })
}

/**
 * 发送登录验证码（邮箱）
 */
export function fetchSendLoginCode(data: Api.Auth.LoginCodeSendParams) {
  return request.post<void>({
    url: '/api/auth/login-code/send',
    data
  })
}

/**
 * 验证码登录（邮箱）
 */
export function fetchLoginByCode(data: Api.Auth.CodeLoginParams) {
  return request.post<Api.Auth.LoginResponse>({
    url: '/api/auth/login-code/login',
    data
  })
}

/**
 * 获取用户基础信息（仅含支撑系统运行的最少字段）
 */
export function fetchGetAccountInfo() {
  return request.get<Api.Auth.AccountInfo>({
    url: '/api/account/info'
  })
}

/**
 * 获取当前用户详细信息（账号中心使用，需要 system:account-center:profile-query 权限）
 */
export function fetchGetAccountDetail() {
  return request.get<Api.Auth.AccountDetail>({
    url: '/api/account/detail'
  })
}

export function fetchGetAccountTabInfo() {
  return request.get<Api.Auth.AccountTabInfo>({
    url: '/api/account/tab'
  })
}

/**
 * 更新当前用户资料
 */
export function fetchUpdateAccountProfile(data: Api.Auth.AccountProfileUpdateCommand) {
  return request.put<boolean>({
    url: '/api/account/profile',
    data
  })
}

/**
 * 修改当前用户密码
 */
export function fetchUpdateAccountPassword(data: Api.Auth.AccountPasswordUpdateCommand) {
  return request.put<boolean>({
    url: '/api/account/password',
    data
  })
}

export function fetchUpdateAccountUsername(data: Api.Auth.AccountUsernameUpdateCommand) {
  return request.put<boolean>({
    url: '/api/account/username',
    data
  })
}

export function fetchRequestAccountDeletion(data: Api.Auth.AccountDeletionCommand) {
  return request.post<boolean>({
    url: '/api/account/deletion',
    data
  })
}

export function fetchRecoverAccount(data: Api.Auth.AccountRecoveryCommand) {
  return request.post<boolean>({
    url: '/api/account/recovery',
    data
  })
}

/**
 * 更新当前用户头像
 */
export function fetchUpdateAccountAvatar(data: Api.Auth.AvatarUpdateCommand) {
  return request.put<Api.Auth.AccountInfo>({
    url: '/api/account/avatar',
    data
  })
}

/**
 * 更新当前用户语言偏好
 */
export function fetchUpdateUserLanguage(language: string) {
  return request.put<boolean>({
    url: '/api/account/language',
    data: { language }
  })
}

export const fetchGetUserInfo = fetchGetAccountInfo
export const fetchGetUserDetail = fetchGetAccountDetail
export const fetchUpdateUserProfile = fetchUpdateAccountProfile
export const fetchUpdateUserPassword = fetchUpdateAccountPassword
export const fetchUpdateUserAvatar = fetchUpdateAccountAvatar

/**
 * 注册
 * @param params 注册参数
 * @returns 注册响应
 */
export function fetchRegister(params: Api.Auth.RegisterParams) {
  return request.post<void>({
    url: '/api/auth/register',
    params
  })
}

export function fetchSendResetPasswordCode(data: Api.Auth.ForgotPasswordCodeParams) {
  return request.post<void>({
    url: '/api/auth/forgot-password/code',
    data
  })
}

export function fetchResetPassword(data: Api.Auth.ResetPasswordParams) {
  return request.post<void>({
    url: '/api/auth/forgot-password/reset',
    data
  })
}

export function fetchLogout() {
  return request.post<void>({
    url: '/api/auth/logout',
    showErrorMessage: false
  })
}

/**
 * 发送邮箱二次验证邮件验证码
 */
export function fetchSendMfaChallengeCode(data: Api.Auth.MfaChallengeSendParams) {
  return request.post<void>({
    url: '/api/auth/mfa/challenge/send-code',
    data
  })
}

/**
 * 校验虚拟 MFA 设备验证并完成登录
 */
export function fetchVerifyMfaChallenge(data: Api.Auth.MfaChallengeVerifyParams) {
  return request.post<Api.Auth.LoginResponse>({
    url: '/api/auth/mfa/challenge/verify',
    data
  })
}

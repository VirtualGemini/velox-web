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
 * 发送登录验证码（邮箱/手机号）
 */
export function fetchSendLoginCode(data: Api.Auth.LoginCodeSendParams) {
  return request.post<void>({
    url: '/api/auth/login-code/send',
    data
  })
}

/**
 * 验证码登录（邮箱/手机号）
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
export function fetchGetUserInfo() {
  return request.get<Api.Auth.UserInfo>({
    url: '/api/user/info'
  })
}

/**
 * 获取当前用户详细信息（个人中心使用，需要 system:user-center:profile-query 权限）
 */
export function fetchGetUserDetail() {
  return request.get<Api.Auth.UserDetail>({
    url: '/api/user/detail'
  })
}

/**
 * 更新当前用户资料
 */
export function fetchUpdateUserProfile(data: Api.Auth.UserProfileUpdateCommand) {
  return request.put<boolean>({
    url: '/api/user/profile',
    data
  })
}

/**
 * 修改当前用户密码
 */
export function fetchUpdateUserPassword(data: Api.Auth.UserPasswordUpdateCommand) {
  return request.put<boolean>({
    url: '/api/user/password',
    data
  })
}

/**
 * 更新当前用户头像
 */
export function fetchUpdateUserAvatar(data: Api.Auth.AvatarUpdateCommand) {
  return request.put<Api.Auth.UserInfo>({
    url: '/api/user/avatar',
    data
  })
}

/**
 * 更新当前用户语言偏好
 */
export function fetchUpdateUserLanguage(language: string) {
  return request.put<boolean>({
    url: '/api/user/language',
    data: { language }
  })
}

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

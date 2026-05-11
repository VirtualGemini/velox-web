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

export function fetchLoginRoles() {
  return request.get<Api.Auth.LoginRole[]>({
    url: '/api/auth/login-roles'
  })
}

/**
 * 获取用户信息
 * @returns 用户信息
 */
export function fetchGetUserInfo() {
  return request.get<Api.Auth.UserInfo>({
    url: '/api/user/info'
    // 自定义请求头
    // headers: {
    //   'X-Custom-Header': 'your-custom-value'
    // }
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

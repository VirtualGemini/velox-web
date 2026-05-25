/**
 * API 接口类型定义模块
 *
 * 提供所有后端接口的类型定义
 *
 * ## 主要功能
 *
 * - 通用类型（分页参数、响应结构等）
 * - 认证类型（登录、用户信息等）
 * - 系统管理类型（用户、角色等）
 * - 全局命名空间声明
 *
 * ## 使用场景
 *
 * - API 请求参数类型约束
 * - API 响应数据类型定义
 * - 接口文档类型同步
 *
 * ## 注意事项
 *
 * - 在 .vue 文件使用需要在 eslint.config.mjs 中配置 globals: { Api: 'readonly' }
 * - 使用全局命名空间，无需导入即可使用
 *
 * ## 使用方式
 *
 * ```typescript
 * const params: Api.Auth.LoginParams = { userName: 'admin', password: '123456' }
 * const response: Api.Auth.UserInfo = await fetchUserInfo()
 * ```
 *
 * @module types/api/api
 * @author Velox Team
 */

declare namespace Api {
  /** 通用类型 */
  namespace Common {
    /** 分页参数 */
    interface PaginationParams {
      /** 当前页码 */
      current: number
      /** 每页条数 */
      size: number
      /** 总条数 */
      total: number
    }

    /** 通用搜索参数 */
    type CommonSearchParams = Pick<PaginationParams, 'current' | 'size'>

    /** 分页响应基础结构 */
    interface PaginatedResponse<T = any> {
      records: T[]
      current: number
      size: number
      total: number
    }

    /** 启用状态 */
    type EnableStatus = '1' | '2'
  }

  /** 认证类型 */
  namespace Auth {
    /** 登录参数 */
    interface LoginParams {
      userName: string
      password: string
    }

    /** 验证码登录的渠道类型 */
    type LoginCodeChannel = 'email'

    /** 发送登录验证码参数 */
    interface LoginCodeSendParams {
      type: LoginCodeChannel
      target: string
    }

    /** 验证码登录参数 */
    interface CodeLoginParams {
      type: LoginCodeChannel
      target: string
      code: string
    }

    /** 登录响应 */
    interface LoginResponse {
      token: string
      refreshToken: string
      mfaChallenge?: string
      mfaType?: 'email' | 'totp'
      mfaEmailMasked?: string
      mfaTotpDigits?: number
    }

    interface MfaChallengeSendParams {
      challenge: string
    }

    interface MfaChallengeVerifyParams {
      challenge: string
      code: string
    }

    /** 注册参数 */
    interface RegisterParams {
      username: string
      password: string
      confirmPassword: string
    }

    interface ForgotPasswordCodeParams {
      email: string
    }

    interface ResetPasswordParams {
      email: string
      code: string
      newPassword: string
      confirmPassword: string
    }

    /** 用户基础信息（仅保留支撑系统运行所需字段） */
    interface UserInfo {
      buttons: string[]
      roles: string[]
      userId: string
      userName: string
      email: string
      phone?: string
      avatar?: string
      language?: string
    }

    /** 用户详细信息（个人中心专用） */
    interface UserDetail {
      buttons: string[]
      roles: string[]
      tags?: string[]
      userId: string
      userName: string
      email: string
      phone?: string
      avatar?: string
      nickname?: string
      gender?: number
      realName?: string
      address?: string
      introduction?: string
      signature?: string
      position?: string
      company?: string
      language?: string
    }

    interface UserProfileUpdateCommand {
      realName: string
      nickname: string
      email?: string
      phone: string
      address?: string
      gender: number
      introduction?: string
      signature?: string
      position?: string
      company?: string
      tags?: string[]
    }

    interface UserPasswordUpdateCommand {
      currentPassword: string
      newPassword: string
      confirmPassword: string
    }

    interface AvatarUpdateCommand {
      avatarUrl: string
    }
  }

  /** 系统管理类型 */
  namespace SystemManage {
    interface RoleOption {
      roleId: string
      roleCode: string
      roleName: string
      roleLevel?: number
    }

    /** 用户列表 */
    type UserList = Api.Common.PaginatedResponse<UserListItem>

    /** 用户列表项 */
    interface UserListItem {
      id: string
      userId: string
      avatar: string
      status: string
      userName: string
      userGender: string
      nickName: string
      userPhone: string
      userEmail: string
      userRoles: string[]
      createBy: string
      createTime: string
      updateBy: string
      updateTime: string
    }

    /** 用户搜索参数 */
    type UserSearchParams = Partial<
      Pick<UserListItem, 'id' | 'userName' | 'userGender' | 'userPhone' | 'userEmail' | 'status'> &
        Api.Common.CommonSearchParams & {
          createTimeStart: string | null
          createTimeEnd: string | null
          updateTimeStart: string | null
          updateTimeEnd: string | null
        }
    >

    interface UserSaveCommand {
      username: string
      password?: string
      nickname: string
      phone: string
      email?: string
      gender: number
      roleCodes: string[]
    }

    /** 角色列表 */
    type RoleList = Api.Common.PaginatedResponse<RoleListItem>

    /** 角色列表项 */
    interface RoleListItem {
      roleId: string
      roleName: string
      roleCode: string
      description: string
      type: number
      typeName: string
      enabled: boolean
      createTime: string
      updateTime: string
    }

    /** 角色搜索参数 */
    type RoleSearchParams = Partial<
      Pick<RoleListItem, 'roleId' | 'roleName' | 'roleCode' | 'description' | 'enabled'> &
        Api.Common.CommonSearchParams & {
          type: number
          createTimeStart: string | null
          createTimeEnd: string | null
          updateTimeStart: string | null
          updateTimeEnd: string | null
          startTime: string | null
          endTime: string | null
        }
    >

    interface RoleSaveCommand {
      roleName: string
      roleCode: string
      description: string
      enabled: boolean
    }

    interface MenuSaveCommand {
      parentId?: string
      menuType: 'menu' | 'button'
      name: string
      title: string
      path?: string
      component?: string
      redirect?: string
      icon?: string
      authMark: string
      isEnable: boolean
      sort: number
      keepAlive?: boolean
      isHide?: boolean
      isHideTab?: boolean
      link?: string
      isIframe?: boolean
      showBadge?: boolean
      showTextBadge?: string
      fixedTab?: boolean
      activePath?: string
      isFullPage?: boolean
    }

    interface RoleMenuPermissionUpdateCommand {
      menuIds: string[]
    }
  }

  namespace User {
    namespace Security {
      interface MfaStatus {
        email: boolean
        totp: boolean
      }

      interface SecurityStatus {
        email: string
        emailMasked: string
        loginMethods: string[]
        effectiveLoginMethods: string[]
        allowedLoginMethods: string[]
        passwordRequired: boolean
        mfa: MfaStatus
        emailVerifiedAt?: string
        lastPasswordChangeAt?: string
      }

      interface LoginMethodsUpdateCommand {
        methods: string[]
      }

      type EmailRebindProofType = 'current_email_code' | 'totp' | 'password'

      interface EmailRebindProofVerifyCommand {
        proofType: EmailRebindProofType
        currentEmailCode?: string
        totpCode?: string
        currentPassword?: string
      }

      interface EmailRebindProof {
        proofTicket: string
        expiresInSeconds: number
      }

      interface EmailRebindSendCodeCommand {
        newEmail: string
        proofTicket: string
      }

      interface EmailRebindCommand {
        newEmail: string
        newEmailCode: string
        proofTicket: string
      }

      interface MfaEmailUpdateCommand {
        enabled: boolean
        code?: string
      }

      interface MfaTotpProvision {
        secret: string
        otpAuthUri: string
        issuer: string
        accountName: string
        digits: number
        periodSeconds: number
        algorithm: string
      }

      interface MfaTotpEnableCommand {
        secret: string
        code: string
      }

      interface MfaTotpDisableCommand {
        code: string
      }
    }
  }
}

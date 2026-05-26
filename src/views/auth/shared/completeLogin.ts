import { ElNotification } from 'element-plus'
import type { Router } from 'vue-router'
import type { useUserStore } from '@/store/modules/user'

interface CompleteLoginOptions {
  userStore: ReturnType<typeof useUserStore>
  token: string
  refreshToken?: string
  redirect?: string
  router: Router
  successTitle: string
  successMessage: string
}

/**
 * 统一登录完成流程：
 * token 入库 -> 拉取正式用户信息 -> 标记登录态 -> 跳转 -> 展示欢迎提示
 */
export async function completeLogin({
  userStore,
  token,
  refreshToken,
  redirect,
  router,
  successTitle,
  successMessage
}: CompleteLoginOptions): Promise<void> {
  userStore.setToken(token, refreshToken)

  try {
    const userInfo = await userStore.hydrateUserInfo({ force: true })
    userStore.setLoginStatus(true)

    await router.push(redirect || '/')

    window.setTimeout(() => {
      ElNotification({
        title: successTitle,
        type: 'success',
        duration: 2500,
        zIndex: 10000,
        message: userInfo.userName ? `${successMessage}, ${userInfo.userName}!` : successMessage
      })
    }, 80)
  } catch (error) {
    userStore.clearAuthState()
    throw error
  }
}

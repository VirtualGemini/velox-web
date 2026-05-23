<!-- 验证码登录页：验证码表单 -->
<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <AuthLoggedInGuard>
            <h3 class="title">
              {{ channel === 'email' ? $t('login.code.emailTitle') : $t('login.code.phoneTitle') }}
            </h3>
            <p class="sub-title">{{ $t('login.code.subTitle') }}</p>

            <ElForm
              ref="formRef"
              :model="formData"
              :rules="rules"
              :key="formKey"
              @keyup.enter="handleSubmit"
              style="margin-top: 25px"
            >
              <ElFormItem prop="target">
                <ElInput
                  class="custom-height"
                  v-model.trim="formData.target"
                  :placeholder="
                    channel === 'email'
                      ? $t('login.placeholder.email')
                      : $t('login.placeholder.phone')
                  "
                />
              </ElFormItem>

              <ElFormItem prop="code">
                <div class="flex gap-3 w-full">
                  <ElInput
                    class="custom-height flex-1"
                    v-model.trim="formData.code"
                    :placeholder="$t('login.placeholder.code')"
                  />
                  <ElButton
                    class="custom-height"
                    :disabled="countdown > 0 || sendingCode"
                    @click="handleSendCode"
                  >
                    {{ countdown > 0 ? `${countdown}s` : $t('login.code.sendBtn') }}
                  </ElButton>
                </div>
              </ElFormItem>

              <div style="margin-top: 30px">
                <ElButton
                  class="w-full custom-height"
                  type="primary"
                  @click="handleSubmit"
                  :loading="loading"
                  v-ripple
                >
                  {{ $t('login.btnText') }}
                </ElButton>
              </div>

              <div class="mt-5 flex-cb text-sm text-g-600">
                <RouterLink class="text-theme" :to="{ name: 'CodeLogin' }">{{
                  $t('login.channel.title')
                }}</RouterLink>
                <RouterLink class="text-theme" :to="{ name: 'Login' }">{{
                  $t('login.passwordLogin')
                }}</RouterLink>
              </div>
            </ElForm>
          </AuthLoggedInGuard>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { ElNotification, type FormInstance, type FormRules } from 'element-plus'
  import { fetchSendLoginCode, fetchLoginByCode } from '@/api/auth'
  import { useUserStore } from '@/store/modules/user'
  import { HttpError } from '@/utils/http/error'

  defineOptions({ name: 'CodeLoginVerify' })

  const { t, locale } = useI18n()
  const router = useRouter()
  const route = useRoute()
  const userStore = useUserStore()

  const channel = computed<Api.Auth.LoginCodeChannel>(() => {
    const value = route.query.channel
    return value === 'phone' ? 'phone' : 'email'
  })

  // 路由进入时无指定渠道，回到选择页
  onMounted(() => {
    if (!route.query.channel) {
      router.replace({ name: 'CodeLogin' })
    }
  })

  const formRef = ref<FormInstance>()
  const formKey = ref(0)
  const loading = ref(false)
  const sendingCode = ref(false)
  const countdown = ref(0)
  const timer = ref<number | null>(null)

  const formData = reactive({
    target: '',
    code: ''
  })

  watch(locale, () => {
    formKey.value++
  })

  const emailRegex = /^[\w.+-]+@[\w-]+\.[\w.-]+$/
  const phoneRegex = /^1[3-9]\d{9}$/

  const rules = computed<FormRules>(() => ({
    target: [
      {
        required: true,
        message:
          channel.value === 'email' ? t('login.placeholder.email') : t('login.placeholder.phone'),
        trigger: 'blur'
      },
      {
        validator: (_rule, value, callback) => {
          if (!value) return callback()
          if (channel.value === 'email' && !emailRegex.test(value)) {
            return callback(new Error(t('login.code.emailInvalid')))
          }
          if (channel.value === 'phone' && !phoneRegex.test(value)) {
            return callback(new Error(t('login.code.phoneInvalid')))
          }
          callback()
        },
        trigger: 'blur'
      }
    ],
    code: [{ required: true, message: t('login.placeholder.code'), trigger: 'blur' }]
  }))

  const startCountdown = () => {
    countdown.value = 60
    timer.value = window.setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0 && timer.value) {
        window.clearInterval(timer.value)
        timer.value = null
      }
    }, 1000)
  }

  const handleSendCode = async () => {
    if (!formRef.value) return
    try {
      await formRef.value.validateField('target')
    } catch {
      return
    }
    try {
      sendingCode.value = true
      await fetchSendLoginCode({ type: channel.value, target: formData.target })
      ElMessage.success(t('login.code.sent'))
      startCountdown()
    } catch (error) {
      if (!(error instanceof HttpError)) {
        console.error('[CodeLoginVerify] send code failed:', error)
      }
    } finally {
      sendingCode.value = false
    }
  }

  const handleSubmit = async () => {
    if (!formRef.value) return
    try {
      const valid = await formRef.value.validate()
      if (!valid) return

      loading.value = true
      const { token, refreshToken } = await fetchLoginByCode({
        type: channel.value,
        target: formData.target,
        code: formData.code
      })

      if (!token) {
        throw new Error('Login failed - no token received')
      }

      userStore.setToken(token, refreshToken)
      userStore.setLoginStatus(true)

      ElNotification({
        title: t('login.success.title'),
        type: 'success',
        duration: 2500,
        zIndex: 10000,
        message: t('login.success.message')
      })

      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    } catch (error) {
      if (!(error instanceof HttpError)) {
        console.error('[CodeLoginVerify] login failed:', error)
      }
    } finally {
      loading.value = false
    }
  }

  onBeforeUnmount(() => {
    if (timer.value) {
      window.clearInterval(timer.value)
    }
  })
</script>

<style scoped>
  @import '../login/style.css';
</style>

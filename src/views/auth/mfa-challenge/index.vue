<!-- 登录二段验证页：邮箱二段验证码 / TOTP 动态口令 -->
<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <AuthLoggedInGuard>
            <h3 class="title">{{ $t('login.mfaChallenge.title') }}</h3>
            <p class="sub-title">
              {{ subTitleText }}
            </p>

            <ElForm
              ref="formRef"
              :model="formData"
              :rules="rules"
              :key="formKey"
              @keyup.enter="handleSubmit"
              style="margin-top: 25px"
            >
              <ElFormItem prop="code">
                <div class="flex gap-3 w-full">
                  <ElInput
                    class="custom-height flex-1"
                    v-model.trim="formData.code"
                    :placeholder="codePlaceholder"
                    :maxlength="isTotp ? totpDigits : undefined"
                  />
                  <ElButton
                    v-if="!isTotp"
                    class="custom-height code-send-btn"
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
                <RouterLink class="text-theme" :to="{ name: 'Login' }">
                  {{ $t('login.mfaChallenge.backToLogin') }}
                </RouterLink>
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
  import { fetchSendMfaChallengeCode, fetchVerifyMfaChallenge } from '@/api/auth'
  import { useUserStore } from '@/store/modules/user'
  import { HttpError } from '@/utils/http/error'

  defineOptions({ name: 'MfaChallenge' })

  const { t, locale } = useI18n()
  const router = useRouter()
  const route = useRoute()
  const userStore = useUserStore()

  const formRef = ref<FormInstance>()
  const formKey = ref(0)
  const loading = ref(false)
  const sendingCode = ref(false)
  const countdown = ref(0)
  const timer = ref<number | null>(null)

  const challenge = computed(() => (route.query.challenge as string) || '')
  const target = computed(() => (route.query.target as string) || '')
  const mfaType = computed(() => (route.query.mfaType as string) || 'email')
  const totpDigits = computed(() => {
    const raw = Number(route.query.mfaTotpDigits)
    return Number.isFinite(raw) && raw > 0 ? raw : 6
  })
  const isTotp = computed(() => mfaType.value === 'totp')

  const subTitleText = computed(() => {
    if (isTotp.value) {
      return t('login.mfaChallenge.totpSubTitle', { digits: totpDigits.value })
    }
    return target.value
      ? t('login.mfaChallenge.subTitleWithTarget', { target: target.value })
      : t('login.mfaChallenge.subTitle')
  })

  const codePlaceholder = computed(() =>
    isTotp.value
      ? t('login.mfaChallenge.totpPlaceholder', { digits: totpDigits.value })
      : t('login.placeholder.code')
  )

  const formData = reactive({
    code: ''
  })

  watch(locale, () => {
    formKey.value++
  })

  const rules = computed<FormRules>(() => ({
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

  const ensureChallenge = () => {
    if (!challenge.value) {
      ElMessage.warning(t('login.mfaChallenge.missingChallenge'))
      router.replace({ name: 'Login' })
      return false
    }
    return true
  }

  const handleSendCode = async () => {
    if (!ensureChallenge()) return
    try {
      sendingCode.value = true
      await fetchSendMfaChallengeCode({ challenge: challenge.value })
      ElMessage.success(t('login.code.sent'))
      startCountdown()
    } catch (error) {
      if (!(error instanceof HttpError)) {
        console.error('[MfaChallenge] send code failed:', error)
      }
    } finally {
      sendingCode.value = false
    }
  }

  const handleSubmit = async () => {
    if (!formRef.value) return
    if (!ensureChallenge()) return
    try {
      const valid = await formRef.value.validate()
      if (!valid) return

      loading.value = true
      const { token, refreshToken } = await fetchVerifyMfaChallenge({
        challenge: challenge.value,
        code: formData.code
      })

      if (!token) {
        throw new Error('MFA verification failed - no token received')
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
        console.error('[MfaChallenge] verify failed:', error)
      }
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    ensureChallenge()
  })

  onBeforeUnmount(() => {
    if (timer.value) {
      window.clearInterval(timer.value)
    }
  })
</script>

<style scoped>
  @import '../login/style.css';
</style>

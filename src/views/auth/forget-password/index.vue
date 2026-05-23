<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <AuthLoggedInGuard>
            <h3 class="title">{{ $t('forgetPassword.title') }}</h3>
            <p class="sub-title">{{ $t('forgetPassword.subTitle') }}</p>

            <ElForm
              ref="formRef"
              :model="formData"
              :rules="rules"
              label-position="top"
              class="mt-7.5"
            >
              <ElFormItem prop="email">
                <ElInput
                  class="custom-height"
                  v-model.trim="formData.email"
                  :placeholder="$t('forgetPassword.placeholder')"
                />
              </ElFormItem>

              <ElFormItem prop="code">
                <div class="flex gap-3 w-full">
                  <ElInput
                    class="custom-height flex-1"
                    v-model.trim="formData.code"
                    :placeholder="$t('forgetPassword.codePlaceholder')"
                  />
                  <ElButton class="custom-height" :disabled="countdown > 0" @click="sendCode">
                    {{ countdown > 0 ? `${countdown}s` : $t('forgetPassword.sendCodeBtnText') }}
                  </ElButton>
                </div>
              </ElFormItem>

              <ElFormItem prop="newPassword">
                <ElInput
                  class="custom-height"
                  v-model.trim="formData.newPassword"
                  :placeholder="$t('forgetPassword.newPasswordPlaceholder')"
                  type="password"
                  show-password
                />
              </ElFormItem>

              <ElFormItem prop="confirmPassword">
                <ElInput
                  class="custom-height"
                  v-model.trim="formData.confirmPassword"
                  :placeholder="$t('forgetPassword.confirmPasswordPlaceholder')"
                  type="password"
                  show-password
                  @keyup.enter="resetPassword"
                />
              </ElFormItem>

              <div style="margin-top: 15px">
                <ElButton
                  class="w-full custom-height"
                  type="primary"
                  @click="resetPassword"
                  :loading="loading"
                  v-ripple
                >
                  {{ $t('forgetPassword.submitBtnText') }}
                </ElButton>
              </div>

              <div style="margin-top: 15px">
                <ElButton class="w-full custom-height" plain @click="toLogin">
                  {{ $t('forgetPassword.backBtnText') }}
                </ElButton>
              </div>
            </ElForm>
          </AuthLoggedInGuard>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchResetPassword, fetchSendResetPasswordCode } from '@/api/auth'

  defineOptions({ name: 'ForgetPassword' })

  const router = useRouter()
  const { t } = useI18n()
  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const countdown = ref(0)
  const timer = ref<number | null>(null)

  const formData = reactive({
    email: '',
    code: '',
    newPassword: '',
    confirmPassword: ''
  })

  const rules = computed<FormRules>(() => ({
    email: [
      { required: true, message: t('forgetPassword.placeholder'), trigger: 'blur' },
      { type: 'email', message: t('forgetPassword.emailInvalid'), trigger: 'blur' }
    ],
    code: [{ required: true, message: t('forgetPassword.codePlaceholder'), trigger: 'blur' }],
    newPassword: [
      { required: true, message: t('forgetPassword.newPasswordPlaceholder'), trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: t('forgetPassword.confirmPasswordPlaceholder'), trigger: 'blur' }
    ]
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

  const sendCode = async () => {
    if (!formData.email) {
      ElMessage.warning(t('forgetPassword.placeholder'))
      return
    }
    try {
      await fetchSendResetPasswordCode({ email: formData.email })
      ElMessage.success(t('forgetPassword.codeSent'))
      startCountdown()
    } catch (error) {
      console.error('发送验证码失败:', error)
    }
  }

  const resetPassword = async () => {
    if (!formRef.value) return
    try {
      await formRef.value.validate()
      if (formData.newPassword !== formData.confirmPassword) {
        ElMessage.warning(t('forgetPassword.passwordMismatch'))
        return
      }
      loading.value = true
      await fetchResetPassword({
        email: formData.email,
        code: formData.code,
        newPassword: formData.newPassword,
        confirmPassword: formData.confirmPassword
      })
      ElMessage.success(t('forgetPassword.resetSuccess'))
      toLogin()
    } catch (error) {
      console.error('重置密码失败:', error)
    } finally {
      loading.value = false
    }
  }

  const toLogin = () => {
    router.push({ name: 'Login' })
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

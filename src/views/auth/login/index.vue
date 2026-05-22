<!-- 登录页面 -->
<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <Transition name="login-slide" mode="out-in">
            <div v-if="showLoggedInCard" key="logged-in" class="form-panel">
              <h3 class="title">{{ $t('login.loggedIn.title') }}</h3>
              <p class="sub-title">{{ $t('login.loggedIn.subTitle') }}</p>
              <div style="margin-top: 25px">
                <VeloxLoggedInCard
                  :user-info="userInfo"
                  :selected="accountSelected"
                  @select="accountSelected = !accountSelected"
                  @logout="handleLogout"
                />
              </div>
              <div style="margin-top: 30px">
                <ElButton
                  class="w-full custom-height"
                  type="primary"
                  @click="handleContinue"
                  v-ripple
                >
                  {{ $t('login.loggedIn.continueBtn') }}
                </ElButton>
              </div>
            </div>

            <!-- 默认密码登录 -->
            <div v-else-if="mode === 'password'" key="password" class="form-panel">
              <h3 class="title">{{ $t('login.title') }}</h3>
              <p class="sub-title">{{ $t('login.subTitle') }}</p>
              <ElForm
                ref="formRef"
                :model="formData"
                :rules="rules"
                :key="formKey"
                @keyup.enter="handleSubmit"
                style="margin-top: 25px"
              >
                <ElFormItem prop="username">
                  <ElInput
                    class="custom-height"
                    :placeholder="$t('login.placeholder.username')"
                    v-model.trim="formData.username"
                  />
                </ElFormItem>
                <ElFormItem prop="password">
                  <ElInput
                    class="custom-height"
                    :placeholder="$t('login.placeholder.password')"
                    v-model.trim="formData.password"
                    type="password"
                    autocomplete="off"
                    show-password
                  />
                </ElFormItem>

                <!-- 推拽验证 -->
                <div class="relative pb-5 mt-6">
                  <div
                    class="relative z-[2] overflow-hidden select-none rounded-lg border border-transparent tad-300"
                    :class="{ '!border-[#FF4E4F]': !isPassing && isClickPass }"
                  >
                    <VeloxDragVerify
                      ref="dragVerify"
                      v-model:value="isPassing"
                      :text="$t('login.sliderText')"
                      textColor="var(--velox-gray-700)"
                      :successText="$t('login.sliderSuccessText')"
                      progressBarBg="var(--main-color)"
                      :background="isDark ? '#26272F' : '#F1F1F4'"
                      handlerBg="var(--default-box-color)"
                    />
                  </div>
                  <p
                    class="absolute top-0 z-[1] px-px mt-2 text-xs text-[#f56c6c] tad-300"
                    :class="{ 'translate-y-10': !isPassing && isClickPass }"
                  >
                    {{ $t('login.placeholder.slider') }}
                  </p>
                </div>

                <div class="flex-cb mt-2 text-sm">
                  <ElCheckbox v-model="formData.rememberPassword">{{
                    $t('login.rememberPwd')
                  }}</ElCheckbox>
                  <RouterLink class="text-theme" :to="{ name: 'ForgetPassword' }">{{
                    $t('login.forgetPwd')
                  }}</RouterLink>
                </div>

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

                <div class="mt-5 flex-cb text-sm text-gray-600">
                  <div>
                    <span>{{ $t('login.noAccount') }}</span>
                    <RouterLink class="text-theme" :to="{ name: 'Register' }">{{
                      $t('login.register')
                    }}</RouterLink>
                  </div>
                  <a class="text-theme cursor-pointer" @click="switchMode('channel')">{{
                    $t('login.otherLogin')
                  }}</a>
                </div>
              </ElForm>
            </div>

            <!-- 选择登录渠道 -->
            <div v-else-if="mode === 'channel'" key="channel" class="form-panel">
              <h3 class="title">{{ $t('login.channel.title') }}</h3>
              <p class="sub-title">{{ $t('login.channel.subTitle') }}</p>
              <div class="mt-7 flex flex-col gap-3">
                <div class="channel-card" @click="switchMode('codePhone')" v-ripple>
                  <div class="channel-icon">
                    <VeloxSvgIcon icon="ri:smartphone-line" />
                  </div>
                  <div class="channel-text">
                    <p class="channel-title">{{ $t('login.channel.phone') }}</p>
                    <p class="channel-desc">{{ $t('login.code.phoneTitle') }}</p>
                  </div>
                  <VeloxSvgIcon class="channel-arrow" icon="ri:arrow-right-s-line" />
                </div>
                <div class="channel-card" @click="switchMode('codeEmail')" v-ripple>
                  <div class="channel-icon">
                    <VeloxSvgIcon icon="ri:mail-line" />
                  </div>
                  <div class="channel-text">
                    <p class="channel-title">{{ $t('login.channel.email') }}</p>
                    <p class="channel-desc">{{ $t('login.code.emailTitle') }}</p>
                  </div>
                  <VeloxSvgIcon class="channel-arrow" icon="ri:arrow-right-s-line" />
                </div>
              </div>

              <div class="mt-6 text-sm text-gray-600 text-right">
                <a class="text-theme cursor-pointer" @click="switchMode('password')">{{
                  $t('login.passwordLogin')
                }}</a>
              </div>
            </div>

            <!-- 邮箱验证码登录 -->
            <div v-else-if="mode === 'codeEmail'" key="codeEmail" class="form-panel">
              <h3 class="title">{{ $t('login.code.emailTitle') }}</h3>
              <p class="sub-title">{{ $t('login.code.subTitle') }}</p>
              <ElForm
                ref="codeFormRef"
                :model="codeForm"
                :rules="emailRules"
                :key="`email-${formKey}`"
                @keyup.enter="handleCodeLogin"
                style="margin-top: 25px"
              >
                <ElFormItem prop="target">
                  <ElInput
                    class="custom-height"
                    :placeholder="$t('login.placeholder.email')"
                    v-model.trim="codeForm.target"
                  />
                </ElFormItem>

                <ElFormItem prop="code">
                  <div class="flex gap-3 w-full">
                    <ElInput
                      class="custom-height flex-1"
                      :placeholder="$t('login.placeholder.code')"
                      v-model.trim="codeForm.code"
                    />
                    <ElButton
                      class="custom-height"
                      :disabled="countdown > 0"
                      @click="sendCode('email')"
                    >
                      {{ countdown > 0 ? `${countdown}s` : $t('login.code.sendBtn') }}
                    </ElButton>
                  </div>
                </ElFormItem>

                <div style="margin-top: 30px">
                  <ElButton
                    class="w-full custom-height"
                    type="primary"
                    @click="handleCodeLogin"
                    :loading="loading"
                    v-ripple
                  >
                    {{ $t('login.btnText') }}
                  </ElButton>
                </div>

                <div class="mt-5 flex-cb text-sm text-gray-600">
                  <a class="text-theme cursor-pointer" @click="switchMode('channel')">{{
                    $t('login.otherLogin')
                  }}</a>
                  <a class="text-theme cursor-pointer" @click="switchMode('password')">{{
                    $t('login.passwordLogin')
                  }}</a>
                </div>
              </ElForm>
            </div>

            <!-- 手机号验证码登录（占位） -->
            <div v-else-if="mode === 'codePhone'" key="codePhone" class="form-panel">
              <h3 class="title">{{ $t('login.code.phoneTitle') }}</h3>
              <p class="sub-title">{{ $t('login.code.subTitle') }}</p>
              <ElForm
                ref="codeFormRef"
                :model="codeForm"
                :rules="phoneRules"
                :key="`phone-${formKey}`"
                @keyup.enter="handleCodeLogin"
                style="margin-top: 25px"
              >
                <ElFormItem prop="target">
                  <ElInput
                    class="custom-height"
                    :placeholder="$t('login.placeholder.phone')"
                    v-model.trim="codeForm.target"
                  />
                </ElFormItem>

                <ElFormItem prop="code">
                  <div class="flex gap-3 w-full">
                    <ElInput
                      class="custom-height flex-1"
                      :placeholder="$t('login.placeholder.code')"
                      v-model.trim="codeForm.code"
                    />
                    <ElButton
                      class="custom-height"
                      :disabled="countdown > 0"
                      @click="sendCode('phone')"
                    >
                      {{ countdown > 0 ? `${countdown}s` : $t('login.code.sendBtn') }}
                    </ElButton>
                  </div>
                </ElFormItem>

                <div style="margin-top: 30px">
                  <ElButton
                    class="w-full custom-height"
                    type="primary"
                    @click="handleCodeLogin"
                    :loading="loading"
                    v-ripple
                  >
                    {{ $t('login.btnText') }}
                  </ElButton>
                </div>

                <div class="mt-5 flex-cb text-sm text-gray-600">
                  <a class="text-theme cursor-pointer" @click="switchMode('channel')">{{
                    $t('login.otherLogin')
                  }}</a>
                  <a class="text-theme cursor-pointer" @click="switchMode('password')">{{
                    $t('login.passwordLogin')
                  }}</a>
                </div>
              </ElForm>
            </div>
          </Transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import AppConfig from '@/config'
  import { useUserStore } from '@/store/modules/user'
  import { useI18n } from 'vue-i18n'
  import { HttpError } from '@/utils/http/error'
  import { fetchLogin, fetchLoginByCode, fetchSendLoginCode } from '@/api/auth'
  import {
    ElMessage,
    ElMessageBox,
    ElNotification,
    type FormInstance,
    type FormRules
  } from 'element-plus'
  import { useSettingStore } from '@/store/modules/setting'

  defineOptions({ name: 'Login' })

  type LoginMode = 'password' | 'channel' | 'codeEmail' | 'codePhone'

  const EMAIL_REGEX = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
  const PHONE_REGEX = /^1[3-9]\d{9}$/
  const COUNTDOWN_SECONDS = 60

  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)
  const { t, locale } = useI18n()
  const formKey = ref(0)
  const mode = ref<LoginMode>('password')

  watch(locale, () => {
    formKey.value++
  })

  const dragVerify = ref()

  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()
  const isPassing = ref(false)
  const isClickPass = ref(false)

  const { getUserInfo: userInfo } = storeToRefs(userStore)
  const showLoggedInCard = ref(userStore.isLogin && !!userStore.accessToken)
  const accountSelected = ref(false)

  const systemName = AppConfig.systemInfo.name
  const formRef = ref<FormInstance>()
  const codeFormRef = ref<FormInstance>()

  const formData = reactive({
    username: '',
    password: '',
    rememberPassword: true
  })

  const codeForm = reactive({
    target: '',
    code: ''
  })

  const rules = computed<FormRules>(() => ({
    username: [{ required: true, message: t('login.placeholder.username'), trigger: 'blur' }],
    password: [{ required: true, message: t('login.placeholder.password'), trigger: 'blur' }]
  }))

  const emailRules = computed<FormRules>(() => ({
    target: [
      { required: true, message: t('login.placeholder.email'), trigger: 'blur' },
      {
        validator: (_rule, value: string, cb: (err?: Error) => void) => {
          if (value && !EMAIL_REGEX.test(value)) {
            cb(new Error(t('login.code.emailInvalid')))
            return
          }
          cb()
        },
        trigger: 'blur'
      }
    ],
    code: [{ required: true, message: t('login.placeholder.code'), trigger: 'blur' }]
  }))

  const phoneRules = computed<FormRules>(() => ({
    target: [
      { required: true, message: t('login.placeholder.phone'), trigger: 'blur' },
      {
        validator: (_rule, value: string, cb: (err?: Error) => void) => {
          if (value && !PHONE_REGEX.test(value)) {
            cb(new Error(t('login.code.phoneInvalid')))
            return
          }
          cb()
        },
        trigger: 'blur'
      }
    ],
    code: [{ required: true, message: t('login.placeholder.code'), trigger: 'blur' }]
  }))

  const loading = ref(false)
  const countdown = ref(0)
  const countdownTimer = ref<number | null>(null)

  const switchMode = (next: LoginMode) => {
    mode.value = next
    codeForm.target = ''
    codeForm.code = ''
    isClickPass.value = false
    formKey.value++
  }

  const startCountdown = () => {
    countdown.value = COUNTDOWN_SECONDS
    countdownTimer.value = window.setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0 && countdownTimer.value) {
        window.clearInterval(countdownTimer.value)
        countdownTimer.value = null
      }
    }, 1000)
  }

  const sendCode = async (channel: 'email' | 'phone') => {
    if (!codeFormRef.value) return
    try {
      await codeFormRef.value.validateField('target')
    } catch {
      return
    }

    if (channel === 'phone') {
      ElMessage.info(t('login.code.phoneTodo'))
      return
    }

    try {
      await fetchSendLoginCode({ type: channel, target: codeForm.target })
      ElMessage.success(t('login.code.sent'))
      startCountdown()
    } catch (error) {
      console.error('[Login] send code failed:', error)
    }
  }

  // 继续使用当前已登录账号
  const handleContinue = () => {
    if (!accountSelected.value) {
      ElMessage.warning(t('login.loggedIn.selectTip'))
      return
    }
    const redirect = route.query.redirect as string | undefined
    router.push(redirect || '/')
  }

  const handleLogout = () => {
    ElMessageBox.confirm(t('common.logOutTips'), t('common.tips'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      customClass: 'login-out-dialog'
    })
      .then(() => {
        showLoggedInCard.value = false
        userStore.logOut()
      })
      .catch(() => {})
  }

  // 密码登录
  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      const valid = await formRef.value.validate()
      if (!valid) return

      if (!isPassing.value) {
        isClickPass.value = true
        return
      }

      loading.value = true

      const { username, password } = formData

      const { token, refreshToken } = await fetchLogin({
        userName: username,
        password
      })

      finalizeLogin(token, refreshToken)
    } catch (error) {
      if (error instanceof HttpError) {
        // 已在拦截器中提示
      } else {
        console.error('[Login] Unexpected error:', error)
      }
    } finally {
      loading.value = false
      resetDragVerify()
    }
  }

  // 验证码登录
  const handleCodeLogin = async () => {
    if (!codeFormRef.value) return
    try {
      const valid = await codeFormRef.value.validate()
      if (!valid) return

      if (mode.value === 'codePhone') {
        ElMessage.info(t('login.code.phoneTodo'))
        return
      }

      loading.value = true
      const { token, refreshToken } = await fetchLoginByCode({
        type: 'email',
        target: codeForm.target,
        code: codeForm.code
      })
      finalizeLogin(token, refreshToken)
    } catch (error) {
      if (!(error instanceof HttpError)) {
        console.error('[Login] code login error:', error)
      }
    } finally {
      loading.value = false
    }
  }

  const finalizeLogin = (token: string, refreshToken: string) => {
    if (!token) {
      throw new Error('Login failed - no token received')
    }
    userStore.setToken(token, refreshToken)
    userStore.setLoginStatus(true)
    showLoginSuccessNotice()
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  }

  const resetDragVerify = () => {
    dragVerify.value?.reset?.()
  }

  const showLoginSuccessNotice = () => {
    setTimeout(() => {
      ElNotification({
        title: t('login.success.title'),
        type: 'success',
        duration: 2500,
        zIndex: 10000,
        message: `${t('login.success.message')}, ${systemName}!`
      })
    }, 1000)
  }

  onBeforeUnmount(() => {
    if (countdownTimer.value) {
      window.clearInterval(countdownTimer.value)
    }
  })
</script>

<style scoped>
  @import './style.css';
</style>

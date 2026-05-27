<!-- 验证码登录页：选择登录方式 -->
<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <AuthLoggedInGuard>
            <h3 class="title">{{ $t('login.channel.title') }}</h3>
            <p class="sub-title">{{ $t('login.channel.subTitle') }}</p>

            <div class="mt-7.5 flex flex-col gap-3">
              <div class="channel-card" @click="selectChannel('email')">
                <div class="channel-icon channel-icon--bare">
                  <img
                    :src="emailIcon"
                    :alt="$t('login.channel.email')"
                    style="width: 24px; height: 24px"
                  />
                </div>
                <div class="channel-text">
                  <div class="channel-title">{{ $t('login.channel.email') }}</div>
                </div>
                <VeloxSvgIcon icon="ri:arrow-right-s-line" class="channel-arrow" />
              </div>

              <div class="channel-card channel-card--disabled">
                <div class="channel-icon channel-icon--bare">
                  <img
                    :src="githubIcon"
                    :alt="$t('login.channel.github')"
                    style="width: 24px; height: 24px"
                  />
                </div>
                <div class="channel-text">
                  <div class="channel-title">{{ $t('login.channel.github') }}</div>
                </div>
                <span class="channel-tag">{{ $t('login.channel.comingSoon') }}</span>
              </div>

              <div class="channel-card channel-card--disabled">
                <div class="channel-icon channel-icon--bare">
                  <img
                    :src="linuxdoIcon"
                    :alt="$t('login.channel.linuxdo')"
                    style="width: 24px; height: 24px"
                  />
                </div>
                <div class="channel-text">
                  <div class="channel-title">{{ $t('login.channel.linuxdo') }}</div>
                </div>
                <span class="channel-tag">{{ $t('login.channel.comingSoon') }}</span>
              </div>
            </div>

            <div class="mt-7.5 text-sm text-g-600">
              <RouterLink class="text-theme" :to="{ name: 'Login' }">{{
                $t('login.passwordLogin')
              }}</RouterLink>
            </div>
          </AuthLoggedInGuard>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import githubDarkIcon from '@/assets/images/svg/github.svg'
  import githubLightIcon from '@/assets/images/svg/github-light.svg'
  import gmailCleanerDarkIcon from '@/assets/images/svg/gmail-cleaner.svg'
  import gmailCleanerLightIcon from '@/assets/images/svg/gmail-cleaner-light.svg'
  import linuxdoIcon from '@/assets/images/svg/linuxdo.svg'
  import { useSettingStore } from '@/store/modules/setting'
  import { grantAuthRouteAccess } from '../shared/routeAccess'

  defineOptions({ name: 'CodeLogin' })

  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)
  const router = useRouter()
  const route = useRoute()

  const emailIcon = computed(() => (isDark.value ? gmailCleanerLightIcon : gmailCleanerDarkIcon))
  const githubIcon = computed(() => (isDark.value ? githubLightIcon : githubDarkIcon))

  const selectChannel = (next: Api.Auth.LoginCodeChannel) => {
    grantAuthRouteAccess('CodeLoginVerify')
    router.push({
      name: 'CodeLoginVerify',
      query: { channel: next, ...(route.query.redirect ? { redirect: route.query.redirect } : {}) }
    })
  }
</script>

<style scoped>
  @import '../login/style.css';
</style>

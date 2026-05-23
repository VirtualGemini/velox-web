<!-- 验证码登录页：选择登录方式（邮箱 / 手机号） -->
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
                <div class="channel-icon">
                  <VeloxSvgIcon icon="ri:mail-line" />
                </div>
                <div class="channel-text">
                  <div class="channel-title">{{ $t('login.channel.email') }}</div>
                  <div class="channel-desc">{{ $t('login.placeholder.email') }}</div>
                </div>
                <VeloxSvgIcon icon="ri:arrow-right-s-line" class="channel-arrow" />
              </div>

              <div class="channel-card" @click="selectChannel('phone')">
                <div class="channel-icon">
                  <VeloxSvgIcon icon="ri:smartphone-line" />
                </div>
                <div class="channel-text">
                  <div class="channel-title">{{ $t('login.channel.phone') }}</div>
                  <div class="channel-desc">{{ $t('login.placeholder.phone') }}</div>
                </div>
                <VeloxSvgIcon icon="ri:arrow-right-s-line" class="channel-arrow" />
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
  defineOptions({ name: 'CodeLogin' })

  const router = useRouter()
  const route = useRoute()

  const selectChannel = (next: Api.Auth.LoginCodeChannel) => {
    router.push({
      name: 'CodeLoginVerify',
      query: { channel: next, ...(route.query.redirect ? { redirect: route.query.redirect } : {}) }
    })
  }
</script>

<style scoped>
  @import './style.css';
</style>

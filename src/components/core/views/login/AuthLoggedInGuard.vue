<!-- 授权页：已登录账号卡片守卫，挂载时若已登录则替换路由内容 -->
<template>
  <template v-if="showLoggedInCard">
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
      <ElButton class="w-full custom-height" type="primary" @click="handleContinue" v-ripple>
        {{ $t('login.loggedIn.continueBtn') }}
      </ElButton>
    </div>
  </template>
  <slot v-else />
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useUserStore } from '@/store/modules/user'

  defineOptions({ name: 'AuthLoggedInGuard' })

  const { t } = useI18n()
  const router = useRouter()
  const route = useRoute()
  const userStore = useUserStore()
  const { getUserInfo: userInfo } = storeToRefs(userStore)

  const showLoggedInCard = ref(userStore.isLogin && !!userStore.accessToken)
  const accountSelected = ref(false)

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
</script>

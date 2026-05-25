<script setup lang="ts">
  import { computed, ref } from 'vue'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useSecurityStatus } from '../../composables/useSecurityStatus'
  import RebindEmailDialog from './dialogs/RebindEmailDialog.vue'
  import EnableMfaEmailDialog from './dialogs/EnableMfaEmailDialog.vue'
  import EnableMfaTotpDialog from './dialogs/EnableMfaTotpDialog.vue'
  import DisableMfaTotpDialog from './dialogs/DisableMfaTotpDialog.vue'
  import { fetchUpdateLoginMethods, fetchUpdateMfaEmail } from '@/api/security'

  defineProps<{
    detail: Partial<Api.Auth.UserDetail>
  }>()

  const emit = defineEmits<{
    (e: 'detail-updated', patch: Partial<Api.Auth.UserDetail>): void
  }>()

  const { t } = useI18n()
  const { status, loading, load, refresh } = useSecurityStatus()

  const rebindEmailVisible = ref(false)
  const mfaDialogVisible = ref(false)
  const mfaTargetEnabled = ref(false)
  const mfaTotpEnableVisible = ref(false)
  const mfaTotpDisableVisible = ref(false)

  load()

  const loginMethodKeys = ['password', 'email_code'] as const

  const isMethodEnabled = (key: string) => status.value?.loginMethods?.includes(key) ?? false
  const isMethodAllowed = (key: string) => status.value?.allowedLoginMethods?.includes(key) ?? false

  const passwordRequired = computed(() => status.value?.passwordRequired ?? true)
  const emailMfaOn = computed(() => status.value?.mfa?.email ?? false)
  const totpMfaOn = computed(() => status.value?.mfa?.totp ?? false)

  const onMethodChange = async (key: string, value: boolean) => {
    if (!status.value) return
    const next = new Set(status.value.loginMethods || [])
    if (value) next.add(key)
    else next.delete(key)

    if (next.size === 0) {
      ElMessage.warning(t('pages.system.userCenter.security.loginMethods.atLeastOne'))
      return refresh()
    }
    if (passwordRequired.value && !next.has('password')) {
      ElMessage.warning(t('pages.system.userCenter.security.loginMethods.passwordLocked'))
      return refresh()
    }
    try {
      await fetchUpdateLoginMethods({ methods: Array.from(next) })
      ElMessage.success(t('pages.system.userCenter.security.loginMethods.saveSuccess'))
      await refresh()
    } catch {
      await refresh()
    }
  }

  const onMfaEmailSwitchClick = () => {
    if (totpMfaOn.value) {
      ElMessage.warning(t('pages.system.userCenter.security.mfa.mutualExclusive'))
      return
    }
    if (!status.value?.email) {
      ElMessage.warning(t('pages.system.userCenter.security.mfa.email.noEmail'))
      return
    }
    mfaTargetEnabled.value = !emailMfaOn.value
    mfaDialogVisible.value = true
  }

  const onMfaDisableConfirm = async () => {
    try {
      await fetchUpdateMfaEmail({ enabled: false })
      ElMessage.success(t('pages.system.userCenter.security.mfa.email.disableSuccess'))
      await refresh()
    } catch {
      // 错误由全局拦截器提示
    }
  }

  const onMfaTotpSwitchChange = (val: boolean) => {
    if (val) {
      if (emailMfaOn.value) {
        ElMessage.warning(t('pages.system.userCenter.security.mfa.mutualExclusive'))
        refresh()
        return
      }
      mfaTotpEnableVisible.value = true
    } else {
      mfaTotpDisableVisible.value = true
    }
  }

  const onEmailRebound = (newEmail: string) => {
    emit('detail-updated', { email: newEmail })
    refresh()
  }

  const onMfaEmailEnabled = () => {
    refresh()
  }

  const onMfaTotpEnabled = () => {
    refresh()
  }

  const onMfaTotpDisabled = () => {
    refresh()
  }
</script>

<template>
  <div class="space-y-4">
    <div class="velox-card-sm">
      <h1 class="p-4 text-xl font-normal border-b border-g-300">
        {{ t('pages.system.userCenter.security.title') }}
      </h1>
      <div v-loading="loading" class="p-5 space-y-6">
        <!-- 邮箱 -->
        <div class="flex items-start justify-between">
          <div>
            <div class="text-base font-medium">
              {{ t('pages.system.userCenter.security.email.title') }}
            </div>
            <div class="text-xs text-g-600 mt-1">
              {{ t('pages.system.userCenter.security.email.desc') }}
            </div>
            <div class="text-sm mt-2">
              {{ status?.email || t('pages.system.userCenter.security.email.unbound') }}
            </div>
          </div>
          <ElButton type="primary" link @click="rebindEmailVisible = true">
            {{
              status?.email
                ? t('pages.system.userCenter.security.email.rebind')
                : t('pages.system.userCenter.security.email.bind')
            }}
          </ElButton>
        </div>
      </div>
    </div>

    <!-- 登录方式 -->
    <div class="velox-card-sm">
      <h2 class="p-4 text-base font-medium border-b border-g-300">
        {{ t('pages.system.userCenter.security.loginMethods.title') }}
      </h2>
      <div v-loading="loading" class="p-5 space-y-4">
        <div class="text-xs text-g-600">
          {{ t('pages.system.userCenter.security.loginMethods.desc') }}
        </div>
        <div v-for="key in loginMethodKeys" :key="key" class="flex items-center justify-between">
          <div>
            <div class="text-sm">
              {{ t(`pages.system.userCenter.security.loginMethods.items.${key}`) }}
            </div>
            <div v-if="!isMethodAllowed(key)" class="text-xs text-g-500 mt-1">
              {{ t('pages.system.userCenter.security.loginMethods.globalDisabled') }}
            </div>
            <div v-else-if="key === 'password' && passwordRequired" class="text-xs text-g-500 mt-1">
              {{ t('pages.system.userCenter.security.loginMethods.passwordLocked') }}
            </div>
          </div>
          <ElSwitch
            :model-value="isMethodEnabled(key)"
            :disabled="!isMethodAllowed(key) || (key === 'password' && passwordRequired)"
            @change="(val: string | number | boolean) => onMethodChange(key, Boolean(val))"
          />
        </div>
      </div>
    </div>

    <!-- MFA -->
    <div class="velox-card-sm">
      <h2 class="p-4 text-base font-medium border-b border-g-300">
        {{ t('pages.system.userCenter.security.mfa.title') }}
      </h2>
      <div v-loading="loading" class="p-5 space-y-4">
        <div class="text-xs text-g-600">
          {{ t('pages.system.userCenter.security.mfa.desc') }}
        </div>

        <!-- TOTP（推荐） -->
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm flex items-center gap-2">
              {{ t('pages.system.userCenter.security.mfa.items.totp') }}
              <span class="px-1.5 py-0.5 text-[10px] rounded bg-theme/10 text-theme">
                {{ t('pages.system.userCenter.security.mfa.recommended') }}
              </span>
              <span v-if="totpMfaOn" class="ml-1 text-xs text-green-600">
                {{ t('pages.system.userCenter.security.mfa.enabled') }}
              </span>
            </div>
            <div class="text-xs text-g-500 mt-1">
              {{ t('pages.system.userCenter.security.mfa.totp.desc') }}
            </div>
          </div>
          <ElSwitch
            :model-value="totpMfaOn"
            @click.stop
            @change="(val: string | number | boolean) => onMfaTotpSwitchChange(Boolean(val))"
          />
        </div>

        <!-- 邮箱二段 -->
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm">
              {{ t('pages.system.userCenter.security.mfa.items.email') }}
              <span v-if="emailMfaOn" class="ml-2 text-xs text-green-600">
                {{ t('pages.system.userCenter.security.mfa.enabled') }}
              </span>
            </div>
            <div v-if="totpMfaOn" class="text-xs text-g-500 mt-1">
              {{ t('pages.system.userCenter.security.mfa.mutualExclusive') }}
            </div>
          </div>
          <ElSwitch
            :model-value="emailMfaOn"
            :disabled="totpMfaOn"
            @click.stop
            @change="
              (val: string | number | boolean) => {
                if (val) onMfaEmailSwitchClick()
                else onMfaDisableConfirm()
              }
            "
          />
        </div>
      </div>
    </div>

    <RebindEmailDialog
      v-model:visible="rebindEmailVisible"
      :current-email="status?.email"
      :totp-enabled="status?.mfa?.totp ?? false"
      @success="onEmailRebound"
    />
    <EnableMfaEmailDialog
      v-model:visible="mfaDialogVisible"
      :enable="mfaTargetEnabled"
      @success="onMfaEmailEnabled"
    />
    <EnableMfaTotpDialog v-model:visible="mfaTotpEnableVisible" @success="onMfaTotpEnabled" />
    <DisableMfaTotpDialog v-model:visible="mfaTotpDisableVisible" @success="onMfaTotpDisabled" />
  </div>
</template>

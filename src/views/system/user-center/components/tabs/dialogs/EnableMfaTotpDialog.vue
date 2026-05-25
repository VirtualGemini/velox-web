<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import QrcodeVue from 'qrcode.vue'
  import { fetchEnableMfaTotp, fetchProvisionMfaTotp } from '@/api/security'

  const props = defineProps<{ visible: boolean }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }>()

  const { t } = useI18n()
  const formRef = ref<FormInstance>()
  const form = reactive({ code: '' })
  const submitting = ref(false)
  const loading = ref(false)
  const provision = ref<Api.User.Security.MfaTotpProvision | null>(null)
  const showSecret = ref(false)

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const formattedSecret = computed(() => {
    const s = provision.value?.secret ?? ''
    return s.match(/.{1,4}/g)?.join(' ') ?? ''
  })

  const expectedDigits = computed(() => provision.value?.digits ?? 6)

  const rules = computed<FormRules>(() => ({
    code: [
      {
        required: true,
        message: t('pages.system.userCenter.security.mfa.totp.codeRequired'),
        trigger: 'blur'
      },
      {
        pattern: new RegExp(`^\\d{${expectedDigits.value}}$`),
        message: t('pages.system.userCenter.security.mfa.totp.codeFormat', {
          digits: expectedDigits.value
        }),
        trigger: 'blur'
      }
    ]
  }))

  const loadProvision = async () => {
    loading.value = true
    try {
      provision.value = await fetchProvisionMfaTotp()
    } catch {
      dialogVisible.value = false
    } finally {
      loading.value = false
    }
  }

  watch(
    () => props.visible,
    (v) => {
      if (v) {
        form.code = ''
        showSecret.value = false
        provision.value = null
        formRef.value?.clearValidate()
        loadProvision()
      }
    }
  )

  const copySecret = async () => {
    if (!provision.value?.secret) return
    try {
      await navigator.clipboard.writeText(provision.value.secret)
      ElMessage.success(t('pages.system.userCenter.security.mfa.totp.secretCopied'))
    } catch {
      ElMessage.warning(t('pages.system.userCenter.security.mfa.totp.copyFailed'))
    }
  }

  const submit = async () => {
    if (!provision.value) return
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    submitting.value = true
    try {
      await fetchEnableMfaTotp({
        secret: provision.value.secret,
        code: form.code.trim()
      })
      ElMessage.success(t('pages.system.userCenter.security.mfa.totp.enableSuccess'))
      emit('success')
      dialogVisible.value = false
    } finally {
      submitting.value = false
    }
  }
</script>

<template>
  <ElDialog
    v-model="dialogVisible"
    :title="t('pages.system.userCenter.security.mfa.totp.enableTitle')"
    width="520px"
    align-center
    :close-on-click-modal="false"
  >
    <div v-loading="loading" class="space-y-4">
      <div class="text-sm text-g-700">
        {{ t('pages.system.userCenter.security.mfa.totp.tip') }}
      </div>

      <div v-if="provision" class="flex flex-col items-center gap-3 py-3">
        <QrcodeVue :value="provision.otpAuthUri" :size="200" level="M" />
        <div class="text-xs text-g-500">
          {{ t('pages.system.userCenter.security.mfa.totp.scanTip') }}
        </div>
      </div>

      <div v-if="provision" class="space-y-1">
        <div class="flex items-center justify-between text-xs text-g-600">
          <span>{{ t('pages.system.userCenter.security.mfa.totp.secretLabel') }}</span>
          <div class="flex items-center gap-2">
            <ElButton link size="small" @click="showSecret = !showSecret">
              {{
                showSecret
                  ? t('pages.system.userCenter.security.mfa.totp.hideSecret')
                  : t('pages.system.userCenter.security.mfa.totp.showSecret')
              }}
            </ElButton>
            <ElButton link size="small" @click="copySecret">
              {{ t('pages.system.userCenter.security.mfa.totp.copySecret') }}
            </ElButton>
          </div>
        </div>
        <div class="font-mono text-sm bg-g-100 dark:bg-[#26272F] rounded px-3 py-2 break-all">
          {{ showSecret ? formattedSecret : '•••• •••• •••• ••••' }}
        </div>
      </div>

      <ElForm ref="formRef" :model="form" :rules="rules" label-position="top">
        <ElFormItem :label="t('pages.system.userCenter.security.mfa.totp.code')" prop="code">
          <ElInput
            v-model="form.code"
            :placeholder="
              t('pages.system.userCenter.security.mfa.totp.codePlaceholder', {
                digits: expectedDigits
              })
            "
            :maxlength="expectedDigits"
          />
        </ElFormItem>
      </ElForm>
    </div>
    <template #footer>
      <ElButton @click="dialogVisible = false">{{ t('common.cancel') }}</ElButton>
      <ElButton type="primary" :loading="submitting" :disabled="!provision" @click="submit">
        {{ t('common.confirm') }}
      </ElButton>
    </template>
  </ElDialog>
</template>

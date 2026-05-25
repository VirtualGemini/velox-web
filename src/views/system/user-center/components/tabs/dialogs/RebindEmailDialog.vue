<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { HttpError } from '@/utils/http/error'
  import {
    fetchRebindEmail,
    fetchSendEmailRebindCode,
    fetchSendEmailRebindProofCode,
    fetchVerifyEmailRebindProof
  } from '@/api/security'

  type StepKey = 'verify_identity' | 'bind_new_email'

  const props = defineProps<{
    visible: boolean
    currentEmail?: string
    totpEnabled: boolean
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'success', newEmail: string): void
  }>()

  const { t } = useI18n()

  const identityFormRef = ref<FormInstance>()
  const bindFormRef = ref<FormInstance>()

  const identityForm = reactive({
    currentEmailCode: '',
    totpCode: '',
    currentPassword: ''
  })
  const bindForm = reactive({
    newEmail: '',
    newEmailCode: ''
  })

  const step = ref<StepKey>('verify_identity')
  const proofTicket = ref('')
  const proofExpiresInSeconds = ref(0)
  const proofCooldown = ref(0)
  const bindCooldown = ref(0)
  const sendingProofCode = ref(false)
  const verifyingIdentity = ref(false)
  const sendingBindCode = ref(false)
  const submitting = ref(false)

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const proofType = computed<Api.User.Security.EmailRebindProofType>(() => {
    if (props.currentEmail?.trim()) return 'current_email_code'
    return props.totpEnabled ? 'totp' : 'password'
  })

  const isCurrentEmailProof = computed(() => proofType.value === 'current_email_code')
  const isTotpProof = computed(() => proofType.value === 'totp')
  const dialogTitle = computed(() =>
    props.currentEmail?.trim()
      ? t('pages.system.userCenter.security.email.dialog.title')
      : t('pages.system.userCenter.security.email.dialog.bindTitle')
  )

  const identityRules: FormRules = {
    currentEmailCode: [
      {
        required: isCurrentEmailProof.value,
        message: t('pages.system.userCenter.security.email.dialog.currentEmailCodeRequired'),
        trigger: 'blur'
      }
    ],
    totpCode: [
      {
        required: isTotpProof.value,
        message: t('pages.system.userCenter.security.email.dialog.totpCodeRequired'),
        trigger: 'blur'
      }
    ],
    currentPassword: [
      {
        required: proofType.value === 'password',
        message: t('pages.system.userCenter.security.email.dialog.currentPasswordRequired'),
        trigger: 'blur'
      }
    ]
  }

  const bindRules: FormRules = {
    newEmail: [
      {
        required: true,
        message: t('pages.system.userCenter.security.email.dialog.emailRequired'),
        trigger: 'blur'
      },
      {
        type: 'email',
        message: t('pages.system.userCenter.security.email.dialog.emailInvalid'),
        trigger: 'blur'
      }
    ],
    newEmailCode: [
      {
        required: true,
        message: t('pages.system.userCenter.security.email.dialog.codeRequired'),
        trigger: 'blur'
      }
    ]
  }

  let proofCooldownTimer: number | undefined
  let bindCooldownTimer: number | undefined

  const clearTimer = (timerId?: number) => {
    if (timerId) {
      window.clearInterval(timerId)
    }
  }

  const startCooldown = (target: 'proof' | 'bind') => {
    const setter = target === 'proof' ? proofCooldown : bindCooldown
    setter.value = 60
    const timer = window.setInterval(() => {
      setter.value -= 1
      if (setter.value <= 0) {
        window.clearInterval(timer)
      }
    }, 1000)
    if (target === 'proof') {
      clearTimer(proofCooldownTimer)
      proofCooldownTimer = timer
      return
    }
    clearTimer(bindCooldownTimer)
    bindCooldownTimer = timer
  }

  const resetState = () => {
    step.value = 'verify_identity'
    proofTicket.value = ''
    proofExpiresInSeconds.value = 0
    proofCooldown.value = 0
    bindCooldown.value = 0
    identityForm.currentEmailCode = ''
    identityForm.totpCode = ''
    identityForm.currentPassword = ''
    bindForm.newEmail = ''
    bindForm.newEmailCode = ''
    identityFormRef.value?.clearValidate()
    bindFormRef.value?.clearValidate()
    clearTimer(proofCooldownTimer)
    clearTimer(bindCooldownTimer)
    proofCooldownTimer = undefined
    bindCooldownTimer = undefined
  }

  watch(
    () => props.visible,
    (visible) => {
      if (!visible) {
        resetState()
      }
    }
  )

  const handleProofExpired = (error: unknown) => {
    if (!(error instanceof HttpError)) return false
    if (error.code !== 12047) return false
    ElMessage.warning(t('pages.system.userCenter.security.email.dialog.proofExpired'))
    step.value = 'verify_identity'
    proofTicket.value = ''
    proofExpiresInSeconds.value = 0
    bindForm.newEmailCode = ''
    bindFormRef.value?.clearValidate()
    return true
  }

  const sendProofCode = async () => {
    sendingProofCode.value = true
    try {
      await fetchSendEmailRebindProofCode()
      ElMessage.success(t('pages.system.userCenter.security.email.dialog.codeSent'))
      startCooldown('proof')
    } finally {
      sendingProofCode.value = false
    }
  }

  const verifyIdentity = async () => {
    const valid = await identityFormRef.value?.validate().catch(() => false)
    if (!valid) return
    verifyingIdentity.value = true
    try {
      const result = await fetchVerifyEmailRebindProof({
        proofType: proofType.value,
        currentEmailCode: identityForm.currentEmailCode.trim() || undefined,
        totpCode: identityForm.totpCode.trim() || undefined,
        currentPassword: identityForm.currentPassword.trim() || undefined
      })
      proofTicket.value = result.proofTicket
      proofExpiresInSeconds.value = result.expiresInSeconds
      step.value = 'bind_new_email'
      ElMessage.success(t('pages.system.userCenter.security.email.dialog.verifySuccess'))
    } finally {
      verifyingIdentity.value = false
    }
  }

  const sendBindCode = async () => {
    const ok = await bindFormRef.value?.validateField('newEmail').catch(() => false)
    if (!ok) return
    if (bindForm.newEmail.trim() === (props.currentEmail || '').trim()) {
      ElMessage.warning(t('pages.system.userCenter.security.email.dialog.emailSame'))
      return
    }
    sendingBindCode.value = true
    try {
      await fetchSendEmailRebindCode({
        newEmail: bindForm.newEmail.trim(),
        proofTicket: proofTicket.value
      })
      ElMessage.success(t('pages.system.userCenter.security.email.dialog.codeSent'))
      startCooldown('bind')
    } catch (error) {
      if (handleProofExpired(error)) return
      throw error
    } finally {
      sendingBindCode.value = false
    }
  }

  const submit = async () => {
    const valid = await bindFormRef.value?.validate().catch(() => false)
    if (!valid) return
    submitting.value = true
    try {
      await fetchRebindEmail({
        newEmail: bindForm.newEmail.trim(),
        newEmailCode: bindForm.newEmailCode.trim(),
        proofTicket: proofTicket.value
      })
      ElMessage.success(
        props.currentEmail?.trim()
          ? t('pages.system.userCenter.security.email.dialog.success')
          : t('pages.system.userCenter.security.email.dialog.bindSuccess')
      )
      emit('success', bindForm.newEmail.trim())
      dialogVisible.value = false
    } catch (error) {
      if (handleProofExpired(error)) return
      throw error
    } finally {
      submitting.value = false
    }
  }
</script>

<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="480px"
    align-center
    :close-on-click-modal="false"
  >
    <div class="mb-4 text-xs text-g-500">
      {{
        step === 'verify_identity'
          ? t('pages.system.userCenter.security.email.dialog.verifyStep')
          : t('pages.system.userCenter.security.email.dialog.bindStep')
      }}
    </div>

    <ElForm
      v-if="step === 'verify_identity'"
      ref="identityFormRef"
      :model="identityForm"
      :rules="identityRules"
      label-position="top"
    >
      <div class="mb-4 text-sm text-g-600">
        {{ t('pages.system.userCenter.security.email.dialog.proofTip') }}
      </div>

      <ElFormItem :label="t('pages.system.userCenter.security.email.dialog.proofTypeLabel')">
        <ElInput
          :model-value="
            isCurrentEmailProof
              ? t('pages.system.userCenter.security.email.dialog.proofCurrentEmail')
              : isTotpProof
                ? t('pages.system.userCenter.security.email.dialog.proofTotp')
                : t('pages.system.userCenter.security.email.dialog.proofPassword')
          "
          disabled
        />
      </ElFormItem>

      <div v-if="isCurrentEmailProof" class="mb-4 text-sm text-g-600">
        {{
          t('pages.system.userCenter.security.email.dialog.currentMasked', {
            email: props.currentEmail || ''
          })
        }}
      </div>

      <ElFormItem
        v-if="isCurrentEmailProof"
        :label="t('pages.system.userCenter.security.email.dialog.currentEmailCode')"
        prop="currentEmailCode"
      >
        <div class="flex gap-2 w-full">
          <ElInput
            v-model="identityForm.currentEmailCode"
            :placeholder="
              t('pages.system.userCenter.security.email.dialog.currentEmailCodePlaceholder')
            "
            maxlength="6"
          />
          <ElButton
            :disabled="proofCooldown > 0 || sendingProofCode"
            :loading="sendingProofCode"
            @click="sendProofCode"
          >
            {{
              proofCooldown > 0
                ? `${proofCooldown}s`
                : t('pages.system.userCenter.security.email.dialog.verifyCurrentEmailCode')
            }}
          </ElButton>
        </div>
      </ElFormItem>

      <ElFormItem
        v-else-if="isTotpProof"
        :label="t('pages.system.userCenter.security.email.dialog.totpCode')"
        prop="totpCode"
      >
        <ElInput
          v-model="identityForm.totpCode"
          :placeholder="t('pages.system.userCenter.security.email.dialog.totpCodePlaceholder')"
          maxlength="6"
        />
      </ElFormItem>

      <ElFormItem
        v-else
        :label="t('pages.system.userCenter.security.email.dialog.currentPassword')"
        prop="currentPassword"
      >
        <ElInput
          v-model="identityForm.currentPassword"
          :placeholder="
            t('pages.system.userCenter.security.email.dialog.currentPasswordPlaceholder')
          "
          show-password
          type="password"
        />
      </ElFormItem>
    </ElForm>

    <ElForm v-else ref="bindFormRef" :model="bindForm" :rules="bindRules" label-position="top">
      <ElFormItem
        :label="t('pages.system.userCenter.security.email.dialog.newEmail')"
        prop="newEmail"
      >
        <ElInput
          v-model="bindForm.newEmail"
          :placeholder="t('pages.system.userCenter.security.email.dialog.newEmailPlaceholder')"
        />
      </ElFormItem>

      <ElFormItem
        :label="t('pages.system.userCenter.security.email.dialog.code')"
        prop="newEmailCode"
      >
        <div class="flex gap-2 w-full">
          <ElInput
            v-model="bindForm.newEmailCode"
            :placeholder="t('pages.system.userCenter.security.email.dialog.codePlaceholder')"
            maxlength="6"
          />
          <ElButton
            :disabled="bindCooldown > 0 || sendingBindCode"
            :loading="sendingBindCode"
            @click="sendBindCode"
          >
            {{
              bindCooldown > 0
                ? `${bindCooldown}s`
                : t('pages.system.userCenter.security.email.dialog.sendCode')
            }}
          </ElButton>
        </div>
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="dialogVisible = false">{{ t('common.cancel') }}</ElButton>
      <ElButton
        v-if="step === 'verify_identity'"
        type="primary"
        :loading="verifyingIdentity"
        @click="verifyIdentity"
      >
        {{ t('pages.system.userCenter.security.email.dialog.next') }}
      </ElButton>
      <ElButton v-else type="primary" :loading="submitting" @click="submit">
        {{ t('common.confirm') }}
      </ElButton>
    </template>
  </ElDialog>
</template>

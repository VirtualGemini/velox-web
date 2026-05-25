<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchDisableMfaTotp } from '@/api/security'

  const props = defineProps<{ visible: boolean }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }>()

  const { t } = useI18n()
  const formRef = ref<FormInstance>()
  const form = reactive({ code: '' })
  const submitting = ref(false)

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const rules: FormRules = {
    code: [
      {
        required: true,
        message: t('pages.system.userCenter.security.mfa.totp.codeRequired'),
        trigger: 'blur'
      },
      {
        pattern: /^\d{6,8}$/,
        message: t('pages.system.userCenter.security.mfa.totp.codeFormat', { digits: 6 }),
        trigger: 'blur'
      }
    ]
  }

  watch(
    () => props.visible,
    (v) => {
      if (!v) {
        form.code = ''
        formRef.value?.clearValidate()
      }
    }
  )

  const submit = async () => {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    submitting.value = true
    try {
      await fetchDisableMfaTotp({ code: form.code.trim() })
      ElMessage.success(t('pages.system.userCenter.security.mfa.totp.disableSuccess'))
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
    :title="t('pages.system.userCenter.security.mfa.totp.disableTitle')"
    width="440px"
    align-center
    :close-on-click-modal="false"
  >
    <div class="text-sm text-g-700 mb-4">
      {{ t('pages.system.userCenter.security.mfa.totp.disableTip') }}
    </div>
    <ElForm ref="formRef" :model="form" :rules="rules" label-position="top">
      <ElFormItem :label="t('pages.system.userCenter.security.mfa.totp.code')" prop="code">
        <ElInput
          v-model="form.code"
          :placeholder="
            t('pages.system.userCenter.security.mfa.totp.codePlaceholder', { digits: 6 })
          "
          maxlength="8"
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">{{ t('common.cancel') }}</ElButton>
      <ElButton type="danger" :loading="submitting" @click="submit">
        {{ t('common.confirm') }}
      </ElButton>
    </template>
  </ElDialog>
</template>

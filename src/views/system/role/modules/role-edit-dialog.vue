<template>
  <ElDialog
    v-model="visible"
    :title="
      dialogType === 'add'
        ? t('pages.system.role.dialog.addTitle')
        : t('pages.system.role.dialog.editTitle')
    "
    width="30%"
    align-center
    @close="handleClose"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="120px">
      <ElFormItem v-if="dialogType === 'edit'" :label="t('pages.system.role.dialog.fields.type')">
        <ElInput
          :model-value="
            form.typeName || (isSystemRole ? t('common.status.builtin') : t('common.status.custom'))
          "
          disabled
        />
      </ElFormItem>
      <ElFormItem :label="t('pages.system.role.dialog.fields.roleName')" prop="roleName">
        <ElInput
          v-model="form.roleName"
          :placeholder="t('pages.system.role.dialog.placeholders.roleName')"
        />
      </ElFormItem>
      <ElFormItem :label="t('pages.system.role.dialog.fields.roleCode')" prop="roleCode">
        <ElInput
          v-model="form.roleCode"
          :disabled="isSystemRole"
          :placeholder="t('pages.system.role.dialog.placeholders.roleCode')"
        />
      </ElFormItem>
      <ElFormItem :label="t('pages.system.role.dialog.fields.description')" prop="description">
        <ElInput
          v-model="form.description"
          type="textarea"
          :rows="3"
          :placeholder="t('pages.system.role.dialog.placeholders.description')"
        />
      </ElFormItem>
      <ElFormItem :label="t('pages.system.role.dialog.fields.enabled')">
        <ElSwitch v-model="form.enabled" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="handleClose">{{ t('common.cancel') }}</ElButton>
      <ElButton type="primary" @click="handleSubmit">{{ t('table.form.submit') }}</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  type RoleListItem = Api.SystemManage.RoleListItem

  interface Props {
    modelValue: boolean
    dialogType: 'add' | 'edit'
    roleData?: RoleListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'submit', value: Api.SystemManage.RoleSaveCommand): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    dialogType: 'add',
    roleData: undefined
  })

  const emit = defineEmits<Emits>()
  const { t } = useI18n()

  const formRef = ref<FormInstance>()
  const isSystemRole = computed(() => props.dialogType === 'edit' && props.roleData?.type === 0)

  /**
   * 弹窗显示状态双向绑定
   */
  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  /**
   * 表单验证规则
   */
  const rules = reactive<FormRules>({
    roleName: [
      {
        required: true,
        message: t('pages.system.role.dialog.validation.roleNameRequired'),
        trigger: 'blur'
      },
      {
        min: 2,
        max: 20,
        message: t('pages.system.role.dialog.validation.roleNameLength'),
        trigger: 'blur'
      }
    ],
    roleCode: [
      {
        required: true,
        message: t('pages.system.role.dialog.validation.roleCodeRequired'),
        trigger: 'blur'
      },
      {
        min: 2,
        max: 50,
        message: t('pages.system.role.dialog.validation.roleCodeLength'),
        trigger: 'blur'
      }
    ],
    description: [
      {
        required: true,
        message: t('pages.system.role.dialog.validation.descriptionRequired'),
        trigger: 'blur'
      }
    ]
  })

  /**
   * 表单数据
   */
  const form = reactive<RoleListItem>({
    roleId: '',
    roleName: '',
    roleCode: '',
    description: '',
    type: 1,
    typeName: '',
    createTime: '',
    enabled: true
  })

  /**
   * 监听弹窗打开，初始化表单数据
   */
  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal) initForm()
    }
  )

  /**
   * 监听角色数据变化，更新表单
   */
  watch(
    () => props.roleData,
    (newData) => {
      if (newData && props.modelValue) initForm()
    },
    { deep: true }
  )

  /**
   * 初始化表单数据
   * 根据弹窗类型填充表单或重置表单
   */
  const initForm = () => {
    if (props.dialogType === 'edit' && props.roleData) {
      Object.assign(form, props.roleData)
    } else {
      Object.assign(form, {
        roleId: '',
        roleName: '',
        roleCode: '',
        description: '',
        type: 1,
        typeName: '',
        createTime: '',
        enabled: true
      })
    }
  }

  /**
   * 关闭弹窗并重置表单
   */
  const handleClose = () => {
    visible.value = false
    formRef.value?.resetFields()
  }

  /**
   * 提交表单
   * 验证通过后调用接口保存数据
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()
      emit('submit', {
        roleName: form.roleName.trim(),
        roleCode: form.roleCode.trim(),
        description: form.description.trim(),
        enabled: form.enabled
      })
    } catch (error) {
      console.log('[role-dialog] validation failed:', error)
    }
  }
</script>

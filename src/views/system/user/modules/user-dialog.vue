<template>
  <ElDialog
    v-model="dialogVisible"
    :title="
      dialogType === 'add'
        ? t('pages.system.user.dialog.addTitle')
        : t('pages.system.user.dialog.editTitle')
    "
    width="30%"
    align-center
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="80px">
      <ElFormItem :label="t('pages.system.user.dialog.fields.username')" prop="username">
        <ElInput
          v-model="formData.username"
          :placeholder="t('pages.system.user.dialog.placeholders.username')"
        />
      </ElFormItem>
      <ElFormItem :label="t('pages.system.user.dialog.fields.nickname')" prop="nickname">
        <ElInput
          v-model="formData.nickname"
          :placeholder="t('pages.system.user.dialog.placeholders.nickname')"
        />
      </ElFormItem>
      <ElFormItem :label="t('pages.system.user.dialog.fields.email')" prop="email">
        <ElInput
          v-model="formData.email"
          :placeholder="t('pages.system.user.dialog.placeholders.email')"
        />
      </ElFormItem>
      <ElFormItem :label="t('pages.system.user.dialog.fields.phone')" prop="phone">
        <ElInput
          v-model="formData.phone"
          :placeholder="t('pages.system.user.dialog.placeholders.phone')"
        />
      </ElFormItem>
      <ElFormItem
        v-if="dialogType === 'add'"
        :label="t('pages.system.user.dialog.fields.password')"
        prop="password"
      >
        <ElInput
          v-model="formData.password"
          type="password"
          show-password
          :placeholder="t('pages.system.user.dialog.placeholders.initialPassword')"
        />
      </ElFormItem>
      <ElFormItem v-else :label="t('pages.system.user.dialog.fields.password')">
        <ElInput
          v-model="formData.password"
          type="password"
          show-password
          :placeholder="t('pages.system.user.dialog.placeholders.passwordOptional')"
        />
      </ElFormItem>
      <ElFormItem :label="t('pages.system.user.dialog.fields.gender')" prop="gender">
        <ElSelect v-model="formData.gender">
          <ElOption :label="t('pages.system.user.gender.male')" :value="1" />
          <ElOption :label="t('pages.system.user.gender.female')" :value="2" />
          <ElOption :label="t('pages.system.user.gender.other')" :value="3" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem :label="t('pages.system.user.dialog.fields.roleCodes')" prop="roleCodes">
        <ElSelect v-model="formData.roleCodes" multiple filterable>
          <ElOption
            v-for="role in roleList"
            :key="role.roleCode"
            :value="role.roleCode"
            :label="role.roleName"
          />
        </ElSelect>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">{{ t('common.cancel') }}</ElButton>
        <ElButton type="primary" @click="handleSubmit">{{ t('table.form.submit') }}</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchGetRoleList } from '@/api/system-manage'
  import type { FormInstance, FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  interface Props {
    visible: boolean
    type: string
    userData?: Partial<Api.SystemManage.UserListItem>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', value: Api.SystemManage.UserSaveCommand): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()
  const { t } = useI18n()

  // 角色列表数据
  const roleList = ref<Api.SystemManage.RoleOption[]>([])

  // 对话框显示控制
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)

  // 表单实例
  const formRef = ref<FormInstance>()

  // 表单数据
  const formData = reactive({
    username: '',
    nickname: '',
    email: '',
    phone: '',
    password: '',
    gender: 1,
    roleCodes: [] as string[]
  })

  // 表单验证规则
  const rules: FormRules = {
    username: [
      {
        required: true,
        message: t('pages.system.user.dialog.validation.usernameRequired'),
        trigger: 'blur'
      },
      {
        min: 2,
        max: 20,
        message: t('pages.system.user.dialog.validation.usernameLength'),
        trigger: 'blur'
      }
    ],
    nickname: [
      {
        required: true,
        message: t('pages.system.user.dialog.validation.nicknameRequired'),
        trigger: 'blur'
      }
    ],
    email: [
      {
        type: 'email',
        message: t('pages.system.user.dialog.validation.emailInvalid'),
        trigger: 'blur'
      }
    ],
    phone: [
      {
        required: true,
        message: t('pages.system.user.dialog.validation.phoneRequired'),
        trigger: 'blur'
      },
      {
        pattern: /^1[3-9]\d{9}$/,
        message: t('pages.system.user.dialog.validation.phoneInvalid'),
        trigger: 'blur'
      }
    ],
    password: [
      {
        validator: (_rule, value, callback) => {
          if (dialogType.value === 'add' && !value) {
            callback(new Error(t('pages.system.user.dialog.validation.passwordRequired')))
            return
          }
          if (value && (value.length < 6 || value.length > 32)) {
            callback(new Error(t('pages.system.user.dialog.validation.passwordLength')))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ],
    gender: [
      {
        required: true,
        message: t('pages.system.user.dialog.validation.genderRequired'),
        trigger: 'change'
      }
    ],
    roleCodes: [
      {
        required: true,
        message: t('pages.system.user.dialog.validation.roleRequired'),
        trigger: 'change'
      }
    ]
  }

  /**
   * 初始化表单数据
   * 根据对话框类型（新增/编辑）填充表单
   */
  const initFormData = () => {
    const isEdit = props.type === 'edit' && props.userData
    const row = props.userData

    Object.assign(formData, {
      username: isEdit && row ? row.userName || '' : '',
      nickname: isEdit && row ? row.nickName || row.userName || '' : '',
      email: isEdit && row ? row.userEmail || '' : '',
      phone: isEdit && row ? row.userPhone || '' : '',
      password: '',
      gender: isEdit && row ? (row.userGender === '女' ? 2 : 1) : 1,
      roleCodes: isEdit && row ? (Array.isArray(row.userRoles) ? row.userRoles : []) : []
    })
  }

  const loadRoleList = async () => {
    const response = await fetchGetRoleList({ current: 1, size: 1000 })
    roleList.value = response.records.map((item) => ({
      roleId: item.roleId,
      roleCode: item.roleCode,
      roleName: item.roleName
    }))
  }

  /**
   * 监听对话框状态变化
   * 当对话框打开时初始化表单数据并清除验证状态
   */
  watch(
    () => [props.visible, props.type, props.userData],
    ([visible]) => {
      if (visible) {
        loadRoleList()
        initFormData()
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  /**
   * 提交表单
   * 验证通过后触发提交事件
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    emit('submit', {
      username: formData.username.trim(),
      nickname: formData.nickname.trim(),
      email: formData.email.trim() || undefined,
      phone: formData.phone.trim(),
      password: formData.password.trim() || undefined,
      gender: formData.gender,
      roleCodes: [...formData.roleCodes]
    })
  }
</script>

<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="rules"
    @reset="handleReset"
    @search="handleSearch"
  >
  </ArtSearchBar>
</template>

<script setup lang="ts">
  interface Props {
    modelValue: Api.SystemManage.UserSearchParams
  }
  interface Emits {
    (e: 'update:modelValue', value: Api.SystemManage.UserSearchParams): void
    (e: 'search', params: Api.SystemManage.UserSearchParams): void
    (e: 'reset'): void
  }
  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 表单数据双向绑定
  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  // 校验规则
  const rules = {
    // userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
  }

  // 动态 options
  const statusOptions = ref<{ label: string; value: string; disabled?: boolean }[]>([])

  // 模拟接口返回状态数据
  function fetchStatusOptions(): Promise<typeof statusOptions.value> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve([
          { label: 'pages.system.user.status.online', value: '1' },
          { label: 'pages.system.user.status.offline', value: '2' },
          { label: 'pages.system.user.status.abnormal', value: '3' },
          { label: 'pages.system.user.status.revoked', value: '4' }
        ])
      }, 1000)
    })
  }

  onMounted(async () => {
    statusOptions.value = await fetchStatusOptions()
  })

  // 表单配置
  const formItems = computed(() => [
    {
      label: 'pages.system.user.search.userName',
      key: 'userName',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.user.search.userPhone',
      key: 'userPhone',
      type: 'input',
      props: { maxlength: '11' }
    },
    {
      label: 'pages.system.user.search.userEmail',
      key: 'userEmail',
      type: 'input'
    },
    {
      label: 'pages.system.user.search.status',
      key: 'status',
      type: 'select',
      props: {
        options: statusOptions.value
      }
    },
    {
      label: 'pages.system.user.search.userGender',
      key: 'userGender',
      type: 'radiogroup',
      props: {
        options: [
          { label: 'pages.system.user.gender.male', value: '1' },
          { label: 'pages.system.user.gender.female', value: '2' }
        ]
      }
    }
  ])

  // 事件
  function handleReset() {
    emit('reset')
  }

  async function handleSearch(params: Api.SystemManage.UserSearchParams) {
    await searchBarRef.value.validate()
    emit('search', params)
  }
</script>

<template>
  <VeloxSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="rules"
    @reset="handleReset"
    @search="handleSearch"
  >
  </VeloxSearchBar>
</template>

<script setup lang="ts">
  type RoleSearchFormParams = Api.SystemManage.RoleSearchParams
  type RoleSearchFormModel = {
    roleName?: string
    roleCode?: string
    description?: string
    type?: number
    enabled?: boolean
    createTimeRange?: [string, string]
    updateTimeRange?: [string, string]
  }

  interface Props {
    modelValue: RoleSearchFormModel
  }

  interface Emits {
    (e: 'update:modelValue', value: RoleSearchFormModel): void
    (e: 'search', params: RoleSearchFormParams): void
    (e: 'reset'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const searchBarRef = ref()

  /**
   * 表单数据双向绑定
   */
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  /**
   * 表单校验规则
   */
  const rules = {}

  /**
   * 角色状态选项
   */
  const statusOptions = ref([
    { label: 'common.status.enabled', value: true },
    { label: 'common.status.disabled', value: false }
  ])

  const typeOptions = ref([
    { label: 'common.status.builtin', value: 0 },
    { label: 'common.status.custom', value: 1 }
  ])

  /**
   * 搜索表单配置项
   */
  const formItems = computed(() => [
    {
      label: 'pages.system.role.search.roleName',
      key: 'roleName',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.role.search.roleCode',
      key: 'roleCode',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.role.search.description',
      key: 'description',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.role.search.type',
      key: 'type',
      type: 'select',
      props: {
        options: typeOptions.value,
        clearable: true
      }
    },
    {
      label: 'pages.system.role.search.enabled',
      key: 'enabled',
      type: 'select',
      props: {
        options: statusOptions.value,
        clearable: true
      }
    },
    {
      label: 'pages.system.role.search.createTimeRange',
      key: 'createTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.system.role.search.placeholders.startTime',
        endPlaceholder: 'pages.system.role.search.placeholders.endTime',
        rangeSeparator: 'pages.system.role.search.rangeSeparator'
      }
    },
    {
      label: 'pages.system.role.search.updateTimeRange',
      key: 'updateTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.system.role.search.placeholders.startTime',
        endPlaceholder: 'pages.system.role.search.placeholders.endTime',
        rangeSeparator: 'pages.system.role.search.rangeSeparator'
      }
    }
  ])

  /**
   * 处理重置事件
   */
  const handleReset = () => {
    emit('reset')
  }

  /**
   * 处理搜索事件
   * 验证表单后触发搜索
   */
  const handleSearch = async (params: RoleSearchFormModel) => {
    await searchBarRef.value.validate()
    emit('search', {
      roleName: params.roleName,
      roleCode: params.roleCode,
      description: params.description,
      type: params.type,
      enabled: params.enabled,
      createTimeStart: params.createTimeRange?.[0],
      createTimeEnd: params.createTimeRange?.[1],
      updateTimeStart: params.updateTimeRange?.[0],
      updateTimeEnd: params.updateTimeRange?.[1]
    })
  }
</script>

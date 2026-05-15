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
  type RoleSearchFormParams = Api.SystemManage.RoleSearchParams & {
    daterange?: string[]
  }

  interface Props {
    modelValue: RoleSearchFormParams
  }

  interface Emits {
    (e: 'update:modelValue', value: RoleSearchFormParams): void
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
      label: 'pages.system.role.search.enabled',
      key: 'enabled',
      type: 'select',
      props: {
        options: statusOptions.value,
        clearable: true
      }
    },
    {
      label: 'pages.system.role.search.daterange',
      key: 'daterange',
      type: 'datetime',
      props: {
        style: { width: '100%' },
        placeholder: 'pages.system.role.search.placeholders.daterange',
        type: 'daterange',
        rangeSeparator: 'pages.system.role.search.rangeSeparator',
        startPlaceholder: 'pages.system.role.search.placeholders.startDate',
        endPlaceholder: 'pages.system.role.search.placeholders.endDate',
        valueFormat: 'YYYY-MM-DD',
        shortcuts: [
          { text: 'pages.system.role.search.shortcuts.today', value: [new Date(), new Date()] },
          {
            text: 'pages.system.role.search.shortcuts.lastWeek',
            value: [new Date(Date.now() - 604800000), new Date()]
          },
          {
            text: 'pages.system.role.search.shortcuts.lastMonth',
            value: [new Date(Date.now() - 2592000000), new Date()]
          }
        ]
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
  const handleSearch = async (params: RoleSearchFormParams) => {
    await searchBarRef.value.validate()
    emit('search', params)
  }
</script>

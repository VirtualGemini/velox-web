<!-- 角色管理页面 -->
<template>
  <div class="velox-full-height">
    <RoleSearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></RoleSearch>

    <ElCard class="velox-table-card" :style="{ 'margin-top': showSearchBar ? '12px' : '0' }">
      <VeloxTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton v-if="hasAuth('system:role:create')" @click="showDialog('add')" v-ripple>
              {{ t('pages.system.role.actions.addRole') }}
            </ElButton>
            <ElButton
              v-if="hasAuth('system:role:delete')"
              class="velox-batch-delete"
              type="danger"
              plain
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
              v-ripple
            >
              {{ t('common.batchDelete') }}
            </ElButton>
          </ElSpace>
        </template>
      </VeloxTableHeader>

      <!-- 表格 -->
      <VeloxTable
        ref="veloxTableRef"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </VeloxTable>
    </ElCard>

    <!-- 角色编辑弹窗 -->
    <RoleEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :role-data="currentRoleData"
      @submit="handleRoleSubmit"
    />

    <!-- 菜单权限弹窗 -->
    <RolePermissionDialog
      v-model="permissionDialog"
      :role-data="currentRoleData"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { ButtonMoreItem } from '@/components/core/forms/velox-button-more/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchBatchDeleteRole,
    fetchCreateRole,
    fetchDeleteRole,
    fetchGetRoleList,
    fetchRoleBoundAccounts,
    fetchUpdateRole
  } from '@/api/system-manage'
  import VeloxButtonMore from '@/components/core/forms/velox-button-more/index.vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import RoleSearch from './modules/role-search.vue'
  import RoleEditDialog from './modules/role-edit-dialog.vue'
  import RolePermissionDialog from './modules/role-permission-dialog.vue'
  import { ElTag, ElMessageBox } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'Role' })

  type RoleListItem = Api.SystemManage.RoleListItem
  type RoleSearchFormParams = Api.SystemManage.RoleSearchParams

  const { hasAuth } = useAuth()
  const { t } = useI18n()

  // 搜索表单
  const searchForm = ref({
    roleName: undefined,
    roleCode: undefined,
    description: undefined,
    type: undefined,
    enabled: undefined,
    createTimeRange: undefined as [string, string] | undefined,
    updateTimeRange: undefined as [string, string] | undefined
  })

  const showSearchBar = ref(false)

  const dialogVisible = ref(false)
  const permissionDialog = ref(false)
  const currentRoleData = ref<RoleListItem | undefined>(undefined)
  const veloxTableRef = ref()
  const selectedRows = ref<RoleListItem[]>([])

  const isSystemRole = (row?: RoleListItem) => row?.type === 0
  const isSuperRole = (row?: RoleListItem) => row?.roleCode === 'R_SUPER'
  const getRoleTypeTagType = (type?: number) => (type === 0 ? 'danger' : 'info')

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    replaceSearchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    // 核心配置
    core: {
      apiFn: fetchGetRoleList,
      apiParams: {
        current: 1,
        size: 20
      },
      columnsFactory: () => [
        { type: 'selection', width: 52, selectable: (row: RoleListItem) => !isSystemRole(row) },
        { type: 'index', label: t('table.column.index'), minWidth: 96 },
        {
          prop: 'roleName',
          label: 'pages.system.role.columns.roleName',
          minWidth: 200
        },
        {
          prop: 'roleCode',
          label: 'pages.system.role.columns.roleCode',
          minWidth: 200,
          showOverflowTooltip: true
        },
        {
          prop: 'type',
          label: 'pages.system.role.columns.type',
          minWidth: 152,
          formatter: (row) =>
            h(ElTag, { type: getRoleTypeTagType(row.type) }, () =>
              row.type === 0 ? t('common.status.builtin') : t('common.status.custom')
            )
        },
        {
          prop: 'description',
          label: 'pages.system.role.columns.description',
          minWidth: 280,
          showOverflowTooltip: true
        },
        {
          prop: 'enabled',
          label: 'pages.system.role.columns.enabled',
          minWidth: 152,
          formatter: (row) => {
            const statusConfig = row.enabled
              ? { type: 'success', text: t('common.status.enabled') }
              : { type: 'warning', text: t('common.status.disabled') }
            return h(
              ElTag,
              { type: statusConfig.type as 'success' | 'warning' },
              () => statusConfig.text
            )
          }
        },
        {
          prop: 'createTime',
          label: 'pages.system.role.columns.createTime',
          minWidth: 210,
          sortable: true
        },
        {
          prop: 'updateTime',
          label: 'pages.system.role.columns.updateTime',
          minWidth: 210,
          sortable: true
        },
        {
          prop: 'operation',
          label: 'pages.system.role.columns.operation',
          width: 120,
          fixed: 'right',
          formatter: (row) =>
            h('div', [
              h(VeloxButtonMore, {
                list: buildActionList(row),
                onClick: (item: ButtonMoreItem) => buttonMoreClick(item, row)
              })
            ])
        }
      ]
    }
  })

  const dialogType = ref<'add' | 'edit'>('add')

  const resolveRoleId = (row?: RoleListItem) => {
    const roleId = row?.roleId?.trim()
    if (!roleId) {
      ElMessage.error(t('pages.system.role.messages.missingRoleId'))
      return null
    }
    return roleId
  }

  const showDialog = (type: 'add' | 'edit', row?: RoleListItem) => {
    if (type === 'edit' && !resolveRoleId(row)) {
      return
    }
    if (type === 'edit' && isSuperRole(row)) {
      ElMessage.warning(t('pages.system.role.messages.superRoleCannotEdit'))
      return
    }
    dialogVisible.value = true
    dialogType.value = type
    currentRoleData.value = row
  }

  const handleRoleSubmit = async (payload: Api.SystemManage.RoleSaveCommand) => {
    if (dialogType.value === 'add') {
      await fetchCreateRole(payload)
      ElMessage.success(t('pages.system.role.messages.createSuccess'))
    } else {
      const roleId = resolveRoleId(currentRoleData.value)
      if (!roleId) return
      await fetchUpdateRole(roleId, payload)
      ElMessage.success(t('pages.system.role.messages.updateSuccess'))
    }
    dialogVisible.value = false
    refreshData()
  }

  /**
   * 搜索处理
   * @param params 搜索参数
   */
  const handleSearch = (params: RoleSearchFormParams) => {
    replaceSearchParams(params)
    getData()
  }

  const buttonMoreClick = (item: ButtonMoreItem, row: RoleListItem) => {
    switch (item.key) {
      case 'permission':
        showPermissionDialog(row)
        break
      case 'edit':
        showDialog('edit', row)
        break
      case 'delete':
        deleteRole(row)
        break
    }
  }

  const showPermissionDialog = (row?: RoleListItem) => {
    permissionDialog.value = true
    currentRoleData.value = row
  }

  const buildActionList = (row: RoleListItem): ButtonMoreItem[] => {
    const items: ButtonMoreItem[] = [
      {
        key: 'permission',
        label: 'pages.system.role.actions.permission',
        icon: 'ri:user-3-line',
        auth: 'system:role:permission'
      }
    ]

    if (!isSuperRole(row)) {
      items.push({
        key: 'edit',
        label: 'pages.system.role.actions.editRole',
        icon: 'ri:edit-2-line',
        auth: 'system:role:update'
      })
    }

    if (!isSystemRole(row)) {
      items.push({
        key: 'delete',
        label: 'pages.system.role.actions.deleteRole',
        icon: 'ri:delete-bin-4-line',
        color: '#f56c6c',
        auth: 'system:role:delete'
      })
    }

    return items
  }

  const formatBoundNames = (names: string[]): string => {
    const MAX_DISPLAY = 10
    if (names.length <= MAX_DISPLAY) {
      return names.join('、')
    }
    return (
      names.slice(0, MAX_DISPLAY).join('、') +
      t('pages.system.role.messages.boundMoreSuffix', { count: names.length - MAX_DISPLAY })
    )
  }

  const deleteRole = async (row: RoleListItem) => {
    if (isSystemRole(row)) {
      ElMessage.warning(t('pages.system.role.messages.systemRoleCannotDelete'))
      return
    }
    const roleId = resolveRoleId(row)
    if (!roleId) return

    let confirmMessage = t('pages.system.role.messages.confirmDelete', { name: row.roleName })
    try {
      const bindings = await fetchRoleBoundAccounts([roleId])
      const accountNames = bindings[0]?.accountNames ?? []
      if (accountNames.length > 0) {
        confirmMessage = t('pages.system.role.messages.confirmDeleteBound', {
          accounts: formatBoundNames(accountNames)
        })
      }
    } catch {
      // 查询绑定信息失败时退回普通删除提示
    }

    ElMessageBox.confirm(confirmMessage, t('pages.system.role.messages.deleteTitle'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(async () => {
        await fetchDeleteRole(roleId)
        ElMessage.success(t('pages.system.role.messages.deleteSuccess'))
        refreshData()
      })
      .catch(() => {
        ElMessage.info(t('pages.system.role.messages.deleteCancelled'))
      })
  }

  const handleSelectionChange = (selection: RoleListItem[]) => {
    selectedRows.value = selection
  }

  const handleBatchDelete = async () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning(t('common.batchDeleteEmpty'))
      return
    }
    const roleIds = selectedRows.value.map((row) => row.roleId)

    let confirmMessage = t('common.batchDeleteConfirm', { count: selectedRows.value.length })
    try {
      const bindings = await fetchRoleBoundAccounts(roleIds)
      const inUseRoleNames = bindings
        .filter((item) => (item.accountNames?.length ?? 0) > 0)
        .map((item) => item.roleName)
      if (inUseRoleNames.length > 0) {
        confirmMessage = t('pages.system.role.messages.batchDeleteBound', {
          count: selectedRows.value.length,
          roles: formatBoundNames(inUseRoleNames)
        })
      }
    } catch {
      // 查询绑定信息失败时退回普通批量删除提示
    }

    ElMessageBox.confirm(confirmMessage, t('common.batchDeleteTitle'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(async () => {
        await fetchBatchDeleteRole(roleIds)
        ElMessage.success(t('common.batchDeleteSuccess'))
        veloxTableRef.value?.elTableRef?.clearSelection()
        selectedRows.value = []
        refreshData()
      })
      .catch(() => {})
  }
</script>

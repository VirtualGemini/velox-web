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
          </ElSpace>
        </template>
      </VeloxTableHeader>

      <!-- 表格 -->
      <VeloxTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
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
    fetchCreateRole,
    fetchDeleteRole,
    fetchGetRoleList,
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
        { type: 'index', label: t('table.column.index'), minWidth: 60 },
        {
          prop: 'roleName',
          label: 'pages.system.role.columns.roleName',
          minWidth: 120
        },
        {
          prop: 'roleCode',
          label: 'pages.system.role.columns.roleCode',
          minWidth: 120
        },
        {
          prop: 'type',
          label: 'pages.system.role.columns.type',
          minWidth: 100,
          formatter: (row) =>
            h(ElTag, { type: getRoleTypeTagType(row.type) }, () =>
              row.type === 0 ? t('common.status.builtin') : t('common.status.custom')
            )
        },
        {
          prop: 'description',
          label: 'pages.system.role.columns.description',
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'enabled',
          label: 'pages.system.role.columns.enabled',
          minWidth: 100,
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
          minWidth: 180,
          sortable: true
        },
        {
          prop: 'updateTime',
          label: 'pages.system.role.columns.updateTime',
          minWidth: 180,
          sortable: true
        },
        {
          prop: 'operation',
          label: 'pages.system.role.columns.operation',
          width: 100,
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

  const deleteRole = (row: RoleListItem) => {
    if (isSystemRole(row)) {
      ElMessage.warning(t('pages.system.role.messages.systemRoleCannotDelete'))
      return
    }
    ElMessageBox.confirm(
      t('pages.system.role.messages.confirmDelete', { name: row.roleName }),
      t('pages.system.role.messages.deleteTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
      .then(async () => {
        const roleId = resolveRoleId(row)
        if (!roleId) return
        await fetchDeleteRole(roleId)
        ElMessage.success(t('pages.system.role.messages.deleteSuccess'))
        refreshData()
      })
      .catch(() => {
        ElMessage.info(t('pages.system.role.messages.deleteCancelled'))
      })
  }
</script>

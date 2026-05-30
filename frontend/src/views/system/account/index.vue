<template>
  <div class="user-page velox-full-height">
    <AccountSearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    />

    <ElCard class="velox-table-card" :style="{ 'margin-top': showSearchBar ? '12px' : '0' }">
      <VeloxTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton v-if="hasAuth('system:account:create')" @click="showDialog('add')" v-ripple>
              {{ t('pages.system.account.actions.addAccount') }}
            </ElButton>
            <ElButton
              v-if="hasAuth('system:account:delete')"
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

      <VeloxTable
        ref="veloxTableRef"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />

      <AccountDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :account-data="currentAccountData"
        @submit="handleDialogSubmit"
      />
      <AccountDetailDrawer v-model:visible="detailVisible" :detail="currentAccountDetail" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import VeloxButtonTable from '@/components/core/forms/velox-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useAuth } from '@/hooks/core/useAuth'
  import {
    fetchBatchDeleteAccount,
    fetchCreateAccount,
    fetchDeleteAccount,
    fetchGetAccountDetailCard,
    fetchGetAccountList,
    fetchUpdateAccount
  } from '@/api/system-manage'
  import AccountSearch from './modules/user-search.vue'
  import AccountDialog from './modules/user-dialog.vue'
  import AccountDetailDrawer from './modules/account-detail-drawer.vue'
  import { ElTag, ElMessageBox, ElImage } from 'element-plus'
  import { DialogType } from '@/types'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'Account' })

  type AccountListItem = Api.SystemManage.AccountListItem

  const { hasAuth } = useAuth()
  const { t } = useI18n()

  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentAccountData = ref<Partial<AccountListItem>>({})
  const detailVisible = ref(false)
  const currentAccountDetail = ref<Api.SystemManage.AccountDetailCard>()
  const veloxTableRef = ref()
  const selectedRows = ref<AccountListItem[]>([])
  const showSearchBar = ref(false)

  const searchForm = ref({
    username: undefined,
    email: undefined,
    remark: undefined,
    status: undefined,
    createTimeRange: undefined as [string, string] | undefined,
    updateTimeRange: undefined as [string, string] | undefined
  })

  const ACCOUNT_STATUS_CONFIG = {
    '1': { type: 'success' as const, textKey: 'pages.system.account.status.online' },
    '2': { type: 'info' as const, textKey: 'pages.system.account.status.offline' },
    '3': { type: 'warning' as const, textKey: 'pages.system.account.status.abnormal' },
    '4': { type: 'danger' as const, textKey: 'pages.system.account.status.revoked' }
  } as const

  const getAccountStatusConfig = (status: string) =>
    ACCOUNT_STATUS_CONFIG[status as keyof typeof ACCOUNT_STATUS_CONFIG] || {
      type: 'info' as const,
      textKey: 'common.status.unknown'
    }

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
    core: {
      apiFn: fetchGetAccountList,
      apiParams: {
        current: 1,
        size: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection', width: 52 },
        { type: 'index', minWidth: 96, label: t('table.column.index') },
        {
          prop: 'accountInfo',
          label: 'pages.system.account.columns.accountInfo',
          minWidth: 400,
          formatter: (row: AccountListItem) =>
            h('div', { class: 'user flex-c' }, [
              h(ElImage, {
                class: 'size-9.5 rounded-md',
                src: row.avatar,
                previewSrcList: row.avatar ? [row.avatar] : [],
                previewTeleported: true
              }),
              h('div', { class: 'ml-2' }, [
                h('p', { class: 'user-name' }, row.username),
                h('p', { class: 'email' }, row.email || '-')
              ])
            ])
        },
        {
          prop: 'status',
          label: 'pages.system.account.columns.status',
          minWidth: 132,
          formatter: (row: AccountListItem) => {
            const statusConfig = getAccountStatusConfig(row.status)
            return h(ElTag, { type: statusConfig.type }, () => t(statusConfig.textKey))
          }
        },
        {
          prop: 'remark',
          label: 'pages.system.account.columns.remark',
          minWidth: 260,
          showOverflowTooltip: true,
          formatter: (row: AccountListItem) => row.remark || '-'
        },
        {
          prop: 'createTime',
          label: 'pages.system.account.columns.createTime',
          minWidth: 210,
          sortable: true
        },
        {
          prop: 'updateTime',
          label: 'pages.system.account.columns.updateTime',
          minWidth: 210,
          sortable: true
        },
        {
          prop: 'operation',
          label: 'pages.system.account.columns.operation',
          width: 180,
          fixed: 'right',
          formatter: (row: AccountListItem) =>
            h(
              'div',
              [
                hasAuth('system:account:query')
                  ? h(VeloxButtonTable, {
                      type: 'view',
                      onClick: () => showDetail(row)
                    })
                  : null,
                hasAuth('system:account:update')
                  ? h(VeloxButtonTable, {
                      type: 'edit',
                      onClick: () => showDialog('edit', row)
                    })
                  : null,
                hasAuth('system:account:delete')
                  ? h(VeloxButtonTable, {
                      type: 'delete',
                      onClick: () => deleteAccount(row)
                    })
                  : null
              ].filter(Boolean)
            )
        }
      ]
    }
  })

  const handleSearch = (params: Api.SystemManage.AccountSearchParams) => {
    replaceSearchParams(params)
    getData()
  }

  const showDialog = (type: DialogType, row?: AccountListItem): void => {
    dialogType.value = type
    currentAccountData.value = row || {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const showDetail = async (row: AccountListItem) => {
    try {
      currentAccountDetail.value = await fetchGetAccountDetailCard(row.accountId)
      detailVisible.value = true
    } catch (error) {
      console.error('获取账号详情失败:', error)
    }
  }

  const deleteAccount = (row: AccountListItem): void => {
    ElMessageBox.confirm(
      t('pages.system.account.messages.confirmDelete'),
      t('pages.system.account.messages.deleteTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'error'
      }
    ).then(async () => {
      await fetchDeleteAccount(row.accountId)
      ElMessage.success(t('pages.system.account.messages.deleteSuccess'))
      refreshData()
    })
  }

  const handleDialogSubmit = async (payload: Api.SystemManage.AccountSaveCommand) => {
    try {
      if (dialogType.value === 'add') {
        await fetchCreateAccount(payload)
        ElMessage.success(t('pages.system.account.messages.createSuccess'))
      } else if (currentAccountData.value.accountId) {
        await fetchUpdateAccount(currentAccountData.value.accountId, payload)
        ElMessage.success(t('pages.system.account.messages.updateSuccess'))
      }
      dialogVisible.value = false
      currentAccountData.value = {}
      refreshData()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  const handleSelectionChange = (selection: AccountListItem[]): void => {
    selectedRows.value = selection
  }

  const handleBatchDelete = (): void => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning(t('common.batchDeleteEmpty'))
      return
    }
    ElMessageBox.confirm(
      t('common.batchDeleteConfirm', { count: selectedRows.value.length }),
      t('common.batchDeleteTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    ).then(async () => {
      await fetchBatchDeleteAccount(selectedRows.value.map((row) => row.accountId))
      ElMessage.success(t('common.batchDeleteSuccess'))
      veloxTableRef.value?.elTableRef?.clearSelection()
      selectedRows.value = []
      refreshData()
    })
  }
</script>

<style scoped>
  .user {
    min-width: 0;
  }

  .user-name,
  .email {
    margin: 0;
    line-height: 1.5;
  }

  .user-name {
    color: var(--el-text-color-primary);
  }

  .email {
    color: var(--el-text-color-secondary);
    word-break: break-all;
  }

  @media (max-width: 768px) {
    .user {
      align-items: flex-start;
    }

    .user-name,
    .email {
      white-space: normal;
    }
  }
</style>

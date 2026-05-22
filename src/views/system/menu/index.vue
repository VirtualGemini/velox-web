<!-- 菜单管理页面 -->
<template>
  <div class="menu-page velox-full-height">
    <!-- 搜索栏 -->
    <VeloxSearchBar
      v-show="showSearchBar"
      v-model="formFilters"
      :items="formItems"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="velox-table-card" :style="{ 'margin-top': showSearchBar ? '12px' : '0' }">
      <!-- 表格头部 -->
      <VeloxTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        @refresh="handleRefresh"
      >
        <template #left>
          <ElButton v-auth="'system:menu:create'" @click="handleAddMenu" v-ripple>
            {{ t('pages.system.menu.actions.addMenu') }}
          </ElButton>
          <ElButton @click="toggleExpand" v-ripple>
            {{
              isExpanded
                ? t('pages.system.menu.actions.collapse')
                : t('pages.system.menu.actions.expand')
            }}
          </ElButton>
        </template>
      </VeloxTableHeader>

      <VeloxTable
        ref="tableRef"
        rowKey="path"
        :loading="loading"
        :columns="columns"
        :data="filteredTableData"
        :stripe="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
      />

      <!-- 菜单弹窗 -->
      <MenuDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :editData="editData"
        :parentData="parentData"
        :lockType="lockMenuType"
        @submit="handleSubmit || ''"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { formatMenuTitle } from '@/utils/router'
  import VeloxButtonTable from '@/components/core/forms/velox-button-table/index.vue'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import { useAuth } from '@/hooks/core/useAuth'
  import type { AppRouteRecord } from '@/types/router'
  import MenuDialog from './modules/menu-dialog.vue'
  import {
    fetchCreateMenu,
    fetchDeleteMenu,
    fetchGetGrantableMenus,
    fetchUpdateMenu
  } from '@/api/system-manage'
  import { ElTag, ElMessageBox } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'Menus' })

  const { hasAuth } = useAuth()
  const { t } = useI18n()

  // 状态管理
  const loading = ref(false)
  const isExpanded = ref(false)
  const tableRef = ref()
  const showSearchBar = ref(false)

  // 弹窗相关
  const dialogVisible = ref(false)
  const dialogType = ref<'menu' | 'button'>('menu')
  const editData = ref<AppRouteRecord | any>(null)
  const parentData = ref<AppRouteRecord | null>(null)
  const lockMenuType = ref(false)

  // 搜索相关
  const initialSearchState = {
    name: '',
    route: '',
    status: undefined as boolean | undefined,
    createTimeRange: undefined as [string, string] | undefined,
    updateTimeRange: undefined as [string, string] | undefined
  }

  const formFilters = reactive({ ...initialSearchState })
  const appliedFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: 'pages.system.menu.search.name',
      key: 'name',
      type: 'input',
      props: { clearable: true }
    },
    {
      label: 'pages.system.menu.search.route',
      key: 'route',
      type: 'input',
      props: { clearable: true }
    },
    {
      label: 'pages.system.menu.search.status',
      key: 'status',
      type: 'select',
      props: {
        clearable: true,
        options: [
          { label: 'common.status.enabled', value: true },
          { label: 'common.status.disabled', value: false }
        ]
      }
    },
    {
      label: 'pages.system.menu.search.createTimeRange',
      key: 'createTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.system.menu.search.placeholders.startTime',
        endPlaceholder: 'pages.system.menu.search.placeholders.endTime',
        rangeSeparator: 'pages.system.menu.search.rangeSeparator'
      }
    },
    {
      label: 'pages.system.menu.search.updateTimeRange',
      key: 'updateTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.system.menu.search.placeholders.startTime',
        endPlaceholder: 'pages.system.menu.search.placeholders.endTime',
        rangeSeparator: 'pages.system.menu.search.rangeSeparator'
      }
    }
  ])

  onMounted(() => {
    getMenuList()
  })

  const resolveMenuId = (row?: Pick<AppRouteRecord, 'id'> | null) => {
    const menuId = row?.id?.trim()
    if (!menuId) {
      ElMessage.error(t('pages.system.menu.messages.missingMenuId'))
      return null
    }
    return menuId
  }

  /**
   * 获取菜单列表数据
   */
  const getMenuList = async (): Promise<void> => {
    loading.value = true

    try {
      const list = await fetchGetGrantableMenus()
      tableData.value = list
    } catch (error) {
      throw error instanceof Error ? error : new Error(t('pages.system.menu.messages.fetchFailed'))
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取菜单类型标签颜色
   * @param row 菜单行数据
   * @returns 标签颜色类型
   */
  const getMenuTypeTag = (
    row: AppRouteRecord
  ): 'primary' | 'success' | 'warning' | 'info' | 'danger' => {
    if (row.meta?.isAuthButton) return 'danger'
    if (row.children?.length) return 'info'
    if (row.meta?.link && row.meta?.isIframe) return 'success'
    if (row.path) return 'primary'
    if (row.meta?.link) return 'warning'
    return 'info'
  }

  /**
   * 获取菜单类型文本
   * @param row 菜单行数据
   * @returns 菜单类型文本
   */
  const getMenuTypeText = (row: AppRouteRecord): string => {
    if (row.meta?.isAuthButton) return t('pages.system.menu.types.button')
    if (row.children?.length) return t('pages.system.menu.types.directory')
    if (row.meta?.link && row.meta?.isIframe) return t('pages.system.menu.types.iframe')
    if (row.path) return t('pages.system.menu.types.menu')
    if (row.meta?.link) return t('pages.system.menu.types.link')
    return t('common.status.unknown')
  }

  // 表格列配置
  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'meta.title',
      label: 'pages.system.menu.columns.title',
      minWidth: 120,
      formatter: (row: AppRouteRecord) => formatMenuTitle(row.meta?.title)
    },
    {
      prop: 'type',
      label: 'pages.system.menu.columns.type',
      minWidth: 120,
      formatter: (row: AppRouteRecord) => {
        return h(ElTag, { type: getMenuTypeTag(row) }, () => getMenuTypeText(row))
      }
    },
    {
      prop: 'path',
      label: 'pages.system.menu.columns.route',
      minWidth: 180,
      formatter: (row: AppRouteRecord) => {
        if (row.meta?.isAuthButton) return ''
        return row.meta?.link || row.path || ''
      }
    },
    {
      prop: 'meta.authList',
      label: 'pages.system.menu.columns.auth',
      minWidth: 180,
      formatter: (row: AppRouteRecord) => {
        if (row.meta?.isAuthButton) {
          return row.meta?.authMark || ''
        }
        if (row.meta?.authMark) {
          return row.meta.authMark
        }
        if (!row.meta?.authList?.length) return ''
        return t('pages.system.menu.messages.authCount', { count: row.meta.authList.length })
      }
    },
    {
      prop: 'status',
      label: 'pages.system.menu.columns.status',
      minWidth: 100,
      formatter: (row: AppRouteRecord) => {
        const isEnabled = row.meta?.isEnable !== false
        return h(ElTag, { type: isEnabled ? 'success' : 'info' }, () =>
          isEnabled ? t('common.status.enabled') : t('common.status.disabled')
        )
      }
    },
    {
      prop: 'meta.createTime',
      label: 'pages.system.menu.columns.createTime',
      minWidth: 180,
      formatter: (row: AppRouteRecord) => row.meta?.createTime || '-'
    },
    {
      prop: 'meta.updateTime',
      label: 'pages.system.menu.columns.updateTime',
      minWidth: 180,
      formatter: (row: AppRouteRecord) => row.meta?.updateTime || '-'
    },
    {
      prop: 'operation',
      label: 'pages.system.menu.columns.operation',
      width: 160,
      fixed: 'right',
      align: 'left',
      formatter: (row: AppRouteRecord) => {
        const buttonStyle = { style: 'text-align: left' }

        if (row.meta?.isAuthButton) {
          return h('div', buttonStyle, [
            hasAuth('system:menu:update')
              ? h(VeloxButtonTable, {
                  type: 'edit',
                  onClick: () => handleEditAuth(row)
                })
              : null,
            hasAuth('system:menu:delete')
              ? h(VeloxButtonTable, {
                  type: 'delete',
                  onClick: () => handleDeleteAuth(row)
                })
              : null
          ])
        }

        return h(
          'div',
          buttonStyle,
          [
            hasAuth('system:menu:create')
              ? h(VeloxButtonTable, {
                  type: 'add',
                  onClick: () => handleAddAuth(row),
                  title: t('pages.system.menu.actions.addPermission')
                })
              : null,
            hasAuth('system:menu:update')
              ? h(VeloxButtonTable, {
                  type: 'edit',
                  onClick: () => handleEditMenu(row)
                })
              : null,
            hasAuth('system:menu:delete')
              ? h(VeloxButtonTable, {
                  type: 'delete',
                  onClick: () => handleDeleteMenu(row)
                })
              : null
          ].filter(Boolean)
        )
      }
    }
  ])

  // 数据相关
  const tableData = ref<AppRouteRecord[]>([])

  /**
   * 重置搜索条件
   */
  const handleReset = (): void => {
    Object.assign(formFilters, { ...initialSearchState })
    Object.assign(appliedFilters, { ...initialSearchState })
    getMenuList()
  }

  /**
   * 执行搜索
   */
  const handleSearch = (): void => {
    Object.assign(appliedFilters, { ...formFilters })
    getMenuList()
  }

  /**
   * 刷新菜单列表
   */
  const handleRefresh = (): void => {
    getMenuList()
  }

  /**
   * 深度克隆对象
   * @param obj 要克隆的对象
   * @returns 克隆后的对象
   */
  const deepClone = <T,>(obj: T): T => {
    if (obj === null || typeof obj !== 'object') return obj
    if (obj instanceof Date) return new Date(obj) as T
    if (Array.isArray(obj)) return obj.map((item) => deepClone(item)) as T

    const cloned = {} as T
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        cloned[key] = deepClone(obj[key])
      }
    }
    return cloned
  }

  const parseDateTime = (value?: string | null) => {
    if (!value) return null
    const timestamp = Date.parse(value.replace(' ', 'T'))
    return Number.isNaN(timestamp) ? null : timestamp
  }

  const matchesTimeRange = (value: string | undefined, start?: string, end?: string) => {
    if (!start && !end) return true
    const target = parseDateTime(value)
    if (target === null) return false
    const startTime = parseDateTime(start)
    const endTime = parseDateTime(end)
    if (startTime !== null && target < startTime) return false
    if (endTime !== null && target > endTime) return false
    return true
  }

  /**
   * 将权限列表转换为子节点
   * @param items 菜单项数组
   * @returns 转换后的菜单项数组
   */
  const convertAuthListToChildren = (items: AppRouteRecord[]): AppRouteRecord[] => {
    return items.map((item) => {
      const clonedItem = deepClone(item)

      if (clonedItem.children?.length) {
        clonedItem.children = convertAuthListToChildren(clonedItem.children)
      }

      if (item.meta?.authList?.length) {
        const authChildren: AppRouteRecord[] = item.meta.authList.map(
          (auth: { id?: string; title: string; authMark: string; sort?: number }) => ({
            id: auth.id,
            path: `${item.path}_auth_${auth.authMark}`,
            name: `${String(item.name)}_auth_${auth.authMark}`,
            parentId: item.id,
            meta: {
              title: auth.title,
              authMark: auth.authMark,
              sort: auth.sort,
              isAuthButton: true,
              parentPath: item.path
            }
          })
        )

        clonedItem.children = clonedItem.children?.length
          ? [...clonedItem.children, ...authChildren]
          : authChildren
      }

      return clonedItem
    })
  }

  /**
   * 搜索菜单
   * @param items 菜单项数组
   * @returns 搜索结果数组
   */
  const searchMenu = (items: AppRouteRecord[]): AppRouteRecord[] => {
    const results: AppRouteRecord[] = []
    const createTimeStart = appliedFilters.createTimeRange?.[0]
    const createTimeEnd = appliedFilters.createTimeRange?.[1]
    const updateTimeStart = appliedFilters.updateTimeRange?.[0]
    const updateTimeEnd = appliedFilters.updateTimeRange?.[1]

    for (const item of items) {
      const searchName = appliedFilters.name?.toLowerCase().trim() || ''
      const searchRoute = appliedFilters.route?.toLowerCase().trim() || ''
      const searchStatus = appliedFilters.status
      const menuTitle = formatMenuTitle(item.meta?.title || '').toLowerCase()
      const menuPath = (item.path || '').toLowerCase()
      const nameMatch = !searchName || menuTitle.includes(searchName)
      const routeMatch = !searchRoute || menuPath.includes(searchRoute)
      const statusMatch =
        searchStatus === undefined || (item.meta?.isEnable !== false) === searchStatus
      const createTimeMatch = matchesTimeRange(
        item.meta?.createTime,
        createTimeStart,
        createTimeEnd
      )
      const updateTimeMatch = matchesTimeRange(
        item.meta?.updateTime,
        updateTimeStart,
        updateTimeEnd
      )

      if (item.children?.length) {
        const matchedChildren = searchMenu(item.children)
        if (matchedChildren.length > 0) {
          const clonedItem = deepClone(item)
          clonedItem.children = matchedChildren
          results.push(clonedItem)
          continue
        }
      }

      if (nameMatch && routeMatch && statusMatch && createTimeMatch && updateTimeMatch) {
        results.push(deepClone(item))
      }
    }

    return results
  }

  // 过滤后的表格数据
  const filteredTableData = computed(() => {
    const searchedData = searchMenu(tableData.value)
    return convertAuthListToChildren(searchedData)
  })

  /**
   * 添加菜单
   */
  const handleAddMenu = (): void => {
    dialogType.value = 'menu'
    editData.value = null
    parentData.value = null
    lockMenuType.value = true
    dialogVisible.value = true
  }

  /**
   * 添加权限按钮
   */
  const handleAddAuth = (row: AppRouteRecord): void => {
    dialogType.value = 'button'
    editData.value = null
    parentData.value = row
    lockMenuType.value = false
    dialogVisible.value = true
  }

  /**
   * 编辑菜单
   * @param row 菜单行数据
   */
  const handleEditMenu = (row: AppRouteRecord): void => {
    if (!resolveMenuId(row)) {
      return
    }
    dialogType.value = 'menu'
    editData.value = row
    parentData.value = null
    lockMenuType.value = true
    dialogVisible.value = true
  }

  /**
   * 编辑权限按钮
   * @param row 权限行数据
   */
  const handleEditAuth = (row: AppRouteRecord): void => {
    const menuId = resolveMenuId(row)
    if (!menuId) {
      return
    }
    dialogType.value = 'button'
    editData.value = {
      id: menuId,
      parentId: (row as any).parentId,
      title: row.meta?.title,
      authMark: row.meta?.authMark,
      sort: row.meta?.sort
    }
    parentData.value = null
    lockMenuType.value = false
    dialogVisible.value = true
  }

  /**
   * 菜单表单数据类型
   */
  interface MenuFormData {
    parentId: string
    id: string
    name: string
    path: string
    label: string
    menuAuthMark: string
    authName: string
    authLabel: string
    authSort: number
    menuType: 'menu' | 'button'
    component?: string
    icon?: string
    sort?: number
    [key: string]: any
  }

  /**
   * 提交表单数据
   * @param formData 表单数据
   */
  const handleSubmit = async (formData: MenuFormData): Promise<void> => {
    const isEdit = Boolean(editData.value)
    const menuId = isEdit ? resolveMenuId({ id: formData.id }) : null
    if (isEdit && !menuId) {
      return
    }

    const payload: Api.SystemManage.MenuSaveCommand =
      formData.menuType === 'button'
        ? {
            parentId: formData.parentId,
            menuType: 'button',
            name: `${String(formData.authLabel).replace(/:/g, '_')}_${formData.parentId}`,
            title: formData.authName,
            authMark: formData.authLabel,
            isEnable: formData.isEnable ?? true,
            sort: formData.authSort || 1
          }
        : {
            parentId: undefined,
            menuType: 'menu',
            name: formData.label,
            title: formData.name,
            path: formData.path,
            component: formData.component,
            icon: formData.icon,
            authMark: formData.menuAuthMark,
            isEnable: formData.isEnable ?? true,
            sort: formData.sort || 1,
            keepAlive: formData.keepAlive,
            isHide: formData.isHide,
            isHideTab: formData.isHideTab,
            link: formData.link,
            isIframe: formData.isIframe,
            showBadge: formData.showBadge,
            showTextBadge: formData.showTextBadge,
            fixedTab: formData.fixedTab,
            activePath: formData.activePath,
            isFullPage: formData.isFullPage
          }

    if (menuId) {
      await fetchUpdateMenu(menuId, payload)
      ElMessage.success(t('pages.system.menu.messages.updateSuccess'))
    } else {
      await fetchCreateMenu(payload)
      ElMessage.success(t('pages.system.menu.messages.createSuccess'))
    }
    dialogVisible.value = false
    editData.value = null
    parentData.value = null
    await getMenuList()
  }

  /**
   * 删除菜单
   */
  const handleDeleteMenu = async (row: AppRouteRecord): Promise<void> => {
    try {
      await ElMessageBox.confirm(
        t('pages.system.menu.messages.confirmDeleteMenu'),
        t('common.tips'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )
      const menuId = resolveMenuId(row)
      if (!menuId) return
      await fetchDeleteMenu(menuId)
      ElMessage.success(t('pages.system.menu.messages.deleteSuccess'))
      getMenuList()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error(t('pages.system.menu.messages.deleteFailed'))
      }
    }
  }

  /**
   * 删除权限按钮
   */
  const handleDeleteAuth = async (row: AppRouteRecord): Promise<void> => {
    try {
      await ElMessageBox.confirm(
        t('pages.system.menu.messages.confirmDeletePermission'),
        t('common.tips'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )
      const menuId = resolveMenuId(row)
      if (!menuId) return
      await fetchDeleteMenu(menuId)
      ElMessage.success(t('pages.system.menu.messages.deleteSuccess'))
      getMenuList()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error(t('pages.system.menu.messages.deleteFailed'))
      }
    }
  }

  /**
   * 切换展开/收起所有菜单
   */
  const toggleExpand = (): void => {
    isExpanded.value = !isExpanded.value
    nextTick(() => {
      if (tableRef.value?.elTableRef && filteredTableData.value) {
        const processRows = (rows: AppRouteRecord[]) => {
          rows.forEach((row) => {
            if (row.children?.length) {
              tableRef.value.elTableRef.toggleRowExpansion(row, isExpanded.value)
              processRows(row.children)
            }
          })
        }
        processRows(filteredTableData.value)
      }
    })
  }
</script>

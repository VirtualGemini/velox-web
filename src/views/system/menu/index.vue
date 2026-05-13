<!-- 菜单管理页面 -->
<template>
  <div class="menu-page art-full-height">
    <!-- 搜索栏 -->
    <ArtSearchBar
      v-show="showSearchBar"
      v-model="formFilters"
      :items="formItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card" :style="{ 'margin-top': showSearchBar ? '12px' : '0' }">
      <!-- 表格头部 -->
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        @refresh="handleRefresh"
      >
        <template #left>
          <ElButton v-auth="'system:menu:create'" @click="handleAddMenu" v-ripple>
            添加菜单
          </ElButton>
          <ElButton @click="toggleExpand" v-ripple>
            {{ isExpanded ? '收起' : '展开' }}
          </ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
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
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import { useAuth } from '@/hooks/core/useAuth'
  import type { AppRouteRecord } from '@/types/router'
  import MenuDialog from './modules/menu-dialog.vue'
  import {
    fetchCreateMenu,
    fetchDeleteMenu,
    fetchGetMenuList,
    fetchUpdateMenu
  } from '@/api/system-manage'
  import { ElTag, ElMessageBox } from 'element-plus'

  defineOptions({ name: 'Menus' })

  const { hasAuth } = useAuth()

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
    route: ''
  }

  const formFilters = reactive({ ...initialSearchState })
  const appliedFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '菜单名称',
      key: 'name',
      type: 'input',
      props: { clearable: true }
    },
    {
      label: '路由地址',
      key: 'route',
      type: 'input',
      props: { clearable: true }
    }
  ])

  onMounted(() => {
    getMenuList()
  })

  const resolveMenuId = (row?: Pick<AppRouteRecord, 'id'> | null) => {
    const menuId = row?.id?.trim()
    if (!menuId) {
      ElMessage.error('当前菜单数据缺少 id，请确认列表接口返回内容')
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
      const list = await fetchGetMenuList()
      tableData.value = list
    } catch (error) {
      throw error instanceof Error ? error : new Error('获取菜单失败')
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
    if (row.meta?.isAuthButton) return '按钮'
    if (row.children?.length) return '目录'
    if (row.meta?.link && row.meta?.isIframe) return '内嵌'
    if (row.path) return '菜单'
    if (row.meta?.link) return '外链'
    return '未知'
  }

  // 表格列配置
  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'meta.title',
      label: '菜单名称',
      minWidth: 120,
      formatter: (row: AppRouteRecord) => formatMenuTitle(row.meta?.title)
    },
    {
      prop: 'type',
      label: '菜单类型',
      minWidth: 120,
      formatter: (row: AppRouteRecord) => {
        return h(ElTag, { type: getMenuTypeTag(row) }, () => getMenuTypeText(row))
      }
    },
    {
      prop: 'path',
      label: '路由',
      minWidth: 180,
      formatter: (row: AppRouteRecord) => {
        if (row.meta?.isAuthButton) return ''
        return row.meta?.link || row.path || ''
      }
    },
    {
      prop: 'meta.authList',
      label: '权限标识',
      minWidth: 180,
      formatter: (row: AppRouteRecord) => {
        if (row.meta?.isAuthButton) {
          return row.meta?.authMark || ''
        }
        if (row.meta?.authMark) {
          return row.meta.authMark
        }
        if (!row.meta?.authList?.length) return ''
        return `${row.meta.authList.length} 个权限标识`
      }
    },
    {
      prop: 'date',
      label: '编辑时间',
      minWidth: 180,
      formatter: (row: AppRouteRecord) => row.meta?.updateTime || '-'
    },
    {
      prop: 'status',
      label: '状态',
      minWidth: 100,
      formatter: (row: AppRouteRecord) => {
        const isEnabled = row.meta?.isEnable !== false
        return h(ElTag, { type: isEnabled ? 'success' : 'info' }, () =>
          isEnabled ? '启用' : '禁用'
        )
      }
    },
    {
      prop: 'operation',
      label: '操作',
      width: 160,
      fixed: 'right',
      align: 'left',
      formatter: (row: AppRouteRecord) => {
        const buttonStyle = { style: 'text-align: left' }

        if (row.meta?.isAuthButton) {
          return h('div', buttonStyle, [
            hasAuth('system:menu:update')
              ? h(ArtButtonTable, {
                  type: 'edit',
                  onClick: () => handleEditAuth(row)
                })
              : null,
            hasAuth('system:menu:delete')
              ? h(ArtButtonTable, {
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
              ? h(ArtButtonTable, {
                  type: 'add',
                  onClick: () => handleAddAuth(row),
                  title: '新增权限'
                })
              : null,
            hasAuth('system:menu:update')
              ? h(ArtButtonTable, {
                  type: 'edit',
                  onClick: () => handleEditMenu(row)
                })
              : null,
            hasAuth('system:menu:delete')
              ? h(ArtButtonTable, {
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

    for (const item of items) {
      const searchName = appliedFilters.name?.toLowerCase().trim() || ''
      const searchRoute = appliedFilters.route?.toLowerCase().trim() || ''
      const menuTitle = formatMenuTitle(item.meta?.title || '').toLowerCase()
      const menuPath = (item.path || '').toLowerCase()
      const nameMatch = !searchName || menuTitle.includes(searchName)
      const routeMatch = !searchRoute || menuPath.includes(searchRoute)

      if (item.children?.length) {
        const matchedChildren = searchMenu(item.children)
        if (matchedChildren.length > 0) {
          const clonedItem = deepClone(item)
          clonedItem.children = matchedChildren
          results.push(clonedItem)
          continue
        }
      }

      if (nameMatch && routeMatch) {
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
      ElMessage.success('编辑成功')
    } else {
      await fetchCreateMenu(payload)
      ElMessage.success('新增成功')
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
      await ElMessageBox.confirm('确定要删除该菜单吗？删除后无法恢复', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const menuId = resolveMenuId(row)
      if (!menuId) return
      await fetchDeleteMenu(menuId)
      ElMessage.success('删除成功')
      getMenuList()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  /**
   * 删除权限按钮
   */
  const handleDeleteAuth = async (row: AppRouteRecord): Promise<void> => {
    try {
      await ElMessageBox.confirm('确定要删除该权限吗？删除后无法恢复', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const menuId = resolveMenuId(row)
      if (!menuId) return
      await fetchDeleteMenu(menuId)
      ElMessage.success('删除成功')
      getMenuList()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
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

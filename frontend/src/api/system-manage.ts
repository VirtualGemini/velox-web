import request from '@/utils/http'
import { AppRouteRecord } from '@/types/router'

function requireId(id: string, resourceName: string) {
  const normalizedId = id?.trim()
  if (!normalizedId) {
    throw new Error(`${resourceName}ID 不能为空`)
  }
  return normalizedId
}

// 获取账号列表
export function fetchGetAccountList(params: Api.SystemManage.AccountSearchParams) {
  return request.get<Api.SystemManage.AccountList>({
    url: '/api/account/list',
    params
  })
}

export function fetchCreateAccount(data: Api.SystemManage.AccountSaveCommand) {
  return request.post<string>({
    url: '/api/account',
    data
  })
}

export function fetchUpdateAccount(accountId: string, data: Api.SystemManage.AccountSaveCommand) {
  const normalizedAccountId = requireId(accountId, '账号')
  return request.put<boolean>({
    url: `/api/account/${normalizedAccountId}`,
    data
  })
}

export function fetchDeleteAccount(accountId: string) {
  const normalizedAccountId = requireId(accountId, '账号')
  return request.del<boolean>({
    url: `/api/account/${normalizedAccountId}`
  })
}

// 批量删除账号
export function fetchBatchDeleteAccount(accountIds: string[]) {
  return request.del<boolean>({
    url: '/api/account/delete-batch',
    params: { ids: accountIds },
    paramsSerializer: { indexes: null }
  })
}

export function fetchGetAccountDetailCard(accountId: string) {
  const normalizedAccountId = requireId(accountId, '账号')
  return request.get<Api.SystemManage.AccountDetailCard>({
    url: `/api/account/${normalizedAccountId}/detail-card`
  })
}

// 获取角色列表
export function fetchGetRoleList(params: Api.SystemManage.RoleSearchParams) {
  return request.get<Api.SystemManage.RoleList>({
    url: '/api/role/list',
    params
  })
}

export function fetchCreateRole(data: Api.SystemManage.RoleSaveCommand) {
  return request.post<string>({
    url: '/api/role',
    data
  })
}

export function fetchUpdateRole(roleId: string, data: Api.SystemManage.RoleSaveCommand) {
  const normalizedRoleId = requireId(roleId, '角色')
  return request.put<boolean>({
    url: `/api/role/${normalizedRoleId}`,
    data
  })
}

export function fetchDeleteRole(roleId: string) {
  const normalizedRoleId = requireId(roleId, '角色')
  return request.del<boolean>({
    url: `/api/role/${normalizedRoleId}`
  })
}

// 批量删除角色
export function fetchBatchDeleteRole(roleIds: string[]) {
  return request.del<boolean>({
    url: '/api/role/delete-batch',
    params: { ids: roleIds },
    paramsSerializer: { indexes: null }
  })
}

export interface RoleBoundAccounts {
  roleId: string
  roleName: string
  accountNames: string[]
}

// 查询角色绑定的账号（删除前风险提示）
export function fetchRoleBoundAccounts(roleIds: string[]) {
  return request.get<RoleBoundAccounts[]>({
    url: '/api/role/bound-accounts',
    params: { ids: roleIds },
    paramsSerializer: { indexes: null }
  })
}

// 获取角色菜单权限
export function fetchGetRoleMenuPermissions(roleId: string) {
  const normalizedRoleId = requireId(roleId, '角色')
  return request.get<string[]>({
    url: `/api/role/${normalizedRoleId}/menu-permissions`
  })
}

// 保存角色菜单权限
export function fetchUpdateRoleMenuPermissions(
  roleId: string,
  data: Api.SystemManage.RoleMenuPermissionUpdateCommand
) {
  const normalizedRoleId = requireId(roleId, '角色')
  return request.put<boolean>({
    url: `/api/role/${normalizedRoleId}/menu-permissions`,
    data
  })
}

// 获取菜单列表（导航渲染用，仅需登录）
export function fetchGetMenuList() {
  return request.get<AppRouteRecord[]>({
    url: '/api/v3/system/menus/simple'
  })
}

// 获取可授予的菜单列表（菜单管理、角色授权弹窗用，需要 system:menu:query 权限）
// 返回数据等同于当前登录用户的可见范围——授予不能越过自己。
export function fetchGetGrantableMenus() {
  return request.get<AppRouteRecord[]>({
    url: '/api/v3/system/menus'
  })
}

export function fetchCreateMenu(data: Api.SystemManage.MenuSaveCommand) {
  return request.post<string>({
    url: '/api/v3/system/menus',
    data
  })
}

export function fetchUpdateMenu(menuId: string, data: Api.SystemManage.MenuSaveCommand) {
  const normalizedMenuId = requireId(menuId, '菜单')
  return request.put<boolean>({
    url: `/api/v3/system/menus/${normalizedMenuId}`,
    data
  })
}

export function fetchDeleteMenu(menuId: string) {
  const normalizedMenuId = requireId(menuId, '菜单')
  return request.del<boolean>({
    url: `/api/v3/system/menus/${normalizedMenuId}`
  })
}

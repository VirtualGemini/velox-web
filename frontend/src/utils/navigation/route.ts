/**
 * 路由工具模块
 *
 * 提供路由处理和菜单路径相关的工具函数
 *
 * ## 主要功能
 *
 * - iframe 路由检测，判断是否为外部嵌入页面
 * - 菜单项有效性验证，过滤隐藏和无效菜单
 * - 路径标准化处理，统一路径格式
 * - 递归查找菜单树中第一个有效路径
 * - 支持多级嵌套菜单的路径解析
 *
 * ## 使用场景
 *
 * - 系统初始化时获取默认跳转路径
 * - 菜单权限过滤后获取首个可访问页面
 * - 路由重定向逻辑处理
 * - iframe 页面特殊处理
 *
 * @module utils/navigation/route
 * @author Velox Team
 */

import { AppRouteRecord } from '@/types'

// 检查是否为 iframe 路由
export function isIframe(url: string): boolean {
  return url.startsWith('/outside/iframe/')
}

/**
 * 判断菜单项是否可作为默认导航落点
 * 隐藏的全屏页面虽然不展示在菜单中，但仍然可能是合法首页。
 */
export const isNavigableMenuItem = (menuItem: AppRouteRecord): boolean => {
  if (!menuItem.path || !menuItem.path.trim()) {
    return false
  }

  if (!menuItem.meta?.isHide) {
    return true
  }

  return menuItem.meta?.isFullPage === true
}

/**
 * 判断菜单项是否拥有可用作落地的真实路径（忽略 isHide）。
 * 用于在没有任何可见菜单时（譬如普通用户仅授权了 AccountCenter）回退寻找首页。
 */
const hasRoutablePath = (menuItem: AppRouteRecord): boolean => {
  if (!menuItem.path || !menuItem.path.trim()) {
    return false
  }
  if (menuItem.meta?.link || menuItem.meta?.isIframe === true) {
    return false
  }
  return Boolean(menuItem.component)
}

/**
 * 标准化路径格式
 * @param path 路径
 * @returns 标准化后的路径
 */
const normalizePath = (path: string): string => {
  return path.startsWith('/') ? path : `/${path}`
}

/**
 * 递归获取菜单的第一个有效路径
 * @param menuList 菜单列表
 * @returns 第一个有效路径，如果没有找到则返回空字符串
 */
export const getFirstMenuPath = (menuList: AppRouteRecord[]): string => {
  if (!Array.isArray(menuList) || menuList.length === 0) {
    return ''
  }

  for (const menuItem of menuList) {
    if (!isNavigableMenuItem(menuItem)) {
      continue
    }

    // 如果有子菜单，优先查找子菜单
    if (menuItem.children?.length) {
      const childPath = getFirstMenuPath(menuItem.children)
      if (childPath) {
        return childPath
      }
    }

    // 返回当前菜单项的标准化路径
    return normalizePath(menuItem.path!)
  }

  return ''
}

/**
 * 在菜单树中查找首个拥有真实路径的菜单（含隐藏项），作为最终兜底。
 * 仅当所有可见菜单都不存在时使用——例如普通用户仅授权了隐藏的账号中心。
 */
const getFirstRoutablePath = (menuList: AppRouteRecord[]): string => {
  if (!Array.isArray(menuList) || menuList.length === 0) {
    return ''
  }

  for (const menuItem of menuList) {
    if (menuItem.children?.length) {
      const childPath = getFirstRoutablePath(menuItem.children)
      if (childPath) {
        return childPath
      }
    }

    if (hasRoutablePath(menuItem)) {
      return normalizePath(menuItem.path!)
    }
  }

  return ''
}

/**
 * 在菜单树中按路径查找第一个匹配的可导航菜单项
 */
const findMenuByPath = (
  menuList: AppRouteRecord[],
  targetPath: string,
  includeHidden = false
): AppRouteRecord | null => {
  for (const menuItem of menuList) {
    const matches = menuItem.path === targetPath
    if (matches && (includeHidden ? hasRoutablePath(menuItem) : isNavigableMenuItem(menuItem))) {
      return menuItem
    }
    if (menuItem.children?.length) {
      const found = findMenuByPath(menuItem.children, targetPath, includeHidden)
      if (found) return found
    }
  }
  return null
}

/**
 * 根据优先级解析登录后的落地页路径
 *
 * 优先级：
 * 1. 工作台 `/dashboard/console`
 * 2. 账号中心 `/system/account-center`（即便配置为隐藏也接受，作为普通用户的兜底首页）
 * 3. 后端返回顺序的第一个可见菜单
 * 4. 仍无落点时，使用首个拥有真实组件路径的菜单（含隐藏项），保证普通用户至少能进入账号中心
 */
export const resolveHomePath = (menuList: AppRouteRecord[]): string => {
  if (!Array.isArray(menuList) || menuList.length === 0) {
    return ''
  }

  const priorities = ['/dashboard/console', '/system/account-center']
  for (const path of priorities) {
    // 优先级路径属于显式指定的兜底首页，即使被标记为 isHide 也算命中。
    if (findMenuByPath(menuList, path, true)) {
      return path
    }
  }

  const visiblePath = getFirstMenuPath(menuList)
  if (visiblePath) {
    return visiblePath
  }

  return getFirstRoutablePath(menuList)
}

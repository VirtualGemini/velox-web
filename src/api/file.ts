import request from '@/utils/http'
import { useUserStore } from '@/store/modules/user'

export interface FileUploadResult {
  path: string
  url: string
  name: string
  size: number
}

export interface FileRecord {
  id: string
  configId: string
  name: string
  path: string
  url: string
  type: string
  size: number
  createBy: string
  uploadTime: string
}

export interface FileQuery {
  page: number
  size: number
  name?: string
  path?: string
  type?: string
  sizeMinBytes?: number
  sizeMaxBytes?: number
  uploadTimeStart?: string
  uploadTimeEnd?: string
}

export interface FilePageResult {
  records: FileRecord[]
  total: number
  page: number
  size: number
  pages: number
}

export interface FileConfig {
  id: string
  name: string
  storage: number
  config: Record<string, any>
  master: boolean
  enabled: number
  remark: string
  createTime: string
  updateTime: string
}

export interface FileConfigQuery {
  page?: number
  size?: number
  name?: string
  storage?: number
  enabled?: number
  createTimeStart?: string
  createTimeEnd?: string
  updateTimeStart?: string
  updateTimeEnd?: string
}

export interface FileConfigPageResult {
  records: FileConfig[]
  total: number
  page: number
  size: number
  pages: number
}

export interface FileConfigSaveCommand {
  id?: string
  name: string
  storage: number
  config: string
  remark?: string
  master?: boolean
}

export interface FilePresignedUrlRespVO {
  configId: string
  uploadUrl: string
  path: string
}

export function fetchFileUpload(file: File, directory?: string) {
  const formData = new FormData()
  formData.append('file', file)
  if (directory) {
    formData.append('directory', directory)
  }
  return request.post<string>({
    url: '/api/file/upload',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function fetchFileList(params: FileQuery) {
  return request.get<FilePageResult>({
    url: '/api/file/page',
    params
  })
}

export function fetchFileGet(id: string) {
  return request.get<FileRecord>({
    url: '/api/file/get',
    params: { id }
  })
}

export function fetchFileDeleteById(id: string) {
  return request.del<boolean>({
    url: '/api/file/delete',
    params: { id }
  })
}

export function fetchFileDeleteBatch(ids: string[]) {
  return request.del<boolean>({
    url: '/api/file/delete-batch',
    params: { ids }
  })
}

export function fetchFilePresignPutUrl(name: string, directory?: string) {
  return request.get<FilePresignedUrlRespVO>({
    url: '/api/file/presigned-url',
    params: { name, directory }
  })
}

export function fetchFilePresignGetUrl(configId: string, url: string, expirationSeconds?: number) {
  return request.get<string>({
    url: '/api/file/presigned-get-url',
    params: { configId, url, expirationSeconds }
  })
}

export function fetchFileCreate(data: { configId: string; path: string; url?: string }) {
  return request.post<string>({
    url: '/api/file/create',
    data
  })
}

export function fetchFileTypes() {
  return request.get<string[]>({
    url: '/api/file/types'
  })
}

export async function fetchFileDownloadBlob(id: string) {
  const userStore = useUserStore()
  const { VITE_API_URL, VITE_WITH_CREDENTIALS } = import.meta.env
  const response = await fetch(`${VITE_API_URL}/api/file/download?id=${encodeURIComponent(id)}`, {
    method: 'GET',
    headers: {
      Authorization: userStore.accessToken || '',
      'X-Time-Zone': Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC',
      'Accept-Language': userStore.language || 'zh'
    },
    credentials: VITE_WITH_CREDENTIALS === 'true' ? 'include' : 'same-origin'
  })

  if (!response.ok) {
    throw new Error(`download failed: ${response.status}`)
  }

  return response.blob()
}

export function fetchFileConfigList(params: FileConfigQuery) {
  return request.get<FileConfigPageResult>({
    url: '/api/file-config/page',
    params
  })
}

export function fetchFileConfigGet(id: string) {
  return request.get<FileConfig>({
    url: '/api/file-config/get',
    params: { id }
  })
}

export function fetchFileConfigCreate(data: FileConfigSaveCommand) {
  return request.post<string>({
    url: '/api/file-config/create',
    data
  })
}

export function fetchFileConfigUpdate(data: FileConfigSaveCommand) {
  return request.put<boolean>({
    url: '/api/file-config/update',
    data
  })
}

export function fetchFileConfigDelete(id: string) {
  return request.del<boolean>({
    url: '/api/file-config/delete',
    params: { id }
  })
}

export function fetchFileConfigDeleteBatch(ids: string[]) {
  return request.del<boolean>({
    url: '/api/file-config/delete-batch',
    params: { ids }
  })
}

export function fetchFileConfigSetMaster(id: string) {
  return request.put<boolean>({
    url: `/api/file-config/update-master?id=${encodeURIComponent(id)}`
  })
}

export function fetchFileConfigUpdateEnabled(id: string, enabled: number) {
  return request.put<boolean>({
    url: `/api/file-config/update-enabled?id=${encodeURIComponent(id)}&enabled=${enabled}`
  })
}

export function fetchFileConfigTest(id: string) {
  return request.get<string>({
    url: '/api/file-config/test',
    params: { id }
  })
}

export function fetchSupportedFileStorages() {
  return request.get<number[]>({
    url: '/api/file-config/supported-storages'
  })
}

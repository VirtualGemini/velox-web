<template>
  <div class="velox-full-height">
    <VeloxSearchBar
      v-show="showSearchBar"
      v-model="searchForm"
      :items="searchItems"
      @search="handleSearch"
      @reset="handleReset"
    >
      <template #sizeRange="{ modelValue }">
        <div class="el-date-editor el-range-editor el-input__wrapper file-size-range">
          <input
            v-model="modelValue.sizeMin"
            class="el-range-input"
            inputmode="decimal"
            :placeholder="t('pages.system.fileManage.search.placeholders.startSize')"
          />
          <ElSelect v-model="modelValue.sizeMinUnit" class="file-size-inline-select">
            <ElOption
              v-for="option in fileSizeUnitOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </ElSelect>
          <span class="el-range-separator">
            {{ t('pages.system.fileManage.search.rangeSeparator') }}
          </span>
          <input
            v-model="modelValue.sizeMax"
            class="el-range-input"
            inputmode="decimal"
            :placeholder="t('pages.system.fileManage.search.placeholders.endSize')"
          />
          <ElSelect v-model="modelValue.sizeMaxUnit" class="file-size-inline-select">
            <ElOption
              v-for="option in fileSizeUnitOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </ElSelect>
        </div>
      </template>
    </VeloxSearchBar>

    <ElCard class="velox-table-card" :style="{ 'margin-top': showSearchBar ? '12px' : '0' }">
      <VeloxTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton v-if="canAccess('system:file:upload')" @click="openUploadDialog" v-ripple>
              {{ t('pages.system.fileManage.actions.uploadFile') }}
            </ElButton>
            <ElButton
              v-if="canAccess('system:file:delete')"
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
    </ElCard>

    <ElDialog
      v-model="uploadDialogVisible"
      :title="t('pages.system.fileManage.dialog.title')"
      width="520px"
      align-center
      @close="handleUploadDialogClose"
    >
      <ElUpload
        ref="uploadRef"
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        :limit="1"
      >
        <ElIcon class="el-icon--upload"><Upload /></ElIcon>
        <div class="el-upload__text">
          {{ t('pages.system.fileManage.dialog.dragHint') }}
          <em>{{ t('pages.system.fileManage.dialog.clickUpload') }}</em>
        </div>
      </ElUpload>

      <template #footer>
        <ElButton @click="handleUploadDialogClose">{{ t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="uploadLoading" @click="handleUploadSubmit">{{
          t('pages.system.fileManage.actions.upload')
        }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { ElButton, ElMessage, ElMessageBox, ElTag, ElUpload, ElIcon } from 'element-plus'
  import { Upload } from '@element-plus/icons-vue'
  import { useRoute } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import { useAuth } from '@/hooks/core/useAuth'
  import VeloxButtonTable from '@/components/core/forms/velox-button-table/index.vue'
  import {
    type FileRecord,
    fetchFileList,
    fetchFileDeleteById,
    fetchFileDeleteBatch,
    fetchFileUpload,
    fetchFilePresignGetUrl,
    fetchFileDownloadBlob,
    fetchFileTypes
  } from '@/api/file'

  defineOptions({ name: 'FileManage' })

  const { hasAuth } = useAuth()
  const route = useRoute()
  const { t } = useI18n()

  type FileSizeUnit = 'B' | 'KB' | 'MB' | 'GB'

  interface FileSearchForm {
    name?: string
    type?: string
    sizeMin?: string
    sizeMinUnit: FileSizeUnit
    sizeMax?: string
    sizeMaxUnit: FileSizeUnit
    uploadTimeRange?: [string, string]
  }

  const fileTypeOptions = ref<string[]>([])
  const fileSizeUnitOptions = computed<{ label: string; value: FileSizeUnit }[]>(() => [
    { label: t('pages.system.fileManage.search.units.B'), value: 'B' },
    { label: t('pages.system.fileManage.search.units.KB'), value: 'KB' },
    { label: t('pages.system.fileManage.search.units.MB'), value: 'MB' },
    { label: t('pages.system.fileManage.search.units.GB'), value: 'GB' }
  ])

  const createDefaultSearchForm = (): FileSearchForm => ({
    name: undefined,
    type: undefined,
    sizeMin: undefined,
    sizeMinUnit: 'KB',
    sizeMax: undefined,
    sizeMaxUnit: 'MB',
    uploadTimeRange: undefined
  })

  const searchForm = ref<FileSearchForm>(createDefaultSearchForm())
  const showSearchBar = ref(false)
  const veloxTableRef = ref()
  const selectedRows = ref<FileRecord[]>([])

  const searchItems = computed(() => [
    {
      label: 'pages.system.fileManage.search.name',
      key: 'name',
      type: 'input',
      props: {
        clearable: true
      }
    },
    {
      label: 'pages.system.fileManage.search.type',
      key: 'type',
      type: 'select',
      props: {
        clearable: true,
        filterable: true,
        options: fileTypeOptions.value.map((type) => ({ label: type, value: type })),
        placeholder: 'pages.system.fileManage.search.placeholders.type',
        popperClass: 'file-type-select-dropdown'
      }
    },
    {
      label: 'pages.system.fileManage.search.sizeRange',
      key: 'sizeRange'
    },
    {
      label: 'pages.system.fileManage.search.uploadTimeRange',
      key: 'uploadTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.system.fileManage.search.placeholders.startTime',
        endPlaceholder: 'pages.system.fileManage.search.placeholders.endTime',
        rangeSeparator: 'pages.system.fileManage.search.rangeSeparator'
      }
    }
  ])

  const routeAuthMarks = computed(() => {
    const authList = Array.isArray(route.meta.authList) ? route.meta.authList : []
    return authList
      .map((item) =>
        item && typeof item === 'object'
          ? String((item as Record<string, unknown>).authMark || '')
          : ''
      )
      .filter(Boolean)
  })

  const hasFileAuthBinding = computed(() =>
    routeAuthMarks.value.some((auth) => auth.startsWith('system:file:'))
  )

  const canAccess = (auth: string) => {
    return hasFileAuthBinding.value ? hasAuth(auth) : true
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
      apiFn: fetchFileList,
      apiParams: {
        page: 1,
        size: 20
      },
      paginationKey: {
        current: 'page',
        size: 'size'
      },
      columnsFactory: () => [
        { type: 'selection', width: 52 },
        {
          type: 'index',
          label: t('table.column.index'),
          minWidth: 96
        },
        {
          prop: 'name',
          label: 'pages.system.fileManage.columns.name',
          minWidth: 240,
          showOverflowTooltip: true
        },
        {
          prop: 'type',
          label: 'pages.system.fileManage.columns.type',
          minWidth: 180,
          formatter: (row: FileRecord) =>
            h(
              ElTag,
              { type: getContentTypeTagType(row.type), size: 'small' },
              () => row.type || '-'
            )
        },
        {
          prop: 'size',
          label: 'pages.system.fileManage.columns.size',
          minWidth: 112,
          formatter: (row: FileRecord) => formatFileSize(row.size)
        },
        {
          prop: 'url',
          label: 'pages.system.fileManage.columns.url',
          minWidth: 280,
          showOverflowTooltip: true,
          formatter: (row: FileRecord) =>
            row.url
              ? h(
                  'a',
                  {
                    href: row.url,
                    target: '_blank',
                    class: 'text-primary hover:underline truncate block max-w-full'
                  },
                  row.url
                )
              : '-'
        },
        {
          prop: 'path',
          label: 'pages.system.fileManage.columns.path',
          minWidth: 260,
          showOverflowTooltip: true
        },
        {
          prop: 'uploadTime',
          label: 'pages.system.fileManage.columns.createTime',
          minWidth: 210,
          formatter: (row: FileRecord) => row.uploadTime || '-'
        },
        {
          prop: 'operation',
          label: 'pages.system.fileManage.columns.operation',
          width: 132,
          fixed: 'right',
          align: 'left',
          formatter: (row: FileRecord) => renderOperationButtons(row)
        }
      ]
    }
  })

  function getContentTypeTagType(contentType?: string) {
    if (!contentType) return 'info'
    if (contentType.startsWith('image/')) return 'success'
    if (contentType.startsWith('video/')) return 'warning'
    if (contentType.startsWith('audio/')) return 'primary'
    if (contentType.includes('pdf')) return 'danger'
    return 'info'
  }

  function formatFileSize(size?: number) {
    if (!size) return '-'
    if (size < 1024) return `${size} B`
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
    if (size < 1024 * 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`
    return `${(size / 1024 / 1024 / 1024).toFixed(1)} GB`
  }

  function renderOperationButtons(row: FileRecord) {
    return h(
      'div',
      { class: 'flex items-center justify-start' },
      [
        canAccess('system:file:download')
          ? h(VeloxButtonTable, {
              type: 'view',
              icon: 'ri:download-2-line',
              onClick: () => handleDownload(row)
            })
          : null,
        canAccess('system:file:delete')
          ? h(VeloxButtonTable, {
              type: 'delete',
              onClick: () => handleDelete(row)
            })
          : null
      ].filter(Boolean)
    )
  }

  async function handleDownload(row: FileRecord) {
    try {
      if (row.url) {
        try {
          const presignedUrl = await fetchFilePresignGetUrl(row.configId, row.url, 3600)
          triggerAnchorDownload(presignedUrl, row.name)
          return
        } catch {
          // Fall back to the streaming download endpoint when signed URLs are unavailable.
        }
      }

      const blob = await fetchFileDownloadBlob(row.id)
      const objectUrl = URL.createObjectURL(blob)
      triggerAnchorDownload(objectUrl, row.name)
      window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1000)
    } catch {
      ElMessage.error(t('pages.system.fileManage.messages.downloadUrlFailed'))
    }
  }

  function triggerAnchorDownload(url: string, fileName: string) {
    const a = document.createElement('a')
    a.href = url
    a.download = fileName
    a.target = '_blank'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  }

  function parseFileSizeValue(value: string | undefined) {
    if (value === undefined || value === null || value.trim() === '') return undefined
    const parsedValue = Number(value)
    if (!Number.isFinite(parsedValue) || parsedValue < 0) {
      return null
    }
    return parsedValue
  }

  function convertSizeToBytes(value: number | undefined, unit: FileSizeUnit) {
    if (value === undefined || value === null) return undefined
    const multipliers: Record<FileSizeUnit, number> = {
      B: 1,
      KB: 1024,
      MB: 1024 * 1024,
      GB: 1024 * 1024 * 1024
    }
    return Math.floor(value * multipliers[unit])
  }

  async function handleDelete(row: FileRecord) {
    await ElMessageBox.confirm(
      t('pages.system.fileManage.messages.confirmDelete', { name: row.name }),
      t('pages.system.fileManage.messages.deleteTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    await fetchFileDeleteById(row.id)
    ElMessage.success(t('pages.system.fileManage.messages.deleteSuccess'))
    refreshData()
  }

  function handleSelectionChange(selection: FileRecord[]) {
    selectedRows.value = selection
  }

  async function handleBatchDelete() {
    if (selectedRows.value.length === 0) {
      ElMessage.warning(t('common.batchDeleteEmpty'))
      return
    }
    await ElMessageBox.confirm(
      t('common.batchDeleteConfirm', { count: selectedRows.value.length }),
      t('common.batchDeleteTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    await fetchFileDeleteBatch(selectedRows.value.map((row) => row.id))
    ElMessage.success(t('common.batchDeleteSuccess'))
    veloxTableRef.value?.elTableRef?.clearSelection()
    selectedRows.value = []
    refreshData()
  }

  function handleSearch(params: Record<string, any>) {
    const searchParams = params as FileSearchForm
    const sizeMinValue = parseFileSizeValue(searchParams.sizeMin)
    const sizeMaxValue = parseFileSizeValue(searchParams.sizeMax)

    if (sizeMinValue === null || sizeMaxValue === null) {
      ElMessage.warning(t('pages.system.fileManage.messages.invalidSizeValue'))
      return
    }

    const sizeMinBytes = convertSizeToBytes(sizeMinValue, searchParams.sizeMinUnit)
    const sizeMaxBytes = convertSizeToBytes(sizeMaxValue, searchParams.sizeMaxUnit)

    if (sizeMinBytes !== undefined && sizeMaxBytes !== undefined && sizeMinBytes > sizeMaxBytes) {
      ElMessage.warning(t('pages.system.fileManage.messages.invalidSizeRange'))
      return
    }

    replaceSearchParams({
      page: 1,
      name: searchParams.name,
      type: searchParams.type,
      sizeMinBytes,
      sizeMaxBytes,
      uploadTimeStart: searchParams.uploadTimeRange?.[0],
      uploadTimeEnd: searchParams.uploadTimeRange?.[1]
    })
    getData()
  }

  function handleReset() {
    searchForm.value = createDefaultSearchForm()
    resetSearchParams()
    getData()
  }

  // Upload dialog
  const uploadDialogVisible = ref(false)
  const uploadLoading = ref(false)
  const uploadRef = ref<InstanceType<typeof ElUpload>>()
  const currentFile = ref<File | null>(null)

  function openUploadDialog() {
    uploadDialogVisible.value = true
  }

  function handleUploadDialogClose() {
    uploadDialogVisible.value = false
    uploadLoading.value = false
    currentFile.value = null
    uploadRef.value?.clearFiles()
  }

  function handleFileChange(uploadFile: any) {
    currentFile.value = uploadFile.raw as File
  }

  async function handleUploadSubmit() {
    if (!currentFile.value) {
      ElMessage.warning(t('pages.system.fileManage.messages.selectFileFirst'))
      return
    }
    uploadLoading.value = true
    try {
      await fetchFileUpload(currentFile.value, 'file')
      ElMessage.success(t('pages.system.fileManage.messages.uploadSuccess'))
      handleUploadDialogClose()
      refreshData()
    } catch {
      ElMessage.error(t('pages.system.fileManage.messages.uploadFailed'))
    } finally {
      uploadLoading.value = false
    }
  }

  async function loadFileTypes() {
    fileTypeOptions.value = await fetchFileTypes()
  }

  onMounted(async () => {
    await loadFileTypes()
  })
</script>

<style scoped>
  :global(.file-type-select-dropdown .el-scrollbar__wrap) {
    max-height: 240px;
  }

  .file-size-range {
    width: 100%;
  }

  :deep(.file-size-range .el-range-input) {
    width: 26%;
    text-align: center;
  }

  :deep(.file-size-range .el-range-separator) {
    flex: initial;
    padding: 0 4px;
  }

  .file-size-inline-select {
    flex-shrink: 0;
    width: 38px;
  }

  :deep(.file-size-inline-select .el-select__wrapper) {
    min-height: 30px;
    padding-right: 2px;
    padding-left: 4px;
    background: transparent;
    box-shadow: none;
  }

  :deep(.file-size-inline-select .el-select__suffix),
  :deep(.file-size-inline-select .el-select__caret) {
    display: none;
  }

  :deep(.file-size-inline-select .el-select__selected-item),
  :deep(.file-size-inline-select .el-select__placeholder) {
    font-size: 13px;
  }
</style>

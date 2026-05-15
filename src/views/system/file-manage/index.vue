<template>
  <div class="art-full-height">
    <ArtSearchBar
      v-show="showSearchBar"
      v-model="searchForm"
      :items="searchItems"
      :showExpand="false"
      @search="handleSearch"
      @reset="handleReset"
    />

    <ElCard class="art-table-card" :style="{ 'margin-top': showSearchBar ? '12px' : '0' }">
      <ArtTableHeader
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
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
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
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import {
    type FileRecord,
    type FileQuery,
    fetchFileList,
    fetchFileDeleteById,
    fetchFileUpload,
    fetchFilePresignGetUrl
  } from '@/api/file'

  defineOptions({ name: 'FileManage' })

  const { hasAuth } = useAuth()
  const route = useRoute()
  const { t } = useI18n()

  const searchForm = ref<Partial<FileQuery>>({
    path: undefined,
    type: undefined
  })
  const showSearchBar = ref(false)

  const searchItems = computed(() => [
    {
      label: 'pages.system.fileManage.search.path',
      key: 'path',
      type: 'input',
      props: {
        clearable: true
      }
    },
    {
      label: 'pages.system.fileManage.search.type',
      key: 'type',
      type: 'input',
      props: {
        clearable: true,
        placeholder: 'pages.system.fileManage.search.placeholders.type'
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
        {
          type: 'index',
          label: t('table.column.index'),
          minWidth: 60
        },
        {
          prop: 'name',
          label: 'pages.system.fileManage.columns.name',
          minWidth: 200,
          showOverflowTooltip: true
        },
        {
          prop: 'type',
          label: 'pages.system.fileManage.columns.type',
          minWidth: 140,
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
          minWidth: 100,
          formatter: (row: FileRecord) => formatFileSize(row.size)
        },
        {
          prop: 'url',
          label: 'pages.system.fileManage.columns.url',
          minWidth: 240,
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
          minWidth: 200,
          showOverflowTooltip: true
        },
        {
          prop: 'createTime',
          label: 'pages.system.fileManage.columns.createTime',
          minWidth: 180
        },
        {
          prop: 'operation',
          label: 'pages.system.fileManage.columns.operation',
          width: 120,
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
          ? h(ArtButtonTable, {
              type: 'view',
              icon: 'ri:download-2-line',
              onClick: () => handleDownload(row)
            })
          : null,
        canAccess('system:file:delete')
          ? h(ArtButtonTable, {
              type: 'delete',
              onClick: () => handleDelete(row)
            })
          : null
      ].filter(Boolean)
    )
  }

  async function handleDownload(row: FileRecord) {
    try {
      let url = row.url
      if (url) {
        url = await fetchFilePresignGetUrl(url, 3600)
      }
      const a = document.createElement('a')
      a.href = url
      a.download = row.name
      a.target = '_blank'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
    } catch {
      ElMessage.error(t('pages.system.fileManage.messages.downloadUrlFailed'))
    }
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

  function handleSearch(params: Partial<FileQuery>) {
    replaceSearchParams({ ...params, page: 1 })
    getData()
  }

  function handleReset() {
    searchForm.value = { path: undefined, type: undefined }
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
</script>

<template>
  <div class="velox-full-height">
    <VeloxSearchBar
      v-show="showSearchBar"
      v-model="searchForm"
      :items="searchItems"
      @search="handleSearch"
      @reset="handleReset"
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
            <ElButton
              v-if="canAccess('system:file-config:create')"
              @click="openCreateDialog"
              v-ripple
            >
              {{ t('pages.config.fileConfig.actions.addConfig') }}
            </ElButton>
          </ElSpace>
        </template>
      </VeloxTableHeader>

      <VeloxTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <ElDialog
      v-model="dialogVisible"
      :title="
        dialogMode === 'create'
          ? t('pages.config.fileConfig.form.titles.create')
          : t('pages.config.fileConfig.form.titles.edit')
      "
      :width="dialogWidth"
      align-center
      class="file-config-dialog"
      @close="handleDialogClose"
    >
      <ElForm
        ref="formRef"
        :model="formModel"
        :rules="formRules"
        :label-width="formLabelWidth"
        label-position="right"
        class="file-config-form"
      >
        <ElRow :gutter="16">
          <ElCol :span="formHalfSpan">
            <ElFormItem :label="t('pages.config.fileConfig.form.fields.name')" prop="name">
              <ElInput
                v-model="formModel.name"
                maxlength="100"
                :placeholder="t('pages.config.fileConfig.form.placeholders.name')"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="formHalfSpan">
            <ElFormItem prop="storage">
              <template #label>
                <LabelTooltip
                  :label="t('pages.config.fileConfig.form.fields.storage')"
                  :tooltip="storageFieldTooltip"
                />
              </template>
              <ElSelect
                v-model="formModel.storage"
                :placeholder="t('pages.config.fileConfig.form.placeholders.storage')"
                @change="handleStorageChange"
              >
                <ElOption
                  v-for="option in allStorageOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElFormItem :label="t('pages.config.fileConfig.form.fields.remark')">
          <ElInput
            v-model="formModel.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            :placeholder="t('pages.config.fileConfig.form.placeholders.remark')"
          />
        </ElFormItem>

        <template v-if="formModel.storage === STORAGE_TYPES.DB">
          <ElRow :gutter="16">
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.domain')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.dbDomain')"
                  />
                </template>
                <ElInput
                  v-model="formModel.domain"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.dbDomain')"
                />
              </ElFormItem>
            </ElCol>
          </ElRow>
        </template>

        <template v-else-if="formModel.storage === STORAGE_TYPES.LOCAL">
          <ElRow :gutter="16">
            <ElCol :span="24">
              <ElFormItem prop="basePath">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.basePath')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.localBasePath')"
                  />
                </template>
                <ElInput
                  v-model="formModel.basePath"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.localBasePath')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.domain')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.localDomain')"
                  />
                </template>
                <ElInput
                  v-model="formModel.domain"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.localDomain')"
                />
              </ElFormItem>
            </ElCol>
          </ElRow>
        </template>

        <template v-else-if="formModel.storage === STORAGE_TYPES.FTP">
          <ElRow :gutter="20">
            <ElCol :span="24">
              <ElFormItem prop="host">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.host')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.ftpHost')"
                  />
                </template>
                <ElInput
                  v-model="formModel.host"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.hostExample')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="port">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.port')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.ftpPort')"
                  />
                </template>
                <ElInputNumber v-model="formModel.port" class="w-full" :min="1" :max="65535" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="username">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.username')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.ftpUsername')"
                  />
                </template>
                <ElInput
                  v-model="formModel.username"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.username')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="password">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.password')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.ftpPassword')"
                  />
                </template>
                <ElInput
                  v-model="formModel.password"
                  type="password"
                  show-password
                  :placeholder="t('pages.config.fileConfig.form.placeholders.password')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.ftpMode')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.ftpMode')"
                  />
                </template>
                <ElSelect
                  v-model="formModel.ftpMode"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.ftpMode')"
                >
                  <ElOption
                    :label="t('pages.config.fileConfig.form.options.ftpMode.active')"
                    value="Active"
                  />
                  <ElOption
                    :label="t('pages.config.fileConfig.form.options.ftpMode.passive')"
                    value="Passive"
                  />
                </ElSelect>
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="basePath">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.basePath')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.ftpBasePath')"
                  />
                </template>
                <ElInput
                  v-model="formModel.basePath"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.remoteBasePath')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="domain">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.domain')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.remoteDomain')"
                  />
                </template>
                <ElInput
                  v-model="formModel.domain"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.optionalDomain')"
                />
              </ElFormItem>
            </ElCol>
          </ElRow>
        </template>

        <template v-else-if="formModel.storage === STORAGE_TYPES.SFTP">
          <ElRow :gutter="20">
            <ElCol :span="24">
              <ElFormItem prop="host">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.host')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.sftpHost')"
                  />
                </template>
                <ElInput
                  v-model="formModel.host"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.hostExample')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="port">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.port')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.sftpPort')"
                  />
                </template>
                <ElInputNumber v-model="formModel.port" class="w-full" :min="1" :max="65535" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="username">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.username')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.sftpUsername')"
                  />
                </template>
                <ElInput
                  v-model="formModel.username"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.username')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="password">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.password')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.sftpPassword')"
                  />
                </template>
                <ElInput
                  v-model="formModel.password"
                  type="password"
                  show-password
                  :placeholder="t('pages.config.fileConfig.form.placeholders.password')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="basePath">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.basePath')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.sftpBasePath')"
                  />
                </template>
                <ElInput
                  v-model="formModel.basePath"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.remoteBasePath')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="domain">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.domain')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.remoteDomain')"
                  />
                </template>
                <ElInput
                  v-model="formModel.domain"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.optionalDomain')"
                />
              </ElFormItem>
            </ElCol>
          </ElRow>
        </template>

        <template v-else-if="formModel.storage === STORAGE_TYPES.S3">
          <ElRow :gutter="20">
            <ElCol :span="24">
              <ElFormItem prop="endpoint">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.endpoint')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.endpoint')"
                  />
                </template>
                <ElInput
                  v-model="formModel.endpoint"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.endpoint')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="bucket">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.bucket')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.bucket')"
                  />
                </template>
                <ElInput
                  v-model="formModel.bucket"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.bucket')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="accessKey">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.accessKey')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.accessKey')"
                  />
                </template>
                <ElInput
                  v-model="formModel.accessKey"
                  type="password"
                  show-password
                  :placeholder="t('pages.config.fileConfig.form.placeholders.accessKey')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="accessSecret">
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.accessSecret')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.accessSecret')"
                  />
                </template>
                <ElInput
                  v-model="formModel.accessSecret"
                  type="password"
                  show-password
                  :placeholder="t('pages.config.fileConfig.form.placeholders.accessSecret')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.objectPrefix')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.objectPrefix')"
                  />
                </template>
                <ElInput
                  v-model="formModel.basePath"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.objectPrefix')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.sessionToken')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.sessionToken')"
                  />
                </template>
                <ElInput
                  v-model="formModel.sessionToken"
                  type="password"
                  show-password
                  :placeholder="t('pages.config.fileConfig.form.placeholders.sessionToken')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.domain')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.s3Domain')"
                  />
                </template>
                <ElInput
                  v-model="formModel.domain"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.s3Domain')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.region')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.region')"
                  />
                </template>
                <ElInput
                  v-model="formModel.region"
                  :placeholder="t('pages.config.fileConfig.form.placeholders.region')"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="formHalfSpan">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.enablePathStyleAccess')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.enablePathStyleAccess')"
                  />
                </template>
                <ElSwitch v-model="formModel.enablePathStyleAccess" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="formHalfSpan">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    :label="t('pages.config.fileConfig.form.fields.enablePublicAccess')"
                    :tooltip="t('pages.config.fileConfig.form.tooltips.enablePublicAccess')"
                  />
                </template>
                <ElSwitch v-model="formModel.enablePublicAccess" />
              </ElFormItem>
            </ElCol>
          </ElRow>
        </template>

        <template v-else>
          <ElFormItem prop="configJson">
            <template #label>
              <LabelTooltip
                :label="t('pages.config.fileConfig.form.fields.configJson')"
                :tooltip="t('pages.config.fileConfig.form.tooltips.configJson')"
              />
            </template>
            <ElInput
              v-model="formModel.configJson"
              type="textarea"
              :rows="10"
              :placeholder="t('pages.config.fileConfig.form.placeholders.configJson')"
            />
          </ElFormItem>
        </template>
      </ElForm>

      <template #footer>
        <ElButton @click="handleDialogClose">{{ t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ t('table.form.submit') }}
        </ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { QuestionFilled } from '@element-plus/icons-vue'
  import {
    ElButton,
    ElDropdown,
    ElDropdownItem,
    ElDropdownMenu,
    ElMessage,
    ElMessageBox,
    ElSwitch,
    ElTag,
    type FormInstance,
    type FormRules
  } from 'element-plus'
  import { ElIcon, ElTooltip } from 'element-plus'
  import { useRoute } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import { useWindowSize } from '@vueuse/core'
  import { useTable } from '@/hooks/core/useTable'
  import { useAuth } from '@/hooks/core/useAuth'
  import VeloxButtonTable from '@/components/core/forms/velox-button-table/index.vue'
  import {
    type FileConfig,
    type FileConfigSaveCommand,
    fetchFileConfigCreate,
    fetchFileConfigDelete,
    fetchFileConfigGet,
    fetchFileConfigList,
    fetchFileConfigSetMaster,
    fetchFileConfigTest,
    fetchFileConfigUpdate,
    fetchFileConfigUpdateEnabled,
    fetchSupportedFileStorages
  } from '@/api/file'

  defineOptions({ name: 'FileConfig' })

  const STORAGE_TYPES = {
    DB: 1,
    LOCAL: 10,
    FTP: 11,
    SFTP: 12,
    S3: 20
  } as const

  type StorageType = (typeof STORAGE_TYPES)[keyof typeof STORAGE_TYPES]
  type DialogMode = 'create' | 'edit'

  interface FileConfigFormModel {
    name: string
    storage: StorageType
    remark: string
    basePath: string
    domain: string
    host: string
    port: number
    username: string
    password: string
    ftpMode: string
    endpoint: string
    bucket: string
    accessKey: string
    accessSecret: string
    sessionToken: string
    region: string
    enablePathStyleAccess: boolean
    enablePublicAccess: boolean
    configJson: string
  }

  type FileConfigSearchForm = {
    name?: string
    storage?: number
    enabled?: number
    createTimeRange?: [string, string]
    updateTimeRange?: [string, string]
  }

  const { hasAuth } = useAuth()
  const route = useRoute()
  const { t } = useI18n()
  const { width } = useWindowSize()

  const isMobile = computed(() => width.value <= 640)
  const dialogWidth = computed(() => (isMobile.value ? 'calc(100vw - 24px)' : '920px'))
  const formHalfSpan = computed(() => (isMobile.value ? 24 : 12))
  const formLabelWidth = computed(() => (isMobile.value ? '140px' : 'auto'))
  const supportedStorageTypes = ref<StorageType[]>([STORAGE_TYPES.DB, STORAGE_TYPES.LOCAL])

  const allStorageOptions = computed(() => [
    { label: t('pages.config.fileConfig.storage.db'), value: STORAGE_TYPES.DB },
    { label: t('pages.config.fileConfig.storage.local'), value: STORAGE_TYPES.LOCAL },
    { label: t('pages.config.fileConfig.storage.ftp'), value: STORAGE_TYPES.FTP },
    { label: t('pages.config.fileConfig.storage.sftp'), value: STORAGE_TYPES.SFTP },
    { label: t('pages.config.fileConfig.storage.s3'), value: STORAGE_TYPES.S3 }
  ])

  const unsupportedStorageLabels = computed(() =>
    allStorageOptions.value
      .filter((item) => !supportedStorageTypes.value.includes(item.value))
      .map((item) => item.label)
  )

  const storageFieldTooltip = computed(() =>
    unsupportedStorageLabels.value.length > 0
      ? t('pages.config.fileConfig.messages.unsupportedStorageDescription', {
          storages: unsupportedStorageLabels.value.join(' / ')
        })
      : t('pages.config.fileConfig.form.tooltips.storage')
  )

  const searchForm = ref<FileConfigSearchForm>({
    name: undefined,
    storage: undefined,
    enabled: undefined,
    createTimeRange: undefined,
    updateTimeRange: undefined
  })
  const showSearchBar = ref(false)

  const searchItems = computed(() => [
    {
      label: 'pages.config.fileConfig.search.name',
      key: 'name',
      type: 'input',
      props: {
        clearable: true
      }
    },
    {
      label: 'pages.config.fileConfig.search.storage',
      key: 'storage',
      type: 'select',
      props: {
        clearable: true,
        options: allStorageOptions.value
      }
    },
    {
      label: 'pages.config.fileConfig.search.enabled',
      key: 'enabled',
      type: 'select',
      props: {
        clearable: true,
        options: [
          { label: 'common.status.enabled', value: 1 },
          { label: 'common.status.disabled', value: 0 }
        ]
      }
    },
    {
      label: 'pages.config.fileConfig.search.createTimeRange',
      key: 'createTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.config.fileConfig.search.placeholders.startTime',
        endPlaceholder: 'pages.config.fileConfig.search.placeholders.endTime',
        rangeSeparator: 'pages.config.fileConfig.search.rangeSeparator'
      }
    },
    {
      label: 'pages.config.fileConfig.search.updateTimeRange',
      key: 'updateTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.config.fileConfig.search.placeholders.startTime',
        endPlaceholder: 'pages.config.fileConfig.search.placeholders.endTime',
        rangeSeparator: 'pages.config.fileConfig.search.rangeSeparator'
      }
    }
  ])

  const dialogVisible = ref(false)
  const dialogMode = ref<DialogMode>('create')
  const submitLoading = ref(false)
  const currentEditId = ref('')
  const formRef = ref<FormInstance>()
  const formModel = reactive(createDefaultForm())

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

  const hasFileConfigAuthBinding = computed(() =>
    routeAuthMarks.value.some((auth) => auth.startsWith('system:file-config:'))
  )

  const canAccess = (auth: string) => {
    return hasFileConfigAuthBinding.value ? hasAuth(auth) : true
  }

  const LabelTooltip = defineComponent({
    props: {
      label: {
        type: String,
        required: true
      },
      tooltip: {
        type: String,
        required: true
      }
    },
    setup(props) {
      return () =>
        h('span', { class: 'flex items-center' }, [
          h('span', props.label),
          h(
            ElTooltip,
            {
              content: props.tooltip,
              placement: 'top'
            },
            () =>
              h(ElIcon, { class: 'ml-0.5 cursor-help text-[var(--el-text-color-secondary)]' }, () =>
                h(QuestionFilled)
              )
          )
        ])
    }
  })

  function isValidHttpUrl(value: string) {
    try {
      const url = new URL(value)
      return url.protocol === 'http:' || url.protocol === 'https:'
    } catch {
      return false
    }
  }

  function validateDomain(_rule: unknown, value: string, callback: (error?: Error) => void) {
    const domain = value?.trim?.() || ''
    if (formModel.storage === STORAGE_TYPES.FTP || formModel.storage === STORAGE_TYPES.SFTP) {
      if (!domain) {
        callback()
        return
      }
      callback(
        isValidHttpUrl(domain)
          ? undefined
          : new Error(t('pages.config.fileConfig.messages.invalidDomain'))
      )
      return
    }

    if (!domain) {
      callback(new Error(t('pages.config.fileConfig.messages.enterDomain')))
      return
    }
    callback(
      isValidHttpUrl(domain)
        ? undefined
        : new Error(t('pages.config.fileConfig.messages.invalidDomain'))
    )
  }

  const formRules = reactive<FormRules<FileConfigFormModel>>({
    name: [
      { required: true, message: t('pages.config.fileConfig.messages.enterName'), trigger: 'blur' }
    ],
    storage: [
      {
        required: true,
        message: t('pages.config.fileConfig.messages.selectStorage'),
        trigger: 'change'
      }
    ],
    basePath: [
      {
        required: true,
        message: t('pages.config.fileConfig.messages.enterBasePath'),
        trigger: 'blur'
      }
    ],
    domain: [{ validator: validateDomain, trigger: 'blur' }],
    host: [
      { required: true, message: t('pages.config.fileConfig.messages.enterHost'), trigger: 'blur' }
    ],
    port: [
      { required: true, message: t('pages.config.fileConfig.messages.enterPort'), trigger: 'blur' }
    ],
    username: [
      {
        required: true,
        message: t('pages.config.fileConfig.messages.enterUsername'),
        trigger: 'blur'
      }
    ],
    password: [
      {
        required: true,
        message: t('pages.config.fileConfig.messages.enterPassword'),
        trigger: 'blur'
      }
    ],
    endpoint: [
      {
        required: true,
        message: t('pages.config.fileConfig.messages.enterEndpoint'),
        trigger: 'blur'
      }
    ],
    bucket: [
      {
        required: true,
        message: t('pages.config.fileConfig.messages.enterBucket'),
        trigger: 'blur'
      }
    ],
    accessKey: [
      {
        required: true,
        message: t('pages.config.fileConfig.messages.enterAccessKey'),
        trigger: 'blur'
      }
    ],
    accessSecret: [
      {
        required: true,
        message: t('pages.config.fileConfig.messages.enterAccessSecret'),
        trigger: 'blur'
      }
    ]
  })

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
      apiFn: fetchFileConfigList,
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
          label: 'pages.config.fileConfig.columns.name',
          minWidth: 160
        },
        {
          prop: 'storage',
          label: 'pages.config.fileConfig.columns.storage',
          minWidth: 120,
          formatter: (row: FileConfig) =>
            h(ElTag, { type: getStorageTagType(row.storage) }, () => getStorageLabel(row.storage))
        },
        {
          prop: 'master',
          label: 'pages.config.fileConfig.columns.master',
          minWidth: 100,
          formatter: (row: FileConfig) =>
            h(ElTag, { type: row.master ? 'success' : 'info' }, () =>
              row.master ? t('common.status.yes') : t('common.status.no')
            )
        },
        {
          prop: 'enabled',
          label: 'pages.config.fileConfig.columns.enabled',
          minWidth: 100,
          formatter: (row: FileConfig) =>
            h(ElSwitch, {
              modelValue: row.enabled === 1,
              'onUpdate:modelValue': (val: string | number | boolean) =>
                handleEnabledChange(row, !!val)
            })
        },
        {
          prop: 'remark',
          label: 'pages.config.fileConfig.columns.remark',
          minWidth: 180,
          showOverflowTooltip: true,
          formatter: (row: FileConfig) => row.remark || '-'
        },
        {
          prop: 'createTime',
          label: 'pages.config.fileConfig.columns.createTime',
          minWidth: 180
        },
        {
          prop: 'updateTime',
          label: 'pages.config.fileConfig.columns.updateTime',
          minWidth: 180
        },
        {
          prop: 'operation',
          label: 'pages.config.fileConfig.columns.operation',
          width: 150,
          fixed: 'right',
          align: 'left',
          formatter: (row: FileConfig) => renderOperationButtons(row)
        }
      ]
    }
  })

  function createDefaultForm(storage: StorageType = STORAGE_TYPES.LOCAL): FileConfigFormModel {
    return {
      name: '',
      storage,
      remark: '',
      basePath:
        storage === STORAGE_TYPES.LOCAL
          ? '/tmp/uploads'
          : storage === STORAGE_TYPES.FTP || storage === STORAGE_TYPES.SFTP
            ? '/uploads'
            : '',
      domain: '',
      host: '',
      port: storage === STORAGE_TYPES.SFTP ? 22 : 21,
      username: '',
      password: '',
      ftpMode: 'Passive',
      endpoint: '',
      bucket: '',
      accessKey: '',
      accessSecret: '',
      sessionToken: '',
      region: '',
      enablePathStyleAccess: false,
      enablePublicAccess: true,
      configJson: '{\n  "domain": "http://127.0.0.1:8080"\n}'
    }
  }

  function isSupportedStorage(storage?: number | null) {
    return supportedStorageTypes.value.includes(storage as StorageType)
  }

  async function loadSupportedStorageTypes() {
    const supported = await fetchSupportedFileStorages()
    supportedStorageTypes.value = supported.filter((item): item is StorageType =>
      Object.values(STORAGE_TYPES).includes(item as StorageType)
    )
  }

  function getStorageLabel(storage: number) {
    return (
      allStorageOptions.value.find((item) => item.value === storage)?.label ||
      `${t('common.status.unknown')}(${storage})`
    )
  }

  function getStorageTagType(storage: number) {
    switch (storage) {
      case STORAGE_TYPES.LOCAL:
        return 'info'
      case STORAGE_TYPES.FTP:
        return 'warning'
      case STORAGE_TYPES.SFTP:
        return 'danger'
      case STORAGE_TYPES.S3:
        return 'primary'
      case STORAGE_TYPES.DB:
        return 'success'
      default:
        return 'info'
    }
  }

  function renderOperationButtons(row: FileConfig) {
    const moreItems: ReturnType<typeof h>[] = []

    if (canAccess('system:file-config:update') && !row.master && isSupportedStorage(row.storage)) {
      moreItems.push(
        h(ElDropdownItem, { onClick: () => handleSetMaster(row) }, () =>
          t('pages.config.fileConfig.actions.setMaster')
        )
      )
    }

    if (canAccess('system:file-config:query') && isSupportedStorage(row.storage)) {
      moreItems.push(
        h(ElDropdownItem, { onClick: () => handleTestConfig(row) }, () =>
          t('pages.config.fileConfig.actions.test')
        )
      )
    }

    return h(
      'div',
      { class: 'file-config-operation-buttons flex items-center' },
      [
        canAccess('system:file-config:update')
          ? h(VeloxButtonTable, { type: 'edit', onClick: () => openEditDialog(row) })
          : null,
        canAccess('system:file-config:delete')
          ? h(VeloxButtonTable, { type: 'delete', onClick: () => handleDelete(row) })
          : null,
        moreItems.length > 0
          ? h(
              ElDropdown,
              { trigger: 'click' },
              {
                default: () => h(VeloxButtonTable, { type: 'more' }),
                dropdown: () => h(ElDropdownMenu, {}, () => moreItems)
              }
            )
          : null
      ].filter(Boolean)
    )
  }

  function resolveConfigId(row?: Pick<FileConfig, 'id'>) {
    const configId = row?.id?.trim()
    if (!configId) {
      ElMessage.error(t('pages.config.fileConfig.messages.missingConfigId'))
      return null
    }
    return configId
  }

  function fillForm(config?: FileConfig) {
    Object.assign(formModel, createDefaultForm())

    if (!config) {
      return
    }

    Object.assign(formModel, {
      name: config.name || '',
      storage: (config.storage || STORAGE_TYPES.LOCAL) as StorageType,
      remark: config.remark || ''
    })

    const rawConfig = config.config || {}
    const normalizedConfig = typeof rawConfig === 'string' ? JSON.parse(rawConfig) : rawConfig
    if (config.storage === STORAGE_TYPES.DB) {
      formModel.domain = String(normalizedConfig.domain || '')
      return
    }

    if (config.storage === STORAGE_TYPES.LOCAL) {
      formModel.basePath = String(normalizedConfig.basePath || '')
      formModel.domain = String(normalizedConfig.domain || '')
      return
    }

    if (config.storage === STORAGE_TYPES.FTP) {
      formModel.host = String(normalizedConfig.host || '')
      formModel.port = Number(normalizedConfig.port || 21)
      formModel.username = String(normalizedConfig.username || '')
      formModel.password = String(normalizedConfig.password || '')
      formModel.ftpMode = String(normalizedConfig.mode || 'Passive')
      formModel.basePath = String(normalizedConfig.basePath || '')
      formModel.domain = String(normalizedConfig.domain || '')
      return
    }

    if (config.storage === STORAGE_TYPES.SFTP) {
      formModel.host = String(normalizedConfig.host || '')
      formModel.port = Number(normalizedConfig.port || 22)
      formModel.username = String(normalizedConfig.username || '')
      formModel.password = String(normalizedConfig.password || '')
      formModel.basePath = String(normalizedConfig.basePath || '')
      formModel.domain = String(normalizedConfig.domain || '')
      return
    }

    if (config.storage === STORAGE_TYPES.S3) {
      formModel.endpoint = String(normalizedConfig.endpoint || '')
      formModel.bucket = String(normalizedConfig.bucket || '')
      formModel.accessKey = String(normalizedConfig.accessKey || '')
      formModel.accessSecret = String(normalizedConfig.accessSecret || '')
      formModel.basePath = String(normalizedConfig.basePath || '')
      formModel.sessionToken = String(normalizedConfig.sessionToken || '')
      formModel.domain = String(normalizedConfig.domain || '')
      formModel.region = String(normalizedConfig.region || '')
      formModel.enablePathStyleAccess = Boolean(normalizedConfig.enablePathStyleAccess)
      formModel.enablePublicAccess = normalizedConfig.enablePublicAccess !== false
      return
    }

    formModel.configJson = JSON.stringify(normalizedConfig, null, 2)
  }

  function openCreateDialog() {
    dialogMode.value = 'create'
    currentEditId.value = ''
    fillForm({ storage: STORAGE_TYPES.LOCAL } as FileConfig)
    dialogVisible.value = true
  }

  async function openEditDialog(row: FileConfig) {
    const configId = resolveConfigId(row)
    if (!configId) return

    const detail = await fetchFileConfigGet(configId)
    dialogMode.value = 'edit'
    currentEditId.value = configId
    fillForm(detail)
    dialogVisible.value = true
  }

  function handleDialogClose() {
    dialogVisible.value = false
    submitLoading.value = false
    formRef.value?.resetFields()
    fillForm()
  }

  function handleStorageChange(storage: StorageType) {
    const nextForm = createDefaultForm(storage)
    const preserved = {
      name: formModel.name,
      remark: formModel.remark,
      storage
    }
    Object.assign(formModel, nextForm, preserved)
  }

  function validateStorageConfig() {
    if (formModel.storage === STORAGE_TYPES.DB) {
      return true
    }

    if (formModel.storage === STORAGE_TYPES.LOCAL) {
      if (!formModel.basePath.trim()) {
        ElMessage.error(t('pages.config.fileConfig.messages.enterBasePath'))
        return false
      }
      return true
    }

    if (formModel.storage === STORAGE_TYPES.FTP || formModel.storage === STORAGE_TYPES.SFTP) {
      if (!formModel.host.trim()) {
        ElMessage.error(t('pages.config.fileConfig.messages.enterHost'))
        return false
      }
      if (!formModel.port || formModel.port < 1 || formModel.port > 65535) {
        ElMessage.error(t('pages.config.fileConfig.messages.enterValidPort'))
        return false
      }
      if (!formModel.username.trim() || !formModel.password.trim()) {
        ElMessage.error(t('pages.config.fileConfig.messages.completeUsernameAndPassword'))
        return false
      }
      if (!formModel.basePath.trim()) {
        ElMessage.error(t('pages.config.fileConfig.messages.enterBasePath'))
        return false
      }
      if (formModel.domain.trim() && !isValidHttpUrl(formModel.domain.trim())) {
        ElMessage.error(t('pages.config.fileConfig.messages.invalidDomain'))
        return false
      }
      if (formModel.storage === STORAGE_TYPES.FTP && !formModel.ftpMode.trim()) {
        ElMessage.error(t('pages.config.fileConfig.messages.selectTransferMode'))
        return false
      }
      return true
    }

    if (formModel.storage === STORAGE_TYPES.S3) {
      if (!formModel.endpoint.trim() || !formModel.bucket.trim()) {
        ElMessage.error(t('pages.config.fileConfig.messages.completeEndpointAndBucket'))
        return false
      }
      if (!formModel.accessKey.trim() || !formModel.accessSecret.trim()) {
        ElMessage.error(t('pages.config.fileConfig.messages.completeAccessKeyAndSecret'))
        return false
      }
      return true
    }

    try {
      JSON.parse(formModel.configJson)
      return true
    } catch {
      ElMessage.error(t('pages.config.fileConfig.messages.invalidConfigJson'))
      return false
    }
  }

  function buildPayload(): FileConfigSaveCommand {
    const payload: FileConfigSaveCommand = {
      name: formModel.name.trim(),
      storage: formModel.storage,
      remark: formModel.remark.trim() || undefined,
      config: '{}'
    }

    if (formModel.storage === STORAGE_TYPES.DB) {
      payload.config = JSON.stringify({
        domain: formModel.domain.trim() || undefined
      })
      return payload
    }

    if (formModel.storage === STORAGE_TYPES.LOCAL) {
      payload.config = JSON.stringify({
        basePath: formModel.basePath.trim(),
        domain: formModel.domain.trim() || undefined
      })
      return payload
    }

    if (formModel.storage === STORAGE_TYPES.FTP) {
      payload.config = JSON.stringify({
        host: formModel.host.trim(),
        port: formModel.port,
        username: formModel.username.trim(),
        password: formModel.password.trim(),
        mode: formModel.ftpMode,
        basePath: formModel.basePath.trim(),
        domain: formModel.domain.trim() || undefined
      })
      return payload
    }

    if (formModel.storage === STORAGE_TYPES.SFTP) {
      payload.config = JSON.stringify({
        host: formModel.host.trim(),
        port: formModel.port,
        username: formModel.username.trim(),
        password: formModel.password.trim(),
        basePath: formModel.basePath.trim(),
        domain: formModel.domain.trim() || undefined
      })
      return payload
    }

    if (formModel.storage === STORAGE_TYPES.S3) {
      payload.config = JSON.stringify({
        endpoint: formModel.endpoint.trim(),
        bucket: formModel.bucket.trim(),
        accessKey: formModel.accessKey.trim(),
        accessSecret: formModel.accessSecret.trim(),
        basePath: formModel.basePath.trim() || undefined,
        sessionToken: formModel.sessionToken.trim() || undefined,
        domain: formModel.domain.trim() || undefined,
        region: formModel.region.trim() || undefined,
        enablePathStyleAccess: formModel.enablePathStyleAccess,
        enablePublicAccess: formModel.enablePublicAccess
      })
      return payload
    }

    payload.config = formModel.configJson
    return payload
  }

  async function handleSubmit() {
    if (!formRef.value) return

    try {
      await formRef.value.validate()
    } catch {
      return
    }

    if (!validateStorageConfig()) return

    submitLoading.value = true
    try {
      const payload = buildPayload()
      if (dialogMode.value === 'create') {
        await fetchFileConfigCreate(payload)
        ElMessage.success(t('pages.config.fileConfig.messages.createSuccess'))
      } else {
        payload.id = currentEditId.value
        await fetchFileConfigUpdate(payload)
        ElMessage.success(t('pages.config.fileConfig.messages.updateSuccess'))
      }
      handleDialogClose()
      refreshData()
    } finally {
      submitLoading.value = false
    }
  }

  function handleSearch(params: FileConfigSearchForm) {
    replaceSearchParams({
      name: params.name,
      storage: params.storage,
      enabled: params.enabled,
      createTimeStart: params.createTimeRange?.[0],
      createTimeEnd: params.createTimeRange?.[1],
      updateTimeStart: params.updateTimeRange?.[0],
      updateTimeEnd: params.updateTimeRange?.[1],
      page: 1
    })
    getData()
  }

  function handleReset() {
    searchForm.value = {
      name: undefined,
      storage: undefined,
      enabled: undefined,
      createTimeRange: undefined,
      updateTimeRange: undefined
    }
    resetSearchParams()
    getData()
  }

  onMounted(async () => {
    await loadSupportedStorageTypes()
  })

  async function handleEnabledChange(row: FileConfig, val: boolean) {
    const configId = resolveConfigId(row)
    if (!configId) return

    if (!val && row.master) {
      await ElMessageBox.confirm(
        t('pages.config.fileConfig.messages.confirmDisableMaster'),
        t('pages.config.fileConfig.messages.disableMasterTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )
    }

    await fetchFileConfigUpdateEnabled(configId, val ? 1 : 0)
    row.enabled = val ? 1 : 0
    ElMessage.success(
      val
        ? t('pages.config.fileConfig.messages.enabledSuccess')
        : t('pages.config.fileConfig.messages.disabledSuccess')
    )
  }

  async function handleSetMaster(row: FileConfig) {
    const configId = resolveConfigId(row)
    if (!configId) return

    if (row.enabled !== 1) {
      await ElMessageBox.confirm(
        t('pages.config.fileConfig.messages.confirmSetDisabledMaster'),
        t('pages.config.fileConfig.messages.setMasterTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )
    } else {
      await ElMessageBox.confirm(
        t('pages.config.fileConfig.messages.confirmSetMaster', { name: row.name }),
        t('pages.config.fileConfig.messages.setMasterTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )
    }

    await fetchFileConfigSetMaster(configId)
    ElMessage.success(t('pages.config.fileConfig.messages.setMasterSuccess'))
    refreshData()
  }

  async function handleTestConfig(row: FileConfig) {
    const configId = resolveConfigId(row)
    if (!configId) return

    const url = await fetchFileConfigTest(configId)
    await ElMessageBox.alert(url, t('pages.config.fileConfig.messages.testSuccess'), {
      confirmButtonText: t('pages.config.fileConfig.messages.acknowledged')
    })
  }

  async function handleDelete(row: FileConfig) {
    const configId = resolveConfigId(row)
    if (!configId) return

    await ElMessageBox.confirm(
      t('pages.config.fileConfig.messages.confirmDelete', { name: row.name }),
      t('pages.config.fileConfig.messages.deleteTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )

    await fetchFileConfigDelete(configId)
    ElMessage.success(t('pages.config.fileConfig.messages.deleteSuccess'))
    refreshData()
  }
</script>

<style scoped lang="scss">
  .file-config-form {
    max-width: 820px;
    margin: 0 auto;
  }

  .file-config-dialog {
    :deep(.el-dialog__body) {
      padding-top: 12px;
    }

    :deep(.el-dialog__footer) {
      padding-top: 8px;
    }

    :deep(.el-form-item__label) {
      color: var(--el-text-color-regular);
    }
  }

  @media (width <= 640px) {
    .file-config-form {
      max-width: 100%;
    }

    .file-config-dialog {
      :deep(.el-form-item) {
        display: flex;
        flex-direction: column;
        align-items: stretch;
        margin-bottom: 18px;
      }

      :deep(.el-form-item__label) {
        display: flex;
        justify-content: flex-start;
        width: 100% !important;
        height: auto !important;
        min-height: 0;
        padding-top: 0;
        padding-bottom: 8px;
        line-height: 1.4 !important;
        text-align: left;
        white-space: normal;
      }

      :deep(.el-form-item__label-wrap) {
        display: block;
        width: 100% !important;
        margin: 0;
      }

      :deep(.el-form-item__label > .flex) {
        display: inline-flex;
        flex-wrap: wrap;
        gap: 4px;
        align-items: center;
      }

      :deep(.el-form-item__content) {
        display: flex;
        width: 100%;
        min-width: 0;
        margin-left: 0 !important;
      }
    }
  }

  .file-config-operation-buttons {
    :deep(> *:last-child) {
      margin-right: 0;
    }
  }
</style>

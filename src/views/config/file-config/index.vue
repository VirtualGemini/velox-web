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
            <ElButton
              v-if="canAccess('system:file-config:create')"
              @click="openCreateDialog"
              v-ripple
            >
              {{ t('pages.config.fileConfig.actions.addConfig') }}
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
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增文件配置' : '编辑文件配置'"
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
            <ElFormItem label="配置名称" prop="name">
              <ElInput v-model="formModel.name" maxlength="100" placeholder="请输入配置名称" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="formHalfSpan">
            <ElFormItem label="存储类型" prop="storage">
              <ElSelect
                v-model="formModel.storage"
                :disabled="dialogMode === 'edit' && !isSupportedStorage(formModel.storage)"
                placeholder="请选择存储类型"
                @change="handleStorageChange"
              >
                <ElOption
                  v-for="option in dialogStorageOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElFormItem label="备注">
          <ElInput
            v-model="formModel.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入备注信息"
          />
        </ElFormItem>

        <template v-if="formModel.storage === STORAGE_TYPES.DB">
          <ElRow :gutter="16">
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    label="访问域名"
                    tooltip="可选。数据库存储时用于拼接访问地址，不填时通常走系统默认下载接口。"
                  />
                </template>
                <ElInput
                  v-model="formModel.domain"
                  placeholder="为空时使用相对路径 /api/file/db/..."
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
                    label="基础目录"
                    tooltip="文件会写入服务器本地磁盘目录，请确保服务进程有读写权限。"
                  />
                </template>
                <ElInput v-model="formModel.basePath" placeholder="例如 /tmp/uploads" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    label="访问域名"
                    tooltip="用于拼接文件访问地址，不填时通常返回相对路径或使用系统默认地址。"
                  />
                </template>
                <ElInput v-model="formModel.domain" placeholder="例如 http://127.0.0.1:8080" />
              </ElFormItem>
            </ElCol>
          </ElRow>
        </template>

        <template v-else-if="formModel.storage === STORAGE_TYPES.FTP">
          <ElRow :gutter="20">
            <ElCol :span="24">
              <ElFormItem prop="host">
                <template #label>
                  <LabelTooltip label="Host" tooltip="FTP 服务器地址或域名。" />
                </template>
                <ElInput v-model="formModel.host" placeholder="例如 127.0.0.1" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="port">
                <template #label>
                  <LabelTooltip label="Port" tooltip="FTP 端口，默认一般是 21。" />
                </template>
                <ElInputNumber v-model="formModel.port" class="w-full" :min="1" :max="65535" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="username">
                <template #label>
                  <LabelTooltip label="Username" tooltip="FTP 登录用户名。" />
                </template>
                <ElInput v-model="formModel.username" placeholder="请输入用户名" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="password">
                <template #label>
                  <LabelTooltip label="Password" tooltip="FTP 登录密码。" />
                </template>
                <ElInput
                  v-model="formModel.password"
                  type="password"
                  show-password
                  placeholder="请输入密码"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip label="Mode" tooltip="FTP 传输模式，Active 或 Passive。" />
                </template>
                <ElSelect v-model="formModel.ftpMode" placeholder="请选择模式">
                  <ElOption label="Active" value="Active" />
                  <ElOption label="Passive" value="Passive" />
                </ElSelect>
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="basePath">
                <template #label>
                  <LabelTooltip
                    label="基础目录"
                    tooltip="FTP 远端根目录，文件会按该目录继续分层存放。"
                  />
                </template>
                <ElInput v-model="formModel.basePath" placeholder="例如 /uploads" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="domain">
                <template #label>
                  <LabelTooltip
                    label="访问域名"
                    tooltip="可选，用于拼接文件访问地址；不填时返回相对路径 /api/file/{configId}/get/{path}。"
                  />
                </template>
                <ElInput
                  v-model="formModel.domain"
                  placeholder="选填，例如 http://127.0.0.1:8080"
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
                  <LabelTooltip label="Host" tooltip="SFTP 服务器地址或域名。" />
                </template>
                <ElInput v-model="formModel.host" placeholder="例如 127.0.0.1" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="port">
                <template #label>
                  <LabelTooltip label="Port" tooltip="SFTP 端口，默认一般是 22。" />
                </template>
                <ElInputNumber v-model="formModel.port" class="w-full" :min="1" :max="65535" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="username">
                <template #label>
                  <LabelTooltip label="Username" tooltip="SFTP 登录用户名。" />
                </template>
                <ElInput v-model="formModel.username" placeholder="请输入用户名" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="password">
                <template #label>
                  <LabelTooltip label="Password" tooltip="SFTP 登录密码。" />
                </template>
                <ElInput
                  v-model="formModel.password"
                  type="password"
                  show-password
                  placeholder="请输入密码"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="basePath">
                <template #label>
                  <LabelTooltip
                    label="基础目录"
                    tooltip="SFTP 远端根目录，文件会按该目录继续分层存放。"
                  />
                </template>
                <ElInput v-model="formModel.basePath" placeholder="例如 /uploads" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="domain">
                <template #label>
                  <LabelTooltip
                    label="访问域名"
                    tooltip="可选，用于拼接文件访问地址；不填时返回相对路径 /api/file/{configId}/get/{path}。"
                  />
                </template>
                <ElInput
                  v-model="formModel.domain"
                  placeholder="选填，例如 http://127.0.0.1:8080"
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
                    label="Endpoint"
                    tooltip="对象存储服务地址，例如 OSS、MinIO、COS 提供的访问域名。"
                  />
                </template>
                <ElInput
                  v-model="formModel.endpoint"
                  placeholder="例如 oss-cn-beijing.aliyuncs.com"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="bucket">
                <template #label>
                  <LabelTooltip label="Bucket" tooltip="存储桶名称，对象会上传到这个 bucket。" />
                </template>
                <ElInput v-model="formModel.bucket" placeholder="请输入 Bucket" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="accessKey">
                <template #label>
                  <LabelTooltip
                    label="Access Key"
                    tooltip="访问密钥 ID，和 Access Secret 成对使用。"
                  />
                </template>
                <ElInput
                  v-model="formModel.accessKey"
                  type="password"
                  show-password
                  placeholder="请输入 Access Key"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem prop="accessSecret">
                <template #label>
                  <LabelTooltip
                    label="Access Secret"
                    tooltip="访问密钥密码，和 Access Key 成对使用。"
                  />
                </template>
                <ElInput
                  v-model="formModel.accessSecret"
                  type="password"
                  show-password
                  placeholder="请输入 Access Secret"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    label="对象前缀"
                    tooltip="可选。会作为所有对象 key 的统一前缀，例如 corp-a/uploads。"
                  />
                </template>
                <ElInput
                  v-model="formModel.basePath"
                  placeholder="可选，例如 corp-a 或 app/uploads"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    label="Session Token"
                    tooltip="可选。仅在使用 STS 临时凭证时填写，长期 AK/SK 场景留空。"
                  />
                </template>
                <ElInput
                  v-model="formModel.sessionToken"
                  type="password"
                  show-password
                  placeholder="可选，使用 STS 临时凭证时填写"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    label="访问域名"
                    tooltip="可选。用于覆盖默认访问地址，例如 CDN 域名或自定义域名。"
                  />
                </template>
                <ElInput v-model="formModel.domain" placeholder="为空时自动拼接默认域名" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    label="Region"
                    tooltip="可选。对象存储所在地域，如 cn-beijing、ap-guangzhou；不填时后端会尝试从 endpoint 推断。"
                  />
                </template>
                <ElInput v-model="formModel.region" placeholder="为空时自动推断" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="formHalfSpan">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    label="Path Style"
                    tooltip="开启后使用 endpoint/bucket/key 形式；关闭时通常使用 bucket.endpoint/key 形式。MinIO 等场景常需开启。"
                  />
                </template>
                <ElSwitch v-model="formModel.enablePathStyleAccess" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="formHalfSpan">
              <ElFormItem>
                <template #label>
                  <LabelTooltip
                    label="公开访问"
                    tooltip="开启后返回公开访问地址；关闭后会生成带签名的访问地址。"
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
                label="配置 JSON"
                tooltip="当前存储类型未提供结构化表单，请直接输入完整 JSON 配置。"
              />
            </template>
            <ElInput
              v-model="formModel.configJson"
              type="textarea"
              :rows="10"
              placeholder='请输入 JSON，例如 {"domain":"http://127.0.0.1:8080"}'
            />
          </ElFormItem>
        </template>
      </ElForm>

      <template #footer>
        <ElButton @click="handleDialogClose">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">提交</ElButton>
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
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import {
    type FileConfig,
    type FileConfigQuery,
    type FileConfigSaveCommand,
    fetchFileConfigCreate,
    fetchFileConfigDelete,
    fetchFileConfigGet,
    fetchFileConfigList,
    fetchFileConfigSetMaster,
    fetchFileConfigTest,
    fetchFileConfigUpdate,
    fetchFileConfigUpdateEnabled
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

  const { hasAuth } = useAuth()
  const route = useRoute()
  const { t } = useI18n()
  const { width } = useWindowSize()

  const isMobile = computed(() => width.value <= 640)
  const dialogWidth = computed(() => (isMobile.value ? 'calc(100vw - 24px)' : '920px'))
  const formHalfSpan = computed(() => (isMobile.value ? 24 : 12))
  const formLabelWidth = computed(() => (isMobile.value ? '140px' : '140px'))

  const allStorageOptions = computed(() => [
    { label: t('pages.config.fileConfig.storage.db'), value: STORAGE_TYPES.DB },
    { label: t('pages.config.fileConfig.storage.local'), value: STORAGE_TYPES.LOCAL },
    { label: t('pages.config.fileConfig.storage.ftp'), value: STORAGE_TYPES.FTP },
    { label: t('pages.config.fileConfig.storage.sftp'), value: STORAGE_TYPES.SFTP },
    { label: t('pages.config.fileConfig.storage.s3'), value: STORAGE_TYPES.S3 }
  ])

  const supportedStorageOptions = computed(() =>
    allStorageOptions.value.filter((item) =>
      (
        [
          STORAGE_TYPES.DB,
          STORAGE_TYPES.LOCAL,
          STORAGE_TYPES.FTP,
          STORAGE_TYPES.SFTP,
          STORAGE_TYPES.S3
        ] as StorageType[]
      ).includes(item.value)
    )
  )

  const dialogStorageOptions = computed(() => {
    if (dialogMode.value === 'create' || isSupportedStorage(formModel.storage)) {
      return supportedStorageOptions.value
    }

    const currentOption = allStorageOptions.value.find((item) => item.value === formModel.storage)
    return currentOption
      ? [currentOption, ...supportedStorageOptions.value]
      : supportedStorageOptions.value
  })

  const searchForm = ref<Partial<FileConfigQuery>>({
    name: undefined,
    storage: undefined
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
    return (
      storage === STORAGE_TYPES.DB ||
      storage === STORAGE_TYPES.LOCAL ||
      storage === STORAGE_TYPES.FTP ||
      storage === STORAGE_TYPES.SFTP ||
      storage === STORAGE_TYPES.S3
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
          ? h(ArtButtonTable, { type: 'edit', onClick: () => openEditDialog(row) })
          : null,
        canAccess('system:file-config:delete')
          ? h(ArtButtonTable, { type: 'delete', onClick: () => handleDelete(row) })
          : null,
        moreItems.length > 0
          ? h(
              ElDropdown,
              { trigger: 'click' },
              {
                default: () => h(ArtButtonTable, { type: 'more' }),
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
    fillForm()
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

  function handleSearch(params: Partial<FileConfigQuery>) {
    replaceSearchParams({
      ...params,
      page: 1
    })
    getData()
  }

  function handleReset() {
    searchForm.value = {
      name: undefined,
      storage: undefined
    }
    resetSearchParams()
    getData()
  }

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

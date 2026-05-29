<template>
  <ElDrawer
    v-model="drawerVisible"
    :title="t('pages.system.account.detailCard.title')"
    :size="drawerSize"
    destroy-on-close
    append-to-body
    class="account-detail-drawer"
  >
    <template v-if="detail">
      <div class="account-detail-page">
        <section class="detail-hero">
          <div class="hero-identity">
            <div class="hero-media">
              <ElImage
                :src="detail.header.avatar"
                fit="cover"
                class="hero-avatar"
                :preview-src-list="detail.header.avatar ? [detail.header.avatar] : []"
                preview-teleported
              />
            </div>
            <div class="hero-copy">
              <div class="hero-title-row">
                <h1 class="hero-title">{{ detail.header.username }}</h1>
                <ElTag :type="statusTag.type">{{ t(statusTag.textKey) }}</ElTag>
              </div>
              <p class="hero-subtitle">
                {{ detail.header.nickname || detail.header.realName || '-' }}
              </p>
            </div>
            <div class="hero-role-strip">
              <ElButton
                v-if="roleListScrollable"
                :icon="ArrowLeft"
                :disabled="!canScrollRoleLeft"
                link
                size="small"
                class="hero-role-nav"
                aria-label="向左滚动角色列表"
                @click="scrollRoleList('left')"
              />
              <div
                ref="roleListRef"
                class="hero-role-list"
                :class="{ 'is-dragging': roleListDrag.isDragging.value }"
                @click.capture="roleListDrag.preventClickAfterDrag"
                @pointercancel="roleListDrag.endDrag"
                @pointerdown="startRoleListDrag"
                @pointermove="roleListDrag.moveDrag"
                @pointerup="roleListDrag.endDrag"
                @scroll.passive="updateRoleScrollState"
              >
                <ElTag v-for="role in detail.header.roleCodes" :key="role" effect="plain">
                  {{ role }}
                </ElTag>
              </div>
              <ElButton
                v-if="roleListScrollable"
                :icon="ArrowRight"
                :disabled="!canScrollRoleRight"
                link
                size="small"
                class="hero-role-nav"
                aria-label="向右滚动角色列表"
                @click="scrollRoleList('right')"
              />
            </div>
          </div>

          <div class="hero-meta-list">
            <div class="detail-field detail-field--full">
              <span class="detail-label">
                {{ t('pages.system.account.detailCard.header.remark') }}
              </span>
              <span class="detail-value detail-value--long">{{ detail.header.remark || '-' }}</span>
            </div>
            <div class="detail-field">
              <span class="detail-label">
                {{ t('pages.system.account.detailCard.header.createdAt') }}
              </span>
              <span class="detail-value">{{ detail.header.createTime || '-' }}</span>
            </div>
            <div class="detail-field">
              <span class="detail-label">
                {{ t('pages.system.account.detailCard.header.updatedAt') }}
              </span>
              <span class="detail-value">{{ detail.header.updateTime || '-' }}</span>
            </div>
          </div>
        </section>

        <div
          ref="detailTabsShellRef"
          class="tab-scroll-shell"
          :class="{ 'is-scrollable': detailTabsScrollable }"
          @click.capture="detailTabsDrag.preventClickAfterDrag"
          @pointercancel="detailTabsDrag.endDrag"
          @pointerdown="startDetailTabsDrag"
          @pointermove="detailTabsDrag.moveDrag"
          @pointerup="detailTabsDrag.endDrag"
        >
          <ElButton
            v-if="detailTabsScrollable"
            :icon="ArrowLeft"
            :disabled="!canScrollDetailTabsLeft"
            link
            size="small"
            class="tab-scroll-nav tab-scroll-nav--left"
            aria-label="向左滚动详情标签"
            @click="scrollDetailTabs('left')"
          />
          <ElTabs
            v-model="activeTab"
            class="detail-tabs tab-scroll-tabs"
            :class="{ 'is-dragging': detailTabsDrag.isDragging.value }"
          >
            <ElTabPane
              :label="t('pages.system.account.detailCard.sections.profile')"
              name="profile"
            >
              <div class="detail-list">
                <div
                  v-for="row in profileRows"
                  :key="row.label"
                  class="detail-field"
                  :class="{ 'detail-field--full': row.full }"
                >
                  <span class="detail-label">{{ row.label }}</span>
                  <div v-if="row.kind === 'tags'" class="detail-value tag-list">
                    <ElTag v-for="tag in row.tags" :key="tag" effect="plain">{{ tag }}</ElTag>
                    <span v-if="!row.tags?.length">-</span>
                  </div>
                  <span v-else class="detail-value" :class="{ 'detail-value--long': row.full }">
                    {{ row.value || '-' }}
                  </span>
                </div>
              </div>
            </ElTabPane>

            <ElTabPane
              :label="t('pages.system.account.detailCard.sections.account')"
              name="account"
            >
              <div class="detail-list">
                <div
                  v-for="row in accountRows"
                  :key="row.label"
                  class="detail-field"
                  :class="{ 'detail-field--full': row.full }"
                >
                  <span class="detail-label">{{ row.label }}</span>
                  <span class="detail-value" :class="{ 'detail-value--long': row.full }">
                    {{ row.value || '-' }}
                  </span>
                </div>
              </div>
            </ElTabPane>

            <ElTabPane
              :label="t('pages.system.account.detailCard.sections.security')"
              name="security"
            >
              <div class="detail-list">
                <div
                  v-for="row in securityRows"
                  :key="row.label"
                  class="detail-field"
                  :class="{ 'detail-field--full': row.full }"
                >
                  <span class="detail-label">{{ row.label }}</span>
                  <div v-if="row.kind === 'tags'" class="detail-value tag-list">
                    <ElTag v-for="tag in row.tags" :key="tag" effect="plain">{{ tag }}</ElTag>
                    <span v-if="!row.tags?.length">-</span>
                  </div>
                  <span v-else class="detail-value">{{ row.value || '-' }}</span>
                </div>
              </div>
            </ElTabPane>

            <ElTabPane
              :label="t('pages.system.account.detailCard.sections.thirdParty')"
              name="thirdParty"
            >
              <div class="provider-list">
                <div
                  v-for="provider in thirdPartyProviderRows"
                  :key="`${provider.displayName}-${provider.key}`"
                  class="provider-row"
                >
                  <div class="provider-main">
                    <img
                      v-if="provider.iconUrl"
                      :src="provider.iconUrl"
                      :alt="provider.displayName"
                      class="provider-icon"
                    />
                    <VeloxSvgIcon v-else :icon="provider.icon" class="provider-svg-icon" />
                    <span class="provider-name">{{ provider.displayName }}</span>
                  </div>
                  <ElTag :type="provider.bound ? 'success' : 'info'">
                    {{
                      provider.bound
                        ? t('pages.system.account.detailCard.thirdParty.bound')
                        : t('pages.system.account.detailCard.thirdParty.unbound')
                    }}
                  </ElTag>
                </div>
                <div v-if="!thirdPartyProviderRows.length" class="empty-text">-</div>
              </div>
            </ElTabPane>
          </ElTabs>
          <ElButton
            v-if="detailTabsScrollable"
            :icon="ArrowRight"
            :disabled="!canScrollDetailTabsRight"
            link
            size="small"
            class="tab-scroll-nav tab-scroll-nav--right"
            aria-label="向右滚动详情标签"
            @click="scrollDetailTabs('right')"
          />
        </div>
      </div>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { computed, nextTick, ref, watch } from 'vue'
  import { useWindowSize } from '@vueuse/core'
  import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
  import { useI18n } from 'vue-i18n'
  import { useHorizontalDragScroll } from '@/hooks'
  import linuxdoIcon from '@/assets/images/svg/linuxdo.svg'

  interface DetailRow {
    label: string
    value?: string
    kind?: 'text' | 'tags'
    tags?: string[]
    full?: boolean
  }

  type ThirdPartyProvider = Api.SystemManage.AccountDetailCard['thirdPartyProviders'][number]

  const props = defineProps<{
    visible: boolean
    detail?: Api.SystemManage.AccountDetailCard
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
  }>()

  const { t } = useI18n()
  const { width } = useWindowSize()
  const activeTab = ref('profile')
  const roleListRef = ref<HTMLElement>()
  const roleListScrollable = ref(false)
  const canScrollRoleLeft = ref(false)
  const canScrollRoleRight = ref(false)
  const detailTabsShellRef = ref<HTMLElement>()
  const detailTabsScrollable = ref(false)
  const canScrollDetailTabsLeft = ref(false)
  const canScrollDetailTabsRight = ref(false)
  const roleListDrag = useHorizontalDragScroll()
  const detailTabsDrag = useHorizontalDragScroll()

  const isMobile = computed(() => width.value <= 640)
  const drawerSize = computed(() => {
    if (isMobile.value) return '100%'
    if (width.value <= 960) return '86%'
    return '760px'
  })

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const updateRoleScrollState = () => {
    const roleList = roleListRef.value
    if (!roleList) {
      roleListScrollable.value = false
      canScrollRoleLeft.value = false
      canScrollRoleRight.value = false
      return
    }

    const maxScrollLeft = roleList.scrollWidth - roleList.clientWidth
    roleListScrollable.value = maxScrollLeft > 1
    canScrollRoleLeft.value = roleList.scrollLeft > 1
    canScrollRoleRight.value = roleList.scrollLeft < maxScrollLeft - 1
  }

  const scrollRoleList = (direction: 'left' | 'right') => {
    const roleList = roleListRef.value
    if (!roleList) return

    const distance = Math.max(roleList.clientWidth * 0.8, 96)
    roleList.scrollBy({
      left: direction === 'left' ? -distance : distance,
      behavior: 'smooth'
    })

    window.setTimeout(updateRoleScrollState, 260)
  }

  const startRoleListDrag = (event: PointerEvent) => {
    roleListDrag.startDrag(event, roleListRef.value, updateRoleScrollState)
  }

  const getTabsScroller = (shell?: HTMLElement) =>
    shell?.querySelector<HTMLElement>('.el-tabs__nav-scroll')

  const updateDetailTabsScrollState = () => {
    const tabsScroller = getTabsScroller(detailTabsShellRef.value)
    if (!tabsScroller) {
      detailTabsScrollable.value = false
      canScrollDetailTabsLeft.value = false
      canScrollDetailTabsRight.value = false
      return
    }

    const maxScrollLeft = tabsScroller.scrollWidth - tabsScroller.clientWidth
    detailTabsScrollable.value = maxScrollLeft > 1
    canScrollDetailTabsLeft.value = tabsScroller.scrollLeft > 1
    canScrollDetailTabsRight.value = tabsScroller.scrollLeft < maxScrollLeft - 1
  }

  const scrollDetailTabs = (direction: 'left' | 'right') => {
    const tabsScroller = getTabsScroller(detailTabsShellRef.value)
    if (!tabsScroller) return

    const distance = Math.max(tabsScroller.clientWidth * 0.8, 96)
    tabsScroller.scrollBy({
      left: direction === 'left' ? -distance : distance,
      behavior: 'smooth'
    })

    window.setTimeout(updateDetailTabsScrollState, 260)
  }

  const startDetailTabsDrag = (event: PointerEvent) => {
    detailTabsDrag.startDrag(
      event,
      getTabsScroller(detailTabsShellRef.value),
      updateDetailTabsScrollState
    )
  }

  watch(
    () => [props.visible, props.detail?.header?.roleCodes?.join('|'), width.value],
    () => {
      nextTick(updateRoleScrollState)
    },
    { immediate: true }
  )

  watch(
    () => [props.visible, activeTab.value, width.value],
    () => {
      nextTick(updateDetailTabsScrollState)
    },
    { immediate: true }
  )

  const statusTag = computed(() => {
    const map = {
      '1': { type: 'success' as const, textKey: 'pages.system.account.status.online' },
      '2': { type: 'info' as const, textKey: 'pages.system.account.status.offline' },
      '3': { type: 'warning' as const, textKey: 'pages.system.account.status.abnormal' },
      '4': { type: 'danger' as const, textKey: 'pages.system.account.status.revoked' }
    }
    return (
      map[props.detail?.header?.status as keyof typeof map] || {
        type: 'info' as const,
        textKey: 'common.status.unknown'
      }
    )
  })

  const resolveGender = (gender?: number) => {
    if (gender === 1) return t('pages.system.account.detailCard.gender.male')
    if (gender === 2) return t('pages.system.account.detailCard.gender.female')
    if (gender === 3) return t('pages.system.account.detailCard.gender.other')
    return t('pages.system.account.detailCard.gender.unknown')
  }

  const resolveLoginMethod = (method: string) => {
    const mapping: Record<string, string> = {
      password: 'pages.system.account.detailCard.loginMethods.password',
      email_code: 'pages.system.account.detailCard.loginMethods.emailCode'
    }
    return t(mapping[method] || 'common.status.unknown')
  }

  const resolveThirdPartyProvider = (provider: ThirdPartyProvider) => {
    const source = `${provider.name || ''} ${provider.key || ''}`.toLowerCase()

    if (source.includes('github')) {
      return {
        displayName: 'GitHub',
        icon: 'ri:github-fill'
      }
    }

    if (source.includes('linuxdo') || source.includes('linux.do')) {
      return {
        displayName: 'LinuxDO',
        iconUrl: linuxdoIcon
      }
    }

    return {
      displayName: provider.name || provider.key || '-',
      icon: 'ri:login-circle-line'
    }
  }

  const thirdPartyProviderRows = computed(() =>
    (props.detail?.thirdPartyProviders || []).map((provider) => ({
      ...provider,
      ...resolveThirdPartyProvider(provider)
    }))
  )

  const profileRows = computed<DetailRow[]>(() => {
    const profile = props.detail?.profile
    if (!profile) return []
    return [
      {
        label: t('pages.system.account.detailCard.profile.realName'),
        value: profile.realName || '-'
      },
      {
        label: t('pages.system.account.detailCard.profile.nickname'),
        value: profile.nickname || '-'
      },
      {
        label: t('pages.system.account.detailCard.profile.gender'),
        value: resolveGender(profile.gender)
      },
      {
        label: t('pages.system.account.detailCard.profile.displayEmail'),
        value: profile.displayEmail || '-'
      },
      {
        label: t('pages.system.account.detailCard.profile.displayMobile'),
        value: profile.displayMobile || '-'
      },
      {
        label: t('pages.system.account.detailCard.profile.address'),
        value: profile.address || '-',
        full: true
      },
      {
        label: t('pages.system.account.detailCard.profile.position'),
        value: profile.position || '-'
      },
      {
        label: t('pages.system.account.detailCard.profile.company'),
        value: profile.company || '-'
      },
      {
        label: t('pages.system.account.detailCard.profile.signature'),
        value: profile.signature || '-',
        full: true
      },
      {
        label: t('pages.system.account.detailCard.profile.introduction'),
        value: profile.introduction || '-',
        full: true
      },
      {
        label: t('pages.system.account.detailCard.profile.tags'),
        kind: 'tags',
        tags: profile.tags || [],
        full: true
      }
    ]
  })

  const accountRows = computed<DetailRow[]>(() => {
    const account = props.detail?.account
    if (!account) return []
    return [
      {
        label: t('pages.system.account.detailCard.account.accountId'),
        value: account.accountId || '-'
      },
      {
        label: t('pages.system.account.detailCard.account.username'),
        value: account.username || '-'
      },
      {
        label: t('pages.system.account.detailCard.account.status'),
        value: t(statusTag.value.textKey)
      },
      {
        label: t('pages.system.account.detailCard.account.pendingDeletion'),
        value: account.pendingDeletion ? t('common.status.yes') : t('common.status.no')
      },
      {
        label: t('pages.system.account.detailCard.account.deletionRequestedAt'),
        value: account.deletionRequestedAt || '-'
      },
      {
        label: t('pages.system.account.detailCard.account.deletionExpiresAt'),
        value: account.deletionExpiresAt || '-'
      },
      {
        label: t('pages.system.account.detailCard.account.loginFailCount'),
        value: String(account.loginFailCount ?? 0)
      },
      {
        label: t('pages.system.account.detailCard.account.loginFailTime'),
        value: account.loginFailTime || '-'
      }
    ]
  })

  const securityRows = computed<DetailRow[]>(() => {
    const security = props.detail?.security
    if (!security) return []
    return [
      {
        label: t('pages.system.account.detailCard.security.securityEmail'),
        value: security.securityEmail || '-'
      },
      {
        label: t('pages.system.account.detailCard.security.emailMfaEnabled'),
        value: security.emailMfaEnabled ? t('common.status.enabled') : t('common.status.disabled')
      },
      {
        label: t('pages.system.account.detailCard.security.totpMfaEnabled'),
        value: security.totpMfaEnabled ? t('common.status.enabled') : t('common.status.disabled')
      },
      {
        label: t('pages.system.account.detailCard.security.loginMethods'),
        kind: 'tags',
        tags: (security.loginMethods || []).map(resolveLoginMethod),
        full: true
      },
      {
        label: t('pages.system.account.detailCard.security.emailVerifiedAt'),
        value: security.emailVerifiedAt || '-'
      },
      {
        label: t('pages.system.account.detailCard.security.lastPasswordChangeAt'),
        value: security.lastPasswordChangeAt || '-'
      }
    ]
  })
</script>

<style scoped>
  .account-detail-drawer :deep(.el-drawer__body) {
    padding: 0;
  }

  .account-detail-page {
    width: min(100%, 760px);
    height: 100%;
    padding: 20px 24px 28px;
    margin: 0 auto;
    overflow-y: auto;
  }

  .detail-hero {
    padding-bottom: 8px;
  }

  .hero-identity {
    display: grid;
    grid-template-columns: 80px minmax(0, 1fr);
    gap: 16px;
    align-items: center;
  }

  .hero-media {
    display: flex;
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
    min-width: 0;
  }

  .hero-avatar {
    width: 80px;
    height: 80px;
    overflow: hidden;
    border-radius: 8px;
  }

  .hero-copy {
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 0;
    min-height: 80px;
  }

  .hero-title-row {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
  }

  .hero-title {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    line-height: 32px;
    color: var(--velox-text-color);
    overflow-wrap: anywhere;
  }

  .hero-subtitle {
    margin: 8px 0 0;
    font-size: 14px;
    line-height: 22px;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
    overflow-wrap: anywhere;
  }

  .hero-role-list,
  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    min-width: 0;
  }

  .hero-role-strip {
    display: flex;
    grid-column: 1 / -1;
    gap: 8px;
    align-items: center;
    min-width: 0;
  }

  .hero-role-nav {
    flex: 0 0 auto;
    width: 24px;
    height: 24px;
    padding: 0;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
  }

  .hero-role-nav.is-disabled {
    opacity: 0.35;
  }

  .hero-role-list {
    flex: 1 1 auto;
    flex-wrap: nowrap;
    justify-content: flex-start;
    min-width: 0;
    overflow-x: auto;
    scrollbar-width: none;
    touch-action: pan-y;
    cursor: grab;
    user-select: none;
  }

  .hero-role-list.is-dragging {
    cursor: grabbing;
  }

  .hero-role-list::-webkit-scrollbar {
    display: none;
  }

  .hero-role-list :deep(.el-tag) {
    flex: 0 0 auto;
    height: auto;
    min-height: 24px;
    white-space: nowrap;
  }

  .hero-role-list :deep(.el-tag__content) {
    line-height: 18px;
    white-space: nowrap;
  }

  .hero-meta-list,
  .detail-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    column-gap: 28px;
  }

  .hero-meta-list {
    margin-top: 18px;
  }

  .detail-field {
    display: flex;
    flex-direction: column;
    gap: 6px;
    min-width: 0;
    padding: 13px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .detail-field--full {
    grid-column: 1 / -1;
  }

  .detail-label {
    font-size: 12px;
    line-height: 18px;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
  }

  .detail-value {
    min-width: 0;
    line-height: 22px;
    color: var(--velox-text-color);
    word-break: break-word;
    overflow-wrap: anywhere;
  }

  .detail-value--long {
    line-height: 24px;
    white-space: pre-wrap;
  }

  .detail-tabs {
    margin-top: 14px;
  }

  .tab-scroll-shell {
    position: relative;
  }

  .tab-scroll-shell.is-scrollable .tab-scroll-tabs :deep(.el-tabs__header) {
    padding-right: 28px;
    padding-left: 28px;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav-wrap.is-scrollable) {
    padding: 0;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav-prev),
  .tab-scroll-tabs :deep(.el-tabs__nav-next) {
    display: none;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav-scroll) {
    overflow-x: auto;
    scrollbar-width: none;
    touch-action: pan-y;
    cursor: grab;
    user-select: none;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav-scroll::-webkit-scrollbar) {
    display: none;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav) {
    flex-wrap: nowrap;
    transform: none !important;
  }

  .tab-scroll-tabs.is-dragging :deep(.el-tabs__nav-scroll) {
    cursor: grabbing;
  }

  .tab-scroll-nav {
    position: absolute;
    top: 0;
    z-index: 2;
    width: 24px;
    height: 32px;
    padding: 0;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
  }

  .tab-scroll-nav--left {
    left: 0;
  }

  .tab-scroll-nav--right {
    right: 0;
  }

  .tab-scroll-nav.is-disabled {
    opacity: 0.35;
  }

  .detail-tabs :deep(.el-tabs__header) {
    margin-bottom: 2px;
  }

  .provider-row {
    display: flex;
    gap: 14px;
    align-items: center;
    justify-content: space-between;
    min-width: 0;
    padding: 14px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .provider-main {
    display: flex;
    gap: 10px;
    align-items: center;
    min-width: 0;
  }

  .provider-icon,
  .provider-svg-icon {
    flex: 0 0 24px;
    width: 24px;
    height: 24px;
  }

  .provider-icon {
    object-fit: contain;
  }

  .provider-svg-icon {
    font-size: 24px;
    color: var(--velox-text-color);
  }

  .provider-name {
    font-weight: 500;
    line-height: 22px;
    color: var(--velox-text-color);
    overflow-wrap: anywhere;
  }

  .empty-text {
    padding: 14px 0;
    line-height: 22px;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
    overflow-wrap: anywhere;
  }

  @media (width <= 640px) {
    .account-detail-page {
      width: 100%;
      padding: 16px 16px 24px;
    }

    .hero-identity {
      grid-template-columns: 64px minmax(0, 1fr);
      gap: 12px;
    }

    .hero-role-strip {
      gap: 6px;
    }

    .hero-avatar {
      width: 64px;
      height: 64px;
    }

    .hero-copy {
      min-height: 64px;
    }

    .hero-title {
      font-size: 20px;
      line-height: 28px;
    }

    .hero-meta-list,
    .detail-list {
      grid-template-columns: 1fr;
    }

    .provider-row {
      flex-direction: column;
      gap: 10px;
    }
  }
</style>

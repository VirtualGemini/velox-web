<script setup lang="ts">
  import { computed, nextTick, onMounted, ref, watch } from 'vue'
  import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
  import { useWindowSize } from '@vueuse/core'
  import { useRoute, useRouter } from 'vue-router'
  import { useHorizontalDragScroll } from '@/hooks'
  import { useAccountDetail } from './composables/useUserDetail'
  import { useAccountTab } from './composables/useAccountTab'
  import ProfileTab from './components/tabs/ProfileTab.vue'
  import AccountTab from './components/tabs/AccountTab.vue'
  import AccountSecurityTab from './components/tabs/AccountSecurityTab.vue'
  import ThirdPartyTab from './components/tabs/ThirdPartyTab.vue'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'AccountCenter' })

  const { t } = useI18n()
  const { width } = useWindowSize()
  const route = useRoute()
  const router = useRouter()
  const { detail, load, patch, syncStoreBasic } = useAccountDetail()
  const { detail: accountTabDetail, load: loadAccountTab, patch: patchAccountTab } = useAccountTab()

  const validTabs = ['profile', 'account', 'security', 'third-party'] as const
  type TabKey = (typeof validTabs)[number]

  const resolveTab = (raw: unknown): TabKey => {
    const v = String(raw || '')
    if (v === 'basic') return 'profile'
    if (v === 'password') return 'security'
    return (validTabs as readonly string[]).includes(v) ? (v as TabKey) : 'profile'
  }

  const activeTab = ref<TabKey>(resolveTab(route.query.tab))
  const accountCenterTabsShellRef = ref<HTMLElement>()
  const accountCenterTabsScrollable = ref(false)
  const canScrollAccountCenterTabsLeft = ref(false)
  const canScrollAccountCenterTabsRight = ref(false)
  const accountCenterTabsDrag = useHorizontalDragScroll()

  const getAccountCenterTabsScroller = () =>
    accountCenterTabsShellRef.value?.querySelector<HTMLElement>('.el-tabs__nav-scroll')

  const updateAccountCenterTabsScrollState = () => {
    const tabsScroller = getAccountCenterTabsScroller()
    if (!tabsScroller) {
      accountCenterTabsScrollable.value = false
      canScrollAccountCenterTabsLeft.value = false
      canScrollAccountCenterTabsRight.value = false
      return
    }

    const maxScrollLeft = tabsScroller.scrollWidth - tabsScroller.clientWidth
    accountCenterTabsScrollable.value = maxScrollLeft > 1
    canScrollAccountCenterTabsLeft.value = tabsScroller.scrollLeft > 1
    canScrollAccountCenterTabsRight.value = tabsScroller.scrollLeft < maxScrollLeft - 1
  }

  const scrollAccountCenterTabs = (direction: 'left' | 'right') => {
    const tabsScroller = getAccountCenterTabsScroller()
    if (!tabsScroller) return

    const distance = Math.max(tabsScroller.clientWidth * 0.8, 96)
    tabsScroller.scrollBy({
      left: direction === 'left' ? -distance : distance,
      behavior: 'smooth'
    })

    window.setTimeout(updateAccountCenterTabsScrollState, 260)
  }

  const startAccountCenterTabsDrag = (event: PointerEvent) => {
    accountCenterTabsDrag.startDrag(
      event,
      getAccountCenterTabsScroller(),
      updateAccountCenterTabsScrollState
    )
  }

  watch(
    () => route.query.tab,
    (v) => {
      if (route.name !== 'AccountCenter') return
      activeTab.value = resolveTab(v)
    }
  )

  const onTabChange = (val: string | number) => {
    const next = resolveTab(val)
    activeTab.value = next
    router.replace({ query: { ...route.query, tab: next } })
  }

  onMounted(() => {
    load()
    loadAccountTab()
    nextTick(updateAccountCenterTabsScrollState)
  })

  watch(
    () => [activeTab.value, width.value],
    () => {
      nextTick(updateAccountCenterTabsScrollState)
    },
    { immediate: true }
  )

  const onProfileSaved = (patchValue: Partial<Api.Auth.AccountDetail>) => {
    patch(patchValue)
    syncStoreBasic({ email: patchValue.email, phone: patchValue.phone })
  }

  const onDetailUpdated = (patchValue: Partial<Api.Auth.AccountDetail>) => {
    patch(patchValue)
    syncStoreBasic({ email: patchValue.email, phone: patchValue.phone })
  }

  const onAvatarUpdated = (avatar: string) => {
    patch({ avatar })
  }

  const onUsernameUpdated = (username: string) => {
    patchAccountTab({ username })
    patch({ userName: username })
    syncStoreBasic({ userName: username })
  }

  const onDeletionRequested = () => {
    patchAccountTab({ pendingDeletion: true })
  }

  const onAccountTabPatched = (patchValue: Partial<Api.Auth.AccountTabInfo>) => {
    patchAccountTab(patchValue)
  }

  const cardDetail = computed(() => detail.value)
  const accountCardDetail = computed(() => accountTabDetail.value)
</script>

<template>
  <div class="velox-account-center-page velox-full-height">
    <ElCard class="velox-table-card velox-account-center-card">
      <div
        ref="accountCenterTabsShellRef"
        class="tab-scroll-shell"
        :class="{ 'is-scrollable': accountCenterTabsScrollable }"
        @click.capture="accountCenterTabsDrag.preventClickAfterDrag"
        @pointercancel="accountCenterTabsDrag.endDrag"
        @pointerdown="startAccountCenterTabsDrag"
        @pointermove="accountCenterTabsDrag.moveDrag"
        @pointerup="accountCenterTabsDrag.endDrag"
      >
        <ElButton
          v-if="accountCenterTabsScrollable"
          :icon="ArrowLeft"
          :disabled="!canScrollAccountCenterTabsLeft"
          link
          size="small"
          class="tab-scroll-nav tab-scroll-nav--left"
          aria-label="向左滚动个人中心标签"
          @click="scrollAccountCenterTabs('left')"
        />
        <ElTabs
          :model-value="activeTab"
          class="velox-account-center-tabs tab-scroll-tabs"
          :class="{ 'is-dragging': accountCenterTabsDrag.isDragging.value }"
          @tab-change="onTabChange"
        >
          <ElTabPane :label="t('pages.system.accountCenter.tabs.profile')" name="profile">
            <ProfileTab
              v-if="activeTab === 'profile'"
              :detail="cardDetail"
              @saved="onProfileSaved"
              @avatar-updated="onAvatarUpdated"
            />
          </ElTabPane>
          <ElTabPane :label="t('pages.system.accountCenter.tabs.account')" name="account">
            <AccountTab
              v-if="activeTab === 'account'"
              :detail="accountCardDetail"
              @username-updated="onUsernameUpdated"
              @deletion-requested="onDeletionRequested"
            />
          </ElTabPane>
          <ElTabPane :label="t('pages.system.accountCenter.tabs.security')" name="security">
            <AccountSecurityTab
              v-if="activeTab === 'security'"
              :detail="cardDetail"
              @detail-updated="onDetailUpdated"
              @account-tab-updated="onAccountTabPatched"
            />
          </ElTabPane>
          <ElTabPane :label="t('pages.system.accountCenter.tabs.thirdParty')" name="third-party">
            <ThirdPartyTab v-if="activeTab === 'third-party'" />
          </ElTabPane>
        </ElTabs>
        <ElButton
          v-if="accountCenterTabsScrollable"
          :icon="ArrowRight"
          :disabled="!canScrollAccountCenterTabsRight"
          link
          size="small"
          class="tab-scroll-nav tab-scroll-nav--right"
          aria-label="向右滚动个人中心标签"
          @click="scrollAccountCenterTabs('right')"
        />
      </div>
    </ElCard>
  </div>
</template>

<style scoped>
  .velox-account-center-page {
    width: 100%;
  }

  .velox-account-center-card :deep(.el-card__body) {
    overflow-y: auto;
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

  .velox-account-center-tabs :deep(.el-tabs__header) {
    margin-bottom: 16px;
  }

  .velox-account-center-tabs :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }
</style>

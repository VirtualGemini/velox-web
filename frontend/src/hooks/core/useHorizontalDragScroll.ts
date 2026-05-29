import { ref } from 'vue'

interface DragState {
  captureTarget?: HTMLElement
  scrollTarget?: HTMLElement
  pointerId?: number
  startX: number
  scrollLeft: number
  moved: boolean
  onScroll?: () => void
}

const DRAG_THRESHOLD = 4
const CLICK_SUPPRESS_MS = 240

export function useHorizontalDragScroll() {
  const isDragging = ref(false)
  const shouldSuppressClick = ref(false)
  const state: DragState = {
    startX: 0,
    scrollLeft: 0,
    moved: false
  }

  const resetState = () => {
    state.captureTarget = undefined
    state.scrollTarget = undefined
    state.pointerId = undefined
    state.startX = 0
    state.scrollLeft = 0
    state.moved = false
    state.onScroll = undefined
    isDragging.value = false
  }

  const startDrag = (
    event: PointerEvent,
    scrollTarget?: HTMLElement | null,
    onScroll?: () => void
  ) => {
    if (!scrollTarget) return
    if (event.pointerType === 'mouse' && event.button !== 0) return
    if (scrollTarget.scrollWidth <= scrollTarget.clientWidth + 1) return

    const captureTarget = event.currentTarget as HTMLElement | null
    state.captureTarget = captureTarget || scrollTarget
    state.scrollTarget = scrollTarget
    state.pointerId = event.pointerId
    state.startX = event.clientX
    state.scrollLeft = scrollTarget.scrollLeft
    state.moved = false
    state.onScroll = onScroll

    state.captureTarget?.setPointerCapture?.(event.pointerId)
  }

  const moveDrag = (event: PointerEvent) => {
    if (!state.scrollTarget || state.pointerId !== event.pointerId) return

    const deltaX = event.clientX - state.startX
    if (Math.abs(deltaX) > DRAG_THRESHOLD) {
      state.moved = true
      isDragging.value = true
    }

    if (!state.moved) return

    state.scrollTarget.scrollLeft = state.scrollLeft - deltaX
    state.onScroll?.()
    event.preventDefault()
  }

  const endDrag = (event: PointerEvent) => {
    if (state.pointerId !== event.pointerId) return

    state.captureTarget?.releasePointerCapture?.(event.pointerId)
    if (state.moved) {
      shouldSuppressClick.value = true
      window.setTimeout(() => {
        shouldSuppressClick.value = false
      }, CLICK_SUPPRESS_MS)
    }
    state.onScroll?.()
    resetState()
  }

  const preventClickAfterDrag = (event: MouseEvent) => {
    if (!shouldSuppressClick.value) return

    event.preventDefault()
    event.stopPropagation()
  }

  return {
    isDragging,
    startDrag,
    moveDrag,
    endDrag,
    preventClickAfterDrag
  }
}

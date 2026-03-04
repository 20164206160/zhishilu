<script setup lang="ts">
import { RouterView, useRouter } from 'vue-router'
import { ref, onMounted, onUnmounted } from 'vue'
import CanvasNest from 'canvas-nest.js'

const router = useRouter()
const isLoading = ref(false)
const cachedViews = ref(['HomeView', 'ArticleDetailView'])
const appContainer = ref<HTMLElement | null>(null)
let cn: any = null

// 页面切换动画处理
const onPageEnter = (el: Element) => {
  isLoading.value = false

  // 无过渡动画的页面（如首页）跳过 page-entering
  if (el instanceof HTMLElement) {
    const transitionName = el.closest('[data-v-app]')?.getAttribute('data-transition') ||
      router.currentRoute.value.meta?.transition as string
    if (transitionName === 'none') {
      return
    }
    el.style.willChange = 'transform, opacity'
    requestAnimationFrame(() => {
      el.classList.add('page-entering')
    })
  }
}

const onPageLeave = (el: Element) => {
  if (el instanceof HTMLElement) {
    el.classList.add('page-leaving')
  }
}

// 路由切换监听
let navigationTimeout: number

router.beforeEach((to, from, next) => {
  // 显示加载指示器
  if (from.name && to.name !== from.name) {
    isLoading.value = true

    // 设置超时，防止加载指示器一直显示
    navigationTimeout = setTimeout(() => {
      isLoading.value = false
    }, 5000) as unknown as number
  }
  next()
})

router.afterEach(() => {
  clearTimeout(navigationTimeout)
  // 延迟隐藏加载指示器，确保动画完成
  setTimeout(() => {
    isLoading.value = false
  }, 300)
})

// 初始化 canvas-nest 背景
onMounted(() => {
  if (appContainer.value) {
    cn = new CanvasNest(appContainer.value, {
      color: '21,93,252',    // 蓝色线条
      pointColor: '21,93,252', // 蓝色交点
      opacity: 1,            // 透明度
      count: 200,               // 线条数量
      zIndex: -1               // 层级，确保在内容后面
    })
  }
})

// 组件卸载时清理
onUnmounted(() => {
  clearTimeout(navigationTimeout)
  // 销毁 canvas-nest 背景
  if (cn) {
    cn.destroy()
  }
})
</script>

<template>
  <div ref="appContainer" class="app-container">
    <RouterView v-slot="{ Component, route }">
      <keep-alive :include="cachedViews">
        <Transition
          :name="(route.meta as any).transition || 'fade'"
          mode="out-in"
          @enter="onPageEnter"
          @leave="onPageLeave"
        >
          <component :is="Component" :key="route.path" />
        </Transition>
      </keep-alive>
    </RouterView>
  </div>

  <!-- 全局加载指示器 -->
  <Transition name="fade">
    <div v-if="isLoading" class="fixed inset-0 z-[9999] flex items-center justify-center bg-white/80 backdrop-blur-sm">
      <div class="loading-spinner">
        <div class="spinner-circle"></div>
      </div>
    </div>
  </Transition>
</template>

<style>
/* Global styles */
.app-container {
  min-height: 100vh;
  position: relative;
}
</style>

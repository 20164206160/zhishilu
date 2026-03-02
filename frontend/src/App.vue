<script setup lang="ts">
import { RouterView, useRouter } from 'vue-router'
import { ref, onMounted, onUnmounted } from 'vue'

const router = useRouter()
const isLoading = ref(false)
const cachedViews = ref(['HomeView', 'ArticleDetailView'])

// 页面切换动画处理
const onPageEnter = (el: Element) => {
  isLoading.value = false
  
  // 添加进入动画类
  if (el instanceof HTMLElement) {
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

// 组件卸载时清理
onUnmounted(() => {
  clearTimeout(navigationTimeout)
})
</script>

<template>
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
</style>

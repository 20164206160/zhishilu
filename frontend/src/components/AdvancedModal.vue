<template>
  <Teleport to="body">
    <Transition
      name="advanced-modal"
      appear
      @before-enter="onBeforeEnter"
      @after-enter="onAfterEnter"
      @before-leave="onBeforeLeave"
      @after-leave="onAfterLeave"
    >
      <div 
        v-if="show" 
        class="fixed inset-0 z-[1000] flex items-center justify-center p-4"
        :class="{ 'pointer-events-none': isAnimating }"
      >
        <!-- 增强的背景层 -->
        <div 
          class="absolute inset-0 transition-all duration-500"
          :class="backdropClasses"
          @click="handleBackdropClick"
          @touchstart="handleBackdropTouchStart"
          @touchmove="handleBackdropTouchMove"
          @touchend="handleBackdropTouchEnd"
        >
          <!-- 动态模糊背景 -->
          <div 
            class="absolute inset-0 backdrop-blur-md transition-all duration-300"
            :style="{ opacity: backdropOpacity }"
          ></div>
          <!-- 渐变遮罩 -->
          <div 
            class="absolute inset-0 bg-gradient-to-br from-black/60 via-black/40 to-black/20 transition-all duration-500"
            :style="{ opacity: backdropOpacity }"
          ></div>
        </div>
        
        <!-- 模态框容器 -->
        <div 
          class="relative z-10 w-full max-w-md"
          :class="containerClasses"
          @click.stop
        >
          <!-- 模态框内容 -->
          <div 
            ref="modalRef"
            class="bg-white dark:bg-gray-900 rounded-3xl shadow-2xl overflow-hidden transform transition-all duration-400"
            :class="modalClasses"
            :style="modalStyles"
          >
            <!-- 头部 -->
            <div v-if="showHeader" class="p-6 border-b border-gray-100 dark:border-gray-700">
              <div class="flex items-center justify-between">
                <h3 class="text-xl font-bold text-gray-900 dark:text-white transition-all duration-200">
                  {{ title }}
                </h3>
                <button
                  v-if="showCloseButton"
                  @click="handleClose"
                  class="w-8 h-8 rounded-full bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 text-gray-500 dark:text-gray-400 flex items-center justify-center transition-all duration-150 active:scale-95"
                >
                  <XIcon :size="18" />
                </button>
              </div>
              <p v-if="description" class="text-sm text-gray-500 dark:text-gray-400 mt-2 transition-all duration-200 delay-75">
                {{ description }}
              </p>
            </div>
            
            <!-- 内容区域 -->
            <div class="p-6 transition-all duration-200 delay-150">
              <slot></slot>
            </div>
            
            <!-- 底部操作 -->
            <div v-if="showFooter" class="p-6 border-t border-gray-100 dark:border-gray-700 bg-gray-50 dark:bg-gray-800">
              <div class="flex gap-3">
                <button
                  v-if="showCancelButton"
                  @click="handleCancel"
                  class="flex-1 px-4 py-2.5 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-600 transition-all duration-150 active:scale-95"
                >
                  {{ cancelText }}
                </button>
                <button
                  @click="handleConfirm"
                  :class="['flex-1 px-4 py-2.5 text-sm font-medium text-white rounded-xl transition-all duration-150 active:scale-95', confirmButtonClasses]"
                >
                  {{ confirmText }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { XIcon } from 'lucide-vue-next';
import { computed, ref, nextTick, onMounted, onUnmounted } from 'vue';

interface Props {
  show: boolean;
  title?: string;
  description?: string;
  type?: 'primary' | 'danger' | 'success' | 'warning';
  size?: 'sm' | 'md' | 'lg' | 'xl';
  showHeader?: boolean;
  showFooter?: boolean;
  showCloseButton?: boolean;
  showCancelButton?: boolean;
  closeOnBackdrop?: boolean;
  closeOnEscape?: boolean;
  preventScroll?: boolean;
  confirmText?: string;
  cancelText?: string;
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  size: 'md',
  showHeader: true,
  showFooter: true,
  showCloseButton: true,
  showCancelButton: true,
  closeOnBackdrop: true,
  closeOnEscape: true,
  preventScroll: true,
  confirmText: '确认',
  cancelText: '取消',
});

const emit = defineEmits(['update:show', 'confirm', 'cancel', 'close']);

const modalRef = ref<HTMLElement | null>(null);
const isAnimating = ref(false);
const backdropOpacity = ref(0);
const touchStartY = ref(0);
const currentDragY = ref(0);
const isDragging = ref(false);

// 计算属性
const containerClasses = computed(() => ({
  'max-w-sm': props.size === 'sm',
  'max-w-md': props.size === 'md',
  'max-w-lg': props.size === 'lg',
  'max-w-xl': props.size === 'xl',
}));

const backdropClasses = computed(() => ({
  'cursor-pointer': props.closeOnBackdrop && !isAnimating.value,
  'cursor-default': !props.closeOnBackdrop || isAnimating.value,
}));

const modalClasses = computed(() => ({
  'scale-100 opacity-100': props.show && !isDragging.value,
  'scale-95 opacity-0': !props.show,
  'translate-y-0': !isDragging.value,
}));

const modalStyles = computed(() => ({
  transform: isDragging.value ? `translateY(${currentDragY.value}px)` : 'translateY(0)',
}));

const confirmButtonClasses = computed(() => {
  const base = 'bg-blue-600 hover:bg-blue-700';
  const variants = {
    primary: 'bg-blue-600 hover:bg-blue-700',
    danger: 'bg-red-600 hover:bg-red-700',
    success: 'bg-green-600 hover:bg-green-700',
    warning: 'bg-orange-600 hover:bg-orange-700',
  };
  return variants[props.type] || base;
});

// 动画生命周期
const onBeforeEnter = async () => {
  isAnimating.value = true;
  backdropOpacity.value = 0;
  
  if (props.preventScroll) {
    document.body.style.overflow = 'hidden';
  }
  
  await nextTick();
  
  // 开始背景动画
  requestAnimationFrame(() => {
    backdropOpacity.value = 1;
  });
};

const onAfterEnter = () => {
  isAnimating.value = false;
};

const onBeforeLeave = () => {
  isAnimating.value = true;
  backdropOpacity.value = 0;
};

const onAfterLeave = () => {
  isAnimating.value = false;
  
  if (props.preventScroll) {
    document.body.style.overflow = '';
  }
};

// 事件处理
const handleConfirm = () => {
  if (isAnimating.value) return;
  emit('confirm');
  emit('update:show', false);
};

const handleCancel = () => {
  if (isAnimating.value) return;
  emit('cancel');
  emit('update:show', false);
};

const handleClose = () => {
  if (isAnimating.value) return;
  emit('close');
  emit('update:show', false);
};

const handleBackdropClick = () => {
  if (props.closeOnBackdrop && !isAnimating.value) {
    handleClose();
  }
};

// 触摸交互
const handleBackdropTouchStart = (e: TouchEvent) => {
  if (!props.closeOnBackdrop) return;
  
  touchStartY.value = e.touches[0].clientY;
  currentDragY.value = 0;
  isDragging.value = true;
};

const handleBackdropTouchMove = (e: TouchEvent) => {
  if (!isDragging.value) return;
  
  const currentY = e.touches[0].clientY;
  currentDragY.value = currentY - touchStartY.value;
  
  // 限制向下拖动
  if (currentDragY.value < 0) {
    currentDragY.value = 0;
  }
};

const handleBackdropTouchEnd = () => {
  if (!isDragging.value) return;
  
  const dragDistance = currentDragY.value;
  const shouldClose = dragDistance > 80; // 拖动超过80px关闭
  
  if (shouldClose) {
    handleClose();
  } else {
    // 回弹动画
    currentDragY.value = 0;
  }
  
  isDragging.value = false;
};

// 键盘事件
const handleKeydown = (e: KeyboardEvent) => {
  if (props.closeOnEscape && e.key === 'Escape' && props.show) {
    handleClose();
  }
};

// 生命周期
onMounted(() => {
  document.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown);
  
  if (props.preventScroll) {
    document.body.style.overflow = '';
  }
});
</script>

<style scoped>
/* 高级模态框动画 */
.advanced-modal-enter-active,
.advanced-modal-leave-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.advanced-modal-enter-from {
  opacity: 0;
}

.advanced-modal-enter-to {
  opacity: 1;
}

.advanced-modal-leave-from {
  opacity: 1;
}

.advanced-modal-leave-to {
  opacity: 0;
}

/* 模态框内容动画 */
.advanced-modal-enter-active .bg-white {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.1s;
}

.advanced-modal-leave-active .bg-white {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.advanced-modal-enter-from .bg-white {
  opacity: 0;
  transform: scale(0.8) translateY(30px);
}

.advanced-modal-enter-to .bg-white {
  opacity: 1;
  transform: scale(1) translateY(0);
}

.advanced-modal-leave-from .bg-white {
  opacity: 1;
  transform: scale(1) translateY(0);
}

.advanced-modal-leave-to .bg-white {
  opacity: 0;
  transform: scale(0.9) translateY(-30px);
}

/* 高性能优化 */
.bg-white {
  will-change: transform, opacity;
  backface-visibility: hidden;
  transform: translateZ(0);
}

/* 移动端优化 */
@media (max-width: 640px) {
  .advanced-modal-enter-active,
  .advanced-modal-leave-active {
    transition-duration: 0.4s;
  }
  
  .advanced-modal-enter-active .bg-white {
    transition-duration: 0.4s;
  }
  
  /* 移动端拖动优化 */
  .pointer-events-none {
    pointer-events: none;
  }
}

/* 暗色主题支持 */
@media (prefers-color-scheme: dark) {
  .bg-white {
    background-color: #1f2937;
  }
}

/* 防止点击穿透 */
.fixed.inset-0 {
  -webkit-tap-highlight-color: transparent;
}

/* 平滑滚动 */
body.modal-open {
  overflow: hidden;
}
</style>
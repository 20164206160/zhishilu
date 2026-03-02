<template>
  <Teleport to="body">
    <Transition
      name="modal"
      appear
      @before-enter="onBeforeEnter"
      @after-enter="onAfterEnter"
      @before-leave="onBeforeLeave"
      @after-leave="onAfterLeave"
    >
      <div v-if="show" class="fixed inset-0 z-[1000] flex items-center justify-center p-4">
        <!-- Backdrop with enhanced blur -->
        <div 
          class="absolute inset-0 bg-black/50 backdrop-blur-md transition-all duration-300"
          :class="{ 'opacity-100': show, 'opacity-0': !show }"
          @click="handleBackdropClick"
          @touchstart="handleTouchStart"
          @touchmove="handleTouchMove"
          @touchend="handleTouchEnd"
        ></div>
        
        <!-- Modal Container -->
        <div 
          class="relative z-10 w-full max-w-sm"
          @click.stop
        >
          <!-- Modal Content with enhanced animations -->
          <div 
            class="bg-white rounded-3xl shadow-2xl overflow-hidden transform transition-all duration-300"
            :class="modalClasses"
            ref="modalRef"
          >
            <div class="p-8 text-center">
              <div 
                :class="['mx-auto w-16 h-16 rounded-full flex items-center justify-center mb-4 transition-all duration-300', 
                         type === 'danger' ? 'bg-red-50 text-red-500' : 'bg-blue-50 text-blue-500']"
                :style="{ transform: iconScale }"
              >
                <component :is="icon" :size="32" />
              </div>
              <h3 class="text-xl font-bold text-gray-900 mb-2 transition-all duration-200">{{ title }}</h3>
              <p class="text-sm text-gray-500 leading-relaxed transition-all duration-200 delay-75">{{ message }}</p>
            </div>
            
            <div class="flex border-t border-gray-100">
              <button 
                @click="handleCancel"
                @touchstart="handleButtonTouchStart"
                @touchend="handleButtonTouchEnd"
                class="flex-1 py-4 text-sm font-bold text-gray-400 hover:bg-gray-50 active:bg-gray-100 transition-all duration-150 border-r border-gray-100 active:scale-95"
              >
                取消
              </button>
              <button 
                @click="handleConfirm"
                @touchstart="handleButtonTouchStart"
                @touchend="handleButtonTouchEnd"
                :class="['flex-1 py-4 text-sm font-bold transition-all duration-150 active:scale-95', 
                         type === 'danger' ? 'text-red-500 hover:bg-red-50 active:bg-red-100' : 'text-blue-600 hover:bg-blue-50 active:bg-blue-100']"
              >
                确认
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { AlertCircle, HelpCircle } from 'lucide-vue-next';
import { computed, ref, nextTick, onMounted, onUnmounted } from 'vue';

const props = defineProps<{
  show: boolean;
  title: string;
  message: string;
  type?: 'primary' | 'danger';
  closeOnBackdrop?: boolean;
  closeOnEscape?: boolean;
}>();

const emit = defineEmits(['confirm', 'cancel', 'close']);

const modalRef = ref<HTMLElement | null>(null);
const isAnimating = ref(false);
const touchStartY = ref(0);
const currentDragY = ref(0);
const isDragging = ref(false);

const icon = computed(() => props.type === 'danger' ? AlertCircle : HelpCircle);

// 模态框样式类
const modalClasses = computed(() => ({
  'scale-100 opacity-100': props.show && !isDragging.value,
  'scale-95 opacity-0': !props.show,
  'translate-y-0': !isDragging.value,
  'cursor-grabbing': isDragging.value,
}));

// 图标缩放效果
const iconScale = computed(() => {
  if (isDragging.value) {
    const dragDistance = Math.abs(currentDragY.value);
    const scale = Math.max(0.8, 1 - dragDistance / 200);
    return `scale(${scale})`;
  }
  return 'scale(1)';
});

// 动画生命周期
const onBeforeEnter = (el: Element) => {
  isAnimating.value = true;
  if (el instanceof HTMLElement) {
    el.style.willChange = 'transform, opacity';
  }
};

const onAfterEnter = () => {
  isAnimating.value = false;
};

const onBeforeLeave = (el: Element) => {
  isAnimating.value = true;
};

const onAfterLeave = () => {
  isAnimating.value = false;
};

// 事件处理
const handleConfirm = () => {
  if (isAnimating.value) return;
  emit('confirm');
};

const handleCancel = () => {
  if (isAnimating.value) return;
  emit('cancel');
};

const handleBackdropClick = () => {
  if (props.closeOnBackdrop !== false && !isAnimating.value) {
    handleCancel();
  }
};

// 触摸交互
const handleTouchStart = (e: TouchEvent) => {
  touchStartY.value = e.touches[0].clientY;
  currentDragY.value = 0;
  isDragging.value = true;
};

const handleTouchMove = (e: TouchEvent) => {
  if (!isDragging.value) return;
  
  const currentY = e.touches[0].clientY;
  currentDragY.value = currentY - touchStartY.value;
  
  // 限制向下拖动
  if (currentDragY.value < 0) {
    currentDragY.value = 0;
  }
  
  // 更新模态框位置
  if (modalRef.value) {
    modalRef.value.style.transform = `translateY(${currentDragY.value}px)`;
  }
};

const handleTouchEnd = () => {
  if (!isDragging.value) return;
  
  const dragDistance = currentDragY.value;
  const dragVelocity = dragDistance > 50; // 如果拖动超过50px就关闭
  
  if (dragVelocity) {
    handleCancel();
  } else {
    // 回弹动画
    if (modalRef.value) {
      modalRef.value.style.transition = 'transform 0.3s cubic-bezier(0.4, 0, 0.2, 1)';
      modalRef.value.style.transform = 'translateY(0)';
      
      setTimeout(() => {
        if (modalRef.value) {
          modalRef.value.style.transition = '';
        }
      }, 300);
    }
  }
  
  isDragging.value = false;
  currentDragY.value = 0;
};

// 按钮触摸反馈
const handleButtonTouchStart = (e: Event) => {
  const target = e.currentTarget as HTMLElement;
  target.style.transform = 'scale(0.95)';
};

const handleButtonTouchEnd = (e: Event) => {
  const target = e.currentTarget as HTMLElement;
  target.style.transform = 'scale(1)';
};

// 键盘事件
const handleKeydown = (e: KeyboardEvent) => {
  if (props.closeOnEscape !== false && e.key === 'Escape' && props.show) {
    handleCancel();
  }
};

// 生命周期
onMounted(() => {
  document.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown);
});


</script>

<style scoped>
/* 模态框动画 */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-enter-from {
  opacity: 0;
}

.modal-enter-to {
  opacity: 1;
}

.modal-leave-from {
  opacity: 1;
}

.modal-leave-to {
  opacity: 0;
}

/* 模态框内容动画 */
.modal-enter-active .bg-white {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.1s;
}

.modal-leave-active .bg-white {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-enter-from .bg-white {
  opacity: 0;
  transform: scale(0.8) translateY(20px);
}

.modal-enter-to .bg-white {
  opacity: 1;
  transform: scale(1) translateY(0);
}

.modal-leave-from .bg-white {
  opacity: 1;
  transform: scale(1) translateY(0);
}

.modal-leave-to .bg-white {
  opacity: 0;
  transform: scale(0.9) translateY(-20px);
}

/* 背景模糊动画 */
.bg-black\/50 {
  transition: all 0.4s ease;
}

/* 移动端优化 */
@media (max-width: 640px) {
  .modal-enter-active,
  .modal-leave-active {
    transition-duration: 0.3s;
  }
  
  .modal-enter-active .bg-white {
    transition-duration: 0.3s;
  }
  
  /* 移动端拖动优化 */
  .cursor-grabbing {
    -webkit-user-select: none;
    user-select: none;
    -webkit-touch-callout: none;
  }
}

/* 高性能动画优化 */
.bg-white {
  will-change: transform, opacity;
  backface-visibility: hidden;
  transform: translateZ(0);
}

/* 按钮交互优化 */
button:active {
  transform: scale(0.95);
  transition: transform 0.1s ease;
}

/* 防止点击穿透 */
.fixed.inset-0 {
  -webkit-tap-highlight-color: transparent;
}
</style>

<template>
  <Transition name="fade">
    <div v-if="show" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="$emit('cancel')"></div>
      
      <!-- Modal -->
      <div class="bg-white rounded-3xl shadow-2xl w-full max-w-sm overflow-hidden z-10 transform transition-all duration-300 scale-100">
        <div class="p-8 text-center">
          <div :class="['mx-auto w-16 h-16 rounded-full flex items-center justify-center mb-4', type === 'danger' ? 'bg-red-50 text-red-500' : 'bg-blue-50 text-blue-500']">
            <component :is="icon" :size="32" />
          </div>
          <h3 class="text-xl font-bold text-gray-900 mb-2">{{ title }}</h3>
          <p class="text-sm text-gray-500 leading-relaxed">{{ message }}</p>
        </div>
        
        <div class="flex border-t border-gray-100">
          <button 
            @click="$emit('cancel')"
            class="flex-1 py-4 text-sm font-bold text-gray-400 hover:bg-gray-50 transition-colors border-r border-gray-100"
          >
            取消
          </button>
          <button 
            @click="$emit('confirm')"
            :class="['flex-1 py-4 text-sm font-bold transition-colors', type === 'danger' ? 'text-red-500 hover:bg-red-50' : 'text-blue-600 hover:bg-blue-50']"
          >
            确认
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { AlertCircle, HelpCircle } from 'lucide-vue-next';
import { computed } from 'vue';

const props = defineProps<{
  show: boolean;
  title: string;
  message: string;
  type?: 'primary' | 'danger';
}>();

defineEmits(['confirm', 'cancel']);

const icon = computed(() => props.type === 'danger' ? AlertCircle : HelpCircle);
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>

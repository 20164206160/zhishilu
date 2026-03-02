<template>
  <div class="h-full flex flex-col overflow-hidden">
    <!-- Header -->
    <div class="px-4 sm:px-6 py-3 sm:py-4 border-b border-gray-100 flex items-center justify-between shrink-0">
      <button @click="onBack" class="flex items-center gap-1 sm:gap-2 text-gray-500 hover:text-blue-600 transition-colors">
        <ChevronLeft :size="18" class="sm:w-5 sm:h-5" />
        <span class="font-bold text-sm sm:text-base">返回列表</span>
      </button>
      <div class="flex items-center gap-2">
        <button
          @click="onEdit"
          class="px-3 sm:px-4 py-1.5 sm:py-2 bg-gray-50 hover:bg-blue-50 text-gray-600 hover:text-blue-600 rounded-lg sm:rounded-xl text-xs sm:text-sm font-bold transition-all flex items-center gap-1"
        >
          <Edit2 :size="14" class="sm:w-4 sm:h-4" /> <span>编辑</span>
        </button>
        <button
          @click="onDelete"
          class="px-3 sm:px-4 py-1.5 sm:py-2 bg-gray-50 hover:bg-red-50 text-gray-600 hover:text-red-500 rounded-lg sm:rounded-xl text-xs sm:text-sm font-bold transition-all flex items-center gap-1"
        >
          <Trash2 :size="14" class="sm:w-4 sm:h-4" /> <span>删除</span>
        </button>
      </div>
    </div>

    <!-- Content -->
    <div class="flex-grow overflow-y-auto">
      <!-- Image Gallery -->
      <div v-if="article.images?.length" class="relative bg-black">
        <div class="flex h-48 sm:h-64 md:h-80 w-full transition-transform duration-300 ease-out" 
             :style="{ transform: `translateX(-${currentImgIndex * 100}%)` }">
          <img v-for="(img, i) in article.images" :key="i" 
               :src="getImageUrl(img)" 
               class="w-full h-full object-contain flex-shrink-0 cursor-pointer"
               @click="openPreview(i)" />
        </div>
        <!-- Image Counter -->
        <div v-if="article.images.length > 1" class="absolute top-2 left-1/2 -translate-x-1/2 z-10 px-3 py-1 bg-black/40 rounded-full text-white text-xs font-medium">
          {{ currentImgIndex + 1 }} / {{ article.images.length }}
        </div>
        <!-- Navigation Arrows -->
        <template v-if="article.images.length > 1">
          <button @click="prevImage" 
                  class="absolute left-2 top-1/2 -translate-y-1/2 w-8 h-8 rounded-full bg-black/40 hover:bg-black/60 text-white flex items-center justify-center transition-all">
            <ChevronLeft :size="20" />
          </button>
          <button @click="nextImage" 
                  class="absolute right-2 top-1/2 -translate-y-1/2 w-8 h-8 rounded-full bg-black/40 hover:bg-black/60 text-white flex items-center justify-center transition-all">
            <ChevronRight :size="20" />
          </button>
        </template>
        <!-- Dot Indicators -->
        <div v-if="article.images.length > 1" class="absolute bottom-2 inset-x-0 flex justify-center gap-1.5 z-10">
          <button v-for="(_, i) in article.images" :key="i" 
                  @click="currentImgIndex = i"
                  class="h-1.5 rounded-full transition-all" 
                  :class="[currentImgIndex === i ? 'w-4 bg-white' : 'w-1.5 bg-white/40']"></button>
        </div>
      </div>

      <!-- Article Info -->
      <div class="p-4 sm:p-6 space-y-4">
        <!-- Title -->
        <h1 class="text-lg sm:text-2xl font-black text-gray-900">{{ article.title }}</h1>

        <!-- Meta -->
        <div class="flex flex-wrap items-center gap-2 text-xs sm:text-sm text-gray-400">
          <span>{{ formatDate(article.createdTime) }} 发布</span>
          <span v-if="article.location" class="flex items-center gap-1 text-blue-500">
            <MapPin :size="12" /> {{ article.location }}
          </span>
        </div>

        <!-- Categories -->
        <div v-if="article.categories?.length" class="flex flex-wrap gap-2">
          <span v-for="(cat, idx) in article.categories" :key="idx"
                class="text-xs font-bold text-blue-500 bg-blue-50 px-2 py-1 rounded-md uppercase tracking-wider">
            #{{ cat }}
          </span>
        </div>

        <!-- Content -->
        <div v-if="article.content" class="prose prose-sm max-w-none text-gray-700" v-html="article.content"></div>

        <!-- Source URL -->
        <div v-if="article.url" class="pt-4 border-t border-gray-100">
          <a :href="article.url" target="_blank" 
             class="text-blue-600 hover:text-blue-700 text-xs sm:text-sm flex items-center gap-1 break-all">
            <LinkIcon :size="12" /> {{ article.url }}
          </a>
        </div>
      </div>
    </div>

    <!-- Image Preview Modal -->
    <teleport to="body">
      <transition name="fade">
        <div v-if="showPreview" @click="closePreview" 
             class="fixed inset-0 bg-black z-[110] flex items-center justify-center">
          <button @click="closePreview" 
                  class="absolute top-4 right-4 z-50 w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center">
            <XIcon :size="22" />
          </button>
          <div class="relative w-screen h-screen overflow-hidden" @click.stop>
            <div class="flex h-full transition-transform duration-300 ease-out" 
                 :style="{ transform: `translateX(-${previewImgIndex * 100}vw)` }">
              <div v-for="(img, i) in article.images" :key="i" 
                   class="w-screen h-full flex-shrink-0 flex items-center justify-center p-4">
                <img :src="getImageUrl(img)" class="max-w-full max-h-full object-contain" />
              </div>
            </div>
            <template v-if="article.images && article.images.length > 1">
              <button @click.stop="previewImgIndex = (previewImgIndex - 1 + article.images.length) % article.images.length" 
                      class="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center">
                <ChevronLeft :size="28" />
              </button>
              <button @click.stop="previewImgIndex = (previewImgIndex + 1) % article.images.length" 
                      class="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center">
                <ChevronRight :size="28" />
              </button>
            </template>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ChevronLeft, ChevronRight, Edit2, Trash2, MapPin, Link as LinkIcon, X as XIcon } from 'lucide-vue-next';
import { getImageUrl } from '@/utils/image';

interface Article {
  id: string;
  title: string;
  content?: string;
  images?: string[];
  categories?: string[];
  location?: string;
  url?: string;
  createdTime?: string;
}

const props = defineProps<{
  article: Article;
}>();

const emit = defineEmits<{
  back: [];
  edit: [article: Article];
  delete: [article: Article];
}>();

const currentImgIndex = ref(0);
const showPreview = ref(false);
const previewImgIndex = ref(0);

const onBack = () => emit('back');
const onEdit = () => emit('edit', props.article);
const onDelete = () => emit('delete', props.article);

const nextImage = () => {
  if (props.article.images?.length) {
    currentImgIndex.value = (currentImgIndex.value + 1) % props.article.images.length;
  }
};

const prevImage = () => {
  if (props.article.images?.length) {
    currentImgIndex.value = (currentImgIndex.value - 1 + props.article.images.length) % props.article.images.length;
  }
};

const openPreview = (index: number) => {
  previewImgIndex.value = index;
  showPreview.value = true;
};

const closePreview = () => {
  showPreview.value = false;
};

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString();
};
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>

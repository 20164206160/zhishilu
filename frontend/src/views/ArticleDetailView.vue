<template>
  <div class="min-h-screen bg-white flex flex-col font-sans">
    <!-- Header Overlay -->
    <header class="fixed top-0 inset-x-0 z-50 h-16 flex items-center justify-between px-4 transition-all" :class="[scrolled ? 'bg-white/80 backdrop-blur-md border-b border-gray-100 shadow-sm' : 'bg-transparent']">
      <button @click="router.back()" class="w-10 h-10 rounded-full flex items-center justify-center transition-all" :class="[scrolled ? 'bg-gray-100 text-gray-900' : 'bg-black/20 text-white backdrop-blur-sm']">
        <ChevronLeft :size="24" />
      </button>
      <div class="flex items-center gap-3">
        <button class="w-10 h-10 rounded-full flex items-center justify-center transition-all" :class="[scrolled ? 'bg-gray-100 text-gray-900' : 'bg-black/20 text-white backdrop-blur-sm']">
          <Share2 :size="20" />
        </button>
      </div>
    </header>

    <main v-if="article" class="flex-grow flex flex-col">
      <!-- Image Gallery -->
      <section v-if="article.images?.length" class="w-full relative bg-gray-50 aspect-[4/5] sm:aspect-video overflow-hidden">
        <div class="flex h-full transition-transform duration-500" :style="{ transform: `translateX(-${currentImgIndex * 100}%)` }">
          <img v-for="(img, i) in article.images" :key="i" :src="getImageUrl(img)" class="w-full h-full object-cover" />
        </div>
        <!-- Indicators -->
        <div v-if="article.images.length > 1" class="absolute bottom-6 inset-x-0 flex justify-center gap-1.5">
          <div v-for="(_, i) in article.images" :key="i" class="h-1.5 rounded-full transition-all" :class="[currentImgIndex === i ? 'w-6 bg-white' : 'w-1.5 bg-white/50']"></div>
        </div>
        <!-- Navigation Buttons -->
        <template v-if="article.images.length > 1">
          <button @click="currentImgIndex = (currentImgIndex - 1 + article.images.length) % article.images.length" class="absolute left-4 top-1/2 -translate-y-1/2 w-8 h-8 rounded-full bg-black/10 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
            <ChevronLeft :size="20" />
          </button>
          <button @click="currentImgIndex = (currentImgIndex + 1) % article.images.length" class="absolute right-4 top-1/2 -translate-y-1/2 w-8 h-8 rounded-full bg-black/10 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
            <ChevronRight :size="20" />
          </button>
        </template>
      </section>

      <!-- Content Area -->
      <article class="max-w-2xl mx-auto w-full px-6 py-10 space-y-8">
        <!-- Meta Information -->
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold">
              {{ article.username?.charAt(0) || 'U' }}
            </div>
            <div>
              <p class="text-sm font-bold text-gray-900">{{ article.username }}</p>
              <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">{{ formatDate(article.createdTime) }}</p>
            </div>
          </div>
          <div class="flex items-center gap-1.5 text-blue-600 bg-blue-50 px-3 py-1 rounded-full">
            <Tag :size="12" />
            <span class="text-xs font-bold">{{ article.category }}</span>
          </div>
        </div>

        <!-- Title & Content -->
        <div class="space-y-4">
          <h1 class="text-3xl font-black text-gray-900 leading-tight">{{ article.title }}</h1>
          <div v-if="article.content" class="text-lg text-gray-700 leading-relaxed whitespace-pre-wrap">
            {{ article.content }}
          </div>
        </div>

        <!-- Extra Info -->
        <div class="space-y-4 pt-8 border-t border-gray-100">
          <div v-if="article.location" class="flex items-center gap-2 text-gray-500">
            <MapPin :size="16" class="text-blue-500" />
            <span class="text-sm font-medium">{{ article.location }}</span>
          </div>
          <div v-if="article.url" class="flex items-center gap-2">
            <LinkIcon :size="16" class="text-gray-400" />
            <a :href="article.url" target="_blank" class="text-sm font-bold text-blue-600 hover:underline line-clamp-1">
              查看原文来源
            </a>
          </div>
        </div>
      </article>
    </main>

    <!-- Loading State -->
    <div v-else class="flex-grow flex items-center justify-center">
      <div class="animate-spin rounded-full h-8 w-8 border-4 border-blue-600 border-t-transparent"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { 
  ChevronLeft, ChevronRight, Share2, Tag, MapPin, Link as LinkIcon 
} from 'lucide-vue-next';
import request from '../utils/request';
import { getImageUrl } from '../utils/image';

const route = useRoute();
const router = useRouter();
const article = ref<any>(null);
const scrolled = ref(false);
const currentImgIndex = ref(0);

const handleScroll = () => {
  scrolled.value = window.scrollY > 50;
};

const loadDetail = async () => {
  try {
    const res = await request.get(`/article/${route.params.id}`);
    if (res.data.code === 200) {
      article.value = res.data.data;
    }
  } catch (err) {
    console.error('Load detail error:', err);
  }
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' 
  });
};

onMounted(() => {
  window.addEventListener('scroll', handleScroll);
  loadDetail();
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>

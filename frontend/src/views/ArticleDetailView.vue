<template>
  <div class="min-h-screen bg-gray-50 flex flex-col font-sans">
    <!-- Header Overlay (Mobile Only) -->
    <header class="md:hidden fixed top-0 inset-x-0 z-50 h-16 flex items-center justify-between px-4 transition-all" :class="[scrolled ? 'bg-white/80 backdrop-blur-md border-b border-gray-100 shadow-sm' : 'bg-transparent']">
      <button @click="router.back()" class="w-10 h-10 rounded-full flex items-center justify-center transition-all" :class="[scrolled ? 'bg-gray-100 text-gray-900' : 'bg-black/20 text-white backdrop-blur-sm']">
        <ChevronLeft :size="24" />
      </button>
      <div class="flex items-center gap-3">
        <button class="w-10 h-10 rounded-full flex items-center justify-center transition-all" :class="[scrolled ? 'bg-gray-100 text-gray-900' : 'bg-black/20 text-white backdrop-blur-sm']">
          <Share2 :size="20" />
        </button>
      </div>
    </header>

    <main v-if="article" class="flex-grow flex flex-col md:flex-row md:max-w-7xl md:mx-auto md:w-full md:bg-white md:shadow-2xl md:my-8 md:rounded-2xl overflow-hidden md:h-[calc(100vh-64px)]">
      <!-- Image Gallery (Left Side on Desktop) -->
      <section v-if="article.images?.length" class="w-full md:flex-grow relative bg-black flex items-center justify-center overflow-hidden group">
        <!-- Desktop Back Button -->
        <button @click="router.back()" class="hidden md:flex absolute top-6 left-6 z-20 w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 text-white backdrop-blur-md items-center justify-center transition-all">
          <ChevronLeft :size="24" />
        </button>

        <div class="flex h-full w-full transition-transform duration-500" :style="{ transform: `translateX(-${currentImgIndex * 100}%)` }">
          <img v-for="(img, i) in article.images" :key="i" :src="getImageUrl(img)" class="w-full h-full object-contain" />
        </div>
        
        <!-- Indicators -->
        <div v-if="article.images.length > 1" class="absolute bottom-6 inset-x-0 flex justify-center gap-1.5 z-10">
          <div v-for="(_, i) in article.images" :key="i" class="h-1.5 rounded-full transition-all" :class="[currentImgIndex === i ? 'w-6 bg-white' : 'w-1.5 bg-white/50']"></div>
        </div>

        <!-- Navigation Buttons -->
        <template v-if="article.images.length > 1">
          <button @click="currentImgIndex = (currentImgIndex - 1 + article.images.length) % article.images.length" class="absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-black/20 hover:bg-black/40 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity backdrop-blur-sm">
            <ChevronLeft :size="24" />
          </button>
          <button @click="currentImgIndex = (currentImgIndex + 1) % article.images.length" class="absolute right-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-black/20 hover:bg-black/40 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity backdrop-blur-sm">
            <ChevronRight :size="24" />
          </button>
        </template>
      </section>

      <!-- Content Area (Right Side Sidebar on Desktop) -->
      <aside class="w-full md:w-[420px] lg:w-[480px] flex flex-col bg-white overflow-hidden">
        <!-- User Header (Sticky) -->
        <div class="px-4 py-4 border-b border-gray-100 flex items-center justify-between bg-white shrink-0">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full bg-gradient-to-tr from-blue-500 to-blue-400 flex items-center justify-center text-white font-bold text-lg shadow-sm">
              {{ article.createdBy?.charAt(0) || 'U' }}
            </div>
            <div>
              <p class="text-[15px] font-bold text-gray-900 leading-none">{{ article.createdBy }}</p>
              <p class="text-[11px] text-gray-400 mt-1 uppercase tracking-wider">{{ formatDate(article.createdTime) }}</p>
            </div>
          </div>
          <button class="px-5 py-1.5 bg-red-500 hover:bg-red-600 text-white text-sm font-bold rounded-full transition-colors shadow-sm">
            关注
          </button>
        </div>

        <!-- Scrollable Content -->
        <div class="flex-grow overflow-y-auto px-6 py-6 space-y-6">
          <div class="space-y-4">
            <h1 class="text-xl font-black text-gray-900 leading-tight">{{ article.title }}</h1>
            <div v-if="article.content" class="text-[15px] text-gray-800 leading-relaxed whitespace-pre-wrap">
              {{ article.content }}
            </div>
          </div>

          <!-- Tags & Location -->
          <div class="space-y-4 pt-4">
            <div class="flex flex-wrap gap-2">
              <div class="flex items-center gap-1.5 text-blue-600 bg-blue-50 px-3 py-1 rounded-full border border-blue-100">
                <Tag :size="12" />
                <span class="text-xs font-bold">{{ article.category }}</span>
              </div>
            </div>
            
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
        </div>

        <!-- Interaction Footer (Sticky) -->
        <div class="px-6 py-4 border-t border-gray-50 bg-white shrink-0">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-6">
              <button class="flex items-center gap-1.5 text-gray-600 hover:text-red-500 transition-colors">
                <Heart :size="22" />
                <span class="text-xs font-bold">3.5万</span>
              </button>
              <button class="flex items-center gap-1.5 text-gray-600 hover:text-yellow-500 transition-colors">
                <Star :size="22" />
                <span class="text-xs font-bold">3720</span>
              </button>
              <button class="flex items-center gap-1.5 text-gray-600 hover:text-blue-500 transition-colors">
                <MessageCircle :size="22" />
                <span class="text-xs font-bold">3933</span>
              </button>
            </div>
            <button class="text-gray-600 hover:text-gray-900 transition-colors">
              <Share2 :size="22" />
            </button>
          </div>
          
          <!-- Comment Input Placeholder -->
          <div class="relative">
            <input 
              type="text" 
              placeholder="说点什么..." 
              class="w-full bg-gray-100 border-none rounded-full py-2.5 px-5 text-sm focus:ring-2 focus:ring-blue-100 transition-all"
            />
          </div>
        </div>
      </aside>
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
  ChevronLeft, ChevronRight, Share2, Tag, MapPin, 
  Link as LinkIcon, Heart, Star, MessageCircle 
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

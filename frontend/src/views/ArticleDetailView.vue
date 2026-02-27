<template>
  <div class="min-h-screen bg-black md:bg-[#f5f5f5] flex flex-col font-sans">
    <!-- Mobile Header -->
    <header class="md:hidden fixed top-0 inset-x-0 z-50 h-14 flex items-center justify-between px-4 bg-black/50 backdrop-blur-sm">
      <button @click="handleBack" class="w-9 h-9 rounded-full bg-black/30 text-white flex items-center justify-center">
        <ChevronLeft :size="22" />
      </button>
      <div class="flex items-center gap-2">
        <button class="w-9 h-9 rounded-full bg-black/30 text-white flex items-center justify-center">
          <Share2 :size="18" />
        </button>
      </div>
    </header>

    <main v-if="article" class="flex-grow flex flex-col md:flex-row md:max-w-[1100px] md:mx-auto md:w-full md:bg-white md:shadow-2xl md:my-4 md:rounded-xl overflow-hidden md:h-[calc(100vh-32px)]">
      <!-- Left: Image Gallery -->
      <section 
        v-if="article.images?.length" 
        class="gallery-section w-full h-[100vw] md:h-auto relative bg-black flex items-center justify-center overflow-hidden group"
        @touchstart="handleTouchStart"
        @touchmove="handleTouchMove"
        @touchend="handleTouchEnd"
      >
        <!-- Desktop Close Button -->
        <button @click="handleBack" class="hidden md:flex absolute top-4 right-4 z-20 w-10 h-10 rounded-full bg-black/40 hover:bg-black/60 text-white items-center justify-center transition-all backdrop-blur-sm">
          <XIcon :size="20" />
        </button>

        <!-- Image Counter -->
        <div v-if="article.images.length > 1" class="absolute top-4 left-1/2 -translate-x-1/2 z-10 px-3 py-1 bg-black/40 rounded-full text-white text-xs font-medium backdrop-blur-sm">
          {{ currentImgIndex + 1 }} / {{ article.images.length }}
        </div>

        <div class="flex h-full w-full transition-transform duration-300 ease-out" :style="{ transform: `translateX(-${currentImgIndex * 100}%)` }">
          <img v-for="(img, i) in article.images" :key="i" :src="getImageUrl(img)" @click="openImagePreview(i)" class="w-full h-full object-contain flex-shrink-0 cursor-zoom-in" />
        </div>
        
        <!-- Dot Indicators -->
        <div v-if="article.images.length > 1" class="absolute bottom-4 inset-x-0 flex justify-center gap-2 z-10">
          <button 
            v-for="(_, i) in article.images" 
            :key="i" 
            @click="currentImgIndex = i"
            class="h-2 rounded-full transition-all duration-300" 
            :class="[currentImgIndex === i ? 'w-5 bg-white' : 'w-2 bg-white/40 hover:bg-white/60']"
          ></button>
        </div>

        <!-- Navigation Arrows -->
        <template v-if="article.images.length > 1">
          <button 
            @click="prevImage" 
            class="absolute left-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all backdrop-blur-sm"
          >
            <ChevronLeft :size="24" />
          </button>
          <button 
            @click="nextImage" 
            class="absolute right-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all backdrop-blur-sm"
          >
            <ChevronRight :size="24" />
          </button>
        </template>
      </section>

      <!-- Right: Content Area -->
      <aside class="w-full md:w-[420px] lg:w-[460px] flex flex-col bg-white overflow-hidden">
        <!-- Author Header (Sticky) -->
        <div class="px-5 py-4 border-b border-[#f0f0f0] flex items-center justify-between bg-white shrink-0">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full bg-gradient-to-tr from-blue-500 to-blue-400 flex items-center justify-center text-white font-bold text-base shadow-sm overflow-hidden cursor-pointer hover:opacity-90 transition-opacity">
              <img v-if="article.creatorAvatar" :src="getAvatarUrl(article.creatorAvatar)" class="w-full h-full object-cover" alt="avatar" />
              <template v-else>{{ article.createdBy?.charAt(0) || 'U' }}</template>
            </div>
            <div>
              <p class="text-[15px] font-semibold text-[#333] leading-tight cursor-pointer hover:text-blue-600 transition-colors">{{ article.createdBy }}</p>
              <p class="text-[12px] text-[#999] mt-0.5">{{ formatDate(article.createdTime) }}</p>
            </div>
          </div>
          <!-- Author Actions -->
          <div v-if="isAuthor" class="flex items-center gap-2">
            <button 
              @click="handleEdit"
              class="w-9 h-9 rounded-full bg-[#f5f5f5] hover:bg-[#e8e8e8] text-[#666] flex items-center justify-center transition-colors"
              title="编辑"
            >
              <Edit3 :size="17" />
            </button>
            <button 
              @click="handleDelete"
              class="w-9 h-9 rounded-full bg-[#f5f5f5] hover:bg-red-50 text-[#666] hover:text-red-500 flex items-center justify-center transition-colors"
              title="删除"
            >
              <Trash2 :size="17" />
            </button>
          </div>
          <button v-else class="px-5 py-1.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-full transition-colors">
            关注
          </button>
        </div>

        <!-- Scrollable Content -->
        <div class="flex-grow overflow-y-auto">
          <!-- Article Content -->
          <div class="px-5 py-5">
            <h1 class="text-lg font-bold text-[#333] leading-snug mb-4">{{ article.title }}</h1>
            <div v-if="article.content" class="text-[15px] text-[#333] leading-[1.8] whitespace-pre-wrap">
              {{ article.content }}
            </div>

            <!-- Tags -->
            <div v-if="article.categories?.length" class="flex flex-wrap gap-2 mt-5">
              <span 
                v-for="(cat, index) in article.categories" 
                :key="index"
                class="text-[#576b95] text-[14px] hover:underline cursor-pointer"
              >
                #{{ cat }}
              </span>
            </div>

            <!-- Location -->
            <div v-if="article.location" class="flex items-center gap-1.5 mt-4 text-[#576b95]">
              <MapPin :size="14" />
              <span class="text-[13px]">{{ article.location }}</span>
            </div>

            <!-- Publish Time -->
            <div class="mt-4 text-[12px] text-[#999]">
              {{ formatFullDate(article.createdTime) }}
            </div>
          </div>

          <!-- Comments Section -->
          <div class="border-t border-[#f0f0f0]">
            <div class="px-5 py-3 flex items-center justify-between">
              <span class="text-[14px] font-medium text-[#333]">共 {{ mockComments.length }} 条评论</span>
              <div class="flex items-center gap-3 text-[13px] text-[#666]">
                <button class="hover:text-blue-600 transition-colors font-medium">最热</button>
                <span class="text-[#e0e0e0]">|</span>
                <button class="hover:text-blue-600 transition-colors">最新</button>
              </div>
            </div>

            <!-- Comment List -->
            <div class="px-5 pb-4 space-y-4">
              <div v-for="(comment, idx) in mockComments" :key="idx" class="flex gap-3">
                <div class="w-8 h-8 rounded-full bg-gradient-to-tr from-gray-400 to-gray-300 flex items-center justify-center text-white text-xs font-bold flex-shrink-0 overflow-hidden">
                  {{ comment.user.charAt(0) }}
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2">
                    <span class="text-[13px] text-[#576b95] font-medium">{{ comment.user }}</span>
                    <span v-if="comment.isAuthor" class="px-1.5 py-0.5 bg-blue-600 text-white text-[10px] rounded">作者</span>
                  </div>
                  <p class="text-[14px] text-[#333] mt-1 leading-relaxed">{{ comment.content }}</p>
                  <div class="flex items-center gap-4 mt-2 text-[12px] text-[#999]">
                    <span>{{ comment.time }}</span>
                    <button class="hover:text-[#666] transition-colors">回复</button>
                  </div>
                  <!-- Replies -->
                  <div v-if="comment.replies?.length" class="mt-3 bg-[#f8f8f8] rounded-lg p-3 space-y-2">
                    <div v-for="(reply, rIdx) in comment.replies" :key="rIdx" class="text-[13px]">
                      <span class="text-[#576b95] font-medium">{{ reply.user }}</span>
                      <span v-if="reply.to" class="text-[#999]"> 回复 </span>
                      <span v-if="reply.to" class="text-[#576b95] font-medium">{{ reply.to }}</span>
                      <span class="text-[#333]">：{{ reply.content }}</span>
                    </div>
                  </div>
                </div>
                <button class="flex flex-col items-center gap-1 text-[#999] hover:text-blue-600 transition-colors flex-shrink-0">
                  <Heart :size="16" />
                  <span class="text-[11px]">{{ comment.likes }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Interaction Footer (Sticky) -->
        <div class="px-5 py-3 border-t border-[#f0f0f0] bg-white shrink-0">
          <div class="flex items-center justify-between mb-3">
            <div class="flex items-center gap-5">
              <button class="flex items-center gap-1.5 text-[#333] hover:text-blue-600 transition-colors group">
                <Heart :size="22" class="group-hover:scale-110 transition-transform" />
                <span class="text-[13px] font-medium">3.5万</span>
              </button>
              <button class="flex items-center gap-1.5 text-[#333] hover:text-[#ffac33] transition-colors group">
                <Star :size="22" class="group-hover:scale-110 transition-transform" />
                <span class="text-[13px] font-medium">3720</span>
              </button>
              <button class="flex items-center gap-1.5 text-[#333] hover:text-[#576b95] transition-colors group">
                <MessageCircle :size="22" class="group-hover:scale-110 transition-transform" />
                <span class="text-[13px] font-medium">3933</span>
              </button>
            </div>
            <button class="text-[#333] hover:text-[#666] transition-colors">
              <Share2 :size="22" />
            </button>
          </div>
          
          <!-- Comment Input -->
          <div class="relative">
            <input 
              type="text" 
              placeholder="说点什么..." 
              class="w-full bg-[#f5f5f5] border-none rounded-full py-2.5 px-4 pr-12 text-[14px] text-[#333] placeholder:text-[#999] focus:outline-none focus:ring-2 focus:ring-blue-100 transition-all"
            />
            <button class="absolute right-2 top-1/2 -translate-y-1/2 px-3 py-1 text-blue-600 text-[13px] font-medium hover:bg-blue-50 rounded-full transition-colors">
              发送
            </button>
          </div>
        </div>
      </aside>
    </main>

    <!-- Loading State -->
    <div v-else class="flex-grow flex items-center justify-center bg-white">
      <div class="flex flex-col items-center gap-3">
        <div class="animate-spin rounded-full h-8 w-8 border-3 border-blue-600 border-t-transparent"></div>
        <span class="text-[13px] text-[#999]">加载中...</span>
      </div>
    </div>

    <!-- Image Preview Modal -->
    <teleport to="body">
      <transition name="fade">
        <div v-if="showImagePreview" @click="closeImagePreview" class="fixed inset-0 bg-black z-[110] flex items-center justify-center">
          <button @click="closeImagePreview" class="absolute top-4 right-4 z-50 w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors">
            <XIcon :size="22" />
          </button>
          
          <!-- Image Counter -->
          <div v-if="(article?.images?.length || 0) > 1" class="absolute top-4 left-1/2 -translate-x-1/2 z-10 px-3 py-1 bg-black/50 rounded-full text-white text-sm font-medium">
            {{ previewImgIndex + 1 }} / {{ article?.images?.length }}
          </div>
          
          <div class="relative w-screen h-screen overflow-hidden" @click.stop>
            <div class="flex h-full transition-transform duration-300 ease-out" :style="{ transform: `translateX(-${previewImgIndex * 100}vw)` }">
              <div 
                v-for="(img, i) in (article?.images || [])" 
                :key="i" 
                class="w-screen h-full flex-shrink-0 flex items-center justify-center p-4"
              >
                <img 
                  :src="getImageUrl(img)" 
                  class="max-w-full max-h-full object-contain" 
                />
              </div>
            </div>
            
            <!-- Navigation -->
            <template v-if="(article?.images?.length || 0) > 1">
              <button 
                @click.stop="previewImgIndex = (previewImgIndex - 1 + (article?.images?.length || 0)) % (article?.images?.length || 1)" 
                class="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors backdrop-blur-sm"
              >
                <ChevronLeft :size="28" />
              </button>
              <button 
                @click.stop="previewImgIndex = (previewImgIndex + 1) % (article?.images?.length || 1)" 
                class="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors backdrop-blur-sm"
              >
                <ChevronRight :size="28" />
              </button>
            </template>

            <!-- Indicators -->
            <div v-if="(article?.images?.length || 0) > 1" class="absolute bottom-8 inset-x-0 flex justify-center gap-2 z-10">
              <button 
                v-for="(_, i) in (article?.images || [])" 
                :key="i" 
                @click.stop="previewImgIndex = i"
                class="h-2 rounded-full transition-all" 
                :class="[previewImgIndex === i ? 'w-6 bg-white' : 'w-2 bg-white/40']"
              ></button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { 
  ChevronLeft, ChevronRight, Share2, Tag, MapPin, 
  Link as LinkIcon, Heart, Star, MessageCircle, Edit3, Trash2, X as XIcon
} from 'lucide-vue-next';
import request from '../utils/request';
import { getImageUrl, getAvatarUrl } from '../utils/image';

const route = useRoute();
const router = useRouter();
const article = ref<any>(null);
const scrolled = ref(false);
const currentImgIndex = ref(0);

// 图片预览相关
const showImagePreview = ref(false);
const previewImgIndex = ref(0);

// 获取来源页面，用于决定返回哪里
const from = computed(() => route.query.from as string);

// 处理返回按钮
const handleBack = () => {
  if (from.value === 'profile') {
    // 返回个人中心的内容管理标签
    router.push({ path: '/profile', query: { from: 'content' } });
  } else {
    router.push('/');
  }
};

// 获取当前登录用户
const currentUser = computed(() => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  }
  return null;
});

// 判断当前用户是否是文章作者
const isAuthor = computed(() => {
  return currentUser.value && article.value && currentUser.value.username === article.value.createdBy;
});

const handleScroll = () => {
  scrolled.value = window.scrollY > 50;
};

// 触摸滑动相关
const touchStartX = ref(0);
const touchEndX = ref(0);
const minSwipeDistance = 50;

const handleTouchStart = (e: TouchEvent) => {
  touchStartX.value = e.touches[0].clientX;
};

const handleTouchMove = (e: TouchEvent) => {
  touchEndX.value = e.touches[0].clientX;
};

const handleTouchEnd = () => {
  if (!article.value || article.value.images.length <= 1) return;
  
  const swipeDistance = touchEndX.value - touchStartX.value;
  
  if (swipeDistance < -minSwipeDistance) {
    nextImage();
  } else if (swipeDistance > minSwipeDistance) {
    prevImage();
  }
  
  touchStartX.value = 0;
  touchEndX.value = 0;
};

// 图片导航
const nextImage = () => {
  if (!article.value) return;
  currentImgIndex.value = (currentImgIndex.value + 1) % article.value.images.length;
};

const prevImage = () => {
  if (!article.value) return;
  currentImgIndex.value = (currentImgIndex.value - 1 + article.value.images.length) % article.value.images.length;
};

// 键盘导航
const handleKeydown = (e: KeyboardEvent) => {
  if (showImagePreview.value) {
    if (e.key === 'Escape') {
      closeImagePreview();
    } else if (e.key === 'ArrowLeft') {
      previewImgIndex.value = (previewImgIndex.value - 1 + (article.value?.images?.length || 1)) % (article.value?.images?.length || 1);
    } else if (e.key === 'ArrowRight') {
      previewImgIndex.value = (previewImgIndex.value + 1) % (article.value?.images?.length || 1);
    }
  } else {
    if (e.key === 'ArrowLeft') {
      prevImage();
    } else if (e.key === 'ArrowRight') {
      nextImage();
    }
  }
};

// 模拟评论数据
const mockComments = ref([
  {
    user: '小明同学',
    content: '写得真好，学到了很多！收藏了~',
    time: '2小时前',
    likes: 128,
    isAuthor: false,
    replies: [
      { user: '作者', to: '小明同学', content: '谢谢支持！会继续分享更多内容' }
    ]
  },
  {
    user: '技术达人',
    content: '这个思路很清晰，我之前也遇到过类似的问题，用这种方法确实能解决。',
    time: '5小时前',
    likes: 86,
    isAuthor: false
  },
  {
    user: '产品经理',
    content: '从产品的角度来看，这个设计方案很合理，用户体验会很好。',
    time: '昨天',
    likes: 45,
    isAuthor: false
  },
  {
    user: '前端小白',
    content: '请问这个是怎么实现的？能详细讲讲吗？',
    time: '昨天',
    likes: 12,
    isAuthor: false,
    replies: [
      { user: '作者', to: '前端小白', content: '可以的，我后面会出一篇详细教程' },
      { user: '热心网友', to: '前端小白', content: '可以看官方文档，里面有详细说明' }
    ]
  }
]);

const loadDetail = async () => {
  try {
    const res = await request.get(`/article/detail/${route.params.id}`);
    if (res.data.code === 200) {
      article.value = res.data.data;
      // 设置页面标题为文章标题
      document.title = article.value.title || '文章详情';
    }
  } catch (err) {
    console.error('Load detail error:', err);
  }
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' });
};

const formatFullDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' 
  });
};

// 跳转到编辑页面
const handleEdit = () => {
  // 传递from参数，保持来源信息
  router.push(`/article/${route.params.id}/edit?from=${from.value || ''}`);
};

// 删除文章
const handleDelete = async () => {
  if (!confirm('确定要删除这篇文章吗？此操作不可恢复。')) {
    return;
  }
  try {
    const res = await request.delete(`/article/delete/${route.params.id}`);
    if (res.data.code === 200) {
      alert('删除成功');
      // 删除成功，设置标记让首页刷新
      sessionStorage.setItem('homeNeedRefresh', 'true');
      console.log('删除成功，设置刷新标记');
      router.push('/');
    }
  } catch (err: any) {
    alert(err.response?.data?.message || '删除失败');
  }
};

// 打开图片预览
const openImagePreview = (index: number) => {
  previewImgIndex.value = index;
  showImagePreview.value = true;
};

// 关闭图片预览
const closeImagePreview = () => {
  showImagePreview.value = false;
};

onMounted(() => {
  window.addEventListener('scroll', handleScroll);
  window.addEventListener('keydown', handleKeydown);
  loadDetail();
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
  window.removeEventListener('keydown', handleKeydown);
});
</script>

<style scoped>
/* 图片预览淡入淡出 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 图片区域宽度控制 */
@media (min-width: 768px) {
  .gallery-section {
    width: 55% !important;
  }
}

@media (min-width: 1024px) {
  .gallery-section {
    width: 58% !important;
  }
}

@media (min-width: 1280px) {
  .gallery-section {
    width: 60% !important;
  }
}

/* 隐藏滚动条但保持滚动功能 */
aside::-webkit-scrollbar {
  width: 0;
  height: 0;
}

aside {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>

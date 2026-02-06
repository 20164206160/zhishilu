<template>
  <div 
    @click="goToDetail"
    class="group bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-xl transition-all duration-300 border border-gray-100 flex flex-col h-full cursor-pointer relative"
  >
    <!-- 正方形展示区域 -->
    <div class="aspect-square w-full relative overflow-hidden bg-gradient-to-br from-gray-50 to-gray-100">
      <!-- 图片模式 -->
      <template v-if="article.images && article.images.length > 0">
        <img 
          :src="getImageUrl(article.images[0])" 
          class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
          alt="article cover"
        />
        <div class="absolute inset-0 bg-black/0 group-hover:bg-black/5 transition-colors duration-300"></div>
      </template>
      
      <!-- 文字模式 (无图片时展示正文内容) -->
      <div v-else class="w-full h-full p-4 flex flex-col justify-center">
        <p class="text-sm text-gray-600 line-clamp-6 leading-relaxed">
          {{ article.content || '暂无内容' }}
        </p>
      </div>

      <!-- 分类标签 -->
      <div class="absolute top-2 left-2 px-2.5 py-1 bg-white/90 backdrop-blur-sm text-blue-600 text-[10px] font-bold rounded-lg shadow-sm z-20 uppercase tracking-wider">
        {{ article.category }}
      </div>
    </div>

    <!-- 底部信息区 -->
    <div class="p-3 space-y-2.5 flex flex-col flex-grow">
      <!-- 标题 -->
      <h3 class="text-sm text-gray-800 line-clamp-2 font-medium leading-snug min-h-[2.5rem] group-hover:text-blue-600 transition-colors">
        {{ article.title }}
      </h3>
      
      <!-- 发布人信息 -->
      <div class="flex items-center space-x-2">
        <div class="w-6 h-6 rounded-full bg-gradient-to-tr from-blue-500 to-blue-400 flex items-center justify-center text-white text-[10px] font-bold flex-shrink-0">
          {{ getUserInitial(article.createdBy) }}
        </div>
        <span class="text-xs text-gray-600 truncate flex-1">{{ article.createdBy }}</span>
      </div>
      
      <!-- 时间和地点 -->
      <div class="flex items-center justify-between text-[11px] text-gray-400">
        <div class="flex items-center space-x-1">
          <ClockIcon :size="12" />
          <span>{{ formatDate(article.createdTime) }}</span>
        </div>
        <div v-if="article.location" class="flex items-center space-x-1 truncate max-w-[100px]" :title="article.location">
          <MapPinIcon :size="12" />
          <span class="truncate">{{ article.location }}</span>
        </div>
      </div>
      
      <!-- 链接 -->
      <div v-if="article.url" class="flex items-center space-x-1 text-[11px] text-blue-500 hover:text-blue-600 transition-colors" @click.stop="openUrl(article.url)">
        <LinkIcon :size="12" />
        <span class="truncate">{{ formatUrl(article.url) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Clock as ClockIcon, MapPin as MapPinIcon, Link as LinkIcon } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import { getImageUrl } from '../utils/image';

const props = defineProps<{
  article: any
}>();

const router = useRouter();

const goToDetail = () => {
  router.push(`/article/${props.article.id}`);
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' });
};

// 获取用户名首字母作为头像
const getUserInitial = (username: string) => {
  if (!username) return 'U';
  return username.charAt(0).toUpperCase();
};

// 格式化URL显示（只显示域名）
const formatUrl = (url: string) => {
  if (!url) return '';
  try {
    const urlObj = new URL(url);
    return urlObj.hostname.replace('www.', '');
  } catch {
    return url.length > 30 ? url.substring(0, 30) + '...' : url;
  }
};

// 打开外部链接
const openUrl = (url: string) => {
  if (url) {
    window.open(url, '_blank');
  }
};
</script>

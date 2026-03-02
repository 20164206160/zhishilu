<template>
  <div
    @click="goToDetail"
    class="group bg-white rounded-lg sm:rounded-xl overflow-hidden shadow-sm hover:shadow-xl transition-all duration-300 ease-out border border-gray-100 flex flex-col h-full cursor-pointer relative active:scale-[0.98] active:duration-150"
  >
    <!-- 顶部图片区域 - 高度减少为3/4正方形 -->
    <div class="aspect-[4/3] w-full relative overflow-hidden bg-gradient-to-br from-gray-50 to-gray-100">
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
        <p 
          class="text-sm text-gray-600 line-clamp-4 leading-relaxed"
          v-html="getDisplayContent()"
        ></p>
      </div>

      <!-- 分类标签 -->
      <div class="absolute top-1.5 sm:top-2 left-1.5 sm:left-2 flex flex-wrap gap-0.5 sm:gap-1 max-w-[75%] sm:max-w-[80%]">
        <span
          v-for="(cat, index) in article.categories?.slice(0, 2)"
          :key="index"
          class="px-1.5 sm:px-2 py-0.5 bg-white/90 backdrop-blur-sm text-blue-600 text-[9px] sm:text-[10px] font-bold rounded-md sm:rounded-lg shadow-sm z-20 uppercase tracking-wider"
        >
          {{ cat }}
        </span>
        <span v-if="article.categories?.length > 2" class="px-1.5 sm:px-2 py-0.5 bg-white/90 backdrop-blur-sm text-blue-600 text-[9px] sm:text-[10px] font-bold rounded-md sm:rounded-lg shadow-sm z-20">
          +{{ article.categories.length - 2 }}
        </span>
      </div>
    </div>

    <!-- 底部信息区 -->
    <div class="p-2 sm:p-3 space-y-1 sm:space-y-2 flex flex-col flex-grow">
      <!-- 标题 -->
      <h3
        class="text-xs sm:text-sm text-gray-800 line-clamp-2 font-medium leading-snug group-hover:text-blue-600 transition-colors"
        v-html="article.highlightTitle || article.title"
      ></h3>

      <!-- 正文摘要（有图片时展示） -->
      <p
        v-if="article.images && article.images.length > 0"
        class="text-[10px] sm:text-xs text-gray-500 line-clamp-2 leading-relaxed hidden sm:block"
        v-html="getDisplayContent()"
      ></p>

      <!-- 占位空间，将底部信息推到底部 -->
      <div class="flex-grow"></div>

      <!-- 发布人信息 -->
      <div class="flex items-center space-x-1.5 sm:space-x-2">
        <div class="w-5 h-5 sm:w-6 sm:h-6 rounded-full bg-gradient-to-tr from-blue-500 to-blue-400 flex items-center justify-center text-white text-[8px] sm:text-[10px] font-bold flex-shrink-0 overflow-hidden">
          <img v-if="article.creatorAvatar" :src="getAvatarUrl(article.creatorAvatar)" class="w-full h-full object-cover" alt="avatar" />
          <template v-else>{{ getUserInitial(article.createdBy) }}</template>
        </div>
        <span class="text-[10px] sm:text-xs text-gray-600 truncate flex-1">{{ article.createdBy }}</span>
      </div>

      <!-- 时间和地点 -->
      <div class="flex items-center justify-between text-[9px] sm:text-[11px] text-gray-400">
        <div class="flex items-center space-x-0.5 sm:space-x-1">
          <ClockIcon :size="10" class="sm:w-3 sm:h-3" />
          <span>{{ formatDate(article.createdTime) }}</span>
        </div>
        <div v-if="article.location || article.highlightLocation" class="flex items-center space-x-0.5 sm:space-x-1 truncate max-w-[70px] sm:max-w-[100px]" :title="article.location">
          <MapPinIcon :size="10" class="sm:w-3 sm:h-3" />
          <span class="truncate" v-html="article.highlightLocation || article.location"></span>
        </div>
      </div>

      <!-- 链接 -->
      <div v-if="article.url" class="flex items-center space-x-0.5 sm:space-x-1 text-[9px] sm:text-[11px] text-blue-500 hover:text-blue-600 transition-colors" @click.stop="openUrl(article.url)">
        <LinkIcon :size="10" class="sm:w-3 sm:h-3" />
        <span class="truncate">{{ formatUrl(article.url) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Clock as ClockIcon, MapPin as MapPinIcon, Link as LinkIcon, User as UserIcon } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import { getImageUrl, getAvatarUrl } from '../utils/image';

const props = defineProps<{
  article: any
  searchKeyword?: string
}>();

const emit = defineEmits<{
  openModal: [articleId: string]
}>();

const router = useRouter();

const goToDetail = () => {
  // 桌面端触发弹窗，移动端跳转页面
  if (window.innerWidth >= 768) {
    emit('openModal', props.article.id);
  } else {
    router.push(`/article/${props.article.id}`);
  }
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

// 获取正文预览（默认展示前25个字符）
const getContentPreview = (content: string | undefined): string => {
  if (!content) return '';
  // 去除HTML标签
  const plainText = content.replace(/<[^>]+>/g, '');
  // 截取前25个字符，超出则添加省略号
  if (plainText.length <= 25) {
    return plainText;
  }
  return plainText.substring(0, 25) + '...';
};

// 获取展示内容：优先使用ES高亮，如果没有则以匹配关键词为中心提取片段
const getDisplayContent = (): string => {
  const article = props.article;
  
  // 如果有ES返回的高亮内容，直接使用
  if (article.highlightContent) {
    return article.highlightContent;
  }
  
  // 如果没有高亮内容但有原始内容，尝试以匹配关键词为中心提取
  const content = article.content;
  if (!content) return '暂无内容';
  
  // 获取搜索关键词（从props或article对象）
  const searchKeyword = props.searchKeyword || article._searchKeyword || '';
  if (!searchKeyword) {
    return getContentPreview(content);
  }
  
  // 以关键词为中心提取片段
  return extractSnippetAroundKeyword(content, searchKeyword);
};

// 以关键词为中心提取文本片段
const extractSnippetAroundKeyword = (content: string, keyword: string): string => {
  if (!content || !keyword) return getContentPreview(content);
  
  // 去除HTML标签获取纯文本
  const plainText = content.replace(/<[^>]+>/g, '');
  
  // 查找关键词位置（不区分大小写）
  const lowerText = plainText.toLowerCase();
  const lowerKeyword = keyword.toLowerCase();
  const index = lowerText.indexOf(lowerKeyword);
  
  if (index === -1) {
    // 没找到关键词，返回默认预览
    return getContentPreview(content);
  }
  
  // 计算片段起始和结束位置（关键词前后各12个字符，总共约25字）
  const contextLength = 12;
  let start = Math.max(0, index - contextLength);
  let end = Math.min(plainText.length, index + keyword.length + contextLength);
  
  // 调整以避免截断单词（向前找最近的标点或空格）
  if (start > 0) {
    const nextPunct = plainText.slice(start).search(/[，。！？、；：\s]/);
    if (nextPunct > 0 && nextPunct < 5) {
      start += nextPunct + 1;
    }
  }
  
  // 提取片段
  let snippet = plainText.substring(start, end);
  
  // 添加省略号
  if (start > 0) {
    snippet = '...' + snippet;
  }
  if (end < plainText.length) {
    snippet = snippet + '...';
  }
  
  // 高亮关键词
  const regex = new RegExp(`(${keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
  snippet = snippet.replace(regex, '<mark class="search-highlight">$1</mark>');
  
  return snippet;
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

<style scoped>
/* 高亮样式 */
:deep(.search-highlight) {
  background: linear-gradient(120deg, #fde047 0%, #fde047 100%);
  color: #1f2937;
  padding: 0 2px;
  border-radius: 2px;
  font-weight: 600;
}
</style>

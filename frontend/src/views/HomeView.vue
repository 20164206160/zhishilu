<template>
  <div class="min-h-screen bg-[#f4f4f4] flex flex-col font-sans">
    <!-- 上部分：导航与搜索 (Header) -->
    <header class="bg-white shadow-sm sticky top-0 z-50">
      <!-- 顶栏：品牌与主操作 -->
      <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between gap-4">
        <h1 class="text-2xl font-black italic tracking-tighter text-blue-600 flex-shrink-0">
          知拾录<span class="text-emerald-500">.</span>
        </h1>
        
        <!-- 搜索框 -->
        <div class="flex-grow max-w-2xl relative group">
          <SearchIcon class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-blue-500 transition-colors" :size="18" />
          <input 
            v-model="searchQuery"
            @keyup.enter="handleSearch"
            type="text" 
            placeholder="搜知识、看干货、找灵感..." 
            class="w-full pl-11 pr-24 py-2.5 bg-gray-100 border-2 border-transparent rounded-full focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-50 transition-all outline-none text-sm"
          />
          <button 
            @click="handleSearch"
            class="absolute right-1.5 top-1/2 -translate-y-1/2 bg-blue-600 hover:bg-blue-700 text-white px-5 py-1.5 rounded-full text-xs font-bold transition-all shadow-sm active:scale-95"
          >
            搜索
          </button>
        </div>
        
        <div class="flex items-center gap-3">
          <router-link to="/post" class="flex items-center gap-1.5 bg-emerald-500 hover:bg-emerald-600 text-white px-4 py-2 rounded-full text-sm font-bold transition-all shadow-md active:scale-95">
            <PlusIcon :size="18" stroke-width="3" />
            <span class="hidden sm:inline">新增</span>
          </router-link>
          <router-link to="/profile" class="w-9 h-9 rounded-full bg-gradient-to-tr from-blue-100 to-blue-50 border border-blue-200 flex items-center justify-center text-blue-600 font-bold cursor-pointer hover:shadow-inner">
            U
          </router-link>
        </div>
      </div>

      <!-- 搜索下栏：热门与分类 -->
      <div class="max-w-7xl mx-auto px-4 pb-3 flex flex-col gap-3">
        <!-- 热门提示词 -->
        <div class="flex items-center gap-3 overflow-x-auto scrollbar-hide py-1">
          <span class="text-[11px] font-bold text-gray-400 uppercase tracking-widest whitespace-nowrap flex-shrink-0">热门搜索:</span>
          <button 
            v-for="word in hotWords" 
            :key="word"
            @click="quickSearch(word)"
            class="text-xs text-gray-500 hover:text-blue-600 transition-colors whitespace-nowrap"
          >
            {{ word }}
          </button>
        </div>

        <!-- 分类选择 -->
        <div class="flex items-center gap-2 overflow-x-auto scrollbar-hide pb-1">
          <button 
            v-for="cat in categories" 
            :key="cat"
            @click="selectedCategory = cat"
            :class="[
              'px-5 py-1.5 rounded-full text-xs font-medium transition-all whitespace-nowrap border',
              selectedCategory === cat 
                ? 'bg-blue-600 border-blue-600 text-white shadow-md' 
                : 'bg-white border-gray-200 text-gray-600 hover:border-blue-400 hover:text-blue-500'
            ]"
          >
            {{ cat }}
          </button>
        </div>
      </div>
    </header>

    <!-- 中部分：内容展示区 (Main Grid) -->
    <main class="flex-grow max-w-7xl mx-auto w-full px-4 py-6">
      <div v-if="loading" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
        <div v-for="i in 12" :key="i" class="aspect-square bg-white border border-gray-100 rounded-xl animate-pulse"></div>
      </div>
      
      <div v-else-if="articles.length > 0" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
        <ArticleCard v-for="item in articles" :key="item.id" :article="item" />
      </div>
      
      <div v-else class="h-[50vh] flex flex-col items-center justify-center text-gray-400 bg-white rounded-3xl shadow-inner border border-gray-50">
        <div class="p-8 bg-gray-50 rounded-full mb-4">
          <EmptyIcon :size="80" stroke-width="1" class="text-gray-200" />
        </div>
        <p class="text-sm font-medium">暂时没有找到相关内容呢~</p>
        <button @click="resetFilters" class="mt-4 text-blue-500 text-xs font-bold hover:underline">清除筛选</button>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="mt-12 mb-8 flex items-center justify-center gap-4">
        <button 
          @click="page--"
          :disabled="page === 0"
          class="p-2 rounded-xl border border-gray-200 bg-white disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors shadow-sm"
        >
          <ChevronLeft :size="20" />
        </button>
        <div class="flex items-center gap-2">
          <span class="px-4 py-1.5 bg-white border border-gray-200 rounded-xl text-xs font-bold text-blue-600 shadow-sm">
            {{ page + 1 }}
          </span>
          <span class="text-xs text-gray-400 font-medium">/ {{ totalPages }}</span>
        </div>
        <button 
          @click="page++"
          :disabled="page >= totalPages - 1"
          class="p-2 rounded-xl border border-gray-200 bg-white disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors shadow-sm"
        >
          <ChevronRight :size="20" />
        </button>
      </div>
    </main>

    <!-- 下部分：开发者信息 (Footer) -->
    <footer class="bg-white border-t border-gray-100 py-10 mt-auto">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex flex-col md:flex-row justify-between items-center gap-8">
          <!-- 站点信息 -->
          <div class="text-center md:text-left space-y-2">
            <h2 class="text-lg font-bold text-gray-800">知拾录 <span class="text-gray-300 font-light ml-1 text-base">Personal Knowledge Library</span></h2>
            <p class="text-xs text-gray-400 max-w-xs leading-relaxed">
              知拾录致力于为用户提供一个优雅、高效的个人知识收藏与管理平台。随时随地，随手记，随心搜。
            </p>
          </div>

          <!-- 开发者信息 -->
          <div class="flex flex-col items-center md:items-end gap-4">
            <div class="flex items-center gap-6">
              <a href="#" class="text-gray-400 hover:text-blue-500 transition-colors"><GithubIcon :size="20" /></a>
              <a href="#" class="text-gray-400 hover:text-blue-500 transition-colors"><MailIcon :size="20" /></a>
              <a href="#" class="text-gray-400 hover:text-blue-500 transition-colors"><TwitterIcon :size="20" /></a>
            </div>
            <div class="text-[11px] text-gray-400 text-center md:text-right font-medium">
              <p>© 2026 知拾录. 版权所有.</p>
              <p class="mt-1">Designed & Developed by <span class="text-gray-600 hover:text-blue-500 cursor-pointer">Zhishilu Team</span></p>
            </div>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { 
  Search as SearchIcon, 
  Plus as PlusIcon, 
  Inbox as EmptyIcon, 
  ChevronLeft, 
  ChevronRight,
  Github as GithubIcon,
  Mail as MailIcon,
  Twitter as TwitterIcon
} from 'lucide-vue-next';
import request from '../utils/request';
import ArticleCard from '../components/ArticleCard.vue';

// 状态定义
const searchQuery = ref('');
const selectedCategory = ref('全部');
const categories = ref(['全部', '技术', '生活', '工作', '读书', '笔记', '其他']);
const hotWords = ref(['Vue3', 'SpringBoot', 'Elasticsearch', 'Tailwind', '设计模式', '面试题']);
const articles = ref<any[]>([]);
const loading = ref(true);
const page = ref(0);
const size = ref(24); // 淘宝风格网格通常每页展示较多
const totalPages = ref(0);

// 获取数据
const fetchArticles = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: page.value,
      size: size.value
    };
    if (searchQuery.value) params.title = searchQuery.value;
    // 只有非"全部"时才传递category参数
    if (selectedCategory.value && selectedCategory.value !== '全部') {
      params.category = selectedCategory.value;
    }

    console.log('请求参数:', params);
    const res = await request.get('/article/list', { params });
    console.log('响应数据:', res.data);
    
    if (res.data.code === 200) {
      articles.value = res.data.data.list;
      totalPages.value = Math.ceil(res.data.data.total / size.value);
      console.log('文章列表:', articles.value);
      console.log('总页数:', totalPages.value);
    }
  } catch (err) {
    console.error('Fetch error:', err);
  } finally {
    // 模拟网络延迟以展示加载效果
    setTimeout(() => { loading.value = false; }, 300);
  }
};

// 操作处理
const handleSearch = () => {
  page.value = 0;
  fetchArticles();
};

const quickSearch = (word: string) => {
  searchQuery.value = word;
  handleSearch();
};

const resetFilters = () => {
  searchQuery.value = '';
  selectedCategory.value = '全部';
  page.value = 0;
  fetchArticles();
};

// 监听器
watch([selectedCategory, page], () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
  fetchArticles();
});

onMounted(() => {
  fetchArticles();
});
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>

<template>
  <div class="min-h-screen flex flex-col">
    <!-- Header -->
    <header class="sticky top-0 z-30 bg-white/80 backdrop-blur-md border-b border-gray-100 px-4 py-3">
      <div class="max-w-7xl mx-auto flex flex-col md:flex-row items-center gap-4">
        <h1 class="text-2xl font-bold bg-gradient-to-r from-blue-600 to-emerald-600 bg-clip-text text-transparent">
          知拾录
        </h1>
        
        <div class="relative flex-grow max-w-2xl w-full">
          <SearchIcon class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" :size="20" />
          <input 
            v-model="searchQuery"
            @keyup.enter="handleSearch"
            type="text" 
            placeholder="搜索你收藏的内容..." 
            class="w-full pl-10 pr-4 py-2 bg-gray-100 border-none rounded-full focus:ring-2 focus:ring-blue-500 transition-all outline-none"
          />
        </div>
        
        <div class="hidden md:flex items-center gap-4">
          <button class="p-2 hover:bg-gray-100 rounded-full transition-colors text-gray-600">
            <PlusIcon :size="24" />
          </button>
          <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold">
            U
          </div>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="flex-grow max-w-7xl mx-auto w-full px-4 py-6 flex gap-6">
      <!-- Sidebar Categories -->
      <aside class="hidden lg:block w-48 flex-shrink-0">
        <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4 px-2">分类列表</h2>
        <nav class="space-y-1">
          <button 
            v-for="cat in categories" 
            :key="cat"
            @click="selectedCategory = cat"
            :class="[
              'w-full text-left px-3 py-2 rounded-lg text-sm transition-colors',
              selectedCategory === cat ? 'bg-blue-50 text-blue-600 font-medium' : 'text-gray-600 hover:bg-gray-100'
            ]"
          >
            {{ cat }}
          </button>
        </nav>
      </aside>

      <!-- Grid -->
      <div class="flex-grow">
        <!-- Mobile Categories -->
        <div class="lg:hidden flex gap-2 overflow-x-auto pb-4 mb-4 scrollbar-hide">
          <button 
            v-for="cat in categories" 
            :key="cat"
            @click="selectedCategory = cat"
            :class="[
              'whitespace-nowrap px-4 py-1.5 rounded-full text-sm transition-colors',
              selectedCategory === cat ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600'
            ]"
          >
            {{ cat }}
          </button>
        </div>

        <div v-if="loading" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
          <div v-for="i in 10" :key="i" class="aspect-[3/4] bg-gray-100 animate-pulse rounded-lg"></div>
        </div>
        
        <div v-else-if="articles.length > 0" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
          <ArticleCard v-for="item in articles" :key="item.id" :article="item" />
        </div>
        
        <div v-else class="h-64 flex flex-col items-center justify-center text-gray-400">
          <EmptyIcon :size="64" stroke-width="1" />
          <p class="mt-4">暂无相关内容</p>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="mt-12 flex justify-center gap-2">
          <button 
            @click="page--"
            :disabled="page === 0"
            class="px-4 py-2 border rounded-lg disabled:opacity-50"
          >
            上一页
          </button>
          <span class="px-4 py-2 text-gray-600">第 {{ page + 1 }} 页 / 共 {{ totalPages }} 页</span>
          <button 
            @click="page++"
            :disabled="page >= totalPages - 1"
            class="px-4 py-2 border rounded-lg disabled:opacity-50"
          >
            下一页
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { Search as SearchIcon, Plus as PlusIcon, Inbox as EmptyIcon } from 'lucide-vue-next';
import axios from 'axios';
import ArticleCard from '../components/ArticleCard.vue';

const searchQuery = ref('');
const selectedCategory = ref('全部');
const categories = ref(['全部', '技术', '生活', '工作', '读书', '其他']);
const articles = ref<any[]>([]);
const loading = ref(true);
const page = ref(0);
const size = ref(20);
const totalPages = ref(0);

const fetchArticles = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: page.value,
      size: size.value
    };
    if (searchQuery.value) params.title = searchQuery.value;
    if (selectedCategory.value !== '全部') params.category = selectedCategory.value;

    const res = await axios.get('/api/article/list', { params });
    if (res.data.code === 200) {
      articles.value = res.data.data.list;
      totalPages.value = Math.ceil(res.data.data.total / size.value);
    }
  } catch (err) {
    console.error('Fetch error:', err);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  page.value = 0;
  fetchArticles();
};

watch([selectedCategory, page], () => {
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

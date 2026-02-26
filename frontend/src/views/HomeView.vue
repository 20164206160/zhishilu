<template>
  <div class="min-h-screen bg-[#f4f4f4] flex flex-col font-sans w-full overflow-x-hidden">
    <!-- 上部分：导航与搜索 (Header) -->
    <header class="bg-white shadow-sm sticky top-0 z-50 w-full">
      <!-- 顶栏：品牌与主操作 -->
      <div class="max-w-7xl mx-auto px-2 sm:px-4 h-14 sm:h-16 flex items-center justify-between gap-2 sm:gap-4">
        <h1 class="text-xl sm:text-2xl font-black italic tracking-tighter text-blue-600 flex-shrink-0">
          知拾录<span class="text-emerald-500">.</span>
        </h1>

        <!-- 搜索框 -->
        <div class="flex-grow max-w-2xl relative min-w-0">
          <div class="flex items-center">
            <!-- 字段选择下拉框 -->
            <div class="relative flex-shrink-0">
              <select
                v-model="searchField"
                class="appearance-none px-2 sm:px-4 py-2 sm:py-2.5 bg-gray-100 border-2 border-r-0 border-transparent rounded-l-full text-xs sm:text-sm text-gray-600 focus:bg-white focus:border-blue-500 outline-none cursor-pointer hover:bg-gray-200 transition-colors pr-6 sm:pr-8"
              >
                <option value="all">全部</option>
                <option value="title">标题</option>
                <option value="category">类别</option>
                <option value="content">内容</option>
                <option value="username">用户名</option>
                <option value="location">地点</option>
              </select>
              <ChevronDown class="absolute right-1 sm:right-2 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" :size="12" />
            </div>
            <div class="relative flex-grow group min-w-0">
              <SearchIcon class="absolute left-2 sm:left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-blue-500 transition-colors" :size="16" />
              <input
                v-model="searchQuery"
                @keyup.enter="handleSearch"
                @input="handleInput"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
                type="text"
                :placeholder="getPlaceholder()"
                class="w-full pl-8 sm:pl-11 pr-16 sm:pr-28 py-2 sm:py-2.5 bg-gray-100 border-2 border-l-0 border-transparent rounded-r-full focus:bg-white focus:border-blue-500 focus:ring-2 sm:focus:ring-4 focus:ring-blue-50 transition-all outline-none text-xs sm:text-sm"
              />
              <!-- 清空按钮 -->
              <button
                v-if="searchQuery || selectedCategory !== '全部'"
                @click="resetFilters"
                class="hidden sm:flex absolute right-[70px] top-1/2 -translate-y-1/2 px-2 py-1 rounded-md bg-orange-500 hover:bg-orange-600 text-white text-xs font-bold items-center gap-1 transition-colors shadow-sm"
                title="清空查询条件"
              >
                <XIcon :size="12" />
                <span>清空</span>
              </button>
              <button
                @click="handleSearch"
                class="absolute right-1 top-1/2 -translate-y-1/2 bg-blue-600 hover:bg-blue-700 text-white px-2 sm:px-4 py-1 sm:py-1.5 rounded-full text-xs font-bold transition-all shadow-sm active:scale-95"
              >
                <span class="hidden sm:inline">搜索</span>
                <SearchIcon class="sm:hidden" :size="14" />
              </button>
              
              <!-- 搜索补全下拉框 -->
              <div
                v-if="showSuggestions && hasSuggestions"
                class="absolute top-full left-0 right-0 mt-1 bg-white rounded-xl shadow-lg border border-gray-100 overflow-hidden z-50"
              >
                <!-- 用户名补全 -->
                <div v-if="suggestions.usernames?.length" class="border-b border-gray-50 last:border-b-0">
                  <div class="px-3 py-1.5 bg-gray-50 text-[10px] text-gray-400 font-medium">用户名</div>
                  <div
                    v-for="item in suggestions.usernames"
                    :key="'user-' + item.text"
                    @mousedown.prevent="applySuggestion(item.text, 'username')"
                    class="px-3 py-2 hover:bg-blue-50 cursor-pointer flex items-center justify-between group"
                  >
                    <span class="text-sm text-gray-700 group-hover:text-blue-600">{{ item.text }}</span>
                    <span class="text-[10px] text-gray-400">{{ item.count }}条</span>
                  </div>
                </div>
                
                <!-- 地点补全 -->
                <div v-if="suggestions.locations?.length" class="border-b border-gray-50 last:border-b-0">
                  <div class="px-3 py-1.5 bg-gray-50 text-[10px] text-gray-400 font-medium">地点</div>
                  <div
                    v-for="item in suggestions.locations"
                    :key="'loc-' + item.text"
                    @mousedown.prevent="applySuggestion(item.text, 'location')"
                    class="px-3 py-2 hover:bg-blue-50 cursor-pointer flex items-center justify-between group"
                  >
                    <span class="text-sm text-gray-700 group-hover:text-blue-600">{{ item.text }}</span>
                    <span class="text-[10px] text-gray-400">{{ item.count }}条</span>
                  </div>
                </div>
                
                <!-- 类别补全 -->
                <div v-if="suggestions.categories?.length" class="border-b border-gray-50 last:border-b-0">
                  <div class="px-3 py-1.5 bg-gray-50 text-[10px] text-gray-400 font-medium">类别</div>
                  <div
                    v-for="item in suggestions.categories"
                    :key="'cat-' + item.text"
                    @mousedown.prevent="applySuggestion(item.text, 'category')"
                    class="px-3 py-2 hover:bg-blue-50 cursor-pointer flex items-center justify-between group"
                  >
                    <span class="text-sm text-gray-700 group-hover:text-blue-600">{{ item.text }}</span>
                    <span class="text-[10px] text-gray-400">{{ item.count }}条</span>
                  </div>
                </div>
                
                <!-- 标题补全 -->
                <div v-if="suggestions.titles?.length" class="border-b border-gray-50 last:border-b-0">
                  <div class="px-3 py-1.5 bg-gray-50 text-[10px] text-gray-400 font-medium">标题</div>
                  <div
                    v-for="item in suggestions.titles"
                    :key="'title-' + item.text"
                    @mousedown.prevent="applySuggestion(item.text, 'title')"
                    class="px-3 py-2 hover:bg-blue-50 cursor-pointer flex items-center justify-between group"
                  >
                    <span class="text-sm text-gray-700 group-hover:text-blue-600 truncate max-w-[200px]">{{ item.text }}</span>
                    <span class="text-[10px] text-gray-400">{{ item.count }}条</span>
                  </div>
                </div>
                
                <!-- 内容补全 -->
                <div v-if="suggestions.contents?.length">
                  <div class="px-3 py-1.5 bg-gray-50 text-[10px] text-gray-400 font-medium">内容</div>
                  <div
                    v-for="item in suggestions.contents"
                    :key="'content-' + item.text"
                    @mousedown.prevent="applySuggestion(item.text, 'content')"
                    class="px-3 py-2 hover:bg-blue-50 cursor-pointer flex items-center justify-between group"
                  >
                    <span class="text-sm text-gray-700 group-hover:text-blue-600 truncate max-w-[200px]">{{ item.text }}</span>
                    <span class="text-[10px] text-gray-400">{{ item.count }}条</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex items-center gap-1 sm:gap-3 flex-shrink-0">
          <router-link to="/post" class="flex items-center gap-1 bg-emerald-500 hover:bg-emerald-600 text-white px-2 sm:px-4 py-2 rounded-full text-xs sm:text-sm font-bold transition-all shadow-md active:scale-95">
            <PlusIcon :size="16" stroke-width="3" />
            <span class="hidden sm:inline">新增</span>
          </router-link>
          <router-link to="/profile" class="w-8 h-8 sm:w-9 sm:h-9 rounded-full bg-gradient-to-tr from-blue-100 to-blue-50 border border-blue-200 flex items-center justify-center text-blue-600 font-bold cursor-pointer hover:shadow-inner overflow-hidden flex-shrink-0">
            <img v-if="currentUserAvatar" :src="currentUserAvatar" class="w-full h-full object-cover" alt="avatar" />
            <span v-else-if="currentUserName">{{ currentUserName.charAt(0).toUpperCase() }}</span>
            <UserIcon v-else :size="16" />
          </router-link>
        </div>
      </div>

      <!-- 搜索下栏：热门与分类 -->
      <div class="max-w-7xl mx-auto px-2 sm:px-4 pb-2 sm:pb-3 flex flex-col gap-2 sm:gap-3">
        <!-- 热门提示词 -->
        <div class="flex items-center gap-2 sm:gap-3 overflow-x-auto scrollbar-hide py-1">
          <span class="text-[10px] sm:text-[11px] font-bold text-gray-400 uppercase tracking-widest whitespace-nowrap flex-shrink-0">热门:</span>
          <button
            v-for="word in hotWords"
            :key="word"
            @click="quickSearch(word)"
            class="text-[11px] sm:text-xs text-gray-500 hover:text-blue-600 transition-colors whitespace-nowrap"
          >
            {{ word }}
          </button>
        </div>

        <!-- 分类选择 -->
        <div class="flex items-center gap-1.5 sm:gap-2 overflow-x-auto scrollbar-hide pb-1">
          <button
            @click="selectedCategory = '全部'"
            :class="[
              'px-3 sm:px-5 py-1 sm:py-1.5 rounded-full text-[11px] sm:text-xs font-medium transition-all whitespace-nowrap border',
              selectedCategory === '全部'
                ? 'bg-blue-600 border-blue-600 text-white shadow-md'
                : 'bg-white border-gray-200 text-gray-600 hover:border-blue-400 hover:text-blue-500'
            ]"
          >
            全部
          </button>
          <button
            v-for="cat in categories"
            :key="cat.category"
            @click="selectedCategory = cat.category"
            :class="[
              'px-3 sm:px-5 py-1 sm:py-1.5 rounded-full text-[11px] sm:text-xs font-medium transition-all whitespace-nowrap border',
              selectedCategory === cat.category
                ? 'bg-blue-600 border-blue-600 text-white shadow-md'
                : 'bg-white border-gray-200 text-gray-600 hover:border-blue-400 hover:text-blue-500'
            ]"
          >
            {{ cat.category }}
          </button>
        </div>
      </div>
    </header>

    <!-- 中部分：内容展示区 (Main Grid) -->
    <main class="flex-grow max-w-7xl mx-auto w-full px-2 sm:px-4 py-4 sm:py-6">
      <!-- 移动端：2列，平板：3列，小桌面：4列，大桌面：5列，超大屏：6列 -->
      <div v-if="loading" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-2 sm:gap-4">
        <div v-for="i in 12" :key="i" class="aspect-square bg-white border border-gray-100 rounded-xl animate-pulse"></div>
      </div>

      <div v-else-if="articles.length > 0" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-2 sm:gap-4">
        <ArticleCard v-for="item in articles" :key="item.id" :article="item" :search-keyword="currentSearchKeyword" />
      </div>
      
      <div v-else class="h-[50vh] flex flex-col items-center justify-center text-gray-400 bg-white rounded-3xl shadow-inner border border-gray-50">
        <div class="p-8 bg-gray-50 rounded-full mb-4">
          <EmptyIcon :size="80" stroke-width="1" class="text-gray-200" />
        </div>
        <p class="text-sm font-medium">暂时没有找到相关内容呢~</p>
        <button @click="resetFilters" class="mt-4 text-blue-500 text-xs font-bold hover:underline">清除筛选</button>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="mt-8 sm:mt-12 mb-6 sm:mb-8 flex items-center justify-center gap-2 sm:gap-4">
        <button
          @click="page--"
          :disabled="page === 0"
          class="p-1.5 sm:p-2 rounded-xl border border-gray-200 bg-white disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors shadow-sm"
        >
          <ChevronLeft :size="18" class="sm:w-5 sm:h-5" />
        </button>
        <div class="flex items-center gap-1.5 sm:gap-2">
          <span class="px-3 sm:px-4 py-1 sm:py-1.5 bg-white border border-gray-200 rounded-xl text-xs font-bold text-blue-600 shadow-sm">
            {{ page + 1 }}
          </span>
          <span class="text-xs text-gray-400 font-medium">/ {{ totalPages }}</span>
        </div>
        <button
          @click="page++"
          :disabled="page >= totalPages - 1"
          class="p-1.5 sm:p-2 rounded-xl border border-gray-200 bg-white disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors shadow-sm"
        >
          <ChevronRight :size="18" class="sm:w-5 sm:h-5" />
        </button>
      </div>
    </main>

    <!-- 下部分：开发者信息 (Footer) -->
    <footer class="bg-white border-t border-gray-100 py-6 sm:py-10 mt-auto w-full">
      <div class="max-w-7xl mx-auto px-2 sm:px-4">
        <div class="flex flex-col md:flex-row justify-between items-center gap-4 sm:gap-8">
          <!-- 站点信息 -->
          <div class="text-center md:text-left space-y-1 sm:space-y-2">
            <h2 class="text-base sm:text-lg font-bold text-gray-800">知拾录 <span class="text-gray-300 font-light ml-1 text-sm sm:text-base">Personal Knowledge Library</span></h2>
            <p class="text-[10px] sm:text-xs text-gray-400 max-w-xs leading-relaxed px-4 md:px-0">
              知拾录致力于为用户提供一个优雅、高效的个人知识收藏与管理平台。随时随地，随手记，随心搜。
            </p>
          </div>

          <!-- 开发者信息 -->
          <div class="flex flex-col items-center md:items-end gap-2 sm:gap-4">
            <div class="flex items-center gap-4 sm:gap-6">
              <a href="#" class="text-gray-400 hover:text-blue-500 transition-colors"><GithubIcon :size="18" class="sm:w-5 sm:h-5" /></a>
              <a href="#" class="text-gray-400 hover:text-blue-500 transition-colors"><MailIcon :size="18" class="sm:w-5 sm:h-5" /></a>
              <a href="#" class="text-gray-400 hover:text-blue-500 transition-colors"><TwitterIcon :size="18" class="sm:w-5 sm:h-5" /></a>
            </div>
            <div class="text-[10px] sm:text-[11px] text-gray-400 text-center md:text-right font-medium">
              <p>© 2026 知拾录. 版权所有.</p>
              <p class="mt-0.5 sm:mt-1">Designed & Developed by <span class="text-gray-600 hover:text-blue-500 cursor-pointer">Zhishilu Team</span></p>
            </div>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'HomeView'
});

import { ref, onMounted, watch, onActivated, computed } from 'vue';
import { useRoute } from 'vue-router';
import { 
  Search as SearchIcon, 
  Plus as PlusIcon, 
  Inbox as EmptyIcon, 
  ChevronLeft, 
  ChevronRight,
  ChevronDown,
  Github as GithubIcon,
  Mail as MailIcon,
  Twitter as TwitterIcon,
  X as XIcon,
  User as UserIcon
} from 'lucide-vue-next';
import request from '../utils/request';
import ArticleCard from '../components/ArticleCard.vue';
import { getAvatarUrl } from '../utils/image';

const route = useRoute();

// 状态定义
const searchQuery = ref('');
const searchField = ref('all');
const selectedCategory = ref('全部');
const categories = ref<CategoryStatResp[]>([]);
const hotWords = ref(['Vue3', 'SpringBoot', 'Elasticsearch', 'Tailwind', '设计模式', '面试题']);
const articles = ref<any[]>([]);
const loading = ref(true);
const page = ref(0);
const size = ref(24); // 淘宝风格网格通常每页展示较多
const totalPages = ref(0);
const currentSearchKeyword = ref(''); // 当前搜索关键词，用于正文高亮
const currentUserAvatar = ref('');
const currentUserName = ref('');

// 搜索补全相关
const suggestions = ref({
  usernames: [],
  locations: [],
  categories: [],
  titles: [],
  contents: []
});
const showSuggestions = ref(false);
let suggestionDebounceTimer: ReturnType<typeof setTimeout> | null = null;

// 计算是否有补全建议
const hasSuggestions = computed(() => {
  return suggestions.value.usernames?.length > 0 ||
         suggestions.value.locations?.length > 0 ||
         suggestions.value.categories?.length > 0 ||
         suggestions.value.titles?.length > 0 ||
         suggestions.value.contents?.length > 0;
});

// 类别统计响应接口
interface CategoryStatResp {
  category: string;
  count: number;
}

// 根据搜索字段获取占位符文本
const getPlaceholder = () => {
  const placeholders: Record<string, string> = {
    all: '搜知识、看干货、找灵感...',
    title: '搜索标题...',
    category: '搜索类别...',
    content: '搜索内容...',
    username: '搜索用户名...',
    location: '搜索地点...'
  };
  return placeholders[searchField.value] || '搜知识、看干货、找灵感...';
};

// 获取类别导航数据
const fetchCategoryNavigation = async () => {
  try {
    const res = await request.get('/category/navigation', {
      params: { maxCount: 20 }
    });
    if (res.data.code === 200) {
      categories.value = res.data.data;
      console.log('类别导航数据:', categories.value);
    }
  } catch (err) {
    console.error('获取类别导航失败:', err);
  }
};

// 获取数据
const fetchArticles = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: page.value,
      size: size.value
    };
    
    // 根据搜索字段设置查询参数
    if (searchQuery.value) {
      if (searchField.value === 'all') {
        params.keyword = searchQuery.value;
      } else if (searchField.value === 'title') {
        params.title = searchQuery.value;
      } else if (searchField.value === 'category') {
        params.categories = [searchQuery.value];
      } else if (searchField.value === 'content') {
        params.content = searchQuery.value;
      } else if (searchField.value === 'username') {
        params.username = searchQuery.value;
      } else if (searchField.value === 'location') {
        params.location = searchQuery.value;
      }
    }
    
    // 只有非"全部"时才传递categories参数（与搜索字段不冲突时）
    if (selectedCategory.value && selectedCategory.value !== '全部' && searchField.value !== 'category') {
      params.categories = [selectedCategory.value];
    }

    console.log('请求参数:', params);
    const res = await request.get('/article/list', { params });
    console.log('响应数据:', res.data);
    
    if (res.data.code === 200) {
      // 将搜索关键词附加到每篇文章，用于前端片段提取
      articles.value = res.data.data.list.map((item: any) => ({
        ...item,
        _searchKeyword: currentSearchKeyword.value
      }));
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
  // 保存当前搜索关键词（用于正文/地点片段高亮）
  currentSearchKeyword.value = (searchField.value === 'content' || searchField.value === 'location') ? searchQuery.value : '';
  fetchArticles();
};

const quickSearch = (word: string) => {
  searchQuery.value = word;
  handleSearch();
};

// 处理输入事件（带防抖）
const handleInput = () => {
  if (suggestionDebounceTimer) {
    clearTimeout(suggestionDebounceTimer);
  }
  
  if (!searchQuery.value || searchQuery.value.length < 1) {
    showSuggestions.value = false;
    return;
  }
  
  suggestionDebounceTimer = setTimeout(() => {
    fetchSuggestions();
  }, 300);
};

// 获取搜索补全建议
const fetchSuggestions = async () => {
  if (!searchQuery.value) return;
  
  try {
    const res = await request.get('/article/suggestions', {
      params: {
        keyword: searchQuery.value,
        field: searchField.value
      }
    });
    
    if (res.data.code === 200) {
      suggestions.value = res.data.data;
      showSuggestions.value = hasSuggestions.value;
    }
  } catch (err) {
    console.error('获取搜索建议失败:', err);
  }
};

// 应用补全建议
const applySuggestion = (text: string, field: string) => {
  searchQuery.value = text;
  searchField.value = field;
  showSuggestions.value = false;
  handleSearch();
};

// 处理输入框聚焦
const handleInputFocus = () => {
  if (hasSuggestions.value && searchQuery.value) {
    showSuggestions.value = true;
  }
};

// 处理输入框失焦
const handleInputBlur = () => {
  // 延迟关闭，让点击事件先执行
  setTimeout(() => {
    showSuggestions.value = false;
  }, 200);
};

const resetFilters = () => {
  searchQuery.value = '';
  selectedCategory.value = '全部';
  page.value = 0;
  showSuggestions.value = false;
  suggestions.value = {
    usernames: [],
    locations: [],
    categories: [],
    titles: [],
    contents: []
  };
  fetchArticles();
};

// 监听器
watch([selectedCategory, page], () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
  fetchArticles();
});

// 刷新数据的通用函数
const checkAndRefresh = (forceRefresh = false) => {
  const needRefresh = sessionStorage.getItem('homeNeedRefresh');
  console.log('HomeView checkAndRefresh, needRefresh:', needRefresh, 'forceRefresh:', forceRefresh);
  
  // 只有在有刷新标记或强制刷新时才调用接口
  if (needRefresh || forceRefresh) {
    if (needRefresh) {
      sessionStorage.removeItem('homeNeedRefresh');
      console.log('检测到刷新标记，准备刷新数据并清除查询条件');
      // 清除查询条件
      searchQuery.value = '';
      searchField.value = 'all';
      selectedCategory.value = '全部';
      page.value = 0;
      currentSearchKeyword.value = '';
    }
    fetchCategoryNavigation();
    fetchArticles();
  } else {
    console.log('无刷新标记，不调用接口');
  }
};

// 加载当前用户信息
const loadCurrentUser = () => {
  const storedUser = localStorage.getItem('user');
  if (storedUser) {
    const userData = JSON.parse(storedUser);
    currentUserName.value = userData.username || '';
    if (userData.avatar) {
      currentUserAvatar.value = getAvatarUrl(userData.avatar);
    }
  }
};

onMounted(() => {
  // 设置页面标题
  document.title = '知拾录';
  // 首次加载强制刷新
  checkAndRefresh(true);
  loadCurrentUser();
});

// 从 keep-alive 缓存中激活时检查是否需要刷新
onActivated(() => {
  console.log('HomeView onActivated');
  // 重新设置页面标题
  document.title = '知拾录';
  // 只有在有标记时才刷新，否则保持现状
  checkAndRefresh(false);
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

/* 自定义下拉框样式 */
select {
  background-image: none;
}

select option {
  padding: 8px 12px;
  font-size: 14px;
  background: white;
  color: #374151;
}

select option:hover,
select option:focus,
select option:checked {
  background: #eff6ff;
  color: #2563eb;
}

/* 移除IE的下拉箭头 */
select::-ms-expand {
  display: none;
}

/* 统一focus时的边框 */
select:focus + div input,
select:focus ~ div input {
  border-left-color: transparent;
}
</style>

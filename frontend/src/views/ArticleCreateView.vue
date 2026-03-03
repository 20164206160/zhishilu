<template>
  <div class="min-h-screen bg-[#f8fafc] flex flex-col font-sans w-full overflow-x-hidden">
    <!-- Header -->
    <header class="bg-white border-b border-gray-100 sticky top-0 z-40 w-full">
      <div class="max-w-3xl mx-auto px-2 sm:px-4 h-14 sm:h-16 flex items-center justify-between">
        <button @click="handleBack" class="flex items-center gap-1 sm:gap-2 text-gray-500 hover:text-blue-600 transition-colors">
          <ChevronLeft :size="18" class="sm:w-5 sm:h-5" />
          <span class="font-bold text-sm sm:text-base">取消</span>
        </button>
        <h1 class="text-base sm:text-lg font-black text-gray-900">发布新内容</h1>
        <div class="flex items-center gap-2 sm:gap-3">
          <span v-if="autoSaveStatus" class="text-[10px] sm:text-xs text-gray-400">{{ autoSaveStatus }}</span>
          <button
            @click="togglePreview"
            class="text-gray-500 hover:text-blue-600 transition-colors px-2 sm:px-3 py-1 text-xs sm:text-sm font-medium"
          >
            <Eye v-if="!showPreview" :size="18" class="sm:w-5 sm:h-5" />
            <EyeOff v-else :size="18" class="sm:w-5 sm:h-5" />
          </button>
          <button
            @click="handleSubmit"
            :disabled="loading"
            class="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white px-3 sm:px-6 py-1 sm:py-1.5 rounded-full text-xs sm:text-sm font-bold shadow-lg shadow-blue-100 transition-all active:scale-95"
          >
            {{ loading ? '发布中...' : '发布' }}
          </button>
        </div>
      </div>
    </header>

    <main class="max-w-3xl mx-auto w-full px-2 sm:px-4 py-4 sm:py-8">
      <!-- 编辑模式 -->
      <div v-if="!showPreview" class="bg-white rounded-2xl sm:rounded-[32px] p-4 sm:p-8 shadow-sm border border-gray-100 space-y-6 sm:space-y-8">
        <!-- Title -->
        <section class="space-y-2">
          <input
            v-model="form.title"
            type="text"
            placeholder="输入标题..."
            class="w-full text-xl sm:text-3xl font-black placeholder:text-gray-200 border-none focus:ring-0 p-0 text-gray-900"
          />
          <div class="h-px bg-gray-50 w-full"></div>
        </section>

        <!-- Category -->
        <section class="space-y-2 sm:space-y-3">
          <div class="flex items-center justify-between">
            <label class="text-xs sm:text-sm font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
              <Tag :size="12" class="sm:w-3.5 sm:h-3.5" /> 分类
            </label>
            <span class="text-[9px] sm:text-[10px] text-gray-300">必填</span>
          </div>
          <!-- Recommended Categories -->
          <div class="flex flex-wrap gap-1.5 sm:gap-2">
            <button
              v-for="cat in topCategories"
              :key="cat"
              @click="toggleCategory(cat)"
              :class="['px-2 sm:px-4 py-1 sm:py-1.5 rounded-full text-[10px] sm:text-xs font-medium transition-all border', form.categories.includes(cat) ? 'bg-blue-50 border-blue-200 text-blue-600' : 'bg-white border-gray-100 text-gray-500 hover:bg-gray-50']"
            >
              {{ cat }}
            </button>
            <div class="relative min-w-[100px] sm:min-w-[120px] flex items-center gap-1 sm:gap-2">
              <input
                v-model="customCategory"
                @keyup.enter="addCustomCategory"
                type="text"
                placeholder="手动输入分类"
                class="w-full px-2 sm:px-4 bg-gray-50 border-none rounded-full text-[10px] sm:text-xs focus:ring-2 focus:ring-blue-500 transition-all h-8 sm:h-11"
              />
              <button
                @click="addCustomCategory"
                class="px-2 sm:px-3 py-1 sm:py-1.5 bg-blue-500 text-white rounded-full text-[10px] sm:text-xs font-medium hover:bg-blue-600 transition-colors"
              >
                添加
              </button>
            </div>
          </div>
          <!-- 已选类别展示 -->
          <div v-if="form.categories.length > 0" class="flex flex-wrap gap-1.5 sm:gap-2 mt-2">
            <span class="text-[10px] sm:text-xs text-gray-400">已选：</span>
            <span
              v-for="(cat, index) in form.categories"
              :key="index"
              class="px-1.5 sm:px-2 py-0.5 bg-blue-100 text-blue-600 rounded-full text-[10px] sm:text-xs font-medium flex items-center gap-0.5 sm:gap-1"
            >
              {{ cat }}
              <button @click="removeCategory(index)" class="hover:text-blue-800">
                <X :size="8" class="sm:w-2.5 sm:h-2.5" />
              </button>
            </span>
          </div>
        </section>

        <!-- Images -->
        <section class="space-y-2 sm:space-y-3">
          <div class="flex items-center justify-between">
            <label class="text-xs sm:text-sm font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
              <ImageIcon :size="12" class="sm:w-3.5 sm:h-3.5" /> 图片
            </label>
            <span class="text-[9px] sm:text-[10px] text-gray-300">支持多张，首张为封面</span>
          </div>
          <div class="grid grid-cols-3 sm:grid-cols-4 gap-2 sm:gap-4">
            <div v-for="(img, index) in form.images" :key="index" class="aspect-square rounded-xl sm:rounded-2xl bg-gray-100 relative group overflow-hidden">
              <img :src="getImageUrl(img)" class="w-full h-full object-cover" />
              <button @click="removeImage(index)" class="absolute top-1 right-1 bg-black/50 text-white p-0.5 sm:p-1 rounded-full opacity-0 group-hover:opacity-100 transition-opacity">
                <X :size="10" class="sm:w-3 sm:h-3" />
              </button>
            </div>
            <button
              @click="triggerUpload"
              class="aspect-square rounded-xl sm:rounded-2xl border-2 border-dashed border-gray-100 hover:border-blue-200 hover:bg-blue-50 transition-all flex flex-col items-center justify-center text-gray-300 hover:text-blue-400 gap-0.5 sm:gap-1"
            >
              <Plus :size="20" class="sm:w-6 sm:h-6" />
              <span class="text-[9px] sm:text-[10px] font-bold">添加图片</span>
            </button>
          </div>
        </section>

        <!-- Content -->
        <section class="space-y-2 sm:space-y-3">
          <label class="text-xs sm:text-sm font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
            <Type :size="12" class="sm:w-3.5 sm:h-3.5" /> 正文
          </label>
          <RichEditor v-model="form.content" placeholder="记下这一刻的想法..." />
        </section>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6 pt-3 sm:pt-4 border-t border-gray-50">
          <!-- Location -->
          <section class="space-y-2 sm:space-y-3">
            <label class="text-xs sm:text-sm font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
              <MapPin :size="12" class="sm:w-3.5 sm:h-3.5" /> 地点
            </label>
            <div class="relative">
              <input
                v-model="form.location"
                type="text"
                placeholder="正在获取位置..."
                class="w-full pl-8 sm:pl-10 pr-3 sm:pr-4 py-2 sm:py-3 bg-gray-50 border-none rounded-lg sm:rounded-xl focus:ring-2 focus:ring-blue-500 transition-all text-xs sm:text-sm"
              />
              <MapPin class="absolute left-2 sm:left-3 top-1/2 -translate-y-1/2 text-blue-500" :size="14" />
            </div>
          </section>

          <!-- Source URL -->
          <section class="space-y-2 sm:space-y-3">
            <label class="text-xs sm:text-sm font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
              <LinkIcon :size="12" class="sm:w-3.5 sm:h-3.5" /> 来源网址
            </label>
            <div class="relative">
              <input
                v-model="form.url"
                type="text"
                placeholder="https://..."
                class="w-full pl-8 sm:pl-10 pr-3 sm:pr-4 py-2 sm:py-3 bg-gray-50 border-none rounded-lg sm:rounded-xl focus:ring-2 focus:ring-blue-500 transition-all text-xs sm:text-sm"
              />
              <LinkIcon class="absolute left-2 sm:left-3 top-1/2 -translate-y-1/2 text-gray-300" :size="14" />
            </div>
          </section>
        </div>
      </div>

      <!-- 预览模式 -->
      <div v-else class="bg-white rounded-2xl sm:rounded-[32px] p-4 sm:p-8 shadow-sm border border-gray-100">
        <!-- 预览标题 -->
        <h1 class="text-xl sm:text-3xl font-black text-gray-900 mb-4 sm:mb-6">{{ form.title || '无标题' }}</h1>
        
        <!-- 预览元信息 -->
        <div class="flex flex-wrap items-center gap-2 sm:gap-3 mb-4 sm:mb-6 text-xs sm:text-sm text-gray-500">
          <span v-if="form.categories.length > 0" class="flex items-center gap-1">
            <Tag :size="12" class="sm:w-3.5 sm:h-3.5" />
            {{ form.categories.join(', ') }}
          </span>
          <span v-if="form.location" class="flex items-center gap-1">
            <MapPin :size="12" class="sm:w-3.5 sm:h-3.5" />
            {{ form.location }}
          </span>
        </div>

        <!-- 预览图片 -->
        <div v-if="form.images.length > 0" class="grid grid-cols-2 sm:grid-cols-3 gap-2 sm:gap-4 mb-4 sm:mb-6">
          <div v-for="(img, index) in form.images" :key="index" class="aspect-square rounded-xl sm:rounded-2xl bg-gray-100 overflow-hidden">
            <img :src="getImageUrl(img)" class="w-full h-full object-cover" />
          </div>
        </div>

        <!-- 预览内容 -->
        <div class="prose prose-sm sm:prose max-w-none text-gray-700" v-html="form.content || '<p class=\'text-gray-400 italic\'>暂无内容</p>'"></div>

        <!-- 预览来源链接 -->
        <div v-if="form.url" class="mt-6 sm:mt-8 pt-4 border-t border-gray-100">
          <a :href="form.url" target="_blank" class="text-blue-600 hover:text-blue-700 text-xs sm:text-sm flex items-center gap-1 break-all">
            <LinkIcon :size="12" class="sm:w-3.5 sm:h-3.5 flex-shrink-0" />
            {{ form.url }}
          </a>
        </div>
      </div>
    </main>

    <!-- Hidden File Input -->
    <input type="file" ref="fileInput" class="hidden" accept="image/*" multiple @change="handleFileUpload" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { 
  ChevronLeft, Tag, Image as ImageIcon, X, Plus, Type, MapPin, Link as LinkIcon, Eye, EyeOff
} from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import request from '../utils/request';
import { getImageUrl } from '../utils/image';
import RichEditor from '../components/RichEditor.vue';

const router = useRouter();
const loading = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);
const topCategories = ref<string[]>([]);

const form = reactive({
  title: '',
  categories: [] as string[],
  images: [] as string[],
  content: '',
  url: '',
  location: ''
});

const customCategory = ref('');
const draftId = ref<string | null>(null);
const autoSaveStatus = ref('');
const autoSaveTimer = ref<number | null>(null);
const lastSavedContent = ref('');
const showPreview = ref(false);

// 判断富文本内容是否有有效文字（去除HTML标签后检查）
const hasRichTextContent = (html: string): boolean => {
  if (!html) return false;
  // 去除HTML标签
  const text = html.replace(/<[^>]+>/g, '');
  // 去除空白字符后检查
  return text.trim().length > 0;
};

// 生成表单内容的唯一标识（用于判断是否有变化）
const getFormContentHash = () => {
  return JSON.stringify({
    title: form.title,
    categories: form.categories,
    images: form.images,
    content: form.content,
    url: form.url,
    location: form.location
  });
};

// 切换类别选择
const toggleCategory = (cat: string) => {
  const index = form.categories.indexOf(cat);
  if (index > -1) {
    form.categories.splice(index, 1);
  } else {
    form.categories.push(cat);
  }
};

// 添加自定义类别
const addCustomCategory = () => {
  if (customCategory.value.trim()) {
    const cat = customCategory.value.trim();
    if (!form.categories.includes(cat)) {
      form.categories.push(cat);
    }
    customCategory.value = '';
  }
};

// 移除类别
const removeCategory = (index: number) => {
  form.categories.splice(index, 1);
};

// 保存草稿（同步方式，用于页面离开前）
const saveDraftSync = async () => {
  const currentContent = getFormContentHash();
  if (currentContent === lastSavedContent.value) {
    return; // 内容未变化，跳过保存
  }
  
  const hasContent = form.title || hasRichTextContent(form.content) || form.images.length > 0 || form.categories.length > 0;
  if (!hasContent) {
    return; // 没有有效内容，跳过保存
  }

  try {
    const draftData = {
      id: draftId.value,
      title: form.title,
      categories: form.categories,
      images: form.images,
      content: form.content,
      url: form.url,
      location: form.location
    };

    const res = await request.post('/article/draft', draftData);
    if (res.data.code === 200) {
      draftId.value = res.data.data.id;
      lastSavedContent.value = currentContent;
    }
  } catch (err) {
    console.error('退出保存草稿失败:', err);
  }
};

const handleBack = async () => {
  // 先保存最新内容
  await saveDraftSync();
  router.back();
};

const triggerUpload = () => {
  fileInput.value?.click();
};

const handleFileUpload = async (e: Event) => {
  const files = (e.target as HTMLInputElement).files;
  if (!files) return;
  
  // 立即上传每个文件到服务器
  for (const file of Array.from(files)) {
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      const res = await request.post('/file/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      if (res.data.code === 200 && res.data.data.path) {
        // 保存服务器返回的相对路径
        form.images.push(res.data.data.path);
      }
    } catch (err) {
      console.error('Upload error:', err);
      alert('图片上传失败');
    }
  }
  
  // 清空文件输入框，允许重复选择同一文件
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

const removeImage = (index: number) => {
  form.images.splice(index, 1);
};

// 通过浏览器 Geolocation API 获取位置
// 注意：浏览器只能获取经纬度，逆地理编码需要调用后端服务或第三方 API
// 由于跨域限制，这里浏览器定位仅作为标记，实际地址使用 IP 定位获取
const fetchLocationByBrowser = (): Promise<string | null> => {
  return new Promise((resolve) => {
    // 浏览器定位在移动端需要用户授权，且第三方逆地理编码 API 有跨域限制
    // 因此优先使用 IP 定位，更稳定可靠
    console.log('浏览器定位需要逆地理编码服务，优先使用 IP 定位');
    resolve(null);
  });
};

// 通过后端 IP 获取位置
const fetchLocationByIP = async (): Promise<string | null> => {
  try {
    const res = await request.get('/article/location');
    if (res.data.code === 200 && res.data.data) {
      return res.data.data;
    }
    return null;
  } catch (err) {
    console.error('IP 定位失败:', err);
    return null;
  }
};

// 自动获取位置：优先浏览器定位，失败则使用 IP 定位
const fetchLocation = async () => {
  try {
    // 1. 先尝试浏览器定位
    const browserLocation = await fetchLocationByBrowser();
    if (browserLocation) {
      form.location = browserLocation;
      return;
    }

    // 2. 浏览器定位失败，使用 IP 定位
    const ipLocation = await fetchLocationByIP();
    if (ipLocation) {
      form.location = ipLocation;
      return;
    }

    // 3. 都失败，提示用户手动输入
    form.location = '';
    console.log('无法自动获取位置，请手动输入');
  } catch (err) {
    console.error('Location error:', err);
    form.location = '';
  }
};

// 自动保存草稿
const autoSaveDraft = async () => {
  // 检查内容是否有变化
  const currentContent = getFormContentHash();
  if (currentContent === lastSavedContent.value) {
    return; // 内容未变化，跳过保存
  }
  
  // 检查是否有有效内容（至少有一个字段不为空）
  const hasContent = form.title || hasRichTextContent(form.content) || form.images.length > 0 || form.categories.length > 0;
  if (!hasContent) {
    return; // 没有有效内容，跳过保存
  }

  try {
    autoSaveStatus.value = '保存中...';
    const draftData = {
      id: draftId.value,
      title: form.title,
      categories: form.categories,
      images: form.images,
      content: form.content,
      url: form.url,
      location: form.location
    };
    
    const res = await request.post('/article/draft', draftData);
    if (res.data.code === 200) {
      draftId.value = res.data.data.id;
      lastSavedContent.value = currentContent;
      autoSaveStatus.value = '已保存';
      // 2秒后清除状态提示
      setTimeout(() => {
        if (autoSaveStatus.value === '已保存') {
          autoSaveStatus.value = '';
        }
      }, 2000);
    }
  } catch (err) {
    console.error('自动保存失败:', err);
    autoSaveStatus.value = '保存失败';
  }
};

// 启动自动保存定时器
const startAutoSave = () => {
  if (autoSaveTimer.value) {
    clearInterval(autoSaveTimer.value);
  }
  autoSaveTimer.value = window.setInterval(() => {
    autoSaveDraft();
  }, 10000); // 每10秒自动保存
};

// 停止自动保存
const stopAutoSave = () => {
  if (autoSaveTimer.value) {
    clearInterval(autoSaveTimer.value);
    autoSaveTimer.value = null;
  }
};

const handleSubmit = async () => {
  if (!form.title || form.categories.length === 0 || !form.location) {
    alert('请填写标题、选择分类和地点');
    return;
  }
  
  if (form.images.length === 0 && !hasRichTextContent(form.content)) {
    alert('图片和正文必须填写其中一个');
    return;
  }

  loading.value = true;
  try {
    // 如果有草稿ID，使用发布草稿接口
    if (draftId.value) {
      const res = await request.post(`/article/draft/${draftId.value}/publish`, form);
      if (res.data.code === 200) {
        alert('发布成功！');
        sessionStorage.setItem('homeNeedRefresh', 'true');
        stopAutoSave();
        router.push('/');
      }
    } else {
      const res = await request.post('/article', form);
      if (res.data.code === 200) {
        alert('发布成功！');
        sessionStorage.setItem('homeNeedRefresh', 'true');
        stopAutoSave();
        router.push('/');
      }
    }
  } catch (err: any) {
    alert(err.response?.data?.message || '发布失败');
  } finally {
    loading.value = false;
  }
};

// 切换预览模式
const togglePreview = () => {
  showPreview.value = !showPreview.value;
};

// 获取分类导航数据
const fetchCategoryNavigation = async () => {
  try {
    const res = await request.get('/category/navigation', {
      params: { maxCount: 20 }
    });
    if (res.data.code === 200) {
      topCategories.value = res.data.data.map((item: { category: string }) => item.category);
    }
  } catch (err) {
    console.error('获取分类导航失败:', err);
  }
};

onMounted(async () => {
  fetchLocation();
  startAutoSave();
  fetchCategoryNavigation();
});

onUnmounted(async () => {
  stopAutoSave();
  // 页面离开时保存最新内容
  await saveDraftSync();
});
</script>

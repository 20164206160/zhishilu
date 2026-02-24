<template>
  <div class="min-h-screen bg-[#f8fafc] flex flex-col font-sans">
    <!-- Header -->
    <header class="bg-white border-b border-gray-100 sticky top-0 z-40">
      <div class="max-w-3xl mx-auto px-4 h-16 flex items-center justify-between">
        <button @click="handleBack" class="flex items-center gap-2 text-gray-500 hover:text-blue-600 transition-colors">
          <ChevronLeft :size="20" />
          <span class="font-bold">取消</span>
        </button>
        <h1 class="text-lg font-black text-gray-900">编辑草稿</h1>
        <div class="flex items-center gap-3">
          <span v-if="autoSaveStatus" class="text-xs text-gray-400">{{ autoSaveStatus }}</span>
          <button 
            @click="handlePublish"
            :disabled="loading"
            class="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white px-6 py-1.5 rounded-full text-sm font-bold shadow-lg shadow-blue-100 transition-all active:scale-95"
          >
            {{ loading ? '发布中...' : '发布' }}
          </button>
        </div>
      </div>
    </header>

    <main class="max-w-3xl mx-auto w-full px-4 py-8">
      <div v-if="loading && !form.title" class="flex items-center justify-center py-20">
        <div class="animate-spin rounded-full h-8 w-8 border-4 border-blue-600 border-t-transparent"></div>
      </div>
      
      <div v-else class="bg-white rounded-[32px] p-8 shadow-sm border border-gray-100 space-y-8">
        <!-- Title -->
        <section class="space-y-2">
          <input 
            v-model="form.title"
            type="text" 
            placeholder="输入标题..." 
            class="w-full text-3xl font-black placeholder:text-gray-200 border-none focus:ring-0 p-0 text-gray-900"
          />
          <div class="h-px bg-gray-50 w-full"></div>
        </section>

        <!-- Category -->
        <section class="space-y-3">
          <div class="flex items-center justify-between">
            <label class="text-sm font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
              <Tag :size="14" /> 分类
            </label>
            <span class="text-[10px] text-gray-300">必填</span>
          </div>
          <!-- Recommended Categories -->
          <div class="flex flex-wrap gap-2">
            <button 
              v-for="cat in topCategories" 
              :key="cat"
              @click="toggleCategory(cat)"
              :class="['px-4 py-1.5 rounded-full text-xs font-medium transition-all border', form.categories.includes(cat) ? 'bg-blue-50 border-blue-200 text-blue-600' : 'bg-white border-gray-100 text-gray-500 hover:bg-gray-50']"
            >
              {{ cat }}
            </button>
            <div class="relative min-w-[120px] flex items-center gap-2">
              <input 
                v-model="customCategory"
                @keyup.enter="addCustomCategory"
                type="text" 
                placeholder="手动输入分类" 
                class="w-full px-4 py-1.5 bg-gray-50 border-none rounded-full text-xs focus:ring-2 focus:ring-blue-500 transition-all"
              />
              <button 
                @click="addCustomCategory"
                class="px-3 py-1.5 bg-blue-500 text-white rounded-full text-xs font-medium hover:bg-blue-600 transition-colors"
              >
                添加
              </button>
            </div>
          </div>
          <!-- 已选类别展示 -->
          <div v-if="form.categories.length > 0" class="flex flex-wrap gap-2 mt-2">
            <span class="text-xs text-gray-400">已选：</span>
            <span 
              v-for="(cat, index) in form.categories" 
              :key="index"
              class="px-2 py-0.5 bg-blue-100 text-blue-600 rounded-full text-xs font-medium flex items-center gap-1"
            >
              {{ cat }}
              <button @click="removeCategory(index)" class="hover:text-blue-800">
                <X :size="10" />
              </button>
            </span>
          </div>
        </section>

        <!-- Images -->
        <section class="space-y-3">
          <div class="flex items-center justify-between">
            <label class="text-sm font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
              <ImageIcon :size="14" /> 图片
            </label>
            <span class="text-[10px] text-gray-300">支持多张，首张为封面</span>
          </div>
          <div class="grid grid-cols-3 sm:grid-cols-4 gap-4">
            <div v-for="(img, index) in form.images" :key="index" class="aspect-square rounded-2xl bg-gray-100 relative group overflow-hidden">
              <img :src="getImageUrl(img)" class="w-full h-full object-cover" />
              <button @click="removeImage(index)" class="absolute top-1 right-1 bg-black/50 text-white p-1 rounded-full opacity-0 group-hover:opacity-100 transition-opacity">
                <X :size="12" />
              </button>
            </div>
            <button 
              @click="triggerUpload"
              class="aspect-square rounded-2xl border-2 border-dashed border-gray-100 hover:border-blue-200 hover:bg-blue-50 transition-all flex flex-col items-center justify-center text-gray-300 hover:text-blue-400 gap-1"
            >
              <Plus :size="24" />
              <span class="text-[10px] font-bold">添加图片</span>
            </button>
          </div>
        </section>

        <!-- Content -->
        <section class="space-y-3">
          <label class="text-sm font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
            <Type :size="14" /> 正文
          </label>
          <textarea 
            v-model="form.content"
            rows="8"
            placeholder="记下这一刻的想法..."
            class="w-full p-4 bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 transition-all text-sm resize-none"
          ></textarea>
        </section>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 pt-4 border-t border-gray-50">
          <!-- Location -->
          <section class="space-y-3">
            <label class="text-sm font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
              <MapPin :size="14" /> 地点
            </label>
            <div class="relative">
              <input 
                v-model="form.location"
                type="text" 
                placeholder="输入地点..." 
                class="w-full pl-10 pr-4 py-3 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-500 transition-all text-sm"
              />
              <MapPin class="absolute left-3 top-1/2 -translate-y-1/2 text-blue-500" :size="16" />
            </div>
          </section>

          <!-- Source URL -->
          <section class="space-y-3">
            <label class="text-sm font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
              <LinkIcon :size="14" /> 来源网址
            </label>
            <div class="relative">
              <input 
                v-model="form.url"
                type="text" 
                placeholder="https://..." 
                class="w-full pl-10 pr-4 py-3 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-500 transition-all text-sm"
              />
              <LinkIcon class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-300" :size="16" />
            </div>
          </section>
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
  ChevronLeft, Tag, Image as ImageIcon, X, Plus, Type, MapPin, Link as LinkIcon 
} from 'lucide-vue-next';
import { useRoute, useRouter } from 'vue-router';
import request from '../utils/request';
import { getImageUrl } from '../utils/image';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);
const topCategories = ref(['技术', '生活', '工作', '读书', '笔记']);
const draftId = ref('');

const form = reactive({
  title: '',
  categories: [] as string[],
  images: [] as string[],
  content: '',
  url: '',
  location: ''
});

const customCategory = ref('');
const autoSaveStatus = ref('');
const autoSaveTimer = ref<number | null>(null);
const lastSavedContent = ref('');

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
  
  const hasContent = form.title || form.content || form.images.length > 0 || form.categories.length > 0 || form.location;
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
      lastSavedContent.value = currentContent;
    }
  } catch (err) {
    console.error('退出保存草稿失败:', err);
  }
};

const handleBack = async () => {
  // 先保存最新内容
  await saveDraftSync();
  // 返回个人中心时带上from参数，让导航栏显示草稿箱tab
  const fromTab = route.query.from as string;
  if (fromTab) {
    router.push({ path: '/profile', query: { from: fromTab } });
  } else {
    router.back();
  }
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

const loadDraft = async () => {
  loading.value = true;
  try {
    const res = await request.get(`/article/draft/${draftId.value}`);
    if (res.data.code === 200) {
      const draft = res.data.data;
      form.title = draft.title || '';
      form.categories = draft.categories || [];
      form.images = draft.images || [];
      form.content = draft.content || '';
      form.url = draft.url || '';
      form.location = draft.location || '';
      // 记录初始内容hash
      lastSavedContent.value = getFormContentHash();
    } else {
      alert('加载草稿失败');
      router.back();
    }
  } catch (err) {
    console.error('Load draft error:', err);
    alert('加载草稿失败');
    router.back();
  } finally {
    loading.value = false;
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
  const hasContent = form.title || form.content || form.images.length > 0 || form.categories.length > 0 || form.location;
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

const handlePublish = async () => {
  if (!form.title || form.categories.length === 0 || !form.location) {
    alert('请填写标题、选择分类和地点');
    return;
  }
  
  if (form.images.length === 0 && !form.content) {
    alert('图片和正文必须填写其中一个');
    return;
  }

  loading.value = true;
  try {
    const res = await request.post(`/article/draft/${draftId.value}/publish`, form);
    if (res.data.code === 200) {
      alert('发布成功！');
      sessionStorage.setItem('homeNeedRefresh', 'true');
      stopAutoSave();
      router.push('/');
    }
  } catch (err: any) {
    alert(err.response?.data?.message || '发布失败');
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  draftId.value = route.params.id as string;
  if (!draftId.value) {
    alert('草稿ID不存在');
    router.back();
    return;
  }
  await loadDraft();
  startAutoSave();
});

onUnmounted(async () => {
  stopAutoSave();
  // 页面离开时保存最新内容
  await saveDraftSync();
});
</script>

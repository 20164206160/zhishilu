<template>
  <div class="h-full flex flex-col overflow-hidden">
    <!-- Header -->
    <div class="px-4 sm:px-6 py-3 sm:py-4 border-b border-gray-100 flex items-center justify-between shrink-0">
      <button @click="onCancel" class="flex items-center gap-1 sm:gap-2 text-gray-500 hover:text-blue-600 transition-colors">
        <ChevronLeft :size="18" class="sm:w-5 sm:h-5" />
        <span class="font-bold text-sm sm:text-base">取消</span>
      </button>
      <h1 class="text-base sm:text-lg font-black text-gray-900">编辑内容</h1>
      <div class="flex items-center gap-2">
        <button @click="togglePreview" class="text-gray-500 hover:text-blue-600 transition-colors px-2 py-1">
          <Eye v-if="!showPreview" :size="18" />
          <EyeOff v-else :size="18" />
        </button>
        <button @click="handleSubmit" :disabled="loading"
                class="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white px-4 py-1.5 rounded-full text-xs sm:text-sm font-bold shadow-lg shadow-blue-100 transition-all">
          {{ loading ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>

    <!-- Content -->
    <div class="flex-grow overflow-y-auto p-4 sm:p-6">
      <!-- Loading -->
      <div v-if="loading && !form.title" class="flex items-center justify-center py-20">
        <div class="animate-spin rounded-full h-8 w-8 border-4 border-blue-600 border-t-transparent"></div>
      </div>

      <!-- Edit Mode -->
      <div v-else-if="!showPreview" class="space-y-6">
        <!-- Title -->
        <section class="space-y-2">
          <input v-model="form.title" type="text" placeholder="输入标题..."
                 class="w-full text-2xl sm:text-3xl font-black placeholder:text-gray-200 border-none focus:ring-0 p-0 text-gray-900" />
          <div class="h-px bg-gray-100 w-full"></div>
        </section>

        <!-- Category -->
        <section class="space-y-3">
          <div class="flex items-center justify-between">
            <label class="text-xs sm:text-sm font-bold text-gray-400 uppercase flex items-center gap-2">
              <Tag :size="14" /> 分类 <span class="text-red-400">*</span>
            </label>
          </div>
          <div class="flex flex-wrap gap-2">
            <button v-for="cat in topCategories" :key="cat" @click="toggleCategory(cat)"
                    :class="['px-3 py-1.5 rounded-full text-xs font-medium transition-all border', 
                             form.categories.includes(cat) ? 'bg-blue-50 border-blue-200 text-blue-600' : 'bg-white border-gray-100 text-gray-500 hover:bg-gray-50']">
              {{ cat }}
            </button>
            <div class="relative flex items-center gap-2">
              <input v-model="customCategory" @keyup.enter="addCustomCategory" type="text" placeholder="手动输入"
                     class="w-24 sm:w-32 px-3 bg-gray-50 border-none rounded-full text-xs focus:ring-2 focus:ring-blue-500 h-9" />
              <button @click="addCustomCategory" class="px-2 py-1.5 bg-blue-500 text-white rounded-full text-xs hover:bg-blue-600">+</button>
            </div>
          </div>
          <div v-if="form.categories.length > 0" class="flex flex-wrap gap-2">
            <span class="text-xs text-gray-400">已选：</span>
            <span v-for="(cat, idx) in form.categories" :key="idx"
                  class="px-2 py-0.5 bg-blue-100 text-blue-600 rounded-full text-xs flex items-center gap-1">
              {{ cat }}
              <button @click="removeCategory(idx)" class="hover:text-blue-800"><X :size="10" /></button>
            </span>
          </div>
        </section>

        <!-- Images -->
        <section class="space-y-3">
          <label class="text-xs sm:text-sm font-bold text-gray-400 uppercase flex items-center gap-2">
            <ImageIcon :size="14" /> 图片
          </label>
          <div class="grid grid-cols-4 sm:grid-cols-5 gap-3">
            <div v-for="(img, idx) in form.images" :key="idx" class="aspect-square rounded-xl bg-gray-100 relative group overflow-hidden">
              <img :src="getImageUrl(img)" class="w-full h-full object-cover" />
              <button @click="removeImage(idx)" class="absolute top-1 right-1 bg-black/50 text-white p-1 rounded-full opacity-0 group-hover:opacity-100">
                <X :size="12" />
              </button>
            </div>
            <button @click="triggerUpload"
                    class="aspect-square rounded-xl border-2 border-dashed border-gray-200 hover:border-blue-300 hover:bg-blue-50 transition-all flex flex-col items-center justify-center text-gray-300 hover:text-blue-400">
              <Plus :size="20" />
              <span class="text-[10px]">添加</span>
            </button>
          </div>
        </section>

        <!-- Content -->
        <section class="space-y-3">
          <label class="text-xs sm:text-sm font-bold text-gray-400 uppercase flex items-center gap-2">
            <Type :size="14" /> 正文
          </label>
          <RichEditor v-model="form.content" placeholder="记下这一刻的想法..." />
        </section>

        <!-- Location & URL -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-4 border-t border-gray-100">
          <section class="space-y-2">
            <label class="text-xs font-bold text-gray-400 uppercase flex items-center gap-2">
              <MapPin :size="12" /> 地点 <span class="text-red-400">*</span>
            </label>
            <div class="relative">
              <input v-model="form.location" type="text" placeholder="输入地点..."
                     class="w-full pl-9 pr-3 py-2.5 bg-gray-50 border-none rounded-xl text-sm focus:ring-2 focus:ring-blue-500" />
              <MapPin class="absolute left-3 top-1/2 -translate-y-1/2 text-blue-500" :size="14" />
            </div>
          </section>
          <section class="space-y-2">
            <label class="text-xs font-bold text-gray-400 uppercase flex items-center gap-2">
              <LinkIcon :size="12" /> 来源网址
            </label>
            <div class="relative">
              <input v-model="form.url" type="text" placeholder="https://..."
                     class="w-full pl-9 pr-3 py-2.5 bg-gray-50 border-none rounded-xl text-sm focus:ring-2 focus:ring-blue-500" />
              <LinkIcon class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-300" :size="14" />
            </div>
          </section>
        </div>
      </div>

      <!-- Preview Mode -->
      <div v-else class="space-y-4">
        <h1 class="text-xl sm:text-2xl font-black text-gray-900">{{ form.title || '无标题' }}</h1>
        <div class="flex flex-wrap items-center gap-2 text-xs text-gray-500">
          <span v-if="form.categories.length" class="flex items-center gap-1">
            <Tag :size="12" /> {{ form.categories.join(', ') }}
          </span>
          <span v-if="form.location" class="flex items-center gap-1">
            <MapPin :size="12" /> {{ form.location }}
          </span>
        </div>
        <div v-if="form.images.length" class="grid grid-cols-2 sm:grid-cols-3 gap-2">
          <div v-for="(img, idx) in form.images" :key="idx" class="aspect-square rounded-xl bg-gray-100 overflow-hidden">
            <img :src="getImageUrl(img)" class="w-full h-full object-cover" />
          </div>
        </div>
        <div class="prose prose-sm max-w-none text-gray-700" v-html="form.content || '<p class=\'text-gray-400 italic\'>暂无内容</p>'"></div>
        <div v-if="form.url" class="pt-4 border-t border-gray-100">
          <a :href="form.url" target="_blank" class="text-blue-600 text-xs flex items-center gap-1 break-all">
            <LinkIcon :size="12" /> {{ form.url }}
          </a>
        </div>
      </div>
    </div>

    <!-- Hidden File Input -->
    <input type="file" ref="fileInput" class="hidden" accept="image/*" multiple @change="handleFileUpload" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ChevronLeft, Tag, Image as ImageIcon, X, Plus, Type, MapPin, Link as LinkIcon, Eye, EyeOff } from 'lucide-vue-next';
import request from '@/utils/request';
import { getImageUrl } from '@/utils/image';
import RichEditor from '@/components/RichEditor.vue';

interface Article {
  id: string;
  title: string;
  content?: string;
  images?: string[];
  categories?: string[];
  location?: string;
  url?: string;
}

const props = defineProps<{
  article: Article;
}>();

const emit = defineEmits<{
  cancel: [];
  saved: [article: Article];
}>();

const loading = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);
const topCategories = ref<string[]>([]);
const showPreview = ref(false);
const customCategory = ref('');

const form = reactive({
  title: '',
  categories: [] as string[],
  images: [] as string[],
  content: '',
  url: '',
  location: ''
});

onMounted(async () => {
  // Initialize form with article data
  form.title = props.article.title || '';
  form.categories = props.article.categories || [];
  form.images = props.article.images || [];
  form.content = props.article.content || '';
  form.url = props.article.url || '';
  form.location = props.article.location || '';
  await fetchCategoryNavigation();
});

const toggleCategory = (cat: string) => {
  const idx = form.categories.indexOf(cat);
  if (idx > -1) form.categories.splice(idx, 1);
  else form.categories.push(cat);
};

const addCustomCategory = () => {
  const cat = customCategory.value.trim();
  if (cat && !form.categories.includes(cat)) {
    form.categories.push(cat);
  }
  customCategory.value = '';
};

const removeCategory = (idx: number) => {
  form.categories.splice(idx, 1);
};

const triggerUpload = () => fileInput.value?.click();

const handleFileUpload = async (e: Event) => {
  const files = (e.target as HTMLInputElement).files;
  if (!files) return;
  for (const file of Array.from(files)) {
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await request.post('/file/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      if (res.data.code === 200 && res.data.data.path) {
        form.images.push(res.data.data.path);
      }
    } catch (err) {
      console.error('Upload error:', err);
      alert('图片上传失败');
    }
  }
  if (fileInput.value) fileInput.value.value = '';
};

const removeImage = (idx: number) => {
  form.images.splice(idx, 1);
};

const handleSubmit = async () => {
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
    const res = await request.put(`/article/update/${props.article.id}`, form);
    if (res.data.code === 200) {
      alert('保存成功！');
      sessionStorage.setItem('homeNeedRefresh', 'true');
      emit('saved', { ...props.article, ...form });
    }
  } catch (err: any) {
    alert(err.response?.data?.message || '保存失败');
  } finally {
    loading.value = false;
  }
};

const togglePreview = () => {
  showPreview.value = !showPreview.value;
};

const onCancel = () => emit('cancel');

const fetchCategoryNavigation = async () => {
  try {
    const res = await request.get('/category/navigation', { params: { maxCount: 20 } });
    if (res.data.code === 200) {
      topCategories.value = res.data.data.map((item: { category: string }) => item.category);
    }
  } catch (err) {
    console.error('获取分类导航失败:', err);
  }
};
</script>

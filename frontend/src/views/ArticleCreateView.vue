<template>
  <div class="min-h-screen bg-[#f8fafc] flex flex-col font-sans">
    <!-- Header -->
    <header class="bg-white border-b border-gray-100 sticky top-0 z-40">
      <div class="max-w-3xl mx-auto px-4 h-16 flex items-center justify-between">
        <button @click="handleBack" class="flex items-center gap-2 text-gray-500 hover:text-blue-600 transition-colors">
          <ChevronLeft :size="20" />
          <span class="font-bold">取消</span>
        </button>
        <h1 class="text-lg font-black text-gray-900">发布新内容</h1>
        <button 
          @click="handleSubmit"
          :disabled="loading"
          class="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white px-6 py-1.5 rounded-full text-sm font-bold shadow-lg shadow-blue-100 transition-all active:scale-95"
        >
          {{ loading ? '发布中...' : '发布' }}
        </button>
      </div>
    </header>

    <main class="max-w-3xl mx-auto w-full px-4 py-8">
      <div class="bg-white rounded-[32px] p-8 shadow-sm border border-gray-100 space-y-8">
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
              @click="form.category = cat"
              :class="['px-4 py-1.5 rounded-full text-xs font-medium transition-all border', form.category === cat ? 'bg-blue-50 border-blue-200 text-blue-600' : 'bg-white border-gray-100 text-gray-500 hover:bg-gray-50']"
            >
              {{ cat }}
            </button>
            <div class="relative min-w-[120px]">
              <input 
                v-model="form.category"
                type="text" 
                placeholder="手动输入分类" 
                class="w-full px-4 py-1.5 bg-gray-50 border-none rounded-full text-xs focus:ring-2 focus:ring-blue-500 transition-all"
              />
            </div>
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
                placeholder="正在获取位置..." 
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
import { ref, reactive, onMounted } from 'vue';
import { 
  ChevronLeft, Tag, Image as ImageIcon, X, Plus, Type, MapPin, Link as LinkIcon 
} from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import request from '../utils/request';
import { getImageUrl } from '../utils/image';

const router = useRouter();
const loading = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);
const topCategories = ref(['技术', '生活', '工作', '读书', '笔记']);

const form = reactive({
  title: '',
  category: '',
  images: [] as string[],
  content: '',
  url: '',
  location: ''
});

const handleBack = () => {
  if (form.title || form.content || form.images.length > 0) {
    if (confirm('内容尚未保存，确定退出吗？')) router.back();
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

// 自动获取位置 (模拟根据IP)
const fetchLocation = async () => {
  try {
    // 实际项目中可以调用 ip-api 等服务
    form.location = '上海市 浦东新区';
  } catch (err) {
    console.error('Location error:', err);
  }
};

const handleSubmit = async () => {
  if (!form.title || !form.category || !form.location) {
    alert('请填写标题、分类和地点');
    return;
  }
  
  if (form.images.length === 0 && !form.content) {
    alert('图片和正文必须填写其中一个');
    return;
  }

  loading.value = true;
  try {
    const res = await request.post('/article', form);
    if (res.data.code === 200) {
      alert('发布成功！');
      router.push('/');
    }
  } catch (err: any) {
    alert(err.response?.data?.message || '发布失败');
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  fetchLocation();
  // TODO: 获取用户常用分类
  // const res = await axios.get('/api/article/categories/top');
  // if(res.data.code === 200) topCategories.value = res.data.data;
});
</script>

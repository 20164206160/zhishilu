<template>
  <div class="min-h-screen bg-[#f8fafc] flex flex-col font-sans">
    <!-- Header -->
    <header class="bg-white border-b border-gray-100 sticky top-0 z-40">
      <div class="max-w-5xl mx-auto px-4 h-16 flex items-center justify-between">
        <router-link to="/" class="flex items-center gap-2 text-blue-600 hover:opacity-80 transition-opacity">
          <ChevronLeft :size="20" />
          <span class="font-bold">返回首页</span>
        </router-link>
        <h1 class="text-lg font-black text-gray-900">个人中心</h1>
        <div class="w-20"></div> <!-- Placeholder for balance -->
      </div>
    </header>

    <main class="max-w-5xl mx-auto w-full px-4 py-8">
      <div class="flex flex-col md:flex-row gap-8">
        <!-- Sidebar Navigation -->
        <aside class="w-full md:w-64 space-y-2">
          <button 
            @click="activeTab = 'profile'"
            :class="['w-full flex items-center gap-3 px-4 py-3 rounded-2xl text-sm font-bold transition-all', activeTab === 'profile' ? 'bg-blue-600 text-white shadow-lg shadow-blue-200' : 'bg-white text-gray-500 hover:bg-gray-50 border border-gray-100']"
          >
            <UserIcon :size="18" /> 账号设置
          </button>
          <button 
            @click="activeTab = 'content'"
            :class="['w-full flex items-center gap-3 px-4 py-3 rounded-2xl text-sm font-bold transition-all', activeTab === 'content' ? 'bg-blue-600 text-white shadow-lg shadow-blue-200' : 'bg-white text-gray-500 hover:bg-gray-50 border border-gray-100']"
          >
            <BookOpen :size="18" /> 内容管理
          </button>
          <button 
            @click="handleLogout"
            class="w-full flex items-center gap-3 px-4 py-3 rounded-2xl text-sm font-bold text-red-500 bg-white hover:bg-red-50 border border-gray-100 transition-all"
          >
            <LogOut :size="18" /> 退出登录
          </button>
        </aside>

        <!-- Content Area -->
        <div class="flex-grow">
          <!-- Account Settings Tab -->
          <div v-if="activeTab === 'profile'" class="bg-white rounded-[32px] p-8 shadow-sm border border-gray-100 space-y-10">
            <!-- Avatar Section -->
            <section class="space-y-4">
              <h2 class="text-xl font-black text-gray-900 flex items-center gap-2">
                <div class="w-1.5 h-6 bg-blue-600 rounded-full"></div> 个人资料
              </h2>
              <div class="flex items-center gap-6">
                <div class="relative group cursor-pointer" @click="triggerAvatarUpload">
                  <div class="w-24 h-24 rounded-[32px] bg-blue-50 flex items-center justify-center overflow-hidden border-4 border-white shadow-inner">
                    <img v-if="user.avatar" :src="user.avatar" class="w-full h-full object-cover" />
                    <UserIcon v-else :size="40" class="text-blue-300" />
                  </div>
                  <div class="absolute inset-0 bg-black/40 rounded-[32px] flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                    <Camera :size="20" class="text-white" />
                  </div>
                </div>
                <div class="space-y-1">
                  <p class="text-lg font-bold text-gray-900">{{ user.username }}</p>
                  <p class="text-sm text-gray-400">{{ user.email || '未设置邮箱' }}</p>
                  <button @click="triggerAvatarUpload" class="text-xs font-bold text-blue-600 hover:underline">点击更换头像</button>
                </div>
              </div>
            </section>

            <!-- Password Section -->
            <section class="space-y-6 pt-6 border-t border-gray-50">
              <h2 class="text-xl font-black text-gray-900 flex items-center gap-2">
                <div class="w-1.5 h-6 bg-emerald-500 rounded-full"></div> 修改密码
              </h2>
              <div class="grid grid-cols-1 gap-4 max-w-md">
                <div class="space-y-1.5">
                  <label class="text-xs font-bold text-gray-400 uppercase ml-1">当前密码</label>
                  <input v-model="pwdForm.oldPassword" type="password" class="w-full px-4 py-3 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-500 transition-all text-sm" placeholder="请输入当前密码" />
                </div>
                <div class="space-y-1.5">
                  <label class="text-xs font-bold text-gray-400 uppercase ml-1">新密码</label>
                  <input v-model="pwdForm.newPassword" type="password" class="w-full px-4 py-3 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-500 transition-all text-sm" placeholder="至少 6 位字符" />
                </div>
                <div class="space-y-1.5">
                  <label class="text-xs font-bold text-gray-400 uppercase ml-1">确认新密码</label>
                  <input v-model="pwdForm.confirmPassword" type="password" class="w-full px-4 py-3 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-500 transition-all text-sm" placeholder="请再次输入新密码" />
                </div>
                <button 
                  @click="confirmUpdatePwd"
                  class="mt-4 bg-gray-900 hover:bg-black text-white py-3 rounded-xl text-sm font-bold shadow-lg shadow-gray-200 transition-all active:scale-95"
                >
                  更新密码
                </button>
              </div>
            </section>
          </div>

          <!-- Content Management Tab -->
          <div v-if="activeTab === 'content'" class="space-y-4">
            <div class="flex items-center justify-between mb-2">
              <h2 class="text-2xl font-black text-gray-900">我的发布</h2>
              <span class="text-xs font-bold text-gray-400 bg-white px-3 py-1 rounded-full border border-gray-100">共 {{ myArticles.length }} 篇</span>
            </div>

            <div v-if="loading" class="space-y-4">
              <div v-for="i in 3" :key="i" class="bg-white p-4 rounded-3xl animate-pulse flex gap-4 h-32 border border-gray-50"></div>
            </div>

            <div v-else-if="myArticles.length > 0" class="grid grid-cols-1 gap-4">
              <div v-for="item in myArticles" :key="item.id" class="bg-white p-4 rounded-3xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow group">
                <div class="flex gap-4">
                  <div @click="router.push(`/article/${item.id}`)" class="w-24 h-24 rounded-2xl bg-gray-50 overflow-hidden flex-shrink-0 cursor-pointer">
                    <img v-if="item.images?.length" :src="getImageUrl(item.images[0])" class="w-full h-full object-cover" />
                    <div v-else class="w-full h-full flex items-center justify-center text-gray-200">
                      <ImageIcon :size="24" />
                    </div>
                  </div>
                  <div class="flex-grow py-1 flex flex-col justify-between">
                    <div @click="router.push(`/article/${item.id}`)" class="cursor-pointer">
                      <span class="text-[10px] font-bold text-blue-500 bg-blue-50 px-2 py-0.5 rounded-md uppercase tracking-wider mb-1 inline-block">{{ item.category }}</span>
                      <h3 class="text-base font-bold text-gray-900 line-clamp-1 group-hover:text-blue-600 transition-colors">{{ item.title }}</h3>
                      <p class="text-xs text-gray-400 mt-1">{{ formatDate(item.createdTime) }} 发布</p>
                    </div>
                    <div class="flex gap-2">
                      <button 
                        @click="handleEdit(item)"
                        class="px-4 py-1.5 bg-gray-50 hover:bg-blue-50 text-gray-600 hover:text-blue-600 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5"
                      >
                        <Edit2 :size="14" /> 编辑
                      </button>
                      <button 
                        @click="confirmDelete(item)"
                        class="px-4 py-1.5 bg-gray-50 hover:bg-red-50 text-gray-600 hover:text-red-500 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5"
                      >
                        <Trash2 :size="14" /> 删除
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="bg-white rounded-[32px] p-20 flex flex-col items-center justify-center text-center border border-dashed border-gray-200">
              <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mb-4">
                <BookOpen :size="32" class="text-gray-200" />
              </div>
              <p class="text-gray-400 font-bold">还没有发布过任何内容哦</p>
              <router-link to="/" class="mt-4 text-blue-600 text-sm font-bold hover:underline">去首页逛逛</router-link>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Confirmation Modal -->
    <ConfirmationModal 
      :show="confirmConfig.show"
      :title="confirmConfig.title"
      :message="confirmConfig.message"
      :type="confirmConfig.type"
      @confirm="confirmAction"
      @cancel="confirmConfig.show = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { 
  ChevronLeft, User as UserIcon, BookOpen, LogOut, 
  Camera, Edit2, Trash2, Image as ImageIcon 
} from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import request from '../utils/request';
import ConfirmationModal from '@/components/ConfirmationModal.vue';
import { getImageUrl } from '../utils/image';

const router = useRouter();
const activeTab = ref('profile');
const loading = ref(true);
const user = ref({ username: '用户', email: '', avatar: '' });
const myArticles = ref<any[]>([]);

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 确认弹窗配置
const confirmConfig = reactive({
  show: false,
  title: '',
  message: '',
  type: 'primary' as 'primary' | 'danger',
  action: null as Function | null
});

// 获取用户信息和发布内容
const loadData = async () => {
  loading.value = true;
  try {
    // 假设从本地或接口获取用户信息
    const storedUser = localStorage.getItem('user');
    if (storedUser) user.value = JSON.parse(storedUser);

    // 获取我的文章 (按当前用户查询)
    const res = await request.get('/article/list', { 
      params: { username: user.value.username, size: 100 } 
    });
    if (res.data.code === 200) {
      myArticles.value = res.data.data.list;
    }
  } catch (err) {
    console.error('Load error:', err);
  } finally {
    loading.value = false;
  }
};

// 更新密码确认
const confirmUpdatePwd = () => {
  if (!pwdForm.newPassword || pwdForm.newPassword !== pwdForm.confirmPassword) {
    alert('密码不一致或为空');
    return;
  }
  confirmConfig.title = '确认更新密码？';
  confirmConfig.message = '密码修改后将需要重新登录。';
  confirmConfig.type = 'primary';
  confirmConfig.show = true;
  confirmConfig.action = async () => {
    // TODO: 调用后端接口
    console.log('Update password', pwdForm);
    alert('更新成功，请重新登录');
    handleLogout();
  };
};

// 删除内容确认
const confirmDelete = (item: any) => {
  confirmConfig.title = '确认删除此内容？';
  confirmConfig.message = `删除 “${item.title}” 后将无法找回，请谨慎操作。`;
  confirmConfig.type = 'danger';
  confirmConfig.show = true;
  confirmConfig.action = async () => {
    try {
      const res = await request.delete(`/article/${item.id}`);
      if (res.data.code === 200) {
        myArticles.value = myArticles.value.filter(a => a.id !== item.id);
      }
    } catch (err) {
      console.error('Delete error:', err);
    }
  };
};

const confirmAction = () => {
  if (confirmConfig.action) confirmConfig.action();
  confirmConfig.show = false;
};

const handleLogout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  router.push('/login');
};

const triggerAvatarUpload = () => {
  alert('功能开发中：点击此处将触发文件选择，随后上传至服务器更新头像。');
};

const handleEdit = (item: any) => {
  confirmConfig.title = '确认编辑此内容？';
  confirmConfig.message = '即将进入内容编辑模式。';
  confirmConfig.type = 'primary';
  confirmConfig.show = true;
  confirmConfig.action = () => {
    console.log('Go to edit page', item);
    // router.push(`/article/edit/${item.id}`);
  };
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString();
};

onMounted(loadData);
</script>

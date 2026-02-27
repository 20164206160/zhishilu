<template>
  <div class="min-h-screen bg-[#f8fafc] flex flex-col font-sans w-full overflow-x-hidden">
    <!-- Header -->
    <header class="bg-white border-b border-gray-100 sticky top-0 z-40 w-full">
      <div class="max-w-5xl mx-auto px-2 sm:px-4 h-14 sm:h-16 flex items-center justify-between">
        <router-link to="/" class="flex items-center gap-1 sm:gap-2 text-blue-600 hover:opacity-80 transition-opacity">
          <ChevronLeft :size="18" class="sm:w-5 sm:h-5" />
          <span class="font-bold text-sm sm:text-base">返回首页</span>
        </router-link>
        <h1 class="text-base sm:text-lg font-black text-gray-900">个人中心</h1>
        <div class="w-16 sm:w-20"></div> <!-- Placeholder for balance -->
      </div>
    </header>

    <main class="max-w-5xl mx-auto w-full px-2 sm:px-4 py-4 sm:py-8">
      <div class="flex flex-col md:flex-row gap-4 sm:gap-8">
        <!-- Sidebar Navigation -->
        <aside class="w-full md:w-64 space-y-1 sm:space-y-2">
          <button
            @click="activeTab = 'profile'"
            :class="['w-full flex items-center gap-2 sm:gap-3 px-3 sm:px-4 py-2.5 sm:py-3 rounded-xl sm:rounded-2xl text-xs sm:text-sm font-bold transition-all', activeTab === 'profile' ? 'bg-blue-600 text-white shadow-lg shadow-blue-200' : 'bg-white text-gray-500 hover:bg-gray-50 border border-gray-100']"
          >
            <UserIcon :size="16" class="sm:w-[18px] sm:h-[18px]" /> <span class="whitespace-nowrap">账号设置</span>
          </button>
          <button
            @click="activeTab = 'content'"
            :class="['w-full flex items-center gap-2 sm:gap-3 px-3 sm:px-4 py-2.5 sm:py-3 rounded-xl sm:rounded-2xl text-xs sm:text-sm font-bold transition-all', activeTab === 'content' ? 'bg-blue-600 text-white shadow-lg shadow-blue-200' : 'bg-white text-gray-500 hover:bg-gray-50 border border-gray-100']"
          >
            <BookOpen :size="16" class="sm:w-[18px] sm:h-[18px]" /> <span class="whitespace-nowrap">内容管理</span>
          </button>
          <button
            @click="activeTab = 'drafts'"
            :class="['w-full flex items-center gap-2 sm:gap-3 px-3 sm:px-4 py-2.5 sm:py-3 rounded-xl sm:rounded-2xl text-xs sm:text-sm font-bold transition-all', activeTab === 'drafts' ? 'bg-blue-600 text-white shadow-lg shadow-blue-200' : 'bg-white text-gray-500 hover:bg-gray-50 border border-gray-100']"
          >
            <FileText :size="16" class="sm:w-[18px] sm:h-[18px]" /> <span class="whitespace-nowrap">草稿箱</span>
            <span v-if="drafts.length > 0" class="ml-auto bg-red-500 text-white text-[9px] sm:text-[10px] px-1.5 sm:px-2 py-0.5 rounded-full">{{ drafts.length }}</span>
          </button>
          <button
            @click="handleLogout"
            class="w-full flex items-center gap-2 sm:gap-3 px-3 sm:px-4 py-2.5 sm:py-3 rounded-xl sm:rounded-2xl text-xs sm:text-sm font-bold text-red-500 bg-white hover:bg-red-50 border border-gray-100 transition-all"
          >
            <LogOut :size="16" class="sm:w-[18px] sm:h-[18px]" /> <span class="whitespace-nowrap">退出登录</span>
          </button>
        </aside>

        <!-- Content Area -->
        <div class="flex-grow min-w-0">
          <!-- Account Settings Tab -->
          <div v-if="activeTab === 'profile'" class="bg-white rounded-2xl sm:rounded-[32px] p-4 sm:p-8 shadow-sm border border-gray-100 space-y-6 sm:space-y-10">
            <!-- Avatar Section -->
            <section class="space-y-3 sm:space-y-4">
              <h2 class="text-lg sm:text-xl font-black text-gray-900 flex items-center gap-2">
                <div class="w-1.5 h-5 sm:h-6 bg-blue-600 rounded-full"></div> 个人资料
              </h2>
              <div class="flex items-center gap-4 sm:gap-6">
                <div class="relative group cursor-pointer" @click="triggerAvatarUpload">
                  <div class="w-16 h-16 sm:w-24 sm:h-24 rounded-2xl sm:rounded-[32px] bg-blue-50 flex items-center justify-center overflow-hidden border-2 sm:border-4 border-white shadow-inner">
                    <img v-if="user.avatar" :src="user.avatar" class="w-full h-full object-cover" />
                    <UserIcon v-else :size="28" class="sm:w-10 sm:h-10 text-blue-300" />
                  </div>
                  <div class="absolute inset-0 bg-black/40 rounded-2xl sm:rounded-[32px] flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                    <Camera v-if="!uploadingAvatar" :size="16" class="sm:w-5 sm:h-5 text-white" />
                    <div v-else class="w-4 h-4 sm:w-5 sm:h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  </div>
                  <input
                    ref="avatarInputRef"
                    type="file"
                    accept="image/jpeg,image/png,image/gif,image/webp"
                    class="hidden"
                    @change="handleAvatarChange"
                  />
                </div>
                <div class="space-y-0.5 sm:space-y-1 min-w-0">
                  <p class="text-base sm:text-lg font-bold text-gray-900 truncate">{{ user.username }}</p>
                  <p class="text-xs sm:text-sm text-gray-400 truncate">{{ user.email || '未设置邮箱' }}</p>
                  <button @click="triggerAvatarUpload" class="text-[10px] sm:text-xs font-bold text-blue-600 hover:underline">点击更换头像</button>
                </div>
              </div>
            </section>

            <!-- Password Section -->
            <section class="space-y-4 sm:space-y-6 pt-4 sm:pt-6 border-t border-gray-50">
              <h2 class="text-lg sm:text-xl font-black text-gray-900 flex items-center gap-2">
                <div class="w-1.5 h-5 sm:h-6 bg-emerald-500 rounded-full"></div> 修改密码
              </h2>
              <div class="grid grid-cols-1 gap-3 sm:gap-4 max-w-md">
                <div class="space-y-1 sm:space-y-1.5">
                  <label class="text-[10px] sm:text-xs font-bold text-gray-400 uppercase ml-1">当前密码</label>
                  <input v-model="pwdForm.oldPassword" type="password" class="w-full px-3 sm:px-4 py-2.5 sm:py-3 bg-gray-50 border-none rounded-lg sm:rounded-xl focus:ring-2 focus:ring-blue-500 transition-all text-xs sm:text-sm" placeholder="请输入当前密码" />
                </div>
                <div class="space-y-1 sm:space-y-1.5">
                  <label class="text-[10px] sm:text-xs font-bold text-gray-400 uppercase ml-1">新密码</label>
                  <input v-model="pwdForm.newPassword" type="password" class="w-full px-3 sm:px-4 py-2.5 sm:py-3 bg-gray-50 border-none rounded-lg sm:rounded-xl focus:ring-2 focus:ring-blue-500 transition-all text-xs sm:text-sm" placeholder="至少 6 位字符" />
                </div>
                <div class="space-y-1 sm:space-y-1.5">
                  <label class="text-[10px] sm:text-xs font-bold text-gray-400 uppercase ml-1">确认新密码</label>
                  <input v-model="pwdForm.confirmPassword" type="password" class="w-full px-3 sm:px-4 py-2.5 sm:py-3 bg-gray-50 border-none rounded-lg sm:rounded-xl focus:ring-2 focus:ring-blue-500 transition-all text-xs sm:text-sm" placeholder="请再次输入新密码" />
                </div>
                <button
                  @click="confirmUpdatePwd"
                  class="mt-2 sm:mt-4 bg-gray-900 hover:bg-black text-white py-2.5 sm:py-3 rounded-lg sm:rounded-xl text-xs sm:text-sm font-bold shadow-lg shadow-gray-200 transition-all active:scale-95"
                >
                  更新密码
                </button>
              </div>
            </section>
          </div>

          <!-- Content Management Tab -->
          <div v-if="activeTab === 'content'" class="space-y-3 sm:space-y-4">
            <div class="flex items-center justify-between mb-2">
              <h2 class="text-lg sm:text-2xl font-black text-gray-900">我的发布</h2>
              <span class="text-[10px] sm:text-xs font-bold text-gray-400 bg-white px-2 sm:px-3 py-1 rounded-full border border-gray-100">共 {{ myArticles.length }} 篇</span>
            </div>

            <div v-if="loading" class="space-y-3 sm:space-y-4">
              <div v-for="i in 3" :key="i" class="bg-white p-3 sm:p-4 rounded-2xl sm:rounded-3xl animate-pulse flex gap-3 sm:gap-4 h-24 sm:h-32 border border-gray-50"></div>
            </div>

            <div v-else-if="myArticles.length > 0" class="grid grid-cols-1 gap-3 sm:gap-4">
              <div v-for="item in myArticles" :key="item.id" class="bg-white p-3 sm:p-4 rounded-2xl sm:rounded-3xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow group">
                <div class="flex gap-3 sm:gap-4">
                  <div @click="router.push(`/article/${item.id}?from=profile`)" class="w-20 h-20 sm:w-24 sm:h-24 rounded-xl sm:rounded-2xl bg-gray-50 overflow-hidden flex-shrink-0 cursor-pointer">
                    <img v-if="item.images?.length" :src="getImageUrl(item.images[0])" class="w-full h-full object-cover" />
                    <div v-else class="w-full h-full flex items-center justify-center text-gray-200">
                      <ImageIcon :size="20" class="sm:w-6 sm:h-6" />
                    </div>
                  </div>
                  <div class="flex-grow py-0.5 sm:py-1 flex flex-col justify-between min-w-0">
                    <div @click="router.push(`/article/${item.id}?from=profile`)" class="cursor-pointer min-w-0">
                      <div class="flex flex-wrap gap-1 mb-1">
                        <span
                          v-for="(cat, idx) in item.categories?.slice(0, 2)"
                          :key="idx"
                          class="text-[9px] sm:text-[10px] font-bold text-blue-500 bg-blue-50 px-1.5 sm:px-2 py-0.5 rounded-md uppercase tracking-wider inline-block"
                        >
                          {{ cat }}
                        </span>
                        <span v-if="item.categories?.length > 2" class="text-[9px] sm:text-[10px] font-bold text-blue-500 bg-blue-50 px-1.5 sm:px-2 py-0.5 rounded-md inline-block">
                          +{{ item.categories.length - 2 }}
                        </span>
                      </div>
                      <h3 class="text-sm sm:text-base font-bold text-gray-900 line-clamp-1 group-hover:text-blue-600 transition-colors">{{ item.title }}</h3>
                      <p class="text-[10px] sm:text-xs text-gray-400 mt-1">{{ formatDate(item.createdTime) }} 发布</p>
                    </div>
                    <div class="flex gap-2">
                      <button
                        @click="handleEdit(item)"
                        class="px-2 sm:px-4 py-1 sm:py-1.5 bg-gray-50 hover:bg-blue-50 text-gray-600 hover:text-blue-600 rounded-lg sm:rounded-xl text-[10px] sm:text-xs font-bold transition-all flex items-center gap-1"
                      >
                        <Edit2 :size="12" class="sm:w-3.5 sm:h-3.5" /> <span class="hidden sm:inline">编辑</span>
                      </button>
                      <button
                        @click="confirmDelete(item)"
                        class="px-2 sm:px-4 py-1 sm:py-1.5 bg-gray-50 hover:bg-red-50 text-gray-600 hover:text-red-500 rounded-lg sm:rounded-xl text-[10px] sm:text-xs font-bold transition-all flex items-center gap-1"
                      >
                        <Trash2 :size="12" class="sm:w-3.5 sm:h-3.5" /> <span class="hidden sm:inline">删除</span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="bg-white rounded-2xl sm:rounded-[32px] p-10 sm:p-20 flex flex-col items-center justify-center text-center border border-dashed border-gray-200">
              <div class="w-14 h-14 sm:w-20 sm:h-20 bg-gray-50 rounded-full flex items-center justify-center mb-3 sm:mb-4">
                <BookOpen :size="24" class="sm:w-8 sm:h-8 text-gray-200" />
              </div>
              <p class="text-gray-400 font-bold text-sm sm:text-base">还没有发布过任何内容哦</p>
              <router-link to="/" class="mt-3 sm:mt-4 text-blue-600 text-xs sm:text-sm font-bold hover:underline">去首页逛逛</router-link>
            </div>
          </div>

          <!-- Drafts Management Tab -->
          <div v-if="activeTab === 'drafts'" class="space-y-3 sm:space-y-4">
            <div class="flex items-center justify-between mb-2">
              <h2 class="text-lg sm:text-2xl font-black text-gray-900">草稿箱</h2>
              <router-link
                to="/post"
                class="bg-blue-600 hover:bg-blue-700 text-white px-2 sm:px-4 py-1.5 sm:py-2 rounded-lg sm:rounded-xl text-xs sm:text-sm font-bold shadow-lg shadow-blue-100 transition-all flex items-center gap-1 sm:gap-2"
              >
                <Plus :size="14" class="sm:w-4 sm:h-4" /> <span class="hidden sm:inline">新建草稿</span>
              </router-link>
            </div>

            <div v-if="draftsLoading" class="space-y-3 sm:space-y-4">
              <div v-for="i in 3" :key="i" class="bg-white p-3 sm:p-4 rounded-2xl sm:rounded-3xl animate-pulse flex gap-3 sm:gap-4 h-24 sm:h-32 border border-gray-50"></div>
            </div>

            <div v-else-if="drafts.length > 0" class="grid grid-cols-1 gap-3 sm:gap-4">
              <div v-for="item in drafts" :key="item.id" class="bg-white p-3 sm:p-4 rounded-2xl sm:rounded-3xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow group">
                <div class="flex gap-3 sm:gap-4">
                  <div class="w-20 h-20 sm:w-24 sm:h-24 rounded-xl sm:rounded-2xl bg-gray-50 overflow-hidden flex-shrink-0 flex items-center justify-center">
                    <img v-if="item.images?.length" :src="getImageUrl(item.images[0])" class="w-full h-full object-cover" />
                    <FileText v-else :size="20" class="sm:w-6 sm:h-6 text-gray-200" />
                  </div>
                  <div class="flex-grow py-0.5 sm:py-1 flex flex-col justify-between min-w-0">
                    <div class="min-w-0">
                      <div class="flex flex-wrap gap-1 mb-1">
                        <span
                          v-for="(cat, idx) in item.categories?.slice(0, 2)"
                          :key="idx"
                          class="text-[9px] sm:text-[10px] font-bold text-blue-500 bg-blue-50 px-1.5 sm:px-2 py-0.5 rounded-md uppercase tracking-wider inline-block"
                        >
                          {{ cat }}
                        </span>
                        <span v-if="item.categories?.length > 2" class="text-[9px] sm:text-[10px] font-bold text-blue-500 bg-blue-50 px-1.5 sm:px-2 py-0.5 rounded-md inline-block">
                          +{{ item.categories.length - 2 }}
                        </span>
                        <span v-if="!item.categories?.length" class="text-[9px] sm:text-[10px] text-gray-300">未分类</span>
                      </div>
                      <h3 class="text-sm sm:text-base font-bold text-gray-900 line-clamp-1 group-hover:text-blue-600 transition-colors">
                        {{ item.title || '无标题' }}
                      </h3>
                      <p class="text-[10px] sm:text-xs text-gray-400 mt-1">
                        最后编辑：{{ formatDate(item.updatedTime) }}
                      </p>
                    </div>
                    <div class="flex gap-2">
                      <button
                        @click="handleEditDraft(item)"
                        class="px-2 sm:px-4 py-1 sm:py-1.5 bg-gray-50 hover:bg-blue-50 text-gray-600 hover:text-blue-600 rounded-lg sm:rounded-xl text-[10px] sm:text-xs font-bold transition-all flex items-center gap-1"
                      >
                        <Edit2 :size="12" class="sm:w-3.5 sm:h-3.5" /> <span class="hidden sm:inline">继续编辑</span>
                      </button>
                      <button
                        @click="confirmDeleteDraft(item)"
                        class="px-2 sm:px-4 py-1 sm:py-1.5 bg-gray-50 hover:bg-red-50 text-gray-600 hover:text-red-500 rounded-lg sm:rounded-xl text-[10px] sm:text-xs font-bold transition-all flex items-center gap-1"
                      >
                        <Trash2 :size="12" class="sm:w-3.5 sm:h-3.5" /> <span class="hidden sm:inline">删除</span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="bg-white rounded-2xl sm:rounded-[32px] p-10 sm:p-20 flex flex-col items-center justify-center text-center border border-dashed border-gray-200">
              <div class="w-14 h-14 sm:w-20 sm:h-20 bg-gray-50 rounded-full flex items-center justify-center mb-3 sm:mb-4">
                <FileText :size="24" class="sm:w-8 sm:h-8 text-gray-200" />
              </div>
              <p class="text-gray-400 font-bold text-sm sm:text-base">还没有草稿</p>
              <router-link to="/post" class="mt-3 sm:mt-4 text-blue-600 text-xs sm:text-sm font-bold hover:underline">去创建草稿</router-link>
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
  Camera, Edit2, Trash2, Image as ImageIcon, FileText, Plus
} from 'lucide-vue-next';
import { useRouter, useRoute } from 'vue-router';
import request from '../utils/request';
import ConfirmationModal from '@/components/ConfirmationModal.vue';
import { getImageUrl, getAvatarUrl } from '../utils/image';

const router = useRouter();
const route = useRoute();
const activeTab = ref('profile');
const loading = ref(true);
const user = ref({ username: '用户', email: '', avatar: '' });
const myArticles = ref<any[]>([]);
const drafts = ref<any[]>([]);
const draftsLoading = ref(false);

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
    // 从本地获取用户信息
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      const userData = JSON.parse(storedUser);
      // 转换头像路径为完整URL
      if (userData.avatar && !userData.avatar.startsWith('http')) {
        userData.avatar = getAvatarUrl(userData.avatar);
      }
      user.value = userData;
    }

    // 获取我的文章 (按当前用户查询)
    const res = await request.get('/article/list', { 
      params: { username: user.value.username, size: 100 } 
    });
    if (res.data.code === 200) {
      myArticles.value = res.data.data.list;
    }
    
    // 获取草稿列表
    await loadDrafts();
  } catch (err) {
    console.error('Load error:', err);
  } finally {
    loading.value = false;
  }
};

// 获取草稿列表
const loadDrafts = async () => {
  draftsLoading.value = true;
  try {
    const res = await request.get('/article/draft/list');
    if (res.data.code === 200) {
      drafts.value = res.data.data;
    }
  } catch (err) {
    console.error('Load drafts error:', err);
  } finally {
    draftsLoading.value = false;
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
      const res = await request.delete(`/article/delete/${item.id}`);
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

const avatarInputRef = ref<HTMLInputElement | null>(null);
const uploadingAvatar = ref(false);

const triggerAvatarUpload = () => {
  avatarInputRef.value?.click();
};

const handleAvatarChange = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
  if (!allowedTypes.includes(file.type)) {
    alert('请选择图片文件（jpg, png, gif, webp）');
    return;
  }

  // 验证文件大小（2MB）
  if (file.size > 2 * 1024 * 1024) {
    alert('头像文件大小不能超过2MB');
    return;
  }

  uploadingAvatar.value = true;
  try {
    const formData = new FormData();
    formData.append('file', file);

    const res = await request.post('/user/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    if (res.data.code === 200) {
      // 更新本地用户头像
      const avatarFileName = res.data.data.avatar;
      user.value.avatar = getAvatarUrl(avatarFileName);
      
      // 更新 localStorage 中的用户信息
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        const userData = JSON.parse(storedUser);
        userData.avatar = avatarFileName;
        localStorage.setItem('user', JSON.stringify(userData));
      }
      
      alert('头像上传成功！');
    } else {
      alert(res.data.message || '头像上传失败');
    }
  } catch (err: any) {
    console.error('头像上传失败:', err);
    alert(err.response?.data?.message || '头像上传失败，请重试');
  } finally {
    uploadingAvatar.value = false;
    // 清空 input，允许重复选择同一文件
    if (avatarInputRef.value) {
      avatarInputRef.value.value = '';
    }
  }
};

const handleEdit = (item: any) => {
  confirmConfig.title = '确认编辑此内容？';
  confirmConfig.message = '即将进入内容编辑模式。';
  confirmConfig.type = 'primary';
  confirmConfig.show = true;
  confirmConfig.action = () => {
    // 跳转到编辑页，带上返回时的tab参数
    router.push(`/article/${item.id}/edit?from=content`);
  };
};

// 编辑草稿
const handleEditDraft = (item: any) => {
  // 跳转到草稿编辑页，带上返回时的tab参数
  router.push(`/draft/${item.id}/edit?from=drafts`);
};

// 删除草稿确认
const confirmDeleteDraft = (item: any) => {
  confirmConfig.title = '确认删除此草稿？';
  confirmConfig.message = `删除 "${item.title || '无标题'}" 后将无法找回，请谨慎操作。`;
  confirmConfig.type = 'danger';
  confirmConfig.show = true;
  confirmConfig.action = async () => {
    try {
      const res = await request.delete(`/article/draft/${item.id}`);
      if (res.data.code === 200) {
        drafts.value = drafts.value.filter(d => d.id !== item.id);
      }
    } catch (err) {
      console.error('Delete draft error:', err);
      alert('删除草稿失败');
    }
  };
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString();
};

onMounted(() => {
  // 设置页面标题
  document.title = '个人中心';
  // 检查路由参数，如果有from参数则切换到对应tab
  const fromTab = route.query.from as string;
  if (fromTab && ['profile', 'content', 'drafts'].includes(fromTab)) {
    activeTab.value = fromTab;
  }
  loadData();
});
</script>

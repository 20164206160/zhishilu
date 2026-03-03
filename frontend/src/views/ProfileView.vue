<template>
  <div class="min-h-screen bg-[#f8fafc] flex flex-col font-sans w-full overflow-x-hidden">
    <!-- Header -->
    <header class="bg-white border-b border-gray-100 sticky top-0 z-40 w-full">
      <div class="w-full px-2 sm:px-4 lg:px-6 h-14 sm:h-16 flex items-center justify-between">
        <router-link to="/" class="flex items-center gap-1 sm:gap-2 text-blue-600 hover:opacity-80 transition-opacity">
          <ChevronLeft :size="18" class="sm:w-5 sm:h-5" />
          <span class="font-bold text-sm sm:text-base">返回首页</span>
        </router-link>
        <h1 class="text-base sm:text-lg font-black text-gray-900">个人中心</h1>
        <div class="w-16 sm:w-20"></div>
      </div>
    </header>

    <main class="w-full px-2 sm:px-4 lg:px-6 py-4 sm:py-6 flex-grow">
      <div class="flex flex-col lg:flex-row gap-4 sm:gap-6 lg:h-[calc(100vh-140px)]">
        <!-- Left Sidebar - 小红书风格简洁导航 -->
        <aside class="w-full lg:w-[208px] shrink-0 lg:h-full overflow-hidden">
          <div class="lg:h-full lg:overflow-y-auto pr-2 scrollbar-hide">
            <!-- User Info Section - 简洁设计 -->
            <div class="flex flex-col items-center text-center py-6">
              <div class="relative group cursor-pointer mb-3" @click="triggerAvatarUpload">
                <div class="w-20 h-20 sm:w-24 sm:h-24 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center overflow-hidden border-4 border-white shadow-lg">
                  <img v-if="user.avatar" :src="user.avatar" class="w-full h-full object-cover" />
                  <UserIcon v-else :size="40" class="text-white" />
                </div>
                <div class="absolute inset-0 bg-black/30 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                  <Camera v-if="!uploadingAvatar" :size="24" class="text-white" />
                  <div v-else class="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                </div>
                <input ref="avatarInputRef" type="file" accept="image/jpeg,image/png,image/gif,image/webp" class="hidden" @change="handleAvatarChange" />
              </div>
              <h2 class="text-lg sm:text-xl font-bold text-gray-900">{{ user.username }}</h2>
              <p class="text-xs sm:text-sm text-gray-400 mt-1">{{ user.email || '未设置邮箱' }}</p>
              <button @click="triggerAvatarUpload" class="mt-2 text-xs text-blue-600 hover:underline">更换头像</button>
            </div>

            <!-- Navigation Section - 小红书风格菜单 -->
            <nav class="space-y-1 px-2">
              <button @click="switchTab('profile')"
                      :class="['w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200', 
                               activeTab === 'profile' && !subView 
                                 ? 'bg-blue-50 text-blue-600' 
                                 : 'text-gray-600 hover:bg-gray-100']">
                <UserIcon :size="18" /> <span>账号设置</span>
              </button>
              <button @click="switchTab('content')"
                      :class="['w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200', 
                               (activeTab === 'content' || subView?.startsWith('article')) && !isDraftsView 
                                 ? 'bg-blue-50 text-blue-600' 
                                 : 'text-gray-600 hover:bg-gray-100']">
                <BookOpen :size="18" /> <span>我的发布</span>
                <span v-if="myArticles.length > 0" class="ml-auto text-xs bg-gray-200 text-gray-600 px-2 py-0.5 rounded-full">{{ myArticles.length }}</span>
              </button>
              <button @click="switchTab('drafts')"
                      :class="['w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200', 
                               (activeTab === 'drafts' || isDraftsView) 
                                 ? 'bg-blue-50 text-blue-600' 
                                 : 'text-gray-600 hover:bg-gray-100']">
                <FileText :size="18" /> <span>草稿箱</span>
                <span v-if="drafts.length > 0" class="ml-auto text-xs bg-red-100 text-red-600 px-2 py-0.5 rounded-full">{{ drafts.length }}</span>
              </button>
            </nav>

            <!-- Logout Section -->
            <div class="pt-4 px-2 pb-4">
              <button @click="handleLogout" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium text-red-500 hover:bg-red-50 transition-all duration-200">
                <LogOut :size="18" /> <span>退出登录</span>
              </button>
            </div>
          </div>
        </aside>

        <!-- Right Content Area - 小红书风格卡片 -->
        <div class="flex-grow lg:w-3/4 min-w-0 lg:overflow-hidden">
          <!-- Profile Settings -->
          <div v-if="activeTab === 'profile' && !subView" class="lg:h-full lg:overflow-y-auto">
            <div class="bg-white rounded-[20px] lg:h-full flex flex-col shadow-sm hover:shadow-md transition-shadow duration-300">
              <!-- Header -->
              <div class="p-4 sm:p-6 border-b border-gray-50">
                <h2 class="text-lg sm:text-xl font-bold text-gray-900">账号设置</h2>
                <p class="text-xs text-gray-400 mt-1">管理您的个人信息和账号安全</p>
              </div>

              <!-- Content -->
              <div class="flex-1 p-4 sm:p-6 lg:overflow-y-auto">
                <div class="max-w-2xl space-y-8">
                  <!-- Avatar Section -->
                  <section class="flex items-start gap-6">
                    <div class="relative group cursor-pointer shrink-0" @click="triggerAvatarUpload">
                      <div class="w-24 h-24 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center overflow-hidden border-4 border-white shadow-lg">
                        <img v-if="user.avatar" :src="user.avatar" class="w-full h-full object-cover" />
                        <UserIcon v-else :size="40" class="text-white" />
                      </div>
                      <div class="absolute inset-0 bg-black/30 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                        <Camera :size="24" class="text-white" />
                      </div>
                    </div>
                    <div class="pt-2">
                      <h3 class="text-base font-bold text-gray-900">头像</h3>
                      <p class="text-xs text-gray-400 mt-1">支持 JPG、PNG、GIF、WebP 格式，最大 2MB</p>
                      <button @click="triggerAvatarUpload" class="mt-3 px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg text-sm font-medium text-gray-700 transition-all duration-200">
                        更换头像
                      </button>
                    </div>
                  </section>

                  <!-- Divider -->
                  <div class="h-px bg-gray-100"></div>

                  <!-- Password Section -->
                  <section class="space-y-4">
                    <div>
                      <h3 class="text-base font-bold text-gray-900">修改密码</h3>
                      <p class="text-xs text-gray-400 mt-1">定期更换密码可以保护您的账号安全</p>
                    </div>
                    <div class="grid grid-cols-1 gap-4 max-w-md">
                      <div class="space-y-1.5">
                        <label class="text-xs font-medium text-gray-600">当前密码</label>
                        <input v-model="pwdForm.oldPassword" type="password" class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200" placeholder="请输入当前密码" />
                      </div>
                      <div class="space-y-1.5">
                        <label class="text-xs font-medium text-gray-600">新密码</label>
                        <input v-model="pwdForm.newPassword" type="password" class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200" placeholder="至少 6 位字符" />
                      </div>
                      <div class="space-y-1.5">
                        <label class="text-xs font-medium text-gray-600">确认新密码</label>
                        <input v-model="pwdForm.confirmPassword" type="password" class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200" placeholder="请再次输入新密码" />
                      </div>
                      <button @click="confirmUpdatePwd" class="mt-2 bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-xl text-sm font-bold transition-all duration-200">
                        更新密码
                      </button>
                    </div>
                  </section>
                </div>
              </div>
            </div>
          </div>

          <!-- Content Management Grid -->
          <div v-else-if="activeTab === 'content' && !subView" class="lg:h-full lg:overflow-y-auto">
            <div class="bg-white rounded-[20px] lg:h-full flex flex-col shadow-sm hover:shadow-md transition-shadow duration-300">
              <!-- Header -->
              <div class="p-4 sm:p-6 border-b border-gray-50 flex items-center justify-between">
                <div>
                  <h2 class="text-lg sm:text-xl font-bold text-gray-900">我的发布</h2>
                  <p class="text-xs text-gray-400 mt-1">共 {{ myArticles.length }} 篇内容</p>
                </div>
              </div>

              <!-- Grid Content -->
              <div class="flex-1 p-4 sm:p-6 lg:overflow-y-auto">
                <div v-if="loading" class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
                  <div v-for="i in 8" :key="i" class="aspect-[3/4] rounded-xl bg-gray-100 animate-pulse"></div>
                </div>

                <div v-else-if="myArticles.length > 0" class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
                  <div v-for="item in myArticles" :key="item.id" 
                       class="group relative aspect-[3/4] rounded-xl overflow-hidden bg-gray-100 cursor-pointer transition-all duration-300 hover:shadow-lg hover:-translate-y-0.5"
                       @click="viewArticle(item)">
                    <!-- Image -->
                    <img v-if="item.images?.length" :src="getImageUrl(item.images[0])" 
                         class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105" />
                    <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200">
                      <BookOpen :size="32" class="text-gray-300" />
                    </div>
                    
                    <!-- Desktop: Hover Overlay -->
                    <div class="hidden lg:flex absolute inset-0 bg-black/0 group-hover:bg-black/40 transition-all duration-300 items-center justify-center opacity-0 group-hover:opacity-100">
                      <div class="flex gap-2">
                        <button @click.stop="editArticle(item)" class="w-10 h-10 rounded-full bg-white/90 hover:bg-white flex items-center justify-center text-gray-700 transition-all duration-200 hover:scale-105">
                          <Edit2 :size="18" />
                        </button>
                        <button @click.stop="confirmDelete(item)" class="w-10 h-10 rounded-full bg-white/90 hover:bg-white flex items-center justify-center text-red-500 transition-all duration-200 hover:scale-105">
                          <Trash2 :size="18" />
                        </button>
                      </div>
                    </div>

                    <!-- Mobile/Tablet: Bottom Right Buttons -->
                    <div class="flex lg:hidden absolute bottom-2 right-2 gap-1.5 z-10">
                      <button @click.stop="editArticle(item)" class="w-8 h-8 rounded-full bg-white/95 shadow-md flex items-center justify-center text-gray-700 active:scale-95 transition-all duration-200">
                        <Edit2 :size="14" />
                      </button>
                      <button @click.stop="confirmDelete(item)" class="w-8 h-8 rounded-full bg-white/95 shadow-md flex items-center justify-center text-red-500 active:scale-95 transition-all duration-200">
                        <Trash2 :size="14" />
                      </button>
                    </div>

                    <!-- Title Overlay -->
                    <div class="absolute bottom-0 left-0 right-0 p-3 bg-gradient-to-t from-black/60 to-transparent">
                      <h3 class="text-white text-xs font-medium line-clamp-2 pr-16 lg:pr-0">{{ item.title }}</h3>
                      <p class="text-white/70 text-[10px] mt-1">{{ formatDate(item.createdTime) }}</p>
                    </div>
                  </div>
                </div>

                <div v-else class="h-full flex flex-col items-center justify-center text-center py-20">
                  <div class="w-20 h-20 rounded-full bg-gray-50 flex items-center justify-center mb-4">
                    <BookOpen :size="32" class="text-gray-200" />
                  </div>
                  <p class="text-gray-400 font-medium">还没有发布过任何内容</p>
                  <router-link to="/post" class="mt-4 bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-full text-sm font-medium transition-all duration-200">
                    去发布
                  </router-link>
                </div>
              </div>
            </div>
          </div>

          <!-- Drafts Grid -->
          <div v-else-if="(activeTab === 'drafts' || isDraftsView) && !subView" class="lg:h-full lg:overflow-y-auto">
            <div class="bg-white rounded-[20px] lg:h-full flex flex-col shadow-sm hover:shadow-md transition-shadow duration-300">
              <!-- Header -->
              <div class="p-4 sm:p-6 border-b border-gray-50 flex items-center justify-between">
                <div>
                  <h2 class="text-lg sm:text-xl font-bold text-gray-900">草稿箱</h2>
                  <p class="text-xs text-gray-400 mt-1">共 {{ drafts.length }} 个草稿</p>
                </div>
                <router-link to="/post" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-full text-sm font-medium transition-all duration-200 flex items-center gap-2">
                  <Plus :size="16" /> 新建
                </router-link>
              </div>

              <!-- Grid Content -->
              <div class="flex-1 p-4 sm:p-6 lg:overflow-y-auto">
                <div v-if="draftsLoading" class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
                  <div v-for="i in 8" :key="i" class="aspect-[3/4] rounded-xl bg-gray-100 animate-pulse"></div>
                </div>

                <div v-else-if="drafts.length > 0" class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
                  <div v-for="item in drafts" :key="item.id" 
                       class="group relative aspect-[3/4] rounded-xl overflow-hidden bg-gray-100 cursor-pointer transition-all duration-300 hover:shadow-lg hover:-translate-y-0.5"
                       @click="editDraft(item)">
                    <!-- Draft Badge -->
                    <div class="absolute top-2 left-2 z-10 px-2 py-1 bg-yellow-400/90 backdrop-blur-sm rounded-md text-[10px] font-bold text-yellow-900">
                      草稿
                    </div>

                    <!-- Image -->
                    <img v-if="item.images?.length" :src="getImageUrl(item.images[0])" 
                         class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105" />
                    <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200">
                      <FileText :size="32" class="text-gray-300" />
                    </div>
                    
                    <!-- Desktop: Hover Overlay -->
                    <div class="hidden lg:flex absolute inset-0 bg-black/0 group-hover:bg-black/40 transition-all duration-300 items-center justify-center opacity-0 group-hover:opacity-100">
                      <div class="flex gap-2">
                        <button @click.stop="editDraft(item)" class="w-10 h-10 rounded-full bg-white/90 hover:bg-white flex items-center justify-center text-gray-700 transition-all duration-200 hover:scale-105">
                          <Edit2 :size="18" />
                        </button>
                        <button @click.stop="confirmDeleteDraft(item)" class="w-10 h-10 rounded-full bg-white/90 hover:bg-white flex items-center justify-center text-red-500 transition-all duration-200 hover:scale-105">
                          <Trash2 :size="18" />
                        </button>
                      </div>
                    </div>

                    <!-- Mobile/Tablet: Bottom Right Buttons -->
                    <div class="flex lg:hidden absolute bottom-2 right-2 gap-1.5 z-10">
                      <button @click.stop="editDraft(item)" class="w-8 h-8 rounded-full bg-white/95 shadow-md flex items-center justify-center text-gray-700 active:scale-95 transition-all duration-200">
                        <Edit2 :size="14" />
                      </button>
                      <button @click.stop="confirmDeleteDraft(item)" class="w-8 h-8 rounded-full bg-white/95 shadow-md flex items-center justify-center text-red-500 active:scale-95 transition-all duration-200">
                        <Trash2 :size="14" />
                      </button>
                    </div>

                    <!-- Title Overlay -->
                    <div class="absolute bottom-0 left-0 right-0 p-3 bg-gradient-to-t from-black/60 to-transparent">
                      <h3 class="text-white text-xs font-medium line-clamp-2 pr-16 lg:pr-0">{{ item.title || '无标题' }}</h3>
                      <p class="text-white/70 text-[10px] mt-1">{{ formatDate(item.updatedTime) }}</p>
                    </div>
                  </div>
                </div>

                <div v-else class="h-full flex flex-col items-center justify-center text-center py-20">
                  <div class="w-20 h-20 rounded-full bg-gray-50 flex items-center justify-center mb-4">
                    <FileText :size="32" class="text-gray-200" />
                  </div>
                  <p class="text-gray-400 font-medium">还没有草稿</p>
                  <router-link to="/post" class="mt-4 bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-full text-sm font-medium transition-all duration-200">
                    去创建
                  </router-link>
                </div>
              </div>
            </div>
          </div>

          <!-- Sub Views: Article Detail / Edit / Draft Edit -->
          <div v-else-if="subView" class="lg:h-full lg:overflow-y-auto">
            <div class="bg-white rounded-[20px] lg:h-full flex flex-col shadow-sm hover:shadow-md transition-shadow duration-300">
              <ArticleDetail v-if="subView === 'article-detail' && selectedArticle" 
                            :article="selectedArticle"
                            @back="closeSubView"
                            @edit="editArticle(selectedArticle)"
                            @delete="confirmDelete(selectedArticle)" />
              <ArticleEdit v-else-if="subView === 'article-edit' && selectedArticle"
                           :article="selectedArticle"
                           @cancel="closeSubView"
                           @saved="onArticleSaved" />
              <DraftEdit v-else-if="subView === 'draft-edit' && selectedDraft"
                         :draft="selectedDraft"
                         @cancel="closeSubView"
                         @published="onDraftPublished" />
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
import { useRouter, useRoute } from 'vue-router';
import { 
  ChevronLeft, User as UserIcon, BookOpen, LogOut, 
  Camera, Edit2, Trash2, Image as ImageIcon, FileText, Plus, Eye
} from 'lucide-vue-next';
import request from '../utils/request';
import ConfirmationModal from '@/components/ConfirmationModal.vue';
import ArticleDetail from '@/components/profile/ArticleDetail.vue';
import ArticleEdit from '@/components/profile/ArticleEdit.vue';
import DraftEdit from '@/components/profile/DraftEdit.vue';
import { getImageUrl, getAvatarUrl } from '../utils/image';

const router = useRouter();
const route = useRoute();

// Tab state
const activeTab = ref('profile');
const subView = ref<string | null>(null); // 'article-detail' | 'article-edit' | 'draft-edit'
const isDraftsView = ref(false);

// Data
const loading = ref(true);
const user = ref({ username: '用户', email: '', avatar: '' });
const myArticles = ref<any[]>([]);
const drafts = ref<any[]>([]);
const draftsLoading = ref(false);
const selectedArticle = ref<any>(null);
const selectedDraft = ref<any>(null);

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const confirmConfig = reactive({
  show: false,
  title: '',
  message: '',
  type: 'primary' as 'primary' | 'danger',
  action: null as Function | null
});

// Tab switching
const switchTab = (tab: string) => {
  activeTab.value = tab;
  subView.value = null;
  isDraftsView.value = tab === 'drafts';
  selectedArticle.value = null;
  selectedDraft.value = null;
};

// Sub view management
const viewArticle = (article: any) => {
  selectedArticle.value = article;
  subView.value = 'article-detail';
};

const editArticle = (article: any) => {
  selectedArticle.value = article;
  subView.value = 'article-edit';
};

const editDraft = (draft: any) => {
  selectedDraft.value = draft;
  subView.value = 'draft-edit';
  isDraftsView.value = true;
};

const closeSubView = () => {
  subView.value = null;
  selectedArticle.value = null;
  selectedDraft.value = null;
  // Refresh data when closing
  loadData();
};

const onArticleSaved = () => {
  subView.value = null;
  selectedArticle.value = null;
  loadData();
};

const onDraftPublished = () => {
  subView.value = null;
  selectedDraft.value = null;
  isDraftsView.value = false;
  activeTab.value = 'content';
  loadData();
};

// Data loading
const loadData = async () => {
  loading.value = true;
  try {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      const userData = JSON.parse(storedUser);
      if (userData.avatar && !userData.avatar.startsWith('http')) {
        userData.avatar = getAvatarUrl(userData.avatar);
      }
      user.value = userData;
    }

    const res = await request.get('/article/list', { 
      params: { username: user.value.username, size: 100 } 
    });
    if (res.data.code === 200) {
      myArticles.value = res.data.data.list;
    }
    
    await loadDrafts();
  } catch (err) {
    console.error('Load error:', err);
  } finally {
    loading.value = false;
  }
};

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

// Password update
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
    console.log('Update password', pwdForm);
    alert('更新成功，请重新登录');
    handleLogout();
  };
};

// Delete article
const confirmDelete = (item: any) => {
  confirmConfig.title = '确认删除此内容？';
  confirmConfig.message = `删除 "${item.title}" 后将无法找回，请谨慎操作。`;
  confirmConfig.type = 'danger';
  confirmConfig.show = true;
  confirmConfig.action = async () => {
    try {
      const res = await request.delete(`/article/delete/${item.id}`);
      if (res.data.code === 200) {
        myArticles.value = myArticles.value.filter(a => a.id !== item.id);
        if (subView.value) {
          closeSubView();
        }
      }
    } catch (err) {
      console.error('Delete error:', err);
    }
  };
};

// Delete draft
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
        if (subView.value) {
          closeSubView();
        }
      }
    } catch (err) {
      console.error('Delete draft error:', err);
      alert('删除草稿失败');
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

// Avatar upload
const avatarInputRef = ref<HTMLInputElement | null>(null);
const uploadingAvatar = ref(false);

const triggerAvatarUpload = () => {
  avatarInputRef.value?.click();
};

const handleAvatarChange = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
  if (!allowedTypes.includes(file.type)) {
    alert('请选择图片文件（jpg, png, gif, webp）');
    return;
  }
  if (file.size > 2 * 1024 * 1024) {
    alert('头像文件大小不能超过2MB');
    return;
  }

  uploadingAvatar.value = true;
  try {
    const formData = new FormData();
    formData.append('file', file);
    const res = await request.post('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    if (res.data.code === 200) {
      const avatarFileName = res.data.data.avatar;
      user.value.avatar = getAvatarUrl(avatarFileName);
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        const userData = JSON.parse(storedUser);
        userData.avatar = avatarFileName;
        localStorage.setItem('user', JSON.stringify(userData));
      }
      alert('头像上传成功！');
    }
  } catch (err: any) {
    alert(err.response?.data?.message || '头像上传失败');
  } finally {
    uploadingAvatar.value = false;
    if (avatarInputRef.value) avatarInputRef.value.value = '';
  }
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString();
};

onMounted(() => {
  document.title = '个人中心';
  const fromTab = route.query.from as string;
  if (fromTab && ['profile', 'content', 'drafts'].includes(fromTab)) {
    activeTab.value = fromTab;
    isDraftsView.value = fromTab === 'drafts';
  }
  loadData();
});
</script>

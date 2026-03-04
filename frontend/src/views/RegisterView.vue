<template>
  <div class="register-page min-h-screen w-full flex items-center justify-center bg-[#eef6ff] relative overflow-hidden">
    <!-- 背景装饰 -->
    <div class="bg-decoration-1"></div>

    <!-- 整体两栏容器 -->
    <div class="page-wrapper z-10">
      <!-- 左侧表单区 -->
      <div class="form-side">
        <!-- 顶部 Logo -->
        <div class="mb-10 text-center">
          <h1 class="text-4xl font-black text-gray-900 tracking-tighter flex items-center justify-center gap-2">
            <LayoutGrid :size="32" class="text-blue-600" />
            知拾录
          </h1>
        </div>

        <div class="register-card bg-white rounded-[40px] shadow-xl p-10 border border-white/20">
          <h2 class="text-4xl font-black text-gray-900 mb-10 text-center sm:text-left">创建新账号</h2>

          <form @submit.prevent="handleRegister" class="space-y-6">
            <!-- 用户名 -->
            <div class="input-row">
              <label class="label">用户名</label>
              <input v-model="regForm.username" type="text" class="custom-input" />
            </div>

            <!-- 邮筱 -->
            <div class="input-row">
              <label class="label">邮筱</label>
              <input v-model="regForm.email" type="email" class="custom-input" />
            </div>

            <!-- 手机号 -->
            <div class="input-row">
              <label class="label">手机号</label>
              <input v-model="regForm.phone" type="tel" class="custom-input" />
            </div>

            <!-- 密码 -->
            <div class="input-row">
              <label class="label">确认密码</label>
              <input v-model="regForm.password" type="password" class="custom-input" />
            </div>

            <!-- 用户协议 -->
            <div class="flex items-start gap-2 pt-2 px-1">
              <input type="checkbox" v-model="agreement" id="agree" class="mt-1 w-4 h-4 border-gray-300 text-blue-600" />
              <label for="agree" class="text-sm text-gray-500 leading-tight">
                我已阅读并同意
                <a href="#" class="text-blue-600 font-medium hover:underline">《用户协议》</a> 和
                <a href="#" class="text-blue-600 font-medium hover:underline">《隐私政策》</a>
              </label>
            </div>

            <!-- 注册按钮 -->
            <button
              type="submit"
              :disabled="!agreement || loading"
              class="register-btn w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-4 rounded-full transition-all shadow-lg active:scale-95 mt-4"
            >
              {{ loading ? '注册中...' : '注册' }}
            </button>
          </form>
        </div>

        <!-- 底部链接 -->
        <div class="mt-10 text-center text-gray-500 flex flex-col sm:flex-row items-center justify-center gap-2">
          <span class="text-xl">已有账号?</span>
          <router-link to="/login" class="text-blue-600 font-bold text-2xl hover:underline decoration-2 underline-offset-8">直接登录</router-link>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.register-page {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* 移除 Live2D canvas 边框 */
:global(#live2dcanvas) {
  border: none !important;
  outline: none !important;
  box-shadow: none !important;
}

.bg-decoration-1 {
  position: absolute;
  bottom: -100px;
  left: -100px;
  width: 400px;
  height: 400px;
  background: #3b82f6;
  border-radius: 50%;
  opacity: 0.15;
  filter: blur(80px);
}

/* 单栏居中布局 */
.page-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 520px;
  padding: 40px 24px;
}

.form-side {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.register-card {
  backdrop-filter: blur(20px);
  background-color: rgba(255, 255, 255, 0.9);
  width: 100%;
}

.input-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (min-width: 640px) {
  .input-row {
    flex-direction: row;
    align-items: center;
    gap: 24px;
  }
  .label {
    width: 100px;
    flex-shrink: 0;
  }
}

.label {
  font-size: 18px;
  font-weight: 500;
  color: #374151;
}

.custom-input {
  flex-grow: 1;
  padding: 12px 16px;
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  outline: none;
  transition: all 0.2s;
  font-size: 16px;
}

.custom-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.register-btn {
  box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.3);
}

.register-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { LayoutGrid } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import request from '../utils/request';

const router = useRouter();
const agreement = ref(false);
const loading = ref(false);

// 动态加载 Live2D 脚本
const loadLive2D = () => {
  return new Promise<void>((resolve) => {
    if ((window as any).L2Dwidget) {
      resolve();
      return;
    }
    const script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/live2d-widget@3.0.4/lib/L2Dwidget.min.js';
    script.onload = () => resolve();
    document.head.appendChild(script);
  });
};

// 如果已登录，直接跳转到首页
onMounted(async () => {
  const token = localStorage.getItem('token');
  if (token) {
    router.push('/');
    return;
  }
  // 只在桌面端（宽度 >= 1024px）初始化 Live2D
  if (window.innerWidth >= 1024) {
    await loadLive2D();
    (window as any).L2Dwidget.init({
      pluginRootPath: 'https://cdn.jsdelivr.net/npm/live2d-widget@3.0.4/',
      pluginJsPath: 'lib/',
      pluginModelPath: 'assets/moc/',
      tagMode: false,
      debug: false,
      model: { jsonPath: 'https://cdn.jsdelivr.net/npm/live2d-widget-model-shizuku@1.0.5/assets/shizuku.model.json' },
      display: { position: 'right', width: 200, height: 360, hOffset: 20, vOffset: 15 },
      mobile: { show: false },
      react: { opacityDefault: 1 }
    });
  }
});

const regForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: ''
});

const handleRegister = async () => {
  if (!regForm.username || !regForm.password) {
    alert('请输入用户名和密码');
    return;
  }

  if (!agreement.value) {
    alert('请阅读并同意用户协议');
    return;
  }

  loading.value = true;
  try {
    const res = await request.post('/auth/register', regForm);
    if (res.data.code === 200) {
      alert('注册成功，请登录');
      router.push('/login');
    } else {
      alert(res.data.message || '注册失败');
    }
  } catch (err: any) {
    alert(err.response?.data?.message || '注册失败，请稍后再试');
  } finally {
    loading.value = false;
  }
};
</script>

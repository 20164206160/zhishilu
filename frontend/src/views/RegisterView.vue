<template>
  <div class="register-page min-h-screen w-full flex flex-col items-center justify-center bg-[#eef6ff] relative overflow-hidden">
    <!-- 背景装饰 -->
    <div class="bg-decoration-1"></div>
    
    <!-- 顶部 Logo -->
    <div class="mb-10 text-center z-10">
      <h1 class="text-4xl font-black text-gray-900 tracking-tighter flex items-center justify-center gap-2">
        <LayoutGrid :size="32" class="text-blue-600" />
        知拾录
      </h1>
    </div>

    <div class="register-card-container w-full max-w-[520px] px-6 z-10">
      <div class="register-card bg-white rounded-[40px] shadow-xl p-10 border border-white/20">
        <h2 class="text-4xl font-black text-gray-900 mb-10 text-center sm:text-left">创建新账号</h2>

        <form @submit.prevent="handleRegister" class="space-y-6">
          <!-- 用户名 -->
          <div class="input-row">
            <label class="label">用户名</label>
            <input v-model="regForm.username" type="text" class="custom-input" />
          </div>

          <!-- 邮箱 -->
          <div class="input-row">
            <label class="label">邮箱</label>
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
</template>

<style scoped>
.register-page {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
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

.register-card {
  backdrop-filter: blur(20px);
  background-color: rgba(255, 255, 255, 0.9);
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
import { ref, reactive } from 'vue';
import { LayoutGrid } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import request from '../utils/request';

const router = useRouter();
const agreement = ref(false);
const loading = ref(false);

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

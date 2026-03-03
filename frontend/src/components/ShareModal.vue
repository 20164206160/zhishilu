<template>
  <teleport to="body">
    <transition name="fade">
      <div
        v-if="visible"
        class="fixed inset-0 bg-black/50 backdrop-blur-sm z-[200] flex items-center justify-center p-4"
        @click="handleClose"
      >
        <div
          class="bg-white rounded-2xl shadow-2xl w-full max-w-sm overflow-hidden"
          @click.stop
        >
          <!-- Header -->
          <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-800">分享到</h3>
            <button
              @click="handleClose"
              class="w-8 h-8 rounded-full hover:bg-gray-100 flex items-center justify-center text-gray-400 hover:text-gray-600 transition-colors"
            >
              <X :size="18" />
            </button>
          </div>

          <!-- Share Options -->
          <div class="p-6">
            <div class="grid grid-cols-4 gap-4 mb-6">
              <!-- WeChat -->
              <button
                @click="shareToWeChat"
                class="flex flex-col items-center gap-2 group"
              >
                <div class="w-14 h-14 rounded-2xl bg-[#07C160] flex items-center justify-center text-white shadow-lg shadow-green-200 group-hover:scale-105 transition-transform">
                  <svg viewBox="0 0 24 24" class="w-7 h-7" fill="currentColor">
                    <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.27-.027-.407-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982z"/>
                  </svg>
                </div>
                <span class="text-xs text-gray-600">微信</span>
              </button>

              <!-- QQ -->
              <button
                @click="shareToQQ"
                class="flex flex-col items-center gap-2 group"
              >
                <div class="w-14 h-14 rounded-2xl bg-[#12B7F5] flex items-center justify-center text-white shadow-lg shadow-blue-200 group-hover:scale-105 transition-transform">
                  <svg viewBox="0 0 24 24" class="w-7 h-7" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm3.5 14.5h-7c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5h7c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5zm0-4h-7c-.83 0-1.5-.67-1.5-1.5S7.67 9.5 8.5 9.5h7c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5z"/>
                  </svg>
                </div>
                <span class="text-xs text-gray-600">QQ</span>
              </button>

              <!-- Copy Link -->
              <button
                @click="copyLink"
                class="flex flex-col items-center gap-2 group"
              >
                <div class="w-14 h-14 rounded-2xl bg-gray-100 flex items-center justify-center text-gray-600 shadow-lg group-hover:scale-105 transition-transform">
                  <Link :size="24" />
                </div>
                <span class="text-xs text-gray-600">复制链接</span>
              </button>

              <!-- More -->
              <button
                @click="shareNative"
                class="flex flex-col items-center gap-2 group"
              >
                <div class="w-14 h-14 rounded-2xl bg-gray-100 flex items-center justify-center text-gray-600 shadow-lg group-hover:scale-105 transition-transform">
                  <MoreHorizontal :size="24" />
                </div>
                <span class="text-xs text-gray-600">更多</span>
              </button>
            </div>

            <!-- QR Code Section (for WeChat sharing) -->
            <div v-if="showQRCode" class="border-t border-gray-100 pt-4">
              <p class="text-sm text-gray-500 text-center mb-3">微信扫一扫，分享文章</p>
              <div class="flex justify-center">
                <div class="w-40 h-40 bg-gray-50 rounded-lg flex items-center justify-center">
                  <!-- QR Code placeholder - in real implementation, generate actual QR code -->
                  <div class="text-center">
                    <QrCode :size="48" class="mx-auto text-gray-400 mb-2" />
                    <p class="text-xs text-gray-400">二维码区域</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { X, Link, MoreHorizontal, QrCode } from 'lucide-vue-next';

interface Props {
  visible: boolean;
  title?: string;
  description?: string;
  url?: string;
  image?: string;
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  description: '',
  url: '',
  image: ''
});

const emit = defineEmits<{
  close: [];
}>();

const showQRCode = ref(false);
const copied = ref(false);

// 获取当前页面URL
const currentUrl = computed(() => {
  if (props.url) return props.url;
  return window.location.href;
});

// 获取分享标题
const shareTitle = computed(() => {
  if (props.title) return props.title;
  return document.title || '知拾录';
});

// 获取分享描述
const shareDesc = computed(() => {
  if (props.description) return props.description;
  return '发现了一篇好文章，分享给你';
});

// 关闭弹窗
const handleClose = () => {
  showQRCode.value = false;
  copied.value = false;
  emit('close');
};

// 分享到微信
const shareToWeChat = () => {
  // 在微信内置浏览器中直接调用分享
  const ua = navigator.userAgent.toLowerCase();
  const isWeChat = ua.includes('micromessenger');
  
  if (isWeChat) {
    // 在微信中显示提示，让用户点击右上角菜单分享
    alert('请点击右上角菜单，选择"分享到朋友圈"或"发送给朋友"');
  } else {
    // 显示二维码，让用户用微信扫描
    showQRCode.value = !showQRCode.value;
  }
};

// 分享到QQ
const shareToQQ = () => {
  const url = encodeURIComponent(currentUrl.value);
  const title = encodeURIComponent(shareTitle.value);
  const desc = encodeURIComponent(shareDesc.value);
  const pics = props.image ? encodeURIComponent(props.image) : '';
  
  // QQ好友分享
  const qqUrl = `https://connect.qq.com/widget/shareqq/index.html?url=${url}&title=${title}&desc=${desc}&pics=${pics}`;
  
  // QQ空间分享
  const qzoneUrl = `https://sns.qzone.qq.com/cgi-bin/qzshare/cgi_qzshare_onekey?url=${url}&title=${title}&desc=${desc}&pics=${pics}`;
  
  // 默认打开QQ好友分享
  window.open(qqUrl, '_blank', 'width=600,height=500');
};

// 复制链接
const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(currentUrl.value);
    copied.value = true;
    alert('链接已复制到剪贴板');
    setTimeout(() => {
      copied.value = false;
    }, 2000);
  } catch (err) {
    // 降级方案
    const input = document.createElement('input');
    input.value = currentUrl.value;
    document.body.appendChild(input);
    input.select();
    document.execCommand('copy');
    document.body.removeChild(input);
    alert('链接已复制到剪贴板');
  }
};

// 调用原生分享（移动端）
const shareNative = async () => {
  const ua = navigator.userAgent.toLowerCase();
  const isMobile = /iphone|ipad|ipod|android|mobile/.test(ua);
  
  // 检查是否支持 Web Share API
  if (navigator.share && isMobile) {
    try {
      await navigator.share({
        title: shareTitle.value,
        text: shareDesc.value,
        url: currentUrl.value,
      });
    } catch (err) {
      // 用户取消分享
      console.log('分享取消');
    }
  } else {
    // 不支持原生分享，显示更多选项
    const moreOptions = confirm('更多分享选项：\n点击"确定"使用微博分享，点击"取消"使用邮件分享');
    if (moreOptions) {
      // 微博分享
      const wbUrl = `https://service.weibo.com/share/share.php?url=${encodeURIComponent(currentUrl.value)}&title=${encodeURIComponent(shareTitle.value)}`;
      window.open(wbUrl, '_blank');
    } else {
      // 邮件分享
      const mailUrl = `mailto:?subject=${encodeURIComponent(shareTitle.value)}&body=${encodeURIComponent(shareDesc.value + '\n\n' + currentUrl.value)}`;
      window.location.href = mailUrl;
    }
  }
};
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

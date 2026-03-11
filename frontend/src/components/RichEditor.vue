<template>
  <div class="rich-editor-wrapper">
    <Toolbar
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      :mode="mode"
      class="editor-toolbar"
    />
    <Editor
      :defaultConfig="editorConfig"
      :mode="mode"
      v-model="valueHtml"
      @onCreated="handleCreated"
      @onChange="handleChange"
      class="editor-content"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, onBeforeUnmount, watch } from 'vue';
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import type { IDomEditor, IEditorConfig } from '@wangeditor/editor';
import request from '../utils/request';
import '@wangeditor/editor/dist/css/style.css';

interface Props {
  modelValue: string;
  placeholder?: string;
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请输入内容...'
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef<IDomEditor>();

// 内容 HTML
const valueHtml = ref(props.modelValue);

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  if (newVal !== valueHtml.value) {
    valueHtml.value = newVal;
  }
});

// 工具栏配置
const toolbarConfig = {
  excludeKeys: []
};

// 编辑器配置
const editorConfig: Partial<IEditorConfig> = {
  placeholder: props.placeholder,
  MENU_CONF: {
    // 图片上传配置
    uploadImage: {
      async customUpload(file: File, insertFn: (url: string, alt?: string, href?: string) => void) {
        try {
          const formData = new FormData();
          formData.append('file', file);
          
          const res = await request.post('/file/upload', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          });
          
          if (res.data.code === 200 && res.data.data.path) {
            // 使用完整的图片 URL
            const imageUrl = import.meta.env.VITE_API_BASE_URL 
              ? `${import.meta.env.VITE_API_BASE_URL}${res.data.data.path}`
              : res.data.data.path;
            insertFn(imageUrl, file.name, imageUrl);
          } else {
            alert('图片上传失败');
          }
        } catch (err) {
          console.error('Upload error:', err);
          alert('图片上传失败');
        }
      }
    },
    // 视频上传配置
    uploadVideo: {
      async customUpload(file: File, insertFn: (url: string, poster?: string) => void) {
        try {
          const formData = new FormData();
          formData.append('file', file);
          
          const res = await request.post('/file/upload', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          });
          
          if (res.data.code === 200 && res.data.data.path) {
            const videoUrl = import.meta.env.VITE_API_BASE_URL 
              ? `${import.meta.env.VITE_API_BASE_URL}${res.data.data.path}`
              : res.data.data.path;
            insertFn(videoUrl);
          } else {
            alert('视频上传失败');
          }
        } catch (err) {
          console.error('Upload error:', err);
          alert('视频上传失败');
        }
      }
    }
  }
};

// 模式
const mode = 'default';

// 编辑器创建完成
const handleCreated = (editor: IDomEditor) => {
  editorRef.value = editor;
};

// 内容变化
const handleChange = (editor: IDomEditor) => {
  const html = editor.getHtml();
  valueHtml.value = html;
  emit('update:modelValue', html);
};

// 组件销毁时，销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value;
  if (editor) {
    editor.destroy();
  }
});
</script>

<style scoped>
.rich-editor-wrapper {
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  overflow: hidden;
}

.editor-toolbar {
  border-bottom: 1px solid #e5e7eb;
}

.editor-content {
  min-height: 300px;
  background-color: #f9fafb;
}

:deep(.w-e-text-container) {
  background-color: #f9fafb !important;
}

:deep(.w-e-bar) {
  background-color: #ffffff;
}

:deep(.w-e-bar-item button) {
  color: #4b5563;
}

:deep(.w-e-bar-item button:hover) {
  color: #2563eb;
  background-color: #eff6ff;
}
</style>

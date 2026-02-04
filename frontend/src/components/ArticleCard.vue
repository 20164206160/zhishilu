<template>
  <div class="bg-white rounded-lg overflow-hidden shadow-sm hover:shadow-md transition-shadow duration-300 border border-gray-100 flex flex-col h-full cursor-pointer">
    <div class="aspect-square bg-gray-100 relative overflow-hidden group">
      <img 
        v-if="article.images && article.images.length > 0" 
        :src="article.images[0]" 
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
        alt="article cover"
      />
      <div v-else class="w-full h-full flex items-center justify-center text-gray-400">
        <ImageIcon :size="48" />
      </div>
      <div class="absolute top-2 left-2 px-2 py-0.5 bg-black/50 text-white text-xs rounded-full backdrop-blur-sm">
        {{ article.category }}
      </div>
    </div>
    <div class="p-3 flex flex-col flex-grow">
      <h3 class="text-sm font-medium text-gray-900 line-clamp-2 mb-2 min-h-[2.5rem]">
        {{ article.title }}
      </h3>
      <div class="mt-auto flex items-center justify-between">
        <div class="flex items-center text-xs text-gray-500">
          <UserIcon :size="12" class="mr-1" />
          {{ article.createdBy }}
        </div>
        <div class="text-[10px] text-gray-400">
          {{ formatDate(article.createdTime) }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Image as ImageIcon, User as UserIcon } from 'lucide-vue-next';

defineProps<{
  article: any
}>();

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString();
};
</script>

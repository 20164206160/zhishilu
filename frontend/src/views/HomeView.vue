<template>
  <div class="min-h-screen bg-[#f4f4f4] flex flex-col font-sans w-full overflow-x-hidden">
    <!-- 上部分：导航与搜索 (Header) -->
    <header class="bg-white shadow-sm sticky top-0 z-50 w-full">
      <!-- 顶栏：品牌与主操作 -->
      <div class="max-w-7xl mx-auto px-2 sm:px-4 h-14 sm:h-16 flex items-center justify-between gap-2 sm:gap-4">
        <h1 class="text-xl sm:text-2xl font-black italic tracking-tighter text-blue-600 flex-shrink-0">
          知拾录<span class="text-emerald-500">.</span>
        </h1>

        <!-- 搜索框 -->
        <div class="flex-grow max-w-2xl relative min-w-0">
          <div class="flex items-center">
            <!-- 字段选择下拉框 -->
            <div class="relative flex-shrink-0">
              <select
                v-model="searchField"
                class="appearance-none px-2 sm:px-4 py-2 sm:py-2.5 bg-gray-100 border-2 border-r-0 border-transparent rounded-l-full text-xs sm:text-sm text-gray-600 focus:bg-white focus:border-blue-500 outline-none cursor-pointer hover:bg-gray-200 transition-colors pr-6 sm:pr-8"
              >
                <option value="all">全部</option>
                <option value="title">标题</option>
                <option value="category">类别</option>
                <option value="content">内容</option>
                <option value="username">用户名</option>
                <option value="location">地点</option>
              </select>
              <ChevronDown class="absolute right-1 sm:right-2 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" :size="12" />
            </div>
            <div class="relative flex-grow group min-w-0">
              <SearchIcon class="absolute left-2 sm:left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-blue-500 transition-colors" :size="16" />
              <input
                v-model="searchQuery"
                @keyup.enter="handleSearch"
                @input="handleInput"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
                type="text"
                :placeholder="getPlaceholder()"
                class="w-full pl-8 sm:pl-11 pr-16 sm:pr-28 py-2 sm:py-2.5 bg-gray-100 border-2 border-l-0 border-transparent rounded-r-full focus:bg-white focus:border-blue-500 focus:ring-2 sm:focus:ring-4 focus:ring-blue-50 transition-all outline-none text-xs sm:text-sm"
              />
              <!-- 清空按钮 -->
              <button
                v-if="searchQuery || selectedCategory !== '全部'"
                @click="resetFilters"
                class="hidden sm:flex absolute right-[70px] top-1/2 -translate-y-1/2 px-2 py-1 rounded-md bg-orange-500 hover:bg-orange-600 text-white text-xs font-bold items-center gap-1 transition-colors shadow-sm"
                title="清空查询条件"
              >
                <XIcon :size="12" />
                <span>清空</span>
              </button>
              <button
                @click="handleSearch"
                class="absolute right-1 top-1/2 -translate-y-1/2 bg-blue-600 hover:bg-blue-700 text-white px-2 sm:px-4 py-1 sm:py-1.5 rounded-full text-xs font-bold transition-all shadow-sm active:scale-95"
              >
                <span class="hidden sm:inline">搜索</span>
                <SearchIcon class="sm:hidden" :size="14" />
              </button>
              
              <!-- 搜索补全下拉框 -->
              <div
                v-if="showSuggestions && hasSuggestions"
                class="absolute top-full left-0 right-0 mt-1 bg-white rounded-xl shadow-lg border border-gray-100 overflow-hidden z-50"
              >
                <!-- 用户名补全 -->
                <div v-if="suggestions.usernames?.length" class="border-b border-gray-50 last:border-b-0">
                  <div class="px-3 py-1.5 bg-gray-50 text-[10px] text-gray-400 font-medium">用户名</div>
                  <div
                    v-for="item in suggestions.usernames"
                    :key="'user-' + item.text"
                    @mousedown.prevent="applySuggestion(item.text, 'username')"
                    class="px-3 py-2 hover:bg-blue-50 cursor-pointer flex items-center justify-between group"
                  >
                    <span class="text-sm text-gray-700 group-hover:text-blue-600">{{ item.text }}</span>
                  </div>
                </div>
                
                <!-- 地点补全 -->
                <div v-if="suggestions.locations?.length" class="border-b border-gray-50 last:border-b-0">
                  <div class="px-3 py-1.5 bg-gray-50 text-[10px] text-gray-400 font-medium">地点</div>
                  <div
                    v-for="item in suggestions.locations"
                    :key="'loc-' + item.text"
                    @mousedown.prevent="applySuggestion(item.text, 'location')"
                    class="px-3 py-2 hover:bg-blue-50 cursor-pointer flex items-center justify-between group"
                  >
                    <span class="text-sm text-gray-700 group-hover:text-blue-600">{{ item.text }}</span>
                  </div>
                </div>
                
                <!-- 类别补全 -->
                <div v-if="suggestions.categories?.length" class="border-b border-gray-50 last:border-b-0">
                  <div class="px-3 py-1.5 bg-gray-50 text-[10px] text-gray-400 font-medium">类别</div>
                  <div
                    v-for="item in suggestions.categories"
                    :key="'cat-' + item.text"
                    @mousedown.prevent="applySuggestion(item.text, 'category')"
                    class="px-3 py-2 hover:bg-blue-50 cursor-pointer flex items-center justify-between group"
                  >
                    <span class="text-sm text-gray-700 group-hover:text-blue-600">{{ item.text }}</span>
                  </div>
                </div>
                
                <!-- 标题补全 -->
                <div v-if="suggestions.titles?.length" class="border-b border-gray-50 last:border-b-0">
                  <div class="px-3 py-1.5 bg-gray-50 text-[10px] text-gray-400 font-medium">标题</div>
                  <div
                    v-for="item in suggestions.titles"
                    :key="'title-' + item.text"
                    @mousedown.prevent="applySuggestion(item.text, 'title')"
                    class="px-3 py-2 hover:bg-blue-50 cursor-pointer flex items-center justify-between group"
                  >
                    <span class="text-sm text-gray-700 group-hover:text-blue-600 truncate max-w-[200px]">{{ item.text }}</span>
                  </div>
                </div>
                
                <!-- 内容补全 -->
                <div v-if="suggestions.contents?.length">
                  <div class="px-3 py-1.5 bg-gray-50 text-[10px] text-gray-400 font-medium">内容</div>
                  <div
                    v-for="item in suggestions.contents"
                    :key="'content-' + item.text"
                    @mousedown.prevent="applySuggestion(item.text, 'content')"
                    class="px-3 py-2 hover:bg-blue-50 cursor-pointer flex items-center justify-between group"
                  >
                    <span class="text-sm text-gray-700 group-hover:text-blue-600 truncate max-w-[200px]">{{ item.text }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex items-center gap-1 sm:gap-3 flex-shrink-0">
          <router-link to="/post" class="flex items-center gap-1 bg-emerald-500 hover:bg-emerald-600 text-white px-2 sm:px-4 py-2 rounded-full text-xs sm:text-sm font-bold transition-all shadow-md active:scale-95">
            <PlusIcon :size="16" stroke-width="3" />
            <span class="hidden sm:inline">新增</span>
          </router-link>
          <router-link to="/profile" class="w-8 h-8 sm:w-9 sm:h-9 rounded-full bg-gradient-to-tr from-blue-100 to-blue-50 border border-blue-200 flex items-center justify-center text-blue-600 font-bold cursor-pointer hover:shadow-inner overflow-hidden flex-shrink-0">
            <img v-if="currentUserAvatar" :src="currentUserAvatar" class="w-full h-full object-cover" alt="avatar" />
            <span v-else-if="currentUserName">{{ currentUserName.charAt(0).toUpperCase() }}</span>
            <UserIcon v-else :size="16" />
          </router-link>
        </div>
      </div>

      <!-- 搜索下栏：热门与分类 -->
      <div class="max-w-7xl mx-auto px-2 sm:px-4 pb-2 sm:pb-3 flex flex-col gap-2 sm:gap-3">
        <!-- 热门提示词 -->
        <div class="flex items-center gap-2 sm:gap-3 overflow-x-auto scrollbar-hide py-1">
          <span class="text-[10px] sm:text-[11px] font-bold text-gray-400 uppercase tracking-widest whitespace-nowrap flex-shrink-0">热门:</span>
          <button
            v-for="word in hotWords"
            :key="word"
            @click="quickSearch(word)"
            class="text-[11px] sm:text-xs text-gray-500 hover:text-blue-600 transition-colors whitespace-nowrap"
          >
            {{ word }}
          </button>
        </div>

        <!-- 分类选择 -->
        <div class="flex items-center gap-1.5 sm:gap-2 overflow-x-auto scrollbar-hide pb-1">
          <button
            @click="selectedCategory = '全部'"
            :class="[
              'px-3 sm:px-5 py-1 sm:py-1.5 rounded-full text-[11px] sm:text-xs font-medium transition-all whitespace-nowrap border',
              selectedCategory === '全部'
                ? 'bg-blue-600 border-blue-600 text-white shadow-md'
                : 'bg-white border-gray-200 text-gray-600 hover:border-blue-400 hover:text-blue-500'
            ]"
          >
            全部
          </button>
          <button
            v-for="cat in categories"
            :key="cat.category"
            @click="selectedCategory = cat.category"
            :class="[
              'px-3 sm:px-5 py-1 sm:py-1.5 rounded-full text-[11px] sm:text-xs font-medium transition-all whitespace-nowrap border',
              selectedCategory === cat.category
                ? 'bg-blue-600 border-blue-600 text-white shadow-md'
                : 'bg-white border-gray-200 text-gray-600 hover:border-blue-400 hover:text-blue-500'
            ]"
          >
            {{ cat.category }}
          </button>
        </div>
      </div>
    </header>

    <!-- 中部分：内容展示区 (Main Grid) -->
    <main class="flex-grow max-w-7xl mx-auto w-full px-2 sm:px-4 py-4 sm:py-6">
      <!-- 移动端：2列，平板：3列，小桌面：4列，大桌面：5列，超大屏：6列 -->
      <div v-if="loading" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-2 sm:gap-4">
        <div v-for="i in 12" :key="i" class="bg-white border border-gray-100 rounded-xl overflow-hidden animate-pulse">
          <!-- 图片占位 -->
          <div class="aspect-square bg-gray-100"></div>
          <!-- 文字占位 -->
          <div class="p-2.5 space-y-1.5">
            <div class="h-3 bg-gray-100 rounded-full w-4/5"></div>
            <div class="h-3 bg-gray-100 rounded-full w-3/5"></div>
            <div class="h-2.5 bg-gray-50 rounded-full w-2/5 mt-2"></div>
          </div>
        </div>
      </div>

      <div v-else-if="articles.length > 0" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-2 sm:gap-4">
        <ArticleCard v-for="item in articles" :key="item.id" :article="item" :search-keyword="currentSearchKeyword" @open-modal="openArticleModal" />
      </div>
      
      <div v-else class="h-[50vh] flex flex-col items-center justify-center text-gray-400 bg-white rounded-3xl shadow-inner border border-gray-50">
        <div class="p-8 bg-gray-50 rounded-full mb-4">
          <EmptyIcon :size="80" stroke-width="1" class="text-gray-200" />
        </div>
        <p class="text-sm font-medium">暂时没有找到相关内容呢~</p>
        <button @click="resetFilters" class="mt-4 text-blue-500 text-xs font-bold hover:underline">清除筛选</button>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="mt-8 sm:mt-12 mb-6 sm:mb-8 flex items-center justify-center gap-2 sm:gap-4">
        <button
          @click="page--"
          :disabled="page === 0"
          class="p-1.5 sm:p-2 rounded-xl border border-gray-200 bg-white disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors shadow-sm"
        >
          <ChevronLeft :size="18" class="sm:w-5 sm:h-5" />
        </button>
        <div class="flex items-center gap-1.5 sm:gap-2">
          <span class="px-3 sm:px-4 py-1 sm:py-1.5 bg-white border border-gray-200 rounded-xl text-xs font-bold text-blue-600 shadow-sm">
            {{ page + 1 }}
          </span>
          <span class="text-xs text-gray-400 font-medium">/ {{ totalPages }}</span>
        </div>
        <button
          @click="page++"
          :disabled="page >= totalPages - 1"
          class="p-1.5 sm:p-2 rounded-xl border border-gray-200 bg-white disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors shadow-sm"
        >
          <ChevronRight :size="18" class="sm:w-5 sm:h-5" />
        </button>
      </div>
    </main>

    <!-- 下部分：开发者信息 (Footer) -->
    <footer class="bg-white border-t border-gray-100 py-6 sm:py-10 mt-auto w-full">
      <div class="max-w-7xl mx-auto px-2 sm:px-4">
        <div class="flex flex-col md:flex-row justify-between items-center gap-4 sm:gap-8">
          <!-- 站点信息 -->
          <div class="text-center md:text-left space-y-1 sm:space-y-2">
            <h2 class="text-base sm:text-lg font-bold text-gray-800">知拾录 <span class="text-gray-300 font-light ml-1 text-sm sm:text-base">Personal Knowledge Library</span></h2>
            <p class="text-[10px] sm:text-xs text-gray-400 max-w-xs leading-relaxed px-4 md:px-0">
              知拾录致力于为用户提供一个优雅、高效的个人知识收藏与管理平台。随时随地，随手记，随心搜。
            </p>
          </div>

          <!-- 开发者信息 -->
          <div class="flex flex-col items-center md:items-end gap-2 sm:gap-4">
            <div class="flex items-center gap-4 sm:gap-6">
              <a href="#" class="text-gray-400 hover:text-blue-500 transition-colors"><GithubIcon :size="18" class="sm:w-5 sm:h-5" /></a>
              <a href="#" class="text-gray-400 hover:text-blue-500 transition-colors"><MailIcon :size="18" class="sm:w-5 sm:h-5" /></a>
              <a href="#" class="text-gray-400 hover:text-blue-500 transition-colors"><TwitterIcon :size="18" class="sm:w-5 sm:h-5" /></a>
            </div>
            <div class="text-[10px] sm:text-[11px] text-gray-400 text-center md:text-right font-medium">
              <p>© 2026 知拾录. 版权所有.</p>
              <p class="mt-0.5 sm:mt-1">Designed & Developed by <span class="text-gray-600 hover:text-blue-500 cursor-pointer">Zhishilu Team</span></p>
            </div>
          </div>
        </div>
      </div>
    </footer>

    <!-- 桌面端详情页弹窗 -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showModal" @click="closeModal" class="fixed inset-0 bg-black/70 backdrop-blur-sm z-[100] flex items-center justify-center p-0 md:p-4">
          <div @click.stop :class="modalArticle?.images?.length ? 'md:max-w-[1000px] lg:max-w-[1100px]' : 'md:max-w-[680px]'" class="relative w-full h-full md:h-[90vh] bg-white md:rounded-2xl shadow-2xl overflow-hidden flex">
            <!-- Mobile Close Button -->
            <button @click="closeModal" class="md:hidden absolute top-4 right-4 z-50 w-10 h-10 rounded-full bg-black/30 text-white flex items-center justify-center">
              <XIcon :size="20" />
            </button>
            
            <!-- 详情页内容 -->
            <div v-if="modalArticle" class="flex w-full h-full flex-col md:flex-row">
              <!-- 左侧图片区（仅当有图片时显示） -->
              <section 
                v-if="modalArticle.images?.length" 
                class="w-full md:flex-1 h-[50vh] md:h-auto relative bg-black flex items-center justify-center overflow-hidden group"
                @touchstart="handleTouchStart"
                @touchmove="handleTouchMove"
                @touchend="handleTouchEnd"
              >
                <!-- Image Counter -->
                <div v-if="modalArticle.images.length > 1" class="absolute top-4 left-1/2 -translate-x-1/2 z-10 px-3 py-1 bg-black/40 rounded-full text-white text-xs font-medium backdrop-blur-sm">
                  {{ currentImgIndex + 1 }} / {{ modalArticle.images.length }}
                </div>

                <div class="flex h-full w-full transition-transform duration-300 ease-out" :style="{ transform: `translateX(-${currentImgIndex * 100}%)` }">
                  <img v-for="(img, i) in modalArticle.images" :key="i" :src="getImageUrl(img)" @click="openImagePreview(i)" class="w-full h-full object-contain flex-shrink-0 cursor-zoom-in" />
                </div>
                
                <!-- Dot Indicators -->
                <div v-if="modalArticle.images.length > 1" class="absolute bottom-4 inset-x-0 flex justify-center gap-2 z-10">
                  <button 
                    v-for="(_, i) in modalArticle.images" 
                    :key="i" 
                    @click="currentImgIndex = i"
                    class="h-2 rounded-full transition-all duration-300" 
                    :class="[currentImgIndex === i ? 'w-5 bg-white' : 'w-2 bg-white/40 hover:bg-white/60']"
                  ></button>
                </div>

                <!-- Navigation Arrows -->
                <template v-if="modalArticle.images.length > 1">
                  <button 
                    @click="currentImgIndex = (currentImgIndex - 1 + modalArticle.images.length) % modalArticle.images.length" 
                    class="absolute left-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all backdrop-blur-sm"
                  >
                    <ChevronLeft :size="24" />
                  </button>
                  <button 
                    @click="currentImgIndex = (currentImgIndex + 1) % modalArticle.images.length" 
                    class="absolute right-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all backdrop-blur-sm"
                  >
                    <ChevronRight :size="24" />
                  </button>
                </template>
              </section>

              <!-- 右侧内容区 -->
              <aside :class="modalArticle.images?.length ? 'w-full md:w-[380px] lg:w-[420px]' : 'w-full'" class="flex flex-col bg-white overflow-hidden">
                <!-- 用户信息 -->
                <div class="px-5 py-4 border-b border-[#f0f0f0] flex items-center justify-between bg-white shrink-0">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full bg-gradient-to-tr from-blue-500 to-blue-400 flex items-center justify-center text-white font-bold text-base shadow-sm overflow-hidden">
                      <img v-if="modalArticle.creatorAvatar" :src="getAvatarUrl(modalArticle.creatorAvatar)" class="w-full h-full object-cover" alt="avatar" />
                      <template v-else>{{ modalArticle.createdBy?.charAt(0) || 'U' }}</template>
                    </div>
                    <div>
                      <p class="text-[15px] font-semibold text-[#333] leading-tight">{{ modalArticle.createdBy }}</p>
                      <p class="text-[12px] text-[#999] mt-0.5">{{ formatModalDate(modalArticle.createdTime) }}</p>
                    </div>
                  </div>
                  <button
                    v-if="isCurrentUserArticle"
                    @click="goToEdit"
                    class="px-5 py-1.5 bg-emerald-500 hover:bg-emerald-600 text-white text-sm font-medium rounded-full transition-colors"
                  >
                    编辑
                  </button>
                  <button
                    v-else
                    class="px-5 py-1.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-full transition-colors"
                  >
                    关注
                  </button>
                </div>

                <!-- 可滚动内容 -->
                <div class="flex-grow overflow-y-auto">
                  <div class="px-5 py-5">
                    <h1 class="text-lg font-bold text-[#333] leading-snug mb-4">{{ modalArticle.title }}</h1>
                    <div v-if="modalArticle.content" class="text-[15px] text-[#333] leading-[1.8] prose prose-sm max-w-none" v-html="modalArticle.content"></div>

                    <!-- Tags -->
                    <div v-if="modalArticle.categories?.length" class="flex flex-wrap gap-2 mt-5">
                      <span 
                        v-for="(cat, index) in modalArticle.categories" 
                        :key="index"
                        class="text-[#576b95] text-[14px] hover:underline cursor-pointer"
                      >
                        #{{ cat }}
                      </span>
                    </div>

                    <!-- Location -->
                    <div v-if="modalArticle.location" class="flex items-center gap-1.5 mt-4 text-[#576b95]">
                      <MapPin :size="14" />
                      <span class="text-[13px]">{{ modalArticle.location }}</span>
                    </div>

                    <!-- Publish Time -->
                    <div class="mt-4 text-[12px] text-[#999]">
                      {{ formatFullModalDate(modalArticle.createdTime) }}
                    </div>
                  </div>

                  <!-- Comments Section -->
                  <div class="border-t border-[#f0f0f0]">
                    <div class="px-5 py-3">
                      <span class="text-[14px] font-medium text-[#333]">共 {{ mockComments.length }} 条评论</span>
                    </div>
                    <div class="px-5 pb-4 space-y-4">
                      <div v-for="(comment, idx) in mockComments" :key="idx" class="flex gap-3">
                        <div class="w-8 h-8 rounded-full bg-gradient-to-tr from-gray-400 to-gray-300 flex items-center justify-center text-white text-xs font-bold flex-shrink-0 overflow-hidden">
                          {{ comment.user.charAt(0) }}
                        </div>
                        <div class="flex-1 min-w-0">
                          <span class="text-[13px] text-[#576b95] font-medium">{{ comment.user }}</span>
                          <p class="text-[14px] text-[#333] mt-0.5 leading-relaxed">{{ comment.content }}</p>
                          <div class="flex items-center gap-3 mt-1.5 text-[12px] text-[#999]">
                            <span>{{ comment.time }}</span>
                            <button class="hover:text-[#666] transition-colors">回复</button>
                          </div>
                        </div>
                        <button class="flex flex-col items-center gap-0.5 text-[#999] hover:text-blue-600 transition-colors flex-shrink-0">
                          <Heart :size="14" />
                          <span class="text-[10px]">{{ comment.likes }}</span>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 互动底部 -->
                <div class="px-5 py-3 border-t border-[#f0f0f0] bg-white shrink-0">
                  <div class="flex items-center justify-between mb-3">
                    <div class="flex items-center gap-5">
                      <button class="flex items-center gap-1.5 text-[#333] hover:text-blue-600 transition-colors group">
                        <Heart :size="22" class="group-hover:scale-110 transition-transform" />
                        <span class="text-[13px] font-medium">3.5万</span>
                      </button>
                      <button class="flex items-center gap-1.5 text-[#333] hover:text-[#ffac33] transition-colors group">
                        <Star :size="22" class="group-hover:scale-110 transition-transform" />
                        <span class="text-[13px] font-medium">3720</span>
                      </button>
                      <button class="flex items-center gap-1.5 text-[#333] hover:text-[#576b95] transition-colors group">
                        <MessageCircle :size="22" class="group-hover:scale-110 transition-transform" />
                        <span class="text-[13px] font-medium">3933</span>
                      </button>
                    </div>
                    <button class="text-[#333] hover:text-[#666] transition-colors">
                      <Share2 :size="22" />
                    </button>
                  </div>
                  
                  <div class="relative">
                    <input 
                      type="text" 
                      placeholder="说点什么..." 
                      class="w-full bg-[#f5f5f5] border-none rounded-full py-2.5 px-4 pr-12 text-[14px] text-[#333] placeholder:text-[#999] focus:outline-none focus:ring-2 focus:ring-blue-100 transition-all"
                    />
                    <button class="absolute right-2 top-1/2 -translate-y-1/2 px-3 py-1 text-blue-600 text-[13px] font-medium hover:bg-blue-50 rounded-full transition-colors">
                      发送
                    </button>
                  </div>
                </div>
              </aside>
            </div>

            <!-- Loading State -->
            <div v-else class="flex items-center justify-center w-full h-full">
              <div class="flex flex-col items-center gap-3">
                <div class="animate-spin rounded-full h-8 w-8 border-3 border-blue-600 border-t-transparent"></div>
                <span class="text-[13px] text-[#999]">加载中...</span>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <!-- 图片大图预览 -->
    <teleport to="body">
      <transition name="fade">
        <div v-if="showImagePreview" @click="closeImagePreview" class="fixed inset-0 bg-black/95 z-[110] flex items-center justify-center">
          <button @click="closeImagePreview" class="absolute top-4 right-4 z-50 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors">
            <XIcon :size="24" />
          </button>
          
          <div class="relative w-screen h-screen overflow-hidden">
            <div class="flex h-full transition-transform duration-500 ease-out" :style="{ transform: `translateX(-${previewImgIndex * 100}vw)` }">
              <div 
                v-for="(img, i) in (modalArticle?.images || [])" 
                :key="i" 
                class="w-screen h-full flex-shrink-0 flex items-center justify-center p-4"
                @click.stop
              >
                <img 
                  :src="getImageUrl(img)" 
                  class="max-w-full max-h-full object-contain" 
                />
              </div>
            </div>
            
            <!-- 导航按钮 -->
            <template v-if="(modalArticle?.images?.length || 0) > 1">
              <button @click.stop="previewImgIndex = (previewImgIndex - 1 + (modalArticle?.images?.length || 0)) % (modalArticle?.images?.length || 1)" class="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors">
                <ChevronLeft :size="28" />
              </button>
              <button @click.stop="previewImgIndex = (previewImgIndex + 1) % (modalArticle?.images?.length || 1)" class="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors">
                <ChevronRight :size="28" />
              </button>
            </template>

            <!-- 指示器 -->
            <div v-if="(modalArticle?.images?.length || 0) > 1" class="absolute bottom-8 inset-x-0 flex justify-center gap-2 z-10">
              <div v-for="(_, i) in (modalArticle?.images || [])" :key="i" class="h-2 rounded-full transition-all" :class="[previewImgIndex === i ? 'w-8 bg-white' : 'w-2 bg-white/50']"></div>
            </div>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'HomeView'
});

import { ref, onMounted, watch, onActivated, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { 
  Search as SearchIcon, 
  Plus as PlusIcon, 
  Inbox as EmptyIcon, 
  ChevronLeft, 
  ChevronRight,
  ChevronDown,
  Github as GithubIcon,
  Mail as MailIcon,
  Twitter as TwitterIcon,
  X as XIcon,
  User as UserIcon,
  Tag,
  MapPin,
  Link as LinkIcon,
  Heart,
  Star,
  MessageCircle,
  Share2
} from 'lucide-vue-next';
import request from '../utils/request';
import ArticleCard from '../components/ArticleCard.vue';
import { getAvatarUrl, getImageUrl } from '../utils/image';

const route = useRoute();
const router = useRouter();

// 状态定义
const searchQuery = ref('');
const searchField = ref('all');
const selectedCategory = ref('全部');
const categories = ref<CategoryStatResp[]>([]);
const hotWords = ref(['Vue3', 'SpringBoot', 'Elasticsearch', 'Tailwind', '设计模式', '面试题']);
const articles = ref<any[]>([]);
const loading = ref(true);
const page = ref(0);
const size = ref(24); // 淘宝风格网格通常每页展示较多
const totalPages = ref(0);
const currentSearchKeyword = ref(''); // 当前搜索关键词，用于正文高亮
const currentUserAvatar = ref('');
const currentUserName = ref('');

// 弹窗相关
const showModal = ref(false);
const modalArticle = ref<any>(null);
const currentImgIndex = ref(0);

// 图片预览相关
const showImagePreview = ref(false);
const previewImgIndex = ref(0);

// 触摸滑动相关
const touchStartX = ref(0);
const touchEndX = ref(0);
const minSwipeDistance = 50;

// 搜索补全相关
const suggestions = ref({
  usernames: [],
  locations: [],
  categories: [],
  titles: [],
  contents: []
});
const showSuggestions = ref(false);
let suggestionDebounceTimer: ReturnType<typeof setTimeout> | null = null;

// 计算是否有补全建议
const hasSuggestions = computed(() => {
  return suggestions.value.usernames?.length > 0 ||
         suggestions.value.locations?.length > 0 ||
         suggestions.value.categories?.length > 0 ||
         suggestions.value.titles?.length > 0 ||
         suggestions.value.contents?.length > 0;
});

// 判断当前文章是否是当前用户发布的
const isCurrentUserArticle = computed(() => {
  if (!modalArticle.value) return false;
  const storedUser = localStorage.getItem('user');
  if (!storedUser) return false;
  const currentUser = JSON.parse(storedUser);
  return currentUser.id === modalArticle.value.creatorId;
});

// 跳转到编辑页面
const goToEdit = () => {
  const articleId = modalArticle.value?.id;
  if (articleId) {
    // 关闭弹窗后再跳转
    closeModal();
    router.push(`/article/edit/${articleId}`);
  }
};

// 类别统计响应接口
interface CategoryStatResp {
  category: string;
  count: number;
}

// 热门关键词响应接口
interface HotKeywordResp {
  keyword: string;
  searchCount: number;
}

// 根据搜索字段获取占位符文本
const getPlaceholder = () => {
  const placeholders: Record<string, string> = {
    all: '搜知识、看干货、找灵感...',
    title: '搜索标题...',
    category: '搜索类别...',
    content: '搜索内容...',
    username: '搜索用户名...',
    location: '搜索地点...'
  };
  return placeholders[searchField.value] || '搜知识、看干货、找灵感...';
};

// 获取类别导航数据
const fetchCategoryNavigation = async () => {
  try {
    const res = await request.get('/category/navigation', {
      params: { maxCount: 20 }
    });
    if (res.data.code === 200) {
      categories.value = res.data.data;
      console.log('类别导航数据:', categories.value);
    }
  } catch (err) {
    console.error('获取类别导航失败:', err);
  }
};

// 获取热门关键词
const fetchHotKeywords = async () => {
  try {
    const res = await request.get('/article/hot-keywords', {
      params: { limit: 15 }
    });
    if (res.data.code === 200 && res.data.data && res.data.data.length > 0) {
      hotWords.value = res.data.data.map((item: HotKeywordResp) => item.keyword);
      console.log('热门关键词:', hotWords.value);
    }
  } catch (err) {
    console.error('获取热门关键词失败:', err);
  }
};

// 获取数据
const fetchArticles = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: page.value,
      size: size.value
    };
    
    // 根据搜索字段设置查询参数
    if (searchQuery.value) {
      if (searchField.value === 'all') {
        params.keyword = searchQuery.value;
      } else if (searchField.value === 'title') {
        params.title = searchQuery.value;
      } else if (searchField.value === 'category') {
        params.categories = [searchQuery.value];
      } else if (searchField.value === 'content') {
        params.content = searchQuery.value;
      } else if (searchField.value === 'username') {
        params.username = searchQuery.value;
      } else if (searchField.value === 'location') {
        params.location = searchQuery.value;
      }
    }
    
    // 只有非"全部"时才传递categories参数（与搜索字段不冲突时）
    if (selectedCategory.value && selectedCategory.value !== '全部' && searchField.value !== 'category') {
      params.categories = [selectedCategory.value];
    }

    console.log('请求参数:', params);
    const res = await request.get('/article/list', { params });
    console.log('响应数据:', res.data);
    
    if (res.data.code === 200) {
      // 将搜索关键词附加到每篇文章，用于前端片段提取
      articles.value = res.data.data.list.map((item: any) => ({
        ...item,
        _searchKeyword: currentSearchKeyword.value
      }));
      totalPages.value = Math.ceil(res.data.data.total / size.value);
      console.log('文章列表:', articles.value);
      console.log('总页数:', totalPages.value);
    }
  } catch (err) {
    console.error('Fetch error:', err);
  } finally {
    // 模拟网络延迟以展示加载效果
    setTimeout(() => { loading.value = false; }, 300);
  }
};

// 操作处理
const handleSearch = () => {
  page.value = 0;
  // 保存当前搜索关键词（用于正文/地点片段高亮）
  currentSearchKeyword.value = (searchField.value === 'content' || searchField.value === 'location') ? searchQuery.value : '';
  fetchArticles();
};

const quickSearch = (word: string) => {
  searchQuery.value = word;
  handleSearch();
};

// 处理输入事件（带防抖）
const handleInput = () => {
  if (suggestionDebounceTimer) {
    clearTimeout(suggestionDebounceTimer);
  }
  
  if (!searchQuery.value || searchQuery.value.length < 1) {
    showSuggestions.value = false;
    return;
  }
  
  suggestionDebounceTimer = setTimeout(() => {
    fetchSuggestions();
  }, 300);
};

// 获取搜索补全建议
const fetchSuggestions = async () => {
  if (!searchQuery.value) return;
  
  try {
    const res = await request.get('/article/suggestions', {
      params: {
        keyword: searchQuery.value,
        field: searchField.value
      }
    });
    
    if (res.data.code === 200) {
      suggestions.value = res.data.data;
      showSuggestions.value = hasSuggestions.value;
    }
  } catch (err) {
    console.error('获取搜索建议失败:', err);
  }
};

// 应用补全建议
const applySuggestion = (text: string, field: string) => {
  searchQuery.value = text;
  searchField.value = field;
  showSuggestions.value = false;
  handleSearch();
};

// 处理输入框聚焦
const handleInputFocus = () => {
  if (hasSuggestions.value && searchQuery.value) {
    showSuggestions.value = true;
  }
};

// 处理输入框失焦
const handleInputBlur = () => {
  // 延迟关闭，让点击事件先执行
  setTimeout(() => {
    showSuggestions.value = false;
  }, 200);
};

const resetFilters = () => {
  searchQuery.value = '';
  selectedCategory.value = '全部';
  page.value = 0;
  showSuggestions.value = false;
  suggestions.value = {
    usernames: [],
    locations: [],
    categories: [],
    titles: [],
    contents: []
  };
  fetchArticles();
};

// 打开文章弹窗
const openArticleModal = async (articleId: string) => {
  showModal.value = true;
  currentImgIndex.value = 0;
  try {
    const res = await request.get(`/article/detail/${articleId}`);
    if (res.data.code === 200) {
      modalArticle.value = res.data.data;
    }
  } catch (err) {
    console.error('Load article error:', err);
    showModal.value = false;
  }
};

// 关闭文章弹窗
const closeModal = () => {
  showModal.value = false;
  modalArticle.value = null;
  currentImgIndex.value = 0;
};

// 打开图片预览
const openImagePreview = (index: number) => {
  previewImgIndex.value = index;
  showImagePreview.value = true;
};

// 关闭图片预览
const closeImagePreview = () => {
  showImagePreview.value = false;
};

// 触摸滑动相关
const handleTouchStart = (e: TouchEvent) => {
  touchStartX.value = e.touches[0].clientX;
};

const handleTouchMove = (e: TouchEvent) => {
  touchEndX.value = e.touches[0].clientX;
};

const handleTouchEnd = () => {
  if (!modalArticle.value || modalArticle.value.images.length <= 1) return;
  
  const swipeDistance = touchEndX.value - touchStartX.value;
  
  if (swipeDistance < -minSwipeDistance) {
    currentImgIndex.value = (currentImgIndex.value + 1) % modalArticle.value.images.length;
  } else if (swipeDistance > minSwipeDistance) {
    currentImgIndex.value = (currentImgIndex.value - 1 + modalArticle.value.images.length) % modalArticle.value.images.length;
  }
  
  touchStartX.value = 0;
  touchEndX.value = 0;
};

// 格式化日期（弹窗使用）
const formatModalDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' });
};

const formatFullModalDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' 
  });
};

// 模拟评论数据
const mockComments = ref([
  {
    user: '小明同学',
    content: '写得真好，学到了很多！收藏了~',
    time: '2小时前',
    likes: 128
  },
  {
    user: '技术达人',
    content: '这个思路很清晰，我之前也遇到过类似的问题。',
    time: '5小时前',
    likes: 86
  },
  {
    user: '产品经理',
    content: '从产品的角度来看，这个设计方案很合理。',
    time: '昨天',
    likes: 45
  },
  {
    user: '前端小白',
    content: '请问这个是怎么实现的？能详细讲讲吗？',
    time: '昨天',
    likes: 12
  }
]);

// 监听器
watch([selectedCategory, page], () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
  fetchArticles();
});

// 刷新数据的通用函数
const checkAndRefresh = (forceRefresh = false) => {
  const needRefresh = sessionStorage.getItem('homeNeedRefresh');
  console.log('HomeView checkAndRefresh, needRefresh:', needRefresh, 'forceRefresh:', forceRefresh);
  
  // 只有在有刷新标记或强制刷新时才调用接口
  if (needRefresh || forceRefresh) {
    if (needRefresh) {
      sessionStorage.removeItem('homeNeedRefresh');
      console.log('检测到刷新标记，准备刷新数据并清除查询条件');
      // 清除查询条件
      searchQuery.value = '';
      searchField.value = 'all';
      selectedCategory.value = '全部';
      page.value = 0;
      currentSearchKeyword.value = '';
    }
    fetchCategoryNavigation();
    fetchHotKeywords();
    fetchArticles();
  } else {
    console.log('无刷新标记，不调用接口');
  }
};

// 加载当前用户信息
const loadCurrentUser = () => {
  const storedUser = localStorage.getItem('user');
  if (storedUser) {
    const userData = JSON.parse(storedUser);
    currentUserName.value = userData.username || '';
    if (userData.avatar) {
      currentUserAvatar.value = getAvatarUrl(userData.avatar);
    }
  }
};

onMounted(() => {
  // 设置页面标题
  document.title = '知拾录';
  // 首次加载强制刷新
  checkAndRefresh(true);
  loadCurrentUser();
});

// 从 keep-alive 缓存中激活时检查是否需要刷新
onActivated(() => {
  console.log('HomeView onActivated');
  // 重新设置页面标题
  document.title = '知拾录';
  // 只有在有标记时才刷新，否则保持现状
  checkAndRefresh(false);
});
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 自定义下拉框样式 */
select {
  background-image: none;
}

select option {
  padding: 8px 12px;
  font-size: 14px;
  background: white;
  color: #374151;
}

select option:hover,
select option:focus,
select option:checked {
  background: #eff6ff;
  color: #2563eb;
}

/* 移除IE的下拉箭头 */
select::-ms-expand {
  display: none;
}

/* 统一focus时的边框 */
select:focus + div input,
select:focus ~ div input {
  border-left-color: transparent;
}

/* 弹窗动画 - 优化版 */
.modal-enter-active {
  transition: opacity 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-leave-active {
  transition: opacity 0.25s cubic-bezier(0.4, 0, 1, 1);
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

/* 弹窗内容动画 - 桌面端缩放效果 */
@media (min-width: 768px) {
  .modal-enter-active > div {
    transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  }
  
  .modal-leave-active > div {
    transition: transform 0.25s cubic-bezier(0.4, 0, 1, 1), opacity 0.25s cubic-bezier(0.4, 0, 1, 1);
  }

  .modal-enter-from > div {
    transform: scale(0.85) translateY(20px);
    opacity: 0;
  }
  
  .modal-leave-to > div {
    transform: scale(0.95);
    opacity: 0;
  }
}

/* 弹窗内容动画 - 移动端从底部滑入 */
@media (max-width: 767px) {
  .modal-enter-active > div {
    transition: transform 0.35s cubic-bezier(0.32, 0.72, 0, 1);
  }
  
  .modal-leave-active > div {
    transition: transform 0.25s cubic-bezier(0.4, 0, 1, 1);
  }

  .modal-enter-from > div {
    transform: translateY(100%);
  }
  
  .modal-leave-to > div {
    transform: translateY(100%);
  }
}

/* 图片预览淡入淡出 - 优化版 */
.fade-enter-active {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-leave-active {
  transition: opacity 0.2s cubic-bezier(0.4, 0, 1, 1);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

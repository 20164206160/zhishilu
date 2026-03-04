<template>
  <div class="min-h-screen flex flex-col font-sans w-full overflow-x-hidden">
    <!-- 顶部导航栏 - 与首页一致 -->
    <header class="sticky top-0 z-50 w-full">
      <div class="max-w-7xl mx-auto px-2 sm:px-4 h-14 sm:h-16 flex items-center justify-between gap-2 sm:gap-4">
        <!-- Logo -->
        <router-link to="/" class="text-xl sm:text-2xl font-black italic tracking-tighter text-blue-600 flex-shrink-0 hover:opacity-80 transition-opacity">
          知拾录<span class="text-emerald-500">.</span>
        </router-link>

        <!-- 搜索框 -->
        <div class="flex-grow max-w-2xl relative min-w-0">
          <div class="flex items-center">
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
                type="text"
                :placeholder="getPlaceholder()"
                class="w-full pl-8 sm:pl-11 pr-16 sm:pr-20 py-2 sm:py-2.5 bg-gray-100 border-2 border-l-0 border-transparent rounded-r-full focus:bg-white focus:border-blue-500 focus:ring-2 sm:focus:ring-4 focus:ring-blue-50 transition-all outline-none text-xs sm:text-sm"
              />
              <button
                @click="handleSearch"
                class="absolute right-1 top-1/2 -translate-y-1/2 bg-blue-600 hover:bg-blue-700 text-white px-2 sm:px-4 py-1 sm:py-1.5 rounded-full text-xs font-bold transition-all shadow-sm active:scale-95"
              >
                <span class="hidden sm:inline">搜索</span>
                <SearchIcon class="sm:hidden" :size="14" />
              </button>
            </div>
          </div>
        </div>

        <!-- 右侧操作区 -->
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
    </header>

    <!-- 主内容区 -->
    <main v-if="article" class="flex-grow max-w-7xl mx-auto w-full px-2 sm:px-4 py-4 sm:py-6">
      <!-- 返回按钮 -->
      <div class="mb-4">
        <button @click="handleBack" class="flex items-center gap-2 text-gray-500 hover:text-blue-600 transition-colors text-sm font-medium">
          <ChevronLeft :size="18" />
          <span>返回首页</span>
        </button>
      </div>

      <!-- 详情卡片 -->
      <div :class="article.images?.length ? 'max-w-[1100px]' : 'max-w-[720px]'" class="mx-auto bg-white rounded-2xl shadow-lg overflow-hidden">
        <div class="flex flex-col md:flex-row">
          <!-- Left: Image Gallery (仅当有图片时显示) -->
          <section
            v-if="article.images?.length"
            ref="galleryRef"
            class="gallery-section w-full md:w-[55%] lg:w-[58%] xl:w-[60%] h-[50vh] md:h-[600px] lg:h-[700px] relative bg-black overflow-hidden group"
            @touchstart="handleTouchStart"
            @touchmove="handleTouchMove"
            @touchend="handleTouchEnd"
          >
            <!-- Image Counter -->
            <div v-if="article.images.length > 1" class="absolute top-4 left-1/2 -translate-x-1/2 z-10 px-3 py-1 bg-black/40 rounded-full text-white text-xs font-medium backdrop-blur-sm">
              {{ currentImgIndex + 1 }} / {{ article.images.length }}
            </div>

            <div class="flex h-full w-full transition-transform duration-[450ms] ease-out" :style="{ transform: `translateX(-${currentImgIndex * 100}%)` }">
              <img v-for="(img, i) in article.images" :key="i" :src="getImageUrl(img)" @click="openImagePreview(i)" class="w-full h-full object-contain flex-shrink-0 cursor-zoom-in" />
            </div>
            
            <!-- Dot Indicators -->
            <div v-if="article.images.length > 1" class="absolute bottom-4 inset-x-0 flex justify-center gap-2 z-10">
              <button 
                v-for="(_, i) in article.images" 
                :key="i" 
                @click="currentImgIndex = i"
                class="h-2 rounded-full transition-all duration-300" 
                :class="[currentImgIndex === i ? 'w-5 bg-white' : 'w-2 bg-white/40 hover:bg-white/60']"
              ></button>
            </div>

            <!-- Navigation Arrows -->
            <template v-if="article.images.length > 1">
              <button 
                @click="prevImage" 
                :class="['absolute left-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-all backdrop-blur-sm', currentImgIndex === 0 ? 'opacity-0 cursor-not-allowed' : 'opacity-0 group-hover:opacity-100']"
                :disabled="currentImgIndex === 0"
              >
                <ChevronLeft :size="24" />
              </button>
              <button 
                @click="nextImage" 
                :class="['absolute right-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-all backdrop-blur-sm', currentImgIndex === article.images.length - 1 ? 'opacity-0 cursor-not-allowed' : 'opacity-0 group-hover:opacity-100']"
                :disabled="currentImgIndex === article.images.length - 1"
              >
                <ChevronRight :size="24" />
              </button>
            </template>
          </section>

          <!-- Right: Content Area -->
          <aside :class="article.images?.length ? 'w-full md:w-[45%] lg:w-[42%] xl:w-[40%]' : 'w-full'" class="flex flex-col bg-white overflow-hidden md:h-[600px] lg:h-[700px]">
        <!-- Author Header (Sticky) -->
        <div class="px-5 py-4 border-b border-[#f0f0f0] flex items-center justify-between bg-white shrink-0">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full bg-gradient-to-tr from-blue-500 to-blue-400 flex items-center justify-center text-white font-bold text-base shadow-sm overflow-hidden cursor-pointer hover:opacity-90 transition-opacity">
              <img v-if="article.creatorAvatar" :src="getAvatarUrl(article.creatorAvatar)" class="w-full h-full object-cover" alt="avatar" />
              <template v-else>{{ article.createdBy?.charAt(0) || 'U' }}</template>
            </div>
            <div>
              <p class="text-[15px] font-semibold text-[#333] leading-tight cursor-pointer hover:text-blue-600 transition-colors">{{ article.createdBy }}</p>
              <p class="text-[12px] text-[#999] mt-0.5">{{ formatDate(article.createdTime) }}</p>
            </div>
          </div>
          <!-- Author Actions -->
          <div v-if="isAuthor" class="flex items-center gap-2">
            <button 
              @click="handleEdit"
              class="w-9 h-9 rounded-full bg-[#f5f5f5] hover:bg-[#e8e8e8] text-[#666] flex items-center justify-center transition-colors"
              title="编辑"
            >
              <Edit3 :size="17" />
            </button>
            <button 
              @click="handleDelete"
              class="w-9 h-9 rounded-full bg-[#f5f5f5] hover:bg-red-50 text-[#666] hover:text-red-500 flex items-center justify-center transition-colors"
              title="删除"
            >
              <Trash2 :size="17" />
            </button>
          </div>
          <button v-else class="px-5 py-1.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-full transition-colors">
            关注
          </button>
        </div>

        <!-- Scrollable Content -->
        <div class="flex-grow overflow-y-auto">
          <!-- Article Content -->
          <div class="px-5 py-5">
            <h1 class="text-lg font-bold text-[#333] leading-snug mb-4">{{ article.title }}</h1>
            <div v-if="article.content" class="text-[15px] text-[#333] leading-[1.8] prose prose-sm max-w-none" v-html="article.content"></div>

            <!-- Tags -->
            <div v-if="article.categories?.length" class="flex flex-wrap gap-2 mt-5">
              <span 
                v-for="(cat, index) in article.categories" 
                :key="index"
                class="text-[#576b95] text-[14px] hover:underline cursor-pointer"
              >
                #{{ cat }}
              </span>
            </div>

            <!-- Location -->
            <div v-if="article.location" class="flex items-center gap-1.5 mt-4 text-[#576b95]">
              <MapPin :size="14" />
              <span class="text-[13px]">{{ article.location }}</span>
            </div>

            <!-- Publish Time -->
            <div class="mt-4 text-[12px] text-[#999]">
              {{ formatFullDate(article.createdTime) }}
            </div>
          </div>

          <!-- Comments Section -->
          <div class="border-t border-[#f0f0f0]">
            <div class="px-5 py-3 flex items-center justify-between">
              <span class="text-[14px] font-medium text-[#333]">共 {{ commentTotal }} 条评论</span>
            </div>

            <!-- Comment List -->
            <div class="px-5 pb-4 space-y-4">
              <!-- 评论加载中 -->
              <div v-if="commentLoading && comments.length === 0" class="py-6 text-center text-[13px] text-[#999]">
                <div class="inline-flex items-center gap-2">
                  <div class="animate-spin rounded-full h-4 w-4 border-2 border-blue-400 border-t-transparent"></div>
                  加载中...
                </div>
              </div>

              <!-- 评论为空 -->
              <div v-else-if="!commentLoading && comments.length === 0" class="py-6 text-center text-[13px] text-[#bbb]">
                暂无评论，来发表第一条吧~
              </div>

              <!-- 评论列表 -->
              <div v-for="comment in comments" :key="comment.id" class="flex gap-3">
                <div class="w-8 h-8 rounded-full bg-gradient-to-tr from-gray-400 to-gray-300 flex items-center justify-center text-white text-xs font-bold flex-shrink-0 overflow-hidden">
                  <img v-if="comment.creatorAvatar" :src="getAvatarUrl(comment.creatorAvatar)" class="w-full h-full object-cover" alt="avatar" />
                  <template v-else>{{ comment.createdBy?.charAt(0) || 'U' }}</template>
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2">
                    <span class="text-[13px] text-[#576b95] font-medium">{{ comment.createdBy }}</span>
                    <span v-if="article && comment.createdBy === article.createdBy" class="px-1.5 py-0.5 bg-blue-600 text-white text-[10px] rounded">作者</span>
                  </div>
                  <p class="text-[14px] text-[#333] mt-1 leading-relaxed break-words">{{ comment.content }}</p>
                  <div class="flex items-center gap-4 mt-2 text-[12px] text-[#999]">
                    <span>{{ formatDate(comment.createdTime) }}</span>
                    <button @click="handleReply(comment)" class="hover:text-[#576b95] transition-colors">回复</button>
                    <button v-if="canDeleteComment(comment)" @click="handleDeleteComment(comment.id)" class="hover:text-red-500 transition-colors">删除</button>
                  </div>

                  <!-- 回复列表 -->
                  <div v-if="getDisplayReplies(comment).length" class="mt-3 bg-[#f8f8f8] rounded-lg p-3 space-y-3">
                    <div v-for="reply in getDisplayReplies(comment)" :key="reply.id" class="text-[13px]">
                      <div class="flex items-start gap-2">
                        <div class="w-6 h-6 rounded-full bg-gradient-to-tr from-blue-200 to-blue-100 flex items-center justify-center text-blue-600 text-[10px] font-bold flex-shrink-0 overflow-hidden mt-0.5">
                          <img v-if="reply.creatorAvatar" :src="getAvatarUrl(reply.creatorAvatar)" class="w-full h-full object-cover" alt="avatar" />
                          <template v-else>{{ reply.createdBy?.charAt(0) || 'U' }}</template>
                        </div>
                        <div class="flex-1 min-w-0">
                          <div class="flex flex-wrap items-baseline gap-1">
                            <span class="text-[#576b95] font-medium">{{ reply.createdBy }}</span>
                            <template v-if="reply.replyToUser">
                              <span class="text-[#bbb]">回复</span>
                              <span class="text-[#576b95] font-medium">{{ reply.replyToUser }}</span>
                            </template>
                            <span class="text-[#aaa] text-[11px]">{{ formatDate(reply.createdTime) }}</span>
                          </div>
                          <p class="text-[#333] mt-0.5 break-words">{{ reply.content }}</p>
                          <div class="flex items-center gap-3 mt-1 text-[11px] text-[#bbb]">
                            <button @click="handleReply(comment, reply)" class="hover:text-[#576b95] transition-colors">回复</button>
                            <button v-if="canDeleteComment(reply)" @click="handleDeleteComment(reply.id)" class="hover:text-red-500 transition-colors">删除</button>
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- 查看全部回复 / 收起 -->
                    <div v-if="comment.totalReplyCount > 3" class="mt-1">
                      <button
                        v-if="!expandedReplies.has(comment.id)"
                        @click="expandReplies(comment.id)"
                        class="text-[12px] text-[#576b95] hover:underline"
                      >
                        查看全部 {{ comment.totalReplyCount }} 条回复
                      </button>
                      <button
                        v-else
                        @click="collapseReplies(comment.id)"
                        class="text-[12px] text-[#999] hover:underline"
                      >
                        收起回复
                      </button>
                    </div>
                  </div>
                </div>

                <!-- 点赞按钮 -->
                <button @click="handleLikeComment(comment)" class="flex flex-col items-center gap-1 text-[#999] hover:text-blue-600 transition-colors flex-shrink-0">
                  <Heart :size="16" />
                  <span class="text-[11px]">{{ comment.likeCount || 0 }}</span>
                </button>
              </div>

              <!-- 加载更多 -->
              <div v-if="hasMoreComments" class="pt-2 text-center">
                <button
                  @click="loadMoreComments"
                  :disabled="commentLoading"
                  class="text-[13px] text-[#576b95] hover:underline disabled:opacity-50"
                >
                  {{ commentLoading ? '加载中...' : '查看更多评论' }}
                </button>
              </div>
            </div>
          </div>
        </div>

            <!-- Interaction Footer (Sticky) -->
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
                <button 
                  @click="showShareModal = true"
                  class="text-[#333] hover:text-[#666] transition-colors"
                >
                  <Share2 :size="22" />
                </button>
              </div>
              
            <!-- Comment Input -->
              <div class="relative">
                <!-- 回复标识 -->
                <div v-if="replyTarget" class="mb-1.5 flex items-center gap-2 text-[12px] text-[#576b95] bg-blue-50 rounded-lg px-3 py-1.5">
                  <span>回复 @{{ replyTarget.username }}</span>
                  <button @click="cancelReply" class="ml-auto text-[#999] hover:text-[#666]">
                    <XIcon :size="12" />
                  </button>
                </div>

                <div class="relative flex items-center">
                  <!-- 表情包按钮 -->
                  <button
                    @click.stop="showEmojiPanel = !showEmojiPanel"
                    class="absolute left-2 top-1/2 -translate-y-1/2 text-[#999] hover:text-[#666] transition-colors z-10"
                  >
                    <span class="text-[18px] leading-none">&#128522;</span>
                  </button>

                  <input
                    ref="commentInputRef"
                    v-model="commentInput"
                    @keyup.enter="sendComment"
                    @keyup.esc="cancelReply"
                    type="text"
                    :placeholder="replyTarget ? `回复 @${replyTarget.username}...` : '说点什么...'"
                    class="w-full bg-[#f5f5f5] border-none rounded-full py-1.5 pl-9 pr-14 text-[14px] text-[#333] placeholder:text-[#999] focus:outline-none focus:ring-2 focus:ring-blue-100 transition-all h-9 leading-5 box-border"
                  />
                  <button
                    @click="sendComment"
                    class="absolute right-2 top-1/2 -translate-y-1/2 px-3 py-1 text-blue-600 text-[13px] font-medium hover:bg-blue-50 rounded-full transition-colors"
                  >
                    发送
                  </button>
                </div>

                <!-- 表情包面板 -->
                <teleport to="body">
                  <div
                    v-if="showEmojiPanel"
                    @click.stop
                    class="fixed z-[200] bg-white border border-gray-200 rounded-2xl shadow-xl p-3"
                    :style="emojiPanelStyle"
                    ref="emojiPanelRef"
                  >
                    <div class="grid grid-cols-10 gap-1">
                      <button
                        v-for="emoji in EMOJI_LIST"
                        :key="emoji"
                        @click="insertEmoji(emoji)"
                        class="w-8 h-8 flex items-center justify-center text-[18px] hover:bg-gray-100 rounded-lg transition-colors"
                      >{{ emoji }}</button>
                    </div>
                  </div>
                </teleport>

                <!-- 表情面板辮照 -->
                <div v-if="showEmojiPanel" @click="showEmojiPanel = false" class="fixed inset-0 z-[199]"></div>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </main>

    <!-- Loading State -->
    <main v-else class="flex-grow max-w-7xl mx-auto w-full px-2 sm:px-4 py-4 sm:py-6">
      <div class="h-[60vh] flex items-center justify-center bg-white rounded-2xl shadow-lg">
        <div class="flex flex-col items-center gap-3">
          <div class="animate-spin rounded-full h-8 w-8 border-3 border-blue-600 border-t-transparent"></div>
          <span class="text-[13px] text-[#999]">加载中...</span>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <footer class="py-6 sm:py-10 mt-auto w-full">
      <div class="max-w-7xl mx-auto px-2 sm:px-4">
        <div class="flex flex-col md:flex-row justify-between items-center gap-4 sm:gap-8">
          <div class="text-center md:text-left space-y-1 sm:space-y-2">
            <h2 class="text-base sm:text-lg font-bold text-gray-800">知拾录 <span class="text-gray-300 font-light ml-1 text-sm sm:text-base">Personal Knowledge Library</span></h2>
            <p class="text-[10px] sm:text-xs text-gray-400 max-w-xs leading-relaxed px-4 md:px-0">
              知拾录致力于为用户提供一个优雅、高效的个人知识收藏与管理平台。随时随地，随手记，随心搜。
            </p>
          </div>
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

    <!-- Image Preview Modal -->
    <teleport to="body">
      <transition name="fade">
        <div v-if="showImagePreview" @click="closeImagePreview" class="fixed inset-0 bg-black z-[110] flex items-center justify-center">
          <button @click="closeImagePreview" class="absolute top-4 right-4 z-50 w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors">
            <XIcon :size="22" />
          </button>
          
          <!-- Image Counter -->
          <div v-if="(article?.images?.length || 0) > 1" class="absolute top-4 left-1/2 -translate-x-1/2 z-10 px-3 py-1 bg-black/50 rounded-full text-white text-sm font-medium">
            {{ previewImgIndex + 1 }} / {{ article?.images?.length }}
          </div>
          
          <div class="relative w-screen h-screen overflow-hidden" @click.stop>
            <div class="flex h-full transition-transform duration-300 ease-out" :style="{ transform: `translateX(-${previewImgIndex * 100}vw)` }">
              <div 
                v-for="(img, i) in (article?.images || [])" 
                :key="i" 
                class="w-screen h-full flex-shrink-0 flex items-center justify-center p-4"
              >
                <img 
                  :src="getImageUrl(img)" 
                  class="max-w-full max-h-full object-contain" 
                />
              </div>
            </div>
            
            <!-- Navigation -->
            <template v-if="(article?.images?.length || 0) > 1">
              <button 
                @click.stop="previewImgIndex = (previewImgIndex - 1 + (article?.images?.length || 0)) % (article?.images?.length || 1)" 
                class="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors backdrop-blur-sm"
              >
                <ChevronLeft :size="28" />
              </button>
              <button 
                @click.stop="previewImgIndex = (previewImgIndex + 1) % (article?.images?.length || 1)" 
                class="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors backdrop-blur-sm"
              >
                <ChevronRight :size="28" />
              </button>
            </template>

            <!-- Indicators -->
            <div v-if="(article?.images?.length || 0) > 1" class="absolute bottom-8 inset-x-0 flex justify-center gap-2 z-10">
              <button 
                v-for="(_, i) in (article?.images || [])" 
                :key="i" 
                @click.stop="previewImgIndex = i"
                class="h-2 rounded-full transition-all" 
                :class="[previewImgIndex === i ? 'w-6 bg-white' : 'w-2 bg-white/40']"
              ></button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, computed, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { 
  ChevronLeft, ChevronRight, ChevronDown, Share2, Tag, MapPin, 
  Link as LinkIcon, Heart, Star, MessageCircle, Edit3, Trash2, X as XIcon,
  Search as SearchIcon, Plus as PlusIcon, User as UserIcon,
  Github as GithubIcon, Mail as MailIcon, Twitter as TwitterIcon
} from 'lucide-vue-next';
import request from '../utils/request';
import { getImageUrl, getAvatarUrl } from '../utils/image';
import ShareModal from '../components/ShareModal.vue';

const route = useRoute();
const router = useRouter();
const article = ref<any>(null);
const scrolled = ref(false);
const currentImgIndex = ref(0);

// 分享弹窗
const showShareModal = ref(false);

// 分享链接
const shareUrl = computed(() => {
  return window.location.href;
});

// 滚轮切换节流控制（防止快速滚动切换多张）
const isWheelSwitching = ref(false);
const WHEEL_SWITCH_DURATION = 450; // 与动画时长一致

// 搜索相关
const searchQuery = ref('');
const searchField = ref('all');
const currentUserAvatar = ref('');
const currentUserName = ref('');

// 获取当前用户信息
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

// 获取占位符文本
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

// 搜索处理
const handleSearch = () => {
  // 跳转到首页并携带搜索参数
  router.push({
    path: '/',
    query: {
      keyword: searchQuery.value,
      field: searchField.value
    }
  });
};

// 图片预览相关
const showImagePreview = ref(false);
const previewImgIndex = ref(0);

// 获取来源页面，用于决定返回哪里
const from = computed(() => route.query.from as string);

// 处理返回按钮
const handleBack = () => {
  if (from.value === 'profile') {
    // 返回个人中心的内容管理标签
    router.push({ path: '/profile', query: { from: 'content' } });
  } else {
    router.push('/');
  }
};

// 获取当前登录用户
const currentUser = computed(() => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  }
  return null;
});

// 判断当前用户是否是文章作者（或管理员）
const isAuthor = computed(() => {
  if (!currentUser.value || !article.value) return false;
  // 管理员可以编辑所有文章，普通用户只能编辑自己的文章
  return currentUser.value.username === article.value.createdBy || currentUser.value.admin === true;
});

const handleScroll = () => {
  scrolled.value = window.scrollY > 50;
};

// 触摸滑动相关
const touchStartX = ref(0);
const touchEndX = ref(0);
const minSwipeDistance = 50;

const handleTouchStart = (e: TouchEvent) => {
  touchStartX.value = e.touches[0].clientX;
};

const handleTouchMove = (e: TouchEvent) => {
  touchEndX.value = e.touches[0].clientX;
};

const handleTouchEnd = () => {
  if (!article.value || article.value.images.length <= 1) return;
  
  const swipeDistance = touchEndX.value - touchStartX.value;
  
  if (swipeDistance < -minSwipeDistance) {
    // 向右滑动 - 下一张（有边界限制）
    if (currentImgIndex.value < article.value.images.length - 1) {
      currentImgIndex.value++;
    }
  } else if (swipeDistance > minSwipeDistance) {
    // 向左滑动 - 上一张（有边界限制）
    if (currentImgIndex.value > 0) {
      currentImgIndex.value--;
    }
  }
  
  touchStartX.value = 0;
  touchEndX.value = 0;
};

// 鼠标滚轮切换图片（有边界限制，不循环，带节流）
const handleWheel = (e: WheelEvent) => {
  // 始终阻止默认滚动，防止页面跟着滚动
  e.preventDefault();

  if (!article.value || article.value.images.length <= 1) return;
  
  // 节流控制：如果正在切换中，忽略此次滚轮事件
  if (isWheelSwitching.value) return;
  
  let shouldSwitch = false;
  
  // 根据滚动方向切换图片（有边界限制）
  if (e.deltaY > 0) {
    // 向下滚动 - 下一张（最后一张时停止）
    if (currentImgIndex.value < article.value.images.length - 1) {
      currentImgIndex.value++;
      shouldSwitch = true;
    }
  } else if (e.deltaY < 0) {
    // 向上滚动 - 上一张（第一张时停止）
    if (currentImgIndex.value > 0) {
      currentImgIndex.value--;
      shouldSwitch = true;
    }
  }
  
  // 如果发生了切换，设置节流标志
  if (shouldSwitch) {
    isWheelSwitching.value = true;
    setTimeout(() => {
      isWheelSwitching.value = false;
    }, WHEEL_SWITCH_DURATION);
  }
};

// 图片导航（有边界限制，不循环）
const nextImage = () => {
  if (!article.value) return;
  // 下一张（最后一张时停止）
  if (currentImgIndex.value < article.value.images.length - 1) {
    currentImgIndex.value++;
  }
};

const prevImage = () => {
  if (!article.value) return;
  // 上一张（第一张时停止）
  if (currentImgIndex.value > 0) {
    currentImgIndex.value--;
  }
};

// 键盘导航（有边界限制，不循环）
const handleKeydown = (e: KeyboardEvent) => {
  if (showImagePreview.value) {
    if (e.key === 'Escape') {
      closeImagePreview();
    } else if (e.key === 'ArrowLeft') {
      // 预览图上一张（有边界限制）
      if (previewImgIndex.value > 0) {
        previewImgIndex.value--;
      }
    } else if (e.key === 'ArrowRight') {
      // 预览图下一张（有边界限制）
      const maxIndex = (article.value?.images?.length || 1) - 1;
      if (previewImgIndex.value < maxIndex) {
        previewImgIndex.value++;
      }
    }
  } else {
    if (e.key === 'ArrowLeft') {
      prevImage();
    } else if (e.key === 'ArrowRight') {
      nextImage();
    }
  }
};

// ======================== 评论功能 ========================

// 评论列表
const comments = ref<any[]>([]);
const commentTotal = ref(0);
const commentPage = ref(0);
const commentPageSize = 10;
const commentLoading = ref(false);
const hasMoreComments = ref(false);

// 评论输入
const commentInput = ref('');
const commentInputRef = ref<HTMLInputElement | null>(null);
const emojiPanelRef = ref<HTMLElement | null>(null);

// 回复状态
const replyTarget = ref<{ id: string; parentId: string; username: string } | null>(null);

// 表情包面板
const showEmojiPanel = ref(false);
const EMOJI_LIST = [
  '😀','😁','😂','🤣','😃','😄','😅','😆','😉','😊',
  '😋','😎','😍','😘','🥰','😗','😙','😚','🙂','🤗',
  '🤩','🤔','🤨','😐','😑','😶','🙄','😏','😣','😥',
  '😮','🤐','😯','😪','😫','🥱','😴','😌','😛','😜',
  '😝','🤤','😒','😓','😔','😕','🙃','🤑','😲','☹️',
  '🙁','😖','😞','😟','😤','😢','😭','😦','😧','😨',
  '😩','🤯','😬','🥵','🥶','😱','😳','🤪','😵','😡',
  '😠','🤬','😷','🤒','🤕','🤢','🤮','🤧','😇','🥳',
  '🥴','🥺','👍','👎','👏','🙌','🤝','🙏','💪','❤️',
  '🔥','✨','🎉','🎊','💯','💕','💞','💓','💗','💖',
];

// 展开回复的根评论ID集合
const expandedReplies = reactive<Set<string>>(new Set());
const fullRepliesMap = ref<Record<string, any[]>>({});

// 表情面板定位样式
const emojiPanelStyle = computed(() => {
  const inputEl = commentInputRef.value;
  if (!inputEl) return {};
  const rect = inputEl.getBoundingClientRect();
  const panelW = 360;
  const panelH = 240;
  let left = rect.left;
  let top = rect.top - panelH - 8;
  // 防止超出屏幕右边
  if (left + panelW > window.innerWidth) {
    left = window.innerWidth - panelW - 8;
  }
  if (left < 8) left = 8;
  // 如果上方空间不足，就展示在下方
  if (top < 8) {
    top = rect.bottom + 8;
  }
  return { left: `${left}px`, top: `${top}px`, width: `${panelW}px` };
});

// 加载评论列表
const loadComments = async (reset = false) => {
  if (!article.value) return;
  if (commentLoading.value) return;
  commentLoading.value = true;
  try {
    const page = reset ? 0 : commentPage.value;
    const res = await request.get('/comment/list', {
      params: { articleId: article.value.id, page, size: commentPageSize }
    });
    if (res.data.code === 200) {
      const result = res.data.data;
      if (reset) {
        comments.value = result.list || [];
        commentPage.value = 0;
      } else {
        comments.value.push(...(result.list || []));
      }
      commentTotal.value = result.total || 0;
      hasMoreComments.value = (commentPage.value + 1) * commentPageSize < (result.total || 0);
    }
  } catch (e) {
    console.error('加载评论失败', e);
  } finally {
    commentLoading.value = false;
  }
};

// 加载更多评论
const loadMoreComments = async () => {
  commentPage.value++;
  await loadComments(false);
};

// 展开全部回复
const expandReplies = async (commentId: string) => {
  expandedReplies.add(commentId);
  try {
    const res = await request.get('/comment/replies', {
      params: { parentId: commentId, page: 0, size: 100 }
    });
    if (res.data.code === 200) {
      fullRepliesMap.value[commentId] = res.data.data.list || [];
    }
  } catch (e) {
    console.error('加载回复失败', e);
  }
};

// 折叠回复
const collapseReplies = (commentId: string) => {
  expandedReplies.delete(commentId);
};

// 获取当前显示的回复列表
const getDisplayReplies = (comment: any) => {
  if (expandedReplies.has(comment.id)) {
    return fullRepliesMap.value[comment.id] || comment.replies || [];
  }
  return comment.replies || [];
};

// 点击回复按钮
const handleReply = (comment: any, reply?: any) => {
  // 如果点击的是回复中的回复，被回复人是reply.createdBy，但parentId仍是根评论comment.id
  if (reply) {
    replyTarget.value = { id: reply.id, parentId: comment.id, username: reply.createdBy };
  } else {
    replyTarget.value = { id: comment.id, parentId: comment.id, username: comment.createdBy };
  }
  commentInput.value = '';
  // 聚焦输入框
  setTimeout(() => {
    commentInputRef.value?.focus();
  }, 50);
};

// 取消回复
const cancelReply = () => {
  replyTarget.value = null;
  commentInput.value = '';
};

// 插入 emoji
const insertEmoji = (emoji: string) => {
  const input = commentInputRef.value;
  if (!input) {
    commentInput.value += emoji;
    return;
  }
  const start = input.selectionStart ?? commentInput.value.length;
  const end = input.selectionEnd ?? commentInput.value.length;
  commentInput.value = commentInput.value.slice(0, start) + emoji + commentInput.value.slice(end);
  // 恢复光标位置
  setTimeout(() => {
    input.focus();
    input.setSelectionRange(start + emoji.length, start + emoji.length);
  }, 0);
  showEmojiPanel.value = false;
};

// 发送评论
const sendComment = async () => {
  const content = commentInput.value.trim();
  if (!content) return;
  if (!currentUser.value) {
    alert('请先登录后再评论');
    router.push('/login');
    return;
  }
  try {
    const payload: any = {
      articleId: article.value.id,
      content,
    };
    if (replyTarget.value) {
      payload.parentId = replyTarget.value.parentId;
      payload.replyToId = replyTarget.value.id;
      payload.replyToUser = replyTarget.value.username;
    }
    const res = await request.post('/comment/create', payload);
    if (res.data.code === 200) {
      commentInput.value = '';
      replyTarget.value = null;
      showEmojiPanel.value = false;
      // 刷新评论列表
      await loadComments(true);
    }
  } catch (e: any) {
    alert(e.response?.data?.message || '评论失败');
  }
};

// 删除评论
const handleDeleteComment = async (commentId: string) => {
  if (!confirm('确定要删除这条评论吗？')) return;
  try {
    const res = await request.delete(`/comment/delete/${commentId}`);
    if (res.data.code === 200) {
      await loadComments(true);
    }
  } catch (e: any) {
    alert(e.response?.data?.message || '删除失败');
  }
};

// 点赞评论
const handleLikeComment = async (comment: any) => {
  if (!currentUser.value) {
    alert('请先登录');
    return;
  }
  try {
    const res = await request.post(`/comment/like/${comment.id}`);
    if (res.data.code === 200) {
      comment.likeCount = res.data.data.likeCount;
    }
  } catch (e) {
    console.error('点赞失败', e);
  }
};

// 判断是否可以删除评论（本人或管理员）
const canDeleteComment = (comment: any) => {
  if (!currentUser.value) return false;
  return currentUser.value.username === comment.createdBy || currentUser.value.admin === true;
};

const loadDetail = async () => {
  try {
    const res = await request.get(`/article/detail/${route.params.id}`);
    if (res.data.code === 200) {
      article.value = res.data.data;
      // 设置页面标题为文章标题
      document.title = article.value.title || '文章详情';
      // 文章加载完成后加载评论
      await loadComments(true);
    }
  } catch (err) {
    console.error('Load detail error:', err);
  }
};

const formatDate = (dateStr: string) => {
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

const formatFullDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' 
  });
};

// 跳转到编辑页面
const handleEdit = () => {
  // 传递from参数，保持来源信息
  router.push(`/article/edit/${route.params.id}?from=${from.value || ''}`);
};

// 删除文章
const handleDelete = async () => {
  if (!confirm('确定要删除这篇文章吗？此操作不可恢复。')) {
    return;
  }
  try {
    const res = await request.delete(`/article/delete/${route.params.id}`);
    if (res.data.code === 200) {
      alert('删除成功');
      // 删除成功，设置标记让首页刷新
      sessionStorage.setItem('homeNeedRefresh', 'true');
      console.log('删除成功，设置刷新标记');
      router.push('/');
    }
  } catch (err: any) {
    alert(err.response?.data?.message || '删除失败');
  }
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

const galleryRef = ref<HTMLElement | null>(null);

// 监听 galleryRef 变化（因为 v-if 异步渲染），用非 passive 方式绑定滚轮事件
watch(galleryRef, (el, prevEl) => {
  if (prevEl) {
    prevEl.removeEventListener('wheel', handleWheel);
  }
  if (el) {
    el.addEventListener('wheel', handleWheel, { passive: false });
  }
});

onMounted(() => {
  window.addEventListener('scroll', handleScroll);
  window.addEventListener('keydown', handleKeydown);
  loadDetail();
  loadCurrentUser();
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
  window.removeEventListener('keydown', handleKeydown);
  galleryRef.value?.removeEventListener('wheel', handleWheel);
});
</script>

<!-- 分享弹窗 -->
<ShareModal
  :visible="showShareModal"
  :title="article?.title"
  :description="article?.content?.replace(/<[^>]*>/g, '').substring(0, 100)"
  :url="shareUrl"
  :image="article?.images?.[0] ? getImageUrl(article.images[0]) : ''"
  @close="showShareModal = false"
/>

<style scoped>
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

/* 隐藏滚动条但保持滚动功能 */
aside::-webkit-scrollbar {
  width: 0;
  height: 0;
}

aside {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 响应式高度调整 */
@media (min-width: 768px) {
  .gallery-section {
    min-height: 500px;
  }
}

@media (min-width: 1024px) {
  .gallery-section {
    min-height: 600px;
  }
}

@media (min-width: 1280px) {
  .gallery-section {
    min-height: 700px;
  }
}
</style>

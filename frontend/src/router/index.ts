import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProfileView from '../views/ProfileView.vue'
import ArticleCreateView from '../views/ArticleCreateView.vue'
import ArticleDetailView from '../views/ArticleDetailView.vue'
import ArticleEditView from '../views/ArticleEditView.vue'

// 路由元信息类型定义
interface RouteMeta {
  requiresAuth: boolean
  transition?: string
}

declare module 'vue-router' {
  interface RouteMeta extends RouteMeta {}
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { 
        requiresAuth: false,
        transition: 'none'
      }
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { 
        requiresAuth: false,
        transition: 'scale'
      }
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { 
        requiresAuth: false,
        transition: 'scale'
      }
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
      meta: { 
        requiresAuth: true,
        transition: 'slide'
      }
    },
    {
      path: '/post',
      name: 'post',
      component: ArticleCreateView,
      meta: { 
        requiresAuth: true,
        transition: 'scale'
      }
    },
    {
      path: '/article/:id',
      name: 'article-detail',
      component: ArticleDetailView,
      meta: { 
        requiresAuth: false,
        transition: 'slide'
      }
    },
    // 文章编辑页面
    {
      path: '/article/edit/:id',
      name: 'article-edit',
      component: ArticleEditView,
      meta: {
        requiresAuth: true,
        transition: 'slide'
      }
    },
    // 草稿编辑页面
    {
      path: '/draft/:id/edit',
      name: 'draft-edit',
      component: ArticleEditView,
      meta: {
        requiresAuth: true,
        transition: 'slide'
      }
    },
  ],
})

// 路由守卫：检查登录状态
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const requiresAuth = to.meta.requiresAuth;

  // 如果路由需要认证
  if (requiresAuth && !token) {
    // 保存用户想访问的页面，登录后跳转回去
    const redirectPath = to.fullPath;
    alert('请先登录');
    next({
      path: '/login',
      query: { redirect: redirectPath }
    });
  } else {
    next();
  }
});

export default router

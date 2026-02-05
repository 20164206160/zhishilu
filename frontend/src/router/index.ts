import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProfileView from '../views/ProfileView.vue'
import ArticleCreateView from '../views/ArticleCreateView.vue'
import ArticleDetailView from '../views/ArticleDetailView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: false }
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { requiresAuth: false }
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
      meta: { requiresAuth: true }
    },
    {
      path: '/post',
      name: 'post',
      component: ArticleCreateView,
      meta: { requiresAuth: true }
    },
    {
      path: '/article/:id',
      name: 'article-detail',
      component: ArticleDetailView,
      meta: { requiresAuth: true }
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

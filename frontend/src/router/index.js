import { createRouter, createWebHistory } from 'vue-router';
import LoginPage from '../views/LoginPage.vue';
import DashBoard from "../views/DashBoard.vue";
import UploadJobs from "../views/UploadJobs.vue";
import DownloadJobs from "../views/DownloadJobs.vue";
import SettingsPage from "../views/SettingsPage.vue";
import Error404 from "../views/Error404.vue";
import * as authService from "@/services/authService";

const routes = [
  {
    path: '/',
    meta: {
      requiresAuth: true
    },
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginPage
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: DashBoard,
    meta: {
      requiresAuth: true
    },
  },
  {
    path: '/uploads',
    name: 'Upload Jobs',
    component: UploadJobs,
    meta: {
      requiresAuth: true
    },
  },
  {
    path: '/downloads',
    name: 'Download Jobs',
    component: DownloadJobs,
    meta: {
      requiresAuth: true
    },
  },
  {
    path: '/settings',
    name: 'Settings',
    component: SettingsPage,
    meta: {
      requiresAuth: true
    },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/error404'
  },
  {
    path: '/error404',
    name: 'Error',
    component: Error404
  },
];

const index = createRouter({
  history: createWebHistory(),
  routes: routes
});

index.beforeEach(async (to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const token = localStorage.getItem('token');
    if (!token) {
      const newToken = await authService.refreshTokenLogic();
      if (newToken) {
        next();
      } else {
        next({ path: '/login' });
      }
    } else {
      const isTokenValid = await authService.isTokenValid(token);
      if (!isTokenValid) {
        const newToken = await authService.refreshTokenLogic();
        if (newToken) {
          next();
        } else {
          next({ path: '/login' });
        }
      } else {
        next();
      }
    }
  } else {
    next();
  }
});


export default index;

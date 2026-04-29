<template>
  <div class="login-page">
    <div class="login-card">
      <div class="intro">
        <h1>校园 OA 系统</h1>
        <p>请输入测试账号登录，验证基础用户体系和角色菜单控制。</p>
      </div>
      <form class="login-form" @submit.prevent="submit">
        <label>
          <span>用户名</span>
          <input v-model.trim="form.username" autocomplete="username" />
        </label>
        <label>
          <span>密码</span>
          <input v-model.trim="form.password" type="password" autocomplete="current-password" />
        </label>
        <button type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
        <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
      </form>
      <div class="account-list">
        <h2>测试账号</h2>
        <ul>
          <li v-for="account in accounts" :key="account.username">
            <strong>{{ account.username }}</strong>
            <span>{{ account.password }}</span>
            <em>{{ account.role }}</em>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '../api/http';
import { useAppStore } from '../stores/app';

const router = useRouter();
const appStore = useAppStore();

const loading = ref(false);
const errorMessage = ref('');
const form = reactive({
  username: 'admin',
  password: '123456'
});

const accounts = [
  { username: 'admin', password: '123456', role: '系统管理员' },
  { username: 'student', password: '123456', role: '学生' },
  { username: 'teacher', password: '123456', role: '教师' },
  { username: 'adviser', password: '123456', role: '班主任' },
  { username: 'research', password: '123456', role: '科技处' },
  { username: 'office', password: '123456', role: '教务处' },
  { username: 'reviewer', password: '123456', role: '评审专家' }
];

async function submit() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await login(form.username, form.password);
    appStore.setAuth({
      token: response.data.token,
      currentUserName: response.data.profile.realName,
      roles: response.data.profile.roles,
      menus: response.data.menus,
      permissions: response.data.permissions
    });
    await router.push('/');
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败';
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: radial-gradient(circle at top, #eaf4ff 0%, #f8fbfd 45%, #eef4f1 100%);
  padding: 24px;
}

.login-card {
  width: min(960px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 24px;
  padding: 28px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.12);
}

.intro h1,
.account-list h2 {
  margin: 0 0 12px;
}

.intro p {
  margin: 0;
  color: #52606d;
  line-height: 1.7;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.login-form label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #102a43;
  font-weight: 600;
}

.login-form input {
  border: 1px solid #d9e2ec;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 15px;
}

.login-form button {
  margin-top: 8px;
  border: none;
  border-radius: 12px;
  padding: 12px 16px;
  background: #124e78;
  color: #fff;
  font-size: 15px;
  cursor: pointer;
}

.login-form button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error-text {
  margin: 0;
  color: #b42318;
}

.account-list ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 10px;
}

.account-list li {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f8fbfd;
}

.account-list span,
.account-list em {
  color: #52606d;
  font-style: normal;
}
</style>

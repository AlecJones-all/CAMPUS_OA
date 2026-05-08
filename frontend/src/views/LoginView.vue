<template>
  <div class="login-page">
    <main class="auth-card" aria-label="账号访问">
      <header class="login-header">
        <span class="brand-mark">OA</span>
        <div>
          <h1>校园 OA 系统</h1>
          <p>统一办公入口</p>
        </div>
      </header>

      <el-tabs v-model="activeMode" stretch @tab-change="clearFeedback">
        <el-tab-pane label="登录" name="login">
          <el-form class="auth-form" label-position="top" @submit.prevent="submitLogin">
            <el-form-item label="用户名">
              <el-input v-model.trim="loginForm.username" autocomplete="username" placeholder="请输入用户名" size="large" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                v-model.trim="loginForm.password"
                type="password"
                autocomplete="current-password"
                placeholder="请输入密码"
                show-password
                size="large"
              />
            </el-form-item>
            <el-button class="auth-submit" type="primary" size="large" native-type="submit" :loading="loading">
              {{ loading ? '登录中...' : '登录系统' }}
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form class="auth-form" label-position="top" @submit.prevent="submitRegister">
            <div class="register-grid">
              <el-form-item label="用户名">
                <el-input v-model.trim="registerForm.username" autocomplete="username" placeholder="4-32 位字母、数字或下划线" size="large" />
              </el-form-item>
              <el-form-item label="姓名">
                <el-input v-model.trim="registerForm.realName" autocomplete="name" placeholder="请输入姓名" size="large" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input
                  v-model.trim="registerForm.password"
                  type="password"
                  autocomplete="new-password"
                  placeholder="至少 6 位"
                  show-password
                  size="large"
                />
              </el-form-item>
              <el-form-item label="确认密码">
                <el-input
                  v-model.trim="registerForm.confirmPassword"
                  type="password"
                  autocomplete="new-password"
                  placeholder="再次输入密码"
                  show-password
                  size="large"
                />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model.trim="registerForm.phone" autocomplete="tel" placeholder="选填" size="large" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model.trim="registerForm.email" autocomplete="email" placeholder="选填" size="large" />
              </el-form-item>
            </div>
            <el-button class="auth-submit" type="primary" size="large" native-type="submit" :loading="registering">
              {{ registering ? '注册中...' : '注册学生账号' }}
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <el-alert v-if="successMessage" class="feedback-alert" type="success" :title="successMessage" show-icon :closable="false" />
      <el-alert v-if="errorMessage" class="feedback-alert" type="error" :title="errorMessage" show-icon :closable="false" />
    </main>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { login, register } from '../api/http';
import { useAppStore } from '../stores/app';

const router = useRouter();
const appStore = useAppStore();

const activeMode = ref<'login' | 'register'>('login');
const loading = ref(false);
const registering = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

const loginForm = reactive({
  username: '',
  password: ''
});

const registerForm = reactive({
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: ''
});

async function submitLogin() {
  if (loading.value) {
    return;
  }
  if (!loginForm.username || !loginForm.password) {
    errorMessage.value = '请填写用户名和密码';
    successMessage.value = '';
    return;
  }

  loading.value = true;
  clearFeedback();
  try {
    const response = await login(loginForm.username, loginForm.password);
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

async function submitRegister() {
  if (registering.value) {
    return;
  }
  if (!registerForm.username || !registerForm.realName || !registerForm.password || !registerForm.confirmPassword) {
    errorMessage.value = '请填写用户名、姓名和密码';
    successMessage.value = '';
    return;
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致';
    successMessage.value = '';
    return;
  }

  registering.value = true;
  clearFeedback();
  try {
    await register({
      username: registerForm.username,
      realName: registerForm.realName,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      phone: registerForm.phone || undefined,
      email: registerForm.email || undefined
    });
    successMessage.value = '注册成功，请使用新账号登录';
    activeMode.value = 'login';
    loginForm.username = registerForm.username;
    loginForm.password = '';
    resetRegisterForm();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '注册失败';
  } finally {
    registering.value = false;
  }
}

function clearFeedback() {
  errorMessage.value = '';
  successMessage.value = '';
}

function resetRegisterForm() {
  registerForm.username = '';
  registerForm.realName = '';
  registerForm.password = '';
  registerForm.confirmPassword = '';
  registerForm.phone = '';
  registerForm.email = '';
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px;
  background:
    linear-gradient(135deg, rgba(15, 76, 129, 0.08), rgba(191, 139, 0, 0.08)),
    #f5f8fc;
}

.auth-card {
  width: min(520px, 100%);
  display: flex;
  flex-direction: column;
  gap: 22px;
  padding: 34px 38px;
  border: 1px solid rgba(216, 226, 236, 0.9);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.12);
}

.login-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  text-align: left;
}

.brand-mark {
  width: 52px;
  height: 52px;
  display: inline-grid;
  place-items: center;
  border-radius: 8px;
  background: var(--app-primary);
  color: #ffffff;
  font-weight: 800;
  font-size: 20px;
}

.login-header h1,
.login-header p {
  margin: 0;
}

.login-header h1 {
  font-size: 26px;
  line-height: 1.2;
  color: var(--app-text);
}

.login-header p {
  margin-top: 4px;
  color: var(--app-text-secondary);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.register-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 14px;
}

.auth-submit {
  width: 100%;
  margin-top: 8px;
}

.feedback-alert {
  margin-top: 4px;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

:deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 700;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
}

@media (max-width: 980px) {
  .auth-card {
    width: min(520px, 100%);
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 18px;
  }

  .auth-card {
    padding: 26px;
  }

  .login-header {
    justify-content: flex-start;
  }

  .register-grid {
    grid-template-columns: 1fr;
  }
}
</style>

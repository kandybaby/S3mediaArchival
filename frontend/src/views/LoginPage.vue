<style scoped src="../css/views/LoginPage.css"></style>
<template>
  <div class="d-flex justify-content-center align-items-center vh-100 bg-dark">
    <div class="login-container p-5 rounded">
      <h2 class="mb-4 text-light">Login</h2>
      <form @submit.prevent="handleLoginFormSubmission">
        <div class="form-group">
          <label for="username" class="text label">Username:</label>
          <input v-model="username" type="text" id="username" class="form-control bg-dark text-light border-0" placeholder="Username">
        </div>
        <div class="form-group">
          <label for="password" class="text label">Password:</label>
          <input v-model="password" type="password" id="password" class="form-control bg-dark text-light border-0" placeholder="Password">
        </div>
        <button type="submit" class="btn btn-primary w-100 mt-3">Login</button>
      </form>
      <p v-if="errorMessage" class="text-danger mt-3 error-message">{{ errorMessage }}</p>
    </div>
  </div>
</template>



<script>
import { handleLogin } from '@/services/authService';

export default {
  data() {
    return {
      username: '',
      password: '',
      errorMessage: ''  // Added error message data property
    };
  },
  methods: {
    async handleLoginFormSubmission() {
      try {
        const success = await handleLogin(this.username, this.password);
        if (success) {
          this.$router.push('/dashboard');
        } else {
          this.errorMessage = 'Login failed. Please check your credentials.';
        }
      } catch (error) {
        console.error('Login error:', error);
        this.errorMessage = 'An error occurred during login. Please try again later.';
      }
    }
  },
  name: 'LoginPage'
};
</script>

<template>
  <div>
    <Navbar/>
    <div class="d-flex justify-content-center align-items-center vh-95">
      <div class="container bg-dark p-5 rounded narrow">
        <div class="settings-container">
          <div class="mb-4">
            <label for="username" class="form-label text-white">Username:</label>
            <div class="input-group">
              <input type="text" id="username" v-model="username" class="form-control" placeholder="Enter new username">
              <button class="btn btn-secondary" :disabled="isProcessing" @click="updateUsername">Update Username</button>
            </div>
          </div>

          <div class="mb-4">
            <label for="password" class="form-label text-white">Password:</label>
            <div class="input-group">
              <input type="password" id="password" v-model="password" class="form-control" placeholder="Enter new password">
              <button class="btn btn-secondary" :disabled="isProcessing" @click="updatePassword">Update Password</button>
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Navbar from '../components/NavBar.vue';
import api from '../services/apiService';
import {useToast} from "vue-toastification";

export default {
  name: 'SettingsView',
  components: {
    Navbar
  },
  setup() {
    const toast = useToast();
    return { toast }
  },
  data() {
    return {
      username: '',
      password: '',
      isProcessing: false
    };
  },
  methods: {
    async updateUsername() {
      this.isProcessing = true;
      try {
        // API call to update username
        await api.put('/users/update', { username: this.username });
        this.toast.success('Username updated successfully',{timeout: 5000});
      } catch (error) {
        this.toast.error('Error updating username');
        console.error('Error updating username:', error);
        this.toast.error('Username update error', {timeout: 5000});
      } finally {
        this.isProcessing = false;
      }
    },
    async updatePassword() {
      this.isProcessing = true;
      try {
        // API call to update password
        await api.put('/users/update', { password: this.password });
        this.toast.success('Password updated successfully',{timeout: 5000});
      } catch (error) {
        this.toast.error('Error updating password');
        this.toast.error('Password update error', {timeout: 5000});
      } finally {
        this.isProcessing = false;
      }
    }
  }
};
</script>

<style src="../css/views/SettingsPage.css" scoped></style>

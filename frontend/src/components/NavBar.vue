<!-- src/components/Navbar.vue -->

<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark" style="position: sticky; top: 0; z-index: 1000; height: 5vh">
    <a class="navbar-brand ms-4">S3 Media Archival</a>
    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNav">
      <ul class="navbar-nav mr-auto">
        <li class="nav-item active">
          <router-link to="/dashboard" class="nav-link">Dashboard</router-link>
        </li>
        <li class="nav-item">
          <router-link to="/uploads" class="nav-link">Upload Jobs</router-link>
        </li>
        <li class="nav-item">
          <router-link to="/downloads" class="nav-link">Download Jobs</router-link>
        </li>
        <li class="nav-item">
          <router-link to="/settings" class="nav-link">Settings</router-link>
        </li>
      </ul>
      <!-- Optionally, add logout or user info on the right side of the navbar -->
      <ul class="navbar-nav">
        <li class="nav-item">
          <a href="#" @click.prevent="logout" class="nav-link">Logout</a>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script>
import {useToast} from "vue-toastification";
import apiService from "@/services/apiService";

export default {
  name: 'NavBar',
  setup() {
    const toast = useToast();

    return {toast}
  },
  methods: {
    async logout() {
      try {
        // Make a POST request to the logout endpoint
        const response = await apiService.post('users/logout');
        if (response.status === 200) {
          localStorage.removeItem('token');
          this.$router.push('/login');

        }
      } catch (error) {
        console.error('Logout failed:', error);
        this.toast.error('Error logging out', {timeout: 5000})
      }
    }
  }
}
</script>
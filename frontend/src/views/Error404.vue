<template>
  <div class="error-page">
    <h1>404 - Page Not Found</h1>
    <p>We're sorry, the page you requested could not be found.</p>
    <router-link to="/">Go back to the homepage</router-link>
  </div>
</template>

<script>
export default {
  name: 'ErrorPage',
};
</script>

<style scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  text-align: center;
}

.error-page h1 {
  font-size: 4rem;
  font-weight: bold;
  margin-bottom: 1rem;
}

.error-page p {
  font-size: 1.5rem;
  margin-bottom: 2rem;
}
</style>

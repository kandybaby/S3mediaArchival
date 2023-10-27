<template>
  <div id="app">
    <router-view></router-view>
  </div>
  <link rel="icon" href="/favicon.ico">
</template>

<script>
export default {
  name: 'App',
  created() {
    document.title = 'S3 Media Archival';
  }
};
</script>

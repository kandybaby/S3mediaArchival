<template>
  <div>
    <Navbar />
    <div class="d-flex justify-content-center align-items-center vh-95">
      <div class="container bg-dark p-5 rounded">
        <JobControl :is-uploads=true></JobControl>
      </div>
    </div>
  </div>
</template>


<script>
import Navbar from '../components/NavBar.vue';
import JobControl from "../components/JobControl.vue";

export default {
  name: 'UploadJobs',
  components: {
    Navbar,
    JobControl
  },
};
</script>
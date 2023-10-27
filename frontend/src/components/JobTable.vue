<template>
  <div class="table-container mt-4">
    <table class="table dark-table">
      <thead>
      <tr>
        <th scope="col"><input type="checkbox" v-model="selectAll" @change="toggleSelectAll"></th>
        <th class="column" scope="col">Name</th>
        <th v-if="isUploads" class="column" scope="col">Upload Job Status</th>
        <th v-if="!isUploads" class="column" scope="col">Download Job Status</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="job in jobs" :key="job.id">
        <td><input type="checkbox" v-model="selectedJobs" :value="job"></td>
        <td>{{ job.name }}</td>
        <td>
          <div v-if="job.jobCancelled">
            Canceled, awaiting removal and cleanup
          </div>
          <div v-else-if="job.uploadProgress > -1">
            <progress-bar :progress="job.uploadProgress"></progress-bar>
          </div>
          <div v-else-if="job.downloadProgress > -1">
            <progress-bar :progress="job.downloadProgress"></progress-bar>
          </div>
          <div v-else>
            {{ getJobStatus(job) }}
          </div>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
  <pagination-controls
      :currentPage="currentPage"
      :pageSize="pageSize"
      :totalItemCount="totalMediaCount"
      :maxPage="maxPage"
      @update-page="goToPage"
      @page-size-changed="changePageSize"
  ></pagination-controls>
</template>

<script>
import api from "@/services/apiService";
import PaginationControls from "@/components/PaginationControls.vue";
import ProgressBar from "@/components/progressBar.vue";

export default {
  name: 'JobTable',
  components: {ProgressBar, PaginationControls},
  props: {
    searchQuery: String,
    isUploads: Boolean
  },
  data() {
    return {
      jobs: [],
      selectedJobs: [],
      selectAll: false,
      currentPage: 0,
      pageSize: 100,
      totalMediaCount: 0
    };
  },
  methods: {
    fetchJobs() {
      const params = {
        page: this.currentPage,
        size: this.pageSize,
        sortBy: this.isUploads ? "uploadJobs" : "downloadJobs",
        search: this.searchQuery,
        isArchiving: this.isUploads,
        isRecovering: !this.isUploads
      };
      api.get('/media-objects', { params })
          .then(response => {
            this.jobs = response.data.content;
            this.totalMediaCount = response.data.totalElements;
          })
          .catch(error => {
            console.error("Error fetching media:", error);
          });
    },
    toggleSelectAll() {
      if (this.selectAll) {
        this.selectedJobs = this.jobs.slice();
      } else {
        this.selectedJobs = [];
      }
    },
    goToPage(page) {
      this.currentPage = page;
      this.fetchJobs();
      this.scrollToTop();
    },
    changePageSize(newSize) {
      this.pageSize = newSize;
      this.fetchJobs();
    },
    scrollToTop() {
      window.scrollTo(0, 0);
    },
    getJobStatus(job) {
      if(this.isUploads){
        if (job.tarring) {
          return "Tarring media";
        }
      } else {
        if(job.restoring){
          return "Restoring media"
        }
        if(job.restored){
          return "Media restored, waiting..."
        }
        if(job.downloadSuccess){
          return "Download complete!"
        }
        if(job.downloadSuccess !== null && !job.downloadSuccess) {
          return "Download failed"
        }
      }
      return "Waiting...";
    },
  },
  created() {
    this.fetchJobs();
  },
  mounted() {
    this.interval = setInterval(this.fetchJobs, 20000);
  },
  beforeUnmount() {
    clearInterval(this.interval);
  },
  computed: {
    maxPage() {
      return Math.ceil(this.totalMediaCount / this.pageSize) - 1;
    },
  },
  watch: {
    selectedJobs(newVal) {
      console.log("Selected jobs in jobs table:", newVal); // Add this
      this.$emit('update:selectedJobs', newVal);
    }
  }
};
</script>

<style src="../css/components/JobTable.css" scoped></style>

<template>
  <div class="library-controls d-flex justify-content-between align-items-center mt-4">
    <div class="controls-left d-flex align-items-center">
      <input type="text" v-model="searchQuery" placeholder="Search" class="form-control library-search-input me-2">
    </div>

    <div class="controls-right">
      <button  class="btn btn-secondary me-2" :disabled="isProcessing || !isAnyJobSelected" @click="cancelJobs">Cancel Selected</button>
      <button  class="btn btn-secondary me-2" :disabled="isProcessing" @click="cancelAllJobs">Cancel All</button>
      <button v-if="!isUploads" class="btn btn-secondary me-2" :disabled="isProcessing || !isAnyJobSelected" @click="clearSelectedDoneJobs">Clear</button>
      <button v-if="!isUploads" class="btn btn-secondary" :disabled="isProcessing" @click="clearAllDoneJobs">Clear All Done</button>
    </div>
  </div>
  <JobTable
      :search-query="searchQuery"
      :is-uploads="isUploads"
      ref="jobTableRef"
      @update:selectedJobs="updateSelectedJobs"
  />
</template>



<script>
import JobTable from './JobTable.vue';
import api from "@/services/apiService";
import {useToast} from "vue-toastification";

export default {
  name: 'JobControl',
  components: {
    JobTable
  },
  props: {
    isUploads: Boolean
  },
  setup() {
    const toast = useToast();

    return { toast }
  },
  data() {
    return {
      searchQuery: '',
      isProcessing: false,
      selectedJobs: []
    };
  },
  methods: {
    cancelJobs() {
      this.isProcessing = true;

      const ids = this.selectedJobs.map(media => {
        return media.id
      })
      api.post('/media-objects/cancel-job', ids)
          .then(() => {
            this.isProcessing = false;
            this.toast.success('Eligible Jobs Canceled', {timeout: 5000})
            this.$refs.jobTableRef.fetchJobs();
          })
          .catch(error => {
            this.isProcessing = false;
            console.error("Error canceling jobs:", error);
            this.toast.error('Error canceling jobs', {timeout: 5000})
          });
    },
    cancelAllJobs() {
      this.isProcessing = true;
      api.post('/media-objects/cancel-all-archive-jobs')
          .then(() => {
            this.isProcessing = false;
            this.toast.success('Eligible Jobs Canceled', {timeout: 5000})
            this.$refs.jobTableRef.fetchJobs();
          })
          .catch(error => {
            this.isProcessing = false;
            console.error("Error canceling jobs:", error);
            this.toast.error('Error canceling jobs', {timeout: 5000})
          });
    },
    clearSelectedDoneJobs() {
      this.isProcessing = true;

      const ids = this.selectedJobs.map(media => {
        return media.id
      })
      console.log({ids});
      api.post('/media-objects/clear-finished', ids)
          .then(() => {
            this.isProcessing = false;
            this.toast.success('Eligible Jobs Cleared', {timeout: 5000})
            this.$refs.jobTableRef.fetchJobs();
          })
          .catch(error => {
            this.isProcessing = false;
            console.error("Error clearing jobs:", error);
            this.toast.error('Error clearing jobs', {timeout: 5000})
          });
    },
    clearAllDoneJobs() {
      this.isProcessing = true;
      api.post('/media-objects/clear-all-finished')
          .then(() => {
            this.isProcessing = false;
            this.toast.success('Eligible Jobs Cleared', {timeout: 5000})
            this.$refs.jobTableRef.fetchJobs();
          })
          .catch(error => {
            this.isProcessing = false;
            console.error("Error clearing jobs:", error);
            this.toast.error('Error clearing jobs', {timeout: 5000})
          });
    },
    updateSelectedJobs(newVal) {
      console.log("New Val in UploadBoard:", newVal); // Add this
      this.selectedJobs = newVal;
    },
  },
  computed: {
    isAnyJobSelected() {
      return this.selectedJobs && this.selectedJobs.length > 0;
    }
  },
  watch: {
    searchQuery(newQuery, oldQuery) {
      this.$nextTick(() => {
            if (newQuery !== oldQuery) {
              this.$refs.jobTableRef.fetchJobs();
            }
          }
      )}
  },
};
</script>

<style src="../css/components/JobControl.css" scoped></style>
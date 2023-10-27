<template>
  <div v-if="isVisible" class="modal fade show d-block" tabindex="-1">
    <div class="wide-modal modal-dialog modal-dialog-centered">
      <div class="modal-content custom-border-modal dark-modal">
        <div class="modal-header">
          <h5 class="modal-title" id="libraryDetailsModalLabel">Library Details</h5>
        </div>
        <div class="modal-body">
          <div class="detail">
            <strong>Name:</strong> {{ library.name }}
          </div>
          <div class="detail">
            <strong>Path:</strong> {{ library.path }}
          </div>
          <div class="detail">
            <strong>Storage Class:</strong> {{ library.storageClass }}
          </div>
          <div class="detail">
            <strong>S3 Bucket Name:</strong> {{ library.bucketName }}
          </div>
          <div class="detail" v-if="library.isTVSeries">
            This is a TV Series.
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-danger" @click="confirmDelete">Delete Library</button>
          <button type="button" class="btn btn-secondary" @click="confirmSynchronize">Synchronize Library</button>
          <button type="button" class="btn btn-secondary" @click="confirmUpload">Upload Library</button>
          <button type="button" class="btn btn-secondary" @click="closeModal">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {useToast} from "vue-toastification";
import api from '../services/apiService'

export default {
  name: 'LibraryDetailsModal',
  props: {
    isVisible: {
      type: Boolean,
      required: true
    },
    library: {
      type: Object,
      required: true
    }
  },
  setup() {
    const toast = useToast();

    return {toast}
  },
  methods: {
    async confirmDelete() {
      if (window.confirm('Are you sure you want to delete this library?')) {
        try {
          const response = await api.delete(`/libraries/${this.library.id}`);
          if (response.status >= 200 && response.status < 300) {
            this.closeModal();
            location.reload();
          }
        } catch (error) {
          this.toast.error('Error deleting the library.', {timeout: 5000});
        }
      }
    },

    async confirmSynchronize() {
      if (window.confirm('Are you sure you want to synchronize this library?')) {
        const toastId = this.toast.info('Synchronizing...', { timeout: false });
        this.$emit('synchronizeStart');
        this.closeModal();

        const pollLibraryStatus = async () => {
          try {
            const response = await api.get(`/libraries/${this.library.id}`);
            if (response.data && !response.data.updating) {
              // Library is not updating, proceed to success message
              this.toast.success('Synchronization successful', { timeout: 5000 });
              this.$emit('reFetchMedia');
              this.toast.dismiss(toastId);
              this.$emit('synchronizeEnd');
            } else {
              // Library is still updating, poll again in 20 seconds
              setTimeout(pollLibraryStatus, 20000);
            }
          } catch (error) {
            this.toast.error('Error checking library status', { timeout: 5000 });
            console.error("Error checking library status:", error);
            this.toast.dismiss(toastId);
            this.$emit('synchronizeEnd');
          }
        };

        try {
          const response = await api.post(`/libraries/${this.library.id}/synchronize`);
          if (response.status >= 200 && response.status < 300) {
            // Start polling the server every 20 seconds to check if the library is updated
            setTimeout(pollLibraryStatus, 20000);
          }
        } catch (error) {
          if (error.response && error.response.status === 409) {
            this.toast.error('Cannot sync library when it has active jobs', { timeout: 5000 });
          } else {
            this.toast.error('Error syncing the library', { timeout: 5000 });
          }
          this.toast.dismiss(toastId);
          this.$emit('synchronizeEnd');
        }
      }
    },

    async confirmUpload() {
      if (window.confirm('Are you sure you want to upload to this library?')) {
        this.closeModal();
        try {
          const response = await api.post(`/libraries/${this.library.id}/archive`);
          if (response.status >= 200 && response.status < 300) {
            this.toast.success('Eligible jobs added successfully', {timeout: 5000});
          }
        } catch (error) {
          this.toast.error('Error adding jobs.', {timeout: 5000});
        }
      }
    },
    closeModal() {
      this.$emit('updateVisibility', false);
    }
  }
};
</script>

<style src="../css/components/LibraryDetailsModal.css" scoped></style>

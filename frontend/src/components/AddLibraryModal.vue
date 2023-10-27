<template>
  <div v-if="isVisible" class="modal fade show d-block" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered"> <!-- center modal -->
      <div class="modal-content custom-border-modal dark-modal">
        <div class="modal-header">
          <h5 class="modal-title" id="addLibraryModalLabel">Add Library</h5>
        </div>
        <div class="modal-body">
          <label for="libraryName">Library Name</label>
          <input id="libraryName" type="text" v-model="newLibrary.name" class="form-control my-2" @input="validateInput('name')">

          <label for="libraryPath">Path</label>
          <input id="libraryPath" type="text" v-model="newLibrary.path" class="form-control my-2" @input="validateInput('path')">

          <label for="storageClass">Storage Class</label>
          <select id="storageClass" v-model="newLibrary.storageClass" class="form-control my-2">
            <option value="DEEP_ARCHIVE">Glacier Deep Archive</option>
            <option value="GLACIER">Glacier Standard</option>
            <option value="GLACIER_IR">Glacier Instant Retrieval</option>
            <option value="STANDARD">Standard</option>
            <option value="STANDARD_IA">Standard Infrequent Access</option>
          </select>

          <label for="bucketName">S3 Bucket Name</label>
          <input id="bucketName" type="text" v-model="newLibrary.bucketName" class="form-control my-2" @input="validateInput('bucketName')">

          <div class="custom-control custom-checkbox my-2">
            <input type="checkbox" class="custom-control-input" id="tvSeriesCheckbox" v-model="isTVSeries">
            <label class="custom-control-label ml-2" for="tvSeriesCheckbox">TV Series ?</label>
          </div>

          <div v-if="errorMessage" class="alert alert-danger mt-3">{{ errorMessage }}</div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeModal">Close</button>
          <button type="button" class="btn btn-primary" @click="addLibrary" :disabled="!isFormValid">Add Library</button>
        </div>
      </div>
    </div>
  </div>
</template>


<script>
import api from '../services/apiService';

export default {
  name: 'NewLibraryModal',
  data() {
    return {
      newLibrary: {
        name: '',
        path: '',
        storageClass: '',
        bucketName: ''
      },
      isTVSeries: false,
      showModal: false,
      errorMessage: null
    };
  },
  props: {
    isVisible: {
      type: Boolean,
      required: true
    }
  },
  methods: {
    async addLibrary() {
      this.newLibrary.category = this.isTVSeries ? 'tv' : 'other';

      try {
        const response = await api.post('/libraries', this.newLibrary);

        if (response.status === 201) {
          this.$emit('libraryAdded');  // Notify parent component that a library was added
          this.closeModal(); // Close the modal
          this.errorMessage = null; // Reset the error message
        } else {
          // Use the response data from the server to set the error message
          this.errorMessage = response.data;
        }
      } catch (error) {
        console.error('Error adding library:', error);
        // If there's an error response from the server, use that. Otherwise, use a generic error message.
        this.errorMessage = error.response && error.response.data ? error.response.data : "Error adding library.";
      }
    },
    closeModal() {
      this.$emit('updateVisibility', false);
      this.resetInputs();
    },
    resetInputs() {
      this.newLibrary = {
        name: '',
        path: '',
        storageClass: '',
        bucketName: ''
      };
      this.isTVSeries = false;
    },
    validateInput(field) {
      this.newLibrary[field] = this.newLibrary[field].replace(/[:*?"<>|]/g, "");
    }
  },
  computed: {
    isFormValid() {
      return this.newLibrary.name &&
          this.newLibrary.path &&
          this.newLibrary.storageClass &&
          this.newLibrary.bucketName;
    }
  },
};
</script>
<style src="../css/components/AddLibraryModal.css" scoped></style>
<template>
  <div>
    <Navbar/>
    <div class="d-flex justify-content-center align-items-center vh-95">
      <div class="container bg-dark p-5 rounded">
        <div class="btn-container">
          <div v-for="library in libraries" :key="library.id" class="btn-wrapper">
            <button
                class="btn btn-secondary library-btn"
                :class="{ 'selected': library.id === selectedLibraryId }"
                @click="selectLibrary(library)">
              {{ library.name }}
            </button>

          </div>
          <div>
            <button class="btn btn-secondary plus-btn" :disabled="isProcessing" @click="openModal">+</button>
          </div>
        </div>
        <div class="library-controls d-flex justify-content-between align-items-center mt-4">
          <!-- Left-aligned controls -->
          <div class="controls-left d-flex align-items-center">
            <input type="text" v-model="searchQuery" placeholder="Search"
                   class="form-control library-search-input me-2">
            <select v-model="selectedArchiveStatus" class="form-select status-select">
              <option value="">Select Archive Status</option> <!-- This is the default option -->
              <option v-for="status in Object.keys(statusMapping)" :value="status" :key="status">
                {{ getDisplayStatus(status) }}
              </option>
            </select>
          </div>

          <div class="controls-right">
            <button class="btn btn-secondary me-2" :disabled="isProcessing" @click="scanLibrary">Scan</button>
            <button class="btn btn-secondary me-2 " :disabled="isProcessing" @click="showLibraryDetails">Library
              Details
            </button>
            <button class="btn btn-secondary me-2" :disabled="!isMediaSelected || isProcessing" @click="uploadMedias">
              Upload
            </button>
            <button class="btn btn-secondary me-2" :disabled="!isMediaSelected || isProcessing" @click="downloadMedias">
              Download
            </button>
            <button class="btn btn-danger me-2" :disabled="!isMediaSelected || isProcessing" @click="deleteMedias">
              Delete
            </button>
          </div>

        </div>
        <MediaTable
            ref="mediaTable"
            :selected-library-id="selectedLibraryId"
            :search-query="searchQuery"
            :selected-archive-status="selectedArchiveStatus"
            @selection-changed="updateMediaSelection"
        />
      </div>
    </div>
    <AddLibraryModal :isVisible="isModalVisible" @updateVisibility="updateModalVisibility"
                     @libraryAdded="fetchLibraries"/>
    <LibraryDetailsModal :isVisible="isLibraryDetailsVisible" :library="selectedLibrary"
                         @updateVisibility="updateLibraryDetailsVisibility" @reFetchMedia="refreshMedia"
                         @synchronizeStart="handleSynchronizeStart"
                         @synchronizeEnd="handleSynchronizeEnd"/>
  </div>
</template>


<script>
import Navbar from '../components/NavBar.vue';
import AddLibraryModal from "../components/AddLibraryModal.vue";
import LibraryDetailsModal from "../components/LibraryDetailsModal.vue";
import MediaTable from '../components/MediaTable.vue';
import api from '../services/apiService'
import {useToast} from "vue-toastification";

export default {
  name: 'DashBoard',
  components: {
    Navbar,
    AddLibraryModal,
    MediaTable,
    LibraryDetailsModal
  },
  setup() {
    const toast = useToast();

    return {toast}
  },
  watch: {
    searchQuery() {
      this.$nextTick(() => {
        this.$refs.mediaTable.fetchMedias();
      });
    },
    selectedArchiveStatus() {
      this.$nextTick(() => {
        this.$refs.mediaTable.fetchMedias();
      });
    },
  },
  methods: {
    getDisplayStatus(statusEnum) {
      return this.statusMapping[statusEnum] || statusEnum;
    },
    selectLibrary(library) {
      this.selectedLibraryId = library.id;
      this.selectedLibrary = library;
    },
    openModal() {
      this.isModalVisible = true;
    },
    updateModalVisibility(value) {
      this.isModalVisible = value;
    },
    async fetchLibraries() {
      try {
        const response = await api.get('/libraries');
        this.libraries = response.data;
        if (this.libraries.length > 0) {
          this.selectedLibraryId = this.libraries[0].id;
          this.selectedLibrary = this.libraries[0];
        }
      } catch (error) {
        this.toast.error('Error fetching the libraries', {timeout: 5000})
        console.error("Error fetching libraries:", error);
      }
    },

    async scanLibrary() {
      this.isProcessing = true; // Start the processing state
      const toastId = this.toast.info('Scanning...', { timeout: false });

      const pollLibraryStatus = async () => {
        try {
          const response = await api.get(`/libraries/${this.selectedLibraryId}`);
          if (response.data && !response.data.updating) {
            // Library is not updating, proceed to success message and refresh media
            this.toast.success('Library scanned successfully!', { timeout: 5000 });
            this.refreshMedia();
            this.isProcessing = false; // Reset the processing state
            this.toast.dismiss(toastId);
          } else {
            // Library is still updating, poll again in 20 seconds
            setTimeout(pollLibraryStatus, 10000);
          }
        } catch (error) {
          this.toast.error('Error checking library status', { timeout: 5000 });
          console.error("Error checking library status:", error);
          this.isProcessing = false; // Reset the processing state
          this.toast.dismiss(toastId);
        }
      };

      try {
        await api.post(`/libraries/${this.selectedLibraryId}/scan`);
        // Start polling the server every 20 seconds to check if the library is updated
        setTimeout(pollLibraryStatus, 10000);
      } catch (error) {
        if (error.response && error.response.status === 409) {
          this.toast.error('Cannot scan library when it has active jobs', { timeout: 5000 });
        } else {
          this.toast.error('Error scanning the library', { timeout: 5000 });
        }
        console.error("Error scanning the library:", error);
        this.isProcessing = false; // Reset the processing state
        this.toast.dismiss(toastId);
      }
    },

    updateMediaSelection(selected) {
      this.isMediaSelected = selected.length > 0;
      this.selectedMedia = selected;
    },
    async sendMediaPathsToEndpoint(endpoint, paths) {
      this.isProcessing = true;

      try {
        const response = await api.post(endpoint, paths);
        if (response.status === 200) {
          this.toast.success('Eligible jobs added successfully', {
            timeout: 5000
          });
        }
      } catch (error) {
        this.toast.error('Error adding jobs', {timeout: 5000})
        console.error(`Error sending media to ${endpoint}:`, error);
      } finally {
        this.isProcessing = false;
      }
    },
    async downloadMedias() {
      const downloadPaths = this.selectedMedia.filter(media =>
          ['ARCHIVED', 'OUT_OF_DATE'].includes(media.archivedStatus)
      ).map(media => {
        return media.path
      });
      await this.sendMediaPathsToEndpoint('media-objects/prepare-download', downloadPaths);
    },
    async uploadMedias() {
      const paths = this.selectedMedia.map(media => {
        return media.path
      })
      await this.sendMediaPathsToEndpoint('media-objects/archive', paths);
    },
    async deleteMedias() {
      if (window.confirm('Deleting media objects will not remove files from S3, or your filesystem.')) {
        this.isProcessing = true;
        const ids = this.selectedMedia.map(media => {
          return media.id
        })
        try {
          const response = await api.post('media-objects/bulk-delete', ids);
          if (response.status === 200) {
            this.toast.success('Media deleted', {
              timeout: 5000
            });
          }
        } catch (error) {
          this.toast.error('Error deleting media', {timeout: 5000})
        } finally {
          this.isProcessing = false;
          this.$refs.mediaTable.fetchMedias();
        }
      }
    },
    showLibraryDetails() {
      this.isLibraryDetailsVisible = true;
    },

    updateLibraryDetailsVisibility(value) {
      this.isLibraryDetailsVisible = value;
    },
    refreshMedia() {
      this.$refs.mediaTable.fetchMedias();
    },
    handleSynchronizeStart() {
      this.isProcessing = true;
    },
    handleSynchronizeEnd() {
      this.isProcessing = false;
    },
  },
  data() {
    return {
      isModalVisible: false,
      libraries: [],
      selectedLibraryId: null,
      selectedLibrary: null,
      statusMapping: {
        ARCHIVED: 'Archived',
        NOT_ARCHIVED: 'Not Archived',
        OUT_OF_DATE: 'Out of Date',
      },
      searchQuery: '',
      selectedArchiveStatus: '',
      isMediaSelected: false,
      selectedMedia: [],
      isProcessing: false,
      isLibraryDetailsVisible: false,
    };
  },
  created() {
    this.fetchLibraries();
  },
};
</script>

<style src="../css/views/DashBoard.css" scoped></style>

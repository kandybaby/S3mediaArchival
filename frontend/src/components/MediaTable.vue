<template>
  <div class="table-container mt-4">
    <table class="table dark-table" ref="mediaTable">
      <thead>
      <tr>
        <th scope="col"><input type="checkbox" v-model="selectAll" @change="toggleSelectAll"></th>
        <th class="wide-column" scope="col" @click="changeSort('name')">
          Name <span>{{ arrowDirection('name') }}</span>
        </th>

        <th class="narrow-column" scope="col" @click="changeSort('archivedStatus')">
          Archived Status <span>{{ arrowDirection('archivedStatus') }}</span>
        </th>

        <th class="narrow-column" scope="col" @click="changeSort('dateLastModified')">
          Date Last Modified <span>{{ arrowDirection('dateLastModified') }}</span>
        </th>

        <th class="narrow-column" scope="col" @click="changeSort('dateArchived')">
          Date Archived <span>{{ arrowDirection('dateArchived') }}</span>
        </th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="media in medias" :key="media">
        <td><input type="checkbox" v-model="selectedMedias" :value="media"></td>
        <td class="wide-column">{{ media.name }}</td>
        <td class="narrow-column">{{ getDisplayStatus(media.archivedStatus) }}</td>
        <td class="narrow-column">{{ formatDate(media.dateLastModified) }}</td>
        <td class="narrow-column">{{ formatDate(media.dateArchived) }}</td>
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
import api from '../services/apiService';
import PaginationControls from './PaginationControls.vue';
export default {
  name: 'MediaTable',
  components: {
    PaginationControls,
  },
  data() {
    return {
      medias: [],
      currentPage: 0,
      sortBy: 'name',
      sortDirection: 'asc',
      totalMediaCount: 0,
      pagesToShowCount: 5,
      selectAll: false,
      selectedMedias: [],
      pageSize: 100,
    };
  },
  props: {
    selectedLibraryId: {
      type: [String, Number],
      default: null
    },
    searchQuery: {
      type: String,
      default: ''
    },
    selectedArchiveStatus: {
      type: String,
      default: ''
    },
  },
  methods: {
    scrollToTop() {
      this.$refs.mediaTable.scrollIntoView({ behavior: 'auto' });
    },
    formatDate(date) {
      if(date){
        return new Date(date).toLocaleString();
      }
      return ""
    },
    fetchMedias() {
      const params = {
        page: this.currentPage,
        size: this.pageSize,
        sortBy: this.sortBy,
        sortDirection: this.sortDirection,
        libraryId: this.selectedLibraryId,
        search: this.searchQuery,
      };
      if(this.selectedArchiveStatus !== ""){
        params.archivedStatus = this.selectedArchiveStatus
      }
      if(this.selectedLibraryId){
        api.get('/media-objects', { params })
            .then(response => {
              this.medias = response.data.content;
              this.totalMediaCount = response.data.totalElements;
            })
            .catch(error => {
              console.error("Error fetching media:", error);
            });
      }
    },
    goToPage(page) {
      this.currentPage = page;
      this.fetchMedias();
      this.scrollToTop();
    },
    changePageSize(newSize) {
      this.pageSize = newSize;
      this.fetchMedias();
    },
    getDisplayStatus(statusEnum) {
      const statusMapping = {
        ARCHIVED: 'Archived',
        NOT_ARCHIVED: 'Not Archived',
        OUT_OF_DATE: 'Out of Date',
        FAILED: 'Failed'
      };
      return statusMapping[statusEnum] || statusEnum;
    },
    toggleSelectAll() {
      if (this.selectAll) {
        this.selectedMedias = this.medias;
      } else {
        this.selectedMedias = [];
      }
    },
    changeSort(column) {
      if (this.sortBy === column) {
        // If the column is already the current sort column, toggle the sort direction.
        this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
      } else {
        // Otherwise, set the new sort column and default to asc order.
        this.sortBy = column;
        this.sortDirection = 'asc';
      }
      this.fetchMedias();
      this.goToPage(0);
    },
    arrowDirection(column) {
      if (this.sortBy === column) {
        return this.sortDirection === 'asc' ? '↑' : '↓';
      }
      return '';
    },
  },
  created() {
    this.fetchMedias();
  },
  watch: {
    selectedLibraryId() {
      this.fetchMedias();
      this.goToPage(0);
    },
    selectedMedias() {
      this.selectAll = this.areAllSelected;
      this.$emit('selection-changed', this.selectedMedias);
    },
  },
  computed: {
    maxPage() {
      return Math.ceil(this.totalMediaCount / this.pageSize) - 1;
    },
    areAllSelected() {
      return this.medias.length && this.medias.length === this.selectedMedias.length;
    }
  }
};

</script>

<style src="../css/components/MediaTable.css" scoped></style>

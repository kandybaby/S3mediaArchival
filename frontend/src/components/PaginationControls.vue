<template>
  <div class="pagination-controls" v-if="maxPage > 0">
    <button @click="goToPage(0)" :disabled="currentPage === 0">&lt;&lt;</button>
    <button @click="prevPage" :disabled="currentPage === 0">&lt;</button>
    <span v-for="page in pagesToShow" :key="page">
      <template v-if="page === '...'">
          ...
      </template>
      <template v-else>
          <button
              @click="goToPage(page-1)"
              :class="{ 'active-page': currentPage === page-1 }">{{ page }}</button>
      </template>
    </span>
    <button @click="nextPage" :disabled="currentPage === maxPage">&gt;</button>
    <button @click="goToPage(maxPage)" :disabled="currentPage === maxPage">&gt;&gt;</button>
  </div>
  <div class="results-per-page mt-2">
    <label for="resultsPerPage"># of Results per page:</label>
    <select id="resultsPerPage" class="results-select" :value="selectedPageSize" @change="onPageSizeChange">
      <option value="25">25</option>
      <option value="50">50</option>
      <option value="75">75</option>
      <option value="100">100</option>
    </select>
  </div>
</template>

<script>
export default {
  name: 'PaginationControls',
  props: {
    currentPage: Number,
    pageSize: Number,
    totalItemCount: Number,
    maxPage: Number,
  },
  data() {
    return {
      selectedPageSize: 100
    };
  },
  methods: {
    prevPage() {
      if (this.currentPage > 0) {
        this.$emit('update-page', this.currentPage - 1);
      }
    },
    nextPage() {
      if (this.currentPage < this.maxPage) {
        this.$emit('update-page', this.currentPage + 1);
      }
    },
    goToPage(page) {
      this.$emit('update-page', page);
    },
    onPageSizeChange(event) {
      this.selectedPageSize = event.target.value;
      this.$emit('page-size-changed', this.selectedPageSize);
    }
  },
  computed: {
    pagesToShow() {
      const pages = [];

      // If only one page, return empty array (pagination controls will be hidden)
      if (this.maxPage === 0) {
        return pages;
      }

      // If 5 pages or fewer, display them all
      if (this.maxPage <= 4) {
        for (let i = 0; i <= this.maxPage; i++) {
          pages.push(i + 1);
        }
        return pages;
      }

      // If within the last five pages, just display those
      if (this.currentPage + 5 > this.maxPage) {
        for (let i = this.maxPage - 4; i <= this.maxPage; i++) {
          pages.push(i + 1);
        }
        return pages;
      }

      // Current page
      pages.push(this.currentPage + 1); // Adding 1 because the backend is 0-based, but we're displaying 1-based to the user

      // Next two pages
      if (this.currentPage + 1 < this.maxPage) {
        pages.push(this.currentPage + 2);
      }
      if (this.currentPage + 2 < this.maxPage) {
        pages.push(this.currentPage + 3);
      }

      // Ellipsis (only if there are more pages between the next pages and the last two pages)
      if (this.currentPage + 3 < this.maxPage - 1) {
        pages.push('...');
      }

      // Second last page (only if more than 2 total pages)
      if (this.maxPage > 2) {
        pages.push(this.maxPage); // second last page
      }

      // Last page (only if more than 1 total page)
      if (this.maxPage > 1) {
        pages.push(this.maxPage + 1); // last page
      }

      return pages;
    },
  }
}
</script>
<style src="../css/components/PaginationControls.css" scoped></style>

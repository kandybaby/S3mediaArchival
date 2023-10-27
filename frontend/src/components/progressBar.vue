<template>
  <div class="progress">
    <div
        class="progress-bar"
        role="progressbar"
        :style="{ width: progress + '%' }"
        :aria-valuenow="progress"
        aria-valuemin="0"
        aria-valuemax="100"
    >
      {{ progress }}%
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProgressBar',
  props: {
    progress: {
      type: Number,
      default: 0,
      validator(value) {
        return value >= 0 && value <= 100;
      }
    }
  }
};
</script>

<style scoped>
.progress {
  background-color: #e0e0e0;
  border-radius: 0.25rem;
  overflow: hidden;
  position: relative;
  height: 20px;
}

.progress-bar {
  background-color: #544caf;
  height: 100%;
  transition: width 0.6s ease;
}
</style>

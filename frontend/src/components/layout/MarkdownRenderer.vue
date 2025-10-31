<template>
  <div class="markdown-body" v-html="renderedMarkdown"></div>
</template>

<script>
import MarkdownIt from 'markdown-it';
import DOMPurify from 'dompurify';

export default {
  props: {
    content: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      md: new MarkdownIt({
        html: false, // Disable HTML tags in source
        xhtmlOut: true, // Use '/' to close single tags
        breaks: true, // Convert '\n' to `<br>`
        linkify: true, // Autoconvert URL-like text to links
        typographer: true, // Enable smartquotes and other typographic replacements
      }),
    };
  },
  computed: {
    renderedMarkdown() {
      return DOMPurify.sanitize(this.md.render(this.content));
    },
  },
};
</script>

<style scoped>
/* Basic GitHub-like markdown styling */
.markdown-body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
  font-size: 16px;
  line-height: 1.5;
  color: #24292e;
  word-wrap: break-word;
}

.markdown-body h1 {
  font-size: 2em;
  margin: 0.67em 0;
  padding-bottom: 0.3em;
  border-bottom: 1px solid #eaecef;
}

.markdown-body h2 {
  font-size: 1.5em;
  margin: 0.83em 0;
  padding-bottom: 0.3em;
  border-bottom: 1px solid #eaecef;
}

.markdown-body ul {
  padding-left: 2em;
  margin-top: 0;
  margin-bottom: 16px;
}

.markdown-body li {
  margin-bottom: 0.25em;
}

.markdown-body strong {
  font-weight: 600;
}

.markdown-body em {
  font-style: italic;
}

.markdown-body a {
  color: #0366d6;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}
</style>

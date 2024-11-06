<script lang="ts">
  import {Tldraw, useEditor} from 'tldraw';
  import 'tldraw/tldraw.css';
  import {hooks} from "svelte-preprocess-react";

  // const editor = hooks(() => useEditor());
  // console.log('editor', editor)
</script>

<main>
  <react:Tldraw
      options={{
				maxPages: 1
			}}
  />
</main>

<style>
  main {
      padding: 2rem;
      height: calc(100vh - 10rem);
      width: calc(100vw - 10rem);
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  }
</style>

<script lang="ts">
    import { onMount } from "svelte";
    import { EventSource } from "eventsource";

    let message = $state<string>();

    onMount(() => {
        const es = new EventSource("/sse");
        es.addEventListener("message", (event) => {
            message = event.data;
        });

        return () => {
            es.close();
        };
    });
</script>

<p>{message}</p>

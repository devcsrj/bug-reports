To run:

```
npm run dev
```

Visit [localhost:5173](http://localhost:5173).

* Notice that the UI updates based on the server time.
* Refresh the page

The server will crash with:

```
node:internal/process/promises:392
      new UnhandledPromiseRejection(reason);
      ^

UnhandledPromiseRejection: This error originated either by throwing inside of an async function without a catch block, or by rejecting a promise which was not handled with .catch(). The promise rejected with the reason "undefined".
    at throwUnhandledRejectionsMode (node:internal/process/promises:392:7)
    at processPromiseRejections (node:internal/process/promises:475:17)
    at process.processTicksAndRejections (node:internal/process/task_queues:106:32) {
  code: 'ERR_UNHANDLED_REJECTION'
}

Node.js v22.11.0
```

Technically can be prevented with:

```ts
process.on('unhandledRejection', (reason) => {
	console.error(`Failed; ${new Date()}`)
	console.error('Unhandled Rejection (global):', reason);
});
```

But the session will keep running behind the scenes (e.g.: the handler printing `Failed` in the console)

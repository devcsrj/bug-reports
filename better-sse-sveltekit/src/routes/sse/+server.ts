import type { RequestHandler } from "@sveltejs/kit";
import { createResponse } from "better-sse";

export const GET: RequestHandler = async ({ request }) => {
  return createResponse(request, (session) => {
    const tick = setInterval(() => {
      session.push(`The time is: ${new Date()}`);
    }, 1000);

    session.on("disconnected", () => {
      clearInterval(tick);
    });
  });
};

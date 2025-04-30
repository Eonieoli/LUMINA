import { AutoClientInterface } from "@elizaos/client-auto";
import { DiscordClientInterface } from "@elizaos/client-discord";
import { TelegramClientInterface } from "@elizaos/client-telegram";
import { TwitterClientInterface } from "@elizaos/client-twitter";
import { Character, IAgentRuntime } from "@elizaos/core";
import { LunaClientInterface } from "./luna-client.ts";

export async function initializeClients(
  character: Character,
  runtime: IAgentRuntime
) {
  const clients = [];
  const clientTypes = character.clients?.map((str) => str.toLowerCase()) || [];

  if (clientTypes.includes("auto")) {
    const autoClient = await AutoClientInterface.start(runtime);
    if (autoClient) clients.push(autoClient);
  }

  if (clientTypes.includes("discord")) {
    clients.push(await DiscordClientInterface.start(runtime));
  }

  if (clientTypes.includes("telegram")) {
    const telegramClient = await TelegramClientInterface.start(runtime);
    if (telegramClient) clients.push(telegramClient);
  }

  if (clientTypes.includes("twitter")) {
    const twitterClients = await TwitterClientInterface.start(runtime);
    clients.push(twitterClients);
  }

  // Luna LLM
  if (clientTypes.includes("auto") && runtime.character.name === "luna") {
    console.log("Luna 에이전트용 커스텀 클라이언트 시작...");
    const lunaClient = await LunaClientInterface.start(runtime);
    if (lunaClient) clients.push(lunaClient);
  }

  if (character.plugins?.length > 0) {
    for (const plugin of character.plugins) {
      if (plugin.clients) {
        for (const client of plugin.clients) {
          clients.push(await client.start(runtime));
        }
      }
    }
  }

  return clients;
}

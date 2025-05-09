import { AutoClientInterface } from "@elizaos/client-auto";
import { DiscordClientInterface } from "@elizaos/client-discord";
// Telegram 및 Twitter 클라이언트 임포트 제거됨
import { Character, IAgentRuntime } from "@elizaos/core";
import { LunaClientInterface } from "./luna-client";
import { LocalLLMInterface } from "./local-llm-server";

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

  // Telegram 및 Twitter 클라이언트 초기화 부분 제거됨

  //luna
  if (runtime.character.name === "luna") {
    console.log("Luna 에이전트 클라이언트 초기화 중");

    //환경변수로 어떤 LLM 사용할 지 결정
    const useLLMType = process.env.USE_LLM_TYPE || "openrouter";

    if (useLLMType === "local") {
      console.log("Local LLM 클라이언트 시작");
      const localLLMClient = await LocalLLMInterface.start(runtime);
      if (localLLMClient) clients.push(localLLMClient);
    } else {
      console.log("openrouter LLM 클라이언트 시작");
      const lunaClient = await LunaClientInterface.start(runtime);
      if (lunaClient) clients.push(lunaClient);
    }
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

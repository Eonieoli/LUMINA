// src/clients/local-llm.ts
// LocalLLM API와의 통신을 처리하는 HTTP 클라이언트
// API 요청/응답 처리 담당
import fetch from "node-fetch";
import { elizaLogger } from "@elizaos/core";

export interface LocalLLMConfig {
  apiUrl: string;
  model: string;
}

export class LocalLLMClient {
  private config: LocalLLMConfig;

  constructor(config: LocalLLMConfig) {
    this.config = config;
    elizaLogger.info(
      `LocalLLM client initialized with URL: ${config.apiUrl}, model: ${config.model}`
    );
  }

  //메세지 배열을 받아 LLM API에 요청하고 결과 반환
  async generateCompletion(
    messages: Array<{ role: string; content: string }>,
    options: any = {}
  ): Promise<string> {
    try {
      // 메시지 배열에서 필요한 정보 추출
      const systemMessage =
        messages.find((msg) => msg.role === "system")?.content || "";
      const userMessages = messages.filter((msg) => msg.role === "user");
      const lastUserMessage =
        userMessages.length > 0
          ? userMessages[userMessages.length - 1].content
          : "";

      // API 요청 준비
      const requestBody = {
        post: lastUserMessage,
      };

      elizaLogger.debug(
        `Sending request to local LLM: ${JSON.stringify(requestBody)}`
      );
      elizaLogger.info("Local LLM 요청 본문:", JSON.stringify(requestBody));

      const headers = {
        "Content-Type": "application/json",
      };

      // API 요청 전송
      const response = await fetch(this.config.apiUrl, {
        method: "POST",
        headers,
        body: JSON.stringify(requestBody),
      });

      // Response 객체 자체는 JSON으로 직렬화할 수 없으므로 필요한 정보만 로깅
      elizaLogger.info("응답 정보:", {
        status: response.status,
        statusText: response.statusText,
        headers: Object.fromEntries(response.headers.entries()),
      });

      //   elizaLogger.info("전체 응답:", JSON.stringify(response));

      // 응답 확인
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`LocalLLM API error: ${response.status} ${errorText}`);
      }

      // 응답 텍스트 가져오기
      const responseText = await response.text();
      elizaLogger.info("원본 응답 텍스트:", responseText);

      // 빈 응답 확인
      if (!responseText || responseText.trim() === "") {
        elizaLogger.warn("LLM 서버가 빈 응답을 반환했습니다.");
        return "죄송합니다, 현재 응답을 생성할 수 없습니다. 나중에 다시 시도해주세요.";
      }

      // JSON 파싱 시도
      let data;
      try {
        data = JSON.parse(responseText);
        elizaLogger.info("파싱된 응답 데이터:", JSON.stringify(data));
      } catch (error) {
        elizaLogger.error("JSON 파싱 오류:", error);
        return "응답 데이터를 처리하는 중 오류가 발생했습니다.";
      }

      // 빈 응답도 유효한 응답으로 처리 (변경된 부분)
      if (!data.response) {
        elizaLogger.warn("LLM 서버가 undefined 응답을 반환했습니다.");
        return "죄송합니다, 현재 응답을 생성할 수 없습니다. 나중에 다시 시도해주세요.";
      }

      // 빈 문자열이라도 기본 응답으로 대체
      return data.response || "안녕하세요! 어떻게 도와드릴까요?";

      //   return data.response;aaaaS
    } catch (error) {
      elizaLogger.error(`Error generating completion: ${error}`);
      throw error;
    }
  }
}

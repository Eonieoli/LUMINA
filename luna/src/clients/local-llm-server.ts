//Express 서버 구현
import { IAgentRuntime, elizaLogger } from "@elizaos/core";
import express from "express";
import cors from "cors";
import bodyParser from "body-parser";
import { LocalLLMClient } from "./local-llm.ts";

export class LocalLLMInterface {
  private runtime: IAgentRuntime;
  private app: express.Application;
  private server: any;
  private port: number;
  private localLLMClient: LocalLLMClient;

  constructor(runtime: IAgentRuntime) {
    this.runtime = runtime;
    this.app = express();
    this.port = parseInt(process.env.LUNA_PLATFORM_PORT || "4000");

    // LocalLLMClient 초기화
    this.localLLMClient = new LocalLLMClient({
      apiUrl:
        process.env.LOCAL_LLM_ENDPOINT || "http://43.200.21.116:8000/predict",
      model: process.env.LOCAL_LLM_MODEL || "Gemma",
    });

    // Express 미들웨어 설정
    this.app.use(cors());
    this.app.use(bodyParser.json());

    // API 라우트 설정
    this.setupRoutes();
  }

  private setupRoutes() {
    // 1. 게시글에 대한 리워드 평가 API
    this.app.post("/api/v1/evaluate-post", async (req, res) => {
      try {
        const { user_id, post_content, post_id, nickname } = req.body;

        if (!user_id || !post_content) {
          return res
            .status(400)
            .json({ error: "사용자 ID와 게시글 내용이 필요합니다." });
        }

        // Local LLM에게 게시글 평가 요청
        const response = await this.evaluateGoodDeed(
          user_id,
          post_content,
          nickname
        );

        // 응답 반환
        res.json({
          post_id,
          reward: response,
          evaluatedBy: this.runtime.character.name,
        });
      } catch (error) {
        elizaLogger.error("게시글 평가 오류:", error);
        res.status(500).json({ error: "게시글 평가 중 오류가 발생했습니다." });
      }
    });

    // 2. @luna 멘션에 대한 응답 생성 API
    this.app.post("/api/v1/reply-mention", async (req, res) => {
      try {
        const { user_id, comment_content, comment_id, nickname } = req.body;

        if (!user_id || !comment_content) {
          return res
            .status(400)
            .json({ error: "사용자 ID와 댓글 내용이 필요합니다." });
        }

        // Local LLM에게 멘션 응답 요청
        const response = await this.respondToMention(
          user_id,
          comment_content,
          nickname
        );

        // 응답 반환
        res.json({
          comment_id,
          reply: response,
          respondedBy: this.runtime.character.name,
        });
      } catch (error) {
        elizaLogger.error("멘션 응답 오류:", error);
        res.status(500).json({ error: "멘션 응답 중 오류가 발생했습니다." });
      }
    });

    // 건강 체크 엔드포인트
    this.app.get("/api/v1/health", (req, res) => {
      res.json({ status: "ok", agent: this.runtime.character.name });
    });

    this.app.get("/", (req, res) => {
      res.send("Luna API 서버가 Local LLM과 연결되어 작동 중입니다.");
    });
  }

  // 선한 행동에 대한 평가 및 리워드 생성 - Local LLM 호출
  private async evaluateGoodDeed(
    userId: string,
    postContent: string,
    nickname: string
  ): Promise<string> {
    // 게시글 평가를 위한 프롬프트 구성

    const promptText = `다음은 사용자(닉네임: ${nickname})가 작성한 게시글입니다:\\n"${postContent}"\\n\\n당신은 선한 행동 SNS 플랫폼의 관리자 Luna입니다. 이 게시글에 대한 반응과 적절한 리워드를 제공해야 합니다.\\n\\n게시글 분석:\\n1. 먼저 게시글이 다음 중 어떤 유형인지 판단하세요:\\n   A) 실제 봉사활동이나 선한 행동을 직접 수행했다는 내용\\n   B) 선한 생각, 정보 공유, 또는 간접적인 선행 관련 내용\\n\\n2. 유형에 따른 리워드 책정:\\n   - A유형: 적절한 리워드 (실제 행동에 대한 가치 인정)\\n   - B유형: 작은 리워드 (의미 있는 공유에 대한 가치 인정)\\n\\n3. 응답 작성:\\n   - 게시글 내용에 직접 반응하는 개인화된 피드백\\n   - 해당 행동/생각이 사회에 미치는 긍정적 영향 강조\\n   - 작성자에게 맞춤형 격려와 응원의 메시지\\n   - 유형에 따른 적절한 리워드 언급\\n\\n응답에서 작성자를 언급할 때는 반드시 닉네임으로 부르세요.\\n게시글 내용 자체를 사람처럼 부르거나 언급하지 마세요.\\n(예: \\"휴지를 주웠어요\\"라면 \\"휴지님\\"이라고 부르지 마세요.)\\n답변은 꼭 한 가지만 작성하세요. 정말로 대화를 하는 챗봇처럼 답장하세요.\\n이제 Luna는 위 내용을 분석한 후, 바로 응답 멘트 하나만 자연스럽게 말해주세요. 포인트나 유형 등은 직접 언급하지 말고, 정말로 사람처럼 응답해 주세요.`;

    elizaLogger.info("게시글 내용:", postContent);
    elizaLogger.info("사용자 ID:", userId);

    try {
      // 기존 LocalLLMClient 활용
      const messages = [
        { role: "system", content: "당신은 Luna입니다." },
        { role: "user", content: promptText },
      ];

      const response = await this.localLLMClient.generateCompletion(messages);
      elizaLogger.info("Local LLM 응답:", response);
      return response;
    } catch (error) {
      elizaLogger.error("Local LLM 호출 오류:", error);
      throw error;
    }
  }

  // @luna 멘션에 대한 응답 생성 - Local LLM 호출
  private async respondToMention(
    userId: string,
    question: string,
    nickname: string
  ): Promise<string> {
    // 멘션 응답을 위한 프롬프트 구성
    const promptText = `다음은 사용자(닉네임: "${nickname}")가 작성한 게시글입니다:\\n"${question}"\\n\\n당신은 선한 행동 SNS 플랫폼의 관리자 Luna입니다. 이 게시글에 대한 반응과 적절한 리워드를 제공해야 합니다.\\n\\n게시글 분석:\\n1. 먼저 게시글이 다음 중 어떤 유형인지 판단하세요:\\n   A) 실제 봉사활동이나 선한 행동을 직접 수행했다는 내용\\n   B) 선한 생각, 정보 공유, 또는 간접적인 선행 관련 내용\\n\\n2. 유형에 따른 리워드 책정:\\n   - A유형: 적절한 리워드 (실제 행동에 대한 가치 인정)\\n   - B유형: 작은 리워드 (의미 있는 공유에 대한 가치 인정)\\n\\n3. 응답 작성:\\n   - 게시글 내용에 직접 반응하는 개인화된 피드백\\n   - 해당 행동/생각이 사회에 미치는 긍정적 영향 강조\\n   - 작성자에게 맞춤형 격려와 응원의 메시지\\n   - 유형에 따른 적절한 리워드 언급\\n\\n응답에서 작성자를 언급할 때는 반드시 닉네임으로 부르세요.\\n게시글 내용 자체를 사람처럼 부르거나 언급하지 마세요.\\n(예: \\"휴지를 주웠어요\\"라면 \\"휴지님\\"이라고 부르지 마세요.)\\n답변은 꼭 한 가지만 작성하세요. 정말로 대화를 하는 챗봇처럼 답장하세요.\\n이제 Luna는 위 내용을 분석한 후, 바로 응답 멘트 하나만 자연스럽게 말해주세요. 포인트나 유형 등은 직접 언급하지 말고, 정말로 사람처럼 응답해 주세요.`;

    elizaLogger.info("멘션 내용:", question);
    elizaLogger.info("사용자 ID:", userId);

    try {
      // 기존 LocalLLMClient 활용
      const messages = [
        { role: "system", content: "당신은 Luna입니다." },
        { role: "user", content: promptText },
      ];

      const response = await this.localLLMClient.generateCompletion(messages);
      elizaLogger.info("Local LLM 응답:", response);
      return response;
    } catch (error) {
      elizaLogger.error("Local LLM 호출 오류:", error);
      throw error;
    }
  }

  // 서버 시작
  private startServer(): Promise<void> {
    return new Promise((resolve) => {
      this.server = this.app.listen(this.port, () => {
        elizaLogger.success(
          elizaLogger.successesTitle,
          `Luna 플랫폼 API 서버가 Local LLM과 연결되어 포트 ${this.port}에서 시작되었습니다.`
        );
        resolve();
      });
    });
  }

  // 서버 중지
  async stop() {
    if (this.server) {
      await new Promise<void>((resolve) => {
        this.server.close(() => {
          elizaLogger.warn("Luna 플랫폼 API 서버가 종료되었습니다.");
          resolve();
        });
      });
    }
  }

  // 클라이언트 시작 (정적 메서드)
  static async start(runtime: IAgentRuntime) {
    try {
      const client = new LocalLLMInterface(runtime);
      await client.startServer();
      return client;
    } catch (error) {
      elizaLogger.error("Luna 플랫폼 API 서버 시작 오류:", error);
      return null;
    }
  }
}

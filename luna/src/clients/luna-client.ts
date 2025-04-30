// src/clients/my-platform-client.ts
// IAgentRuntime: ElizaOS의 에이전트 런타임 인터페이스, 에이전트 작동에 필요한 핵심 기능 담기
// MessageContent: 메시지 내용을 정의하는 인터페이스, 텍스트, 이미지 등 다양한 컨텐츠 타입 포함 가능
// ChatMessageRole: 메시지 역할(사용자, 시스템 등)을 정의하는 ENUM type
// elizaLogger: ELizaOS의 로깅 유틸리티(로그 출력)
// express: Nodejs의 웹 서버 프레임워크, API 엔드포인트 쉽게 생성
// bodyparser: HTTP 요청 본문을 파싱. JSON 요청을 객체로 변환
import { IAgentRuntime, elizaLogger } from "@elizaos/core";
import express from "express";
import cors from "cors";
import bodyParser from "body-parser";

export class LunaClientInterface {
  private runtime: IAgentRuntime;
  private app: express.Application;
  private server: any;
  private port: number;

  constructor(runtime: IAgentRuntime) {
    this.runtime = runtime;
    this.app = express();
    this.port = parseInt(process.env.LUNA_PLATFORM_PORT || "4000");

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
        const { user_id, post_content, post_id } = req.body; //구조분해 할당 -> 요청 본문에서 필요한 값 추출

        if (!user_id || !post_content) {
          return res
            .status(400)
            .json({ error: "사용자 ID와 게시글 내용이 필요합니다." });
        }

        // LLM에게 게시글 평가 요청
        const response = await this.evaluateGoodDeed(user_id, post_content);

        // 응답 반환
        res.json({
          post_id,
          reward: response[0].text,
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
        const { user_id, comment_content, comment_id } = req.body;

        if (!user_id || !comment_content) {
          return res
            .status(400)
            .json({ error: "사용자 ID와 댓글 내용이 필요합니다." });
        }

        // LLM에게 멘션 응답 요청
        const response = await this.respondToMention(user_id, comment_content);

        // 응답 반환
        res.json({
          comment_id,
          reply: response[0].text,
          respondedBy: this.runtime.character.name,
        });
      } catch (error) {
        elizaLogger.error("멘션 응답 오류:", error);
        res.status(500).json({ error: "멘션 응답 중 오류가 발생했습니다." });
      }
    });

    // 건강 체크 엔드포인트(상태 체크 엔드포인트)
    this.app.get("/api/v1/health", (req, res) => {
      res.json({ status: "ok", agent: this.runtime.character.name });
    });

    this.app.get("/", (req, res) => {
      res.send("Luna API 서버가 작동 중입니다.");
    });
  }

  // 선한 행동에 대한 평가 및 리워드 생성
  private async evaluateGoodDeed(
    userId: string,
    postContent: string
  ): Promise<any> {
    // 게시글 평가를 위한 프롬프트 구성
    //

    const promptText = `다음은 사용자가 작성한 선한 행동에 관한 게시글입니다. 
이 행동이 얼마나 선한지 평가하고, 적절한 리워드 메시지를 생성해주세요.
게시글 내용: ${postContent}

리워드 메시지는 다음 형식으로 작성해주세요:
1. 선한 행동에 대한 긍정적인 평가
2. 이 행동이 사회에 미치는 영향
3. 격려와 응원의 메시지`;

    // Eliza 에이전트에 메시지 전달
    // 메시지 처리를 위해 직접 API 요청 사용
    const serverPort = parseInt(process.env.LUNA_PLATFORM_PORT || "4000");
    const response = await fetch(
      `http://localhost:${serverPort}/${this.runtime.character.name}/message`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          text: promptText,
          userId: userId,
          userName: userId,
        }),
      }
    );

    return await response.json();
  }

  // @luna 멘션에 대한 응답 생성
  private async respondToMention(
    userId: string,
    question: string
  ): Promise<any> {
    // 멘션 응답을 위한 프롬프트 구성
    const promptText = `사용자가 @luna 로 언급하며 다음과 같은 질문을 했습니다: 
${question}

당신은 선한 행동 SNS 플랫폼의 관리자 Luna입니다. 
질문이 기부 관련 조언을 구하는 것이라면, 적절한 기부처와 방법을 추천해주세요.
질문이 선한 행동에 관한 것이라면, 도움이 되는 정보와 격려를 제공해주세요.
질문이 다른 주제라면, SNS의 방향에 맞게 친절하게 답변해주세요.`;

    // Eliza 에이전트에 메시지 전달
    const serverPort = parseInt(process.env.LUNA_PLATFORM_PORT || "4000");
    const response = await fetch(
      `http://localhost:${serverPort}/${this.runtime.character.name}/message`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          text: promptText,
          userId: userId,
          userName: userId,
        }),
      }
    );

    return await response.json();
  }

  // 서버 시작
  private startServer(): Promise<void> {
    return new Promise((resolve) => {
      this.server = this.app.listen(this.port, () => {
        elizaLogger.success(
          elizaLogger.successesTitle,
          `Luna 플랫폼 API 서버가 포트 ${this.port}에서 시작되었습니다.`
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
      const client = new LunaClientInterface(runtime);
      await client.startServer();
      return client;
    } catch (error) {
      elizaLogger.error("Luna 플랫폼 API 서버 시작 오류:", error);
      return null;
    }
  }
}

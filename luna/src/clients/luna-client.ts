// luna client, modelProvider: openrouter
import { IAgentRuntime, elizaLogger } from "@elizaos/core";
import express from "express";
import cors from "cors";
import bodyParser from "body-parser";
import { PostGenerator } from "../services/post-generator";
import { PostScheduler } from "../services/scheduler";
import fetch from "node-fetch";

export class LunaClientInterface {
  private runtime: IAgentRuntime;
  private app: express.Application;
  private server: any;
  private port: number;
  private postGenerator: PostGenerator;
  private postScheduler: PostScheduler;

  constructor(runtime: IAgentRuntime) {
    this.runtime = runtime;
    this.app = express();
    this.port = parseInt(process.env.LUNA_PLATFORM_PORT || "4000");

    // 포스트 생성기 초기화
    this.postGenerator = new PostGenerator(runtime);

    // 포스트 스케줄러 초기화 (6시간마다 실행)
    this.postScheduler = new PostScheduler(
      this.createScheduledPost.bind(this) // 6시간 간격
    );

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
        const { user_id, post_content, post_id, nickname } = req.body; //구조분해 할당 -> 요청 본문에서 필요한 값 추출

        if (!user_id || !post_content) {
          return res
            .status(400)
            .json({ error: "사용자 ID와 게시글 내용이 필요합니다." });
        }

        // LLM에게 게시글 평가 요청
        const response = await this.evaluateGoodDeed(
          user_id,
          post_content,
          nickname
        );

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
        const { user_id, comment_content, comment_id, nickname } = req.body;

        if (!user_id || !comment_content) {
          return res
            .status(400)
            .json({ error: "사용자 ID와 댓글 내용이 필요합니다." });
        }

        // LLM에게 멘션 응답 요청
        const response = await this.respondToMention(
          user_id,
          comment_content,
          nickname
        );

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

    // 3. 수동으로 게시글 생성
    this.app.post("/api/v1/generate-post", async (req, res) => {
      try {
        const post = await this.createScheduledPost();
        res.json({ success: true, post });
      } catch (error) {
        elizaLogger.error("게시글 생성 오류:", error);
        res.status(500).json({ error: "게시글 생성 중 오류가 발생했습니다." });
      }
    });

    // 스케줄러 컨트롤 API
    this.app.post("/api/v1/scheduler/start", (req, res) => {
      this.postScheduler.start();
      res.json({ success: true, message: "스케줄러가 시작되었습니다." });
    });

    this.app.post("/api/v1/scheduler/stop", (req, res) => {
      this.postScheduler.stop();
      res.json({ success: true, message: "스케줄러가 중지되었습니다." });
    });
  }

  // 선한 행동에 대한 평가 및 리워드 생성
  private async evaluateGoodDeed(
    userId: string,
    postContent: string,
    nickname: string
  ): Promise<any> {
    // 게시글 평가를 위한 프롬프트 구성
    //

    const promptText = `다음은 사용자(닉네임: "${nickname}")가 작성한 게시글입니다:
    "${postContent}"
    
    당신은 선한 행동 SNS 플랫폼의 관리자 Luna입니다. 이 게시글에 대한 반응과 적절한 리워드를 제공해야 합니다.
    
    게시글 분석:
    1. 먼저 게시글이 다음 중 어떤 유형인지 판단하세요:
       A) 실제 봉사활동이나 선한 행동을 직접 수행했다는 내용
       B) 선한 생각, 정보 공유, 또는 간접적인 선행 관련 내용
    
    2. 유형에 따른 리워드 책정:
       - A유형: 적절한 리워드 (실제 행동에 대한 가치 인정)
       - B유형: 작은 리워드 (의미 있는 공유에 대한 가치 인정)
    
    3. 응답 작성:
       - 게시글 내용에 직접 반응하는 개인화된 피드백
       - 해당 행동/생각이 사회에 미치는 긍정적 영향 강조
       - 작성자에게 맞춤형 격려와 응원의 메시지
       - 유형에 따른 적절한 리워드 언급
    
    응답에서 작성자를 언급할 때는 반드시 "${nickname}"라고 부르세요.
    게시글 내용 자체를 사람처럼 부르거나 언급하지 마세요.
    (예를 들어, 게시글 내용이 "휴지를 주웠어요"라면 "휴지님"이라고 부르지 마세요.)
    
    최종 응답 형식:
    1. 게시글 내용에 대한 직접적인 반응
    2. 사회적 영향에 대한 언급
    3. 격려 메시지와 함께 리워드 안내 (A유형은 B유형보다 더 높은 리워드, B유형은 작은 리워드)`;

    // ElizaOS 메인 서버는 3001 포트에서 실행 중
    const elizaServerPort = parseInt(process.env.ELIZA_SERVER_PORT || "3000");

    elizaLogger.info("게시글 내용:", postContent);
    elizaLogger.info("사용자 ID:", userId);
    elizaLogger.info(
      "요청 URL:",
      `http://localhost:${elizaServerPort}/${this.runtime.character.name}/message`
    );
    // elizaLogger.info("요청 PROMPT:", promptText);

    const response = await fetch(
      `http://localhost:${elizaServerPort}/${this.runtime.character.name}/message`,
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
    question: string,
    nickname: string
  ): Promise<any> {
    // 멘션 응답을 위한 프롬프트 구성
    const promptText = `사용자(닉네임: "${nickname}")가 @luna로 언급하며 다음과 같은 내용을 댓글로 남겼습니다: 
    "${question}"
    
    당신은 선한 행동 SNS 플랫폼의 관리자 Luna입니다. 
    위 댓글에 대한 응답을 작성해주세요.
    
    응답 작성 규칙:
    1. 질문이 기부 관련 조언을 구하는 것이라면, 적절한 기부처와 방법을 추천해주세요.
    2. 질문이 선한 행동에 관한 것이라면, 도움이 되는 정보와 격려를 제공해주세요.
    3. 질문이 다른 주제라면, SNS의 방향에 맞게 친절하게 답변해주세요.
    4. 댓글 작성자를 언급할 때는 반드시 "${nickname}"라고 부르세요.
    5. 댓글 내용을 사람처럼 부르지 마세요.`;

    // Eliza 에이전트에 메시지 전달
    const serverPort = parseInt(process.env.ELIZA_SERVER_PORT || "3000");
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

  // 스케줄된 게시글 생성 및 API 전송
  private async createScheduledPost(): Promise<string> {
    try {
      elizaLogger.info("스케줄된 게시글 생성 시작...");

      // 게시글 생성
      const postContent = await this.postGenerator.generatePost();

      // 백엔드 API에 게시글 전송
      await this.sendPostToBackend(postContent);

      return postContent;
    } catch (error) {
      elizaLogger.error("스케줄된 게시글 생성 중 오류 발생:", error);
      throw error;
    }
  }

  // 백엔드 API에 게시글 전송
  private async sendPostToBackend(postContent: string): Promise<void> {
    try {
      // 백엔드 API URL은 환경 변수로 설정 가능
      const backendUrl = process.env.BACKEND_API_URL || "http://localhost:8080";

      elizaLogger.info(
        `백엔드 API에 게시글 전송 중... URL: ${backendUrl}/api/v1/lumina/post`
      );

      const response = await fetch(`${backendUrl}/api/v1/lumina/post`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          postContent,
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`백엔드 API 오류: ${response.status} ${errorText}`);
      }

      elizaLogger.success(
        elizaLogger.successesTitle,
        "게시글이 성공적으로 전송되었습니다."
      );
    } catch (error) {
      elizaLogger.error("백엔드 API 전송 중 오류 발생:", error);
      throw error;
    }
  }

  // 서버 시작
  private startServer(): Promise<void> {
    return new Promise((resolve) => {
      this.server = this.app.listen(this.port, () => {
        elizaLogger.success(
          elizaLogger.successesTitle,
          `Luna 플랫폼 API 서버가 포트 ${this.port}에서 시작되었습니다.`
        );

        // 서버 시작 시 스케줄러도 시작
        this.postScheduler.start();

        resolve();
      });
    });
  }

  // 서버 중지
  async stop() {
    // 스케줄러 중지
    this.postScheduler.stop();

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

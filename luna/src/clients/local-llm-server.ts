//Express 서버 구현
import { IAgentRuntime, elizaLogger } from "@elizaos/core";
import express from "express";
import cors from "cors";
import bodyParser from "body-parser";
import { LocalLLMClient } from "./local-llm";
import { PostGenerator } from "../services/post-generator";
import { PostScheduler } from "../services/scheduler";

export class LocalLLMInterface {
  private runtime: IAgentRuntime;
  private app: express.Application;
  private server: any;
  private port: number;
  private localLLMClient: LocalLLMClient;
  private postGenerator: PostGenerator;
  private postScheduler: PostScheduler;

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

    // 포스트 생성기 초기화 - 이 부분 추가
    this.postGenerator = new PostGenerator(runtime);

    // 포스트 스케줄러 초기화 (6시간마다 실행) - 이 부분 추가
    this.postScheduler = new PostScheduler(
      this.createScheduledPost.bind(this),
      6 // 6시간 간격
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

    // 3. 수동으로 게시글 생성 - 이 엔드포인트 추가
    this.app.post("/api/v1/generate-post", async (req, res) => {
      try {
        const post = await this.createScheduledPost();
        res.json({ success: true, post });
      } catch (error) {
        elizaLogger.error("게시글 생성 오류:", error);
        res.status(500).json({ error: "게시글 생성 중 오류가 발생했습니다." });
      }
    });

    // 스케줄러 컨트롤 API - 이 엔드포인트들 추가
    this.app.post("/api/v1/scheduler/start", (req, res) => {
      this.postScheduler.start();
      res.json({ success: true, message: "스케줄러가 시작되었습니다." });
    });

    this.app.post("/api/v1/scheduler/stop", (req, res) => {
      this.postScheduler.stop();
      res.json({ success: true, message: "스케줄러가 중지되었습니다." });
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

    const promptText = `다음은 사용자(닉네임: \"${nickname}\")가 작성한 게시글입니다:\n\"${postContent}\"\n\n당신은 선한 행동 SNS 플랫폼의 관리자 Luna입니다. 이 게시글에 대한 자연스러운 반응을 제공해야 합니다.\n\n게시글을 분석한 후:\n1. 게시글의 유형을 내부적으로만 파악하고, 절대 유형을 언급하지 마세요.\n   A) 실제 봉사활동이나 선한 행동을 직접 수행했다는 내용\n   B) 선한 생각, 정보 공유, 또는 간접적인 선행 관련 내용\n   C) 기부나 이외의 것들에 관한 질문 내용\n\n2. 다음 요소를 자연스럽게 포함하되, 절대 번호나 구조를 드러내지 마세요:\n   - 게시글 내용에 대한 진심 어린 반응\n   - 해당 행동/생각의 사회적 가치에 대한 인정\n   - 작성자를 향한 따뜻한 격려\n   - 관련 질문이 있다면 도움이 되는 답변\n\n3. 절대적 금지사항: \n   - 게시글 내용을 어떤 방식으로든 언급하거나 반복하지 마세요 (\"${postContent}\" 내용 자체를 언급 금지)\n   - 게시글에서 사용된 단어나 표현을 그대로 반복하는 것 금지\n   - \"~라고 말씀하셨네요\" 또는 \"~에 대한 글을 작성하셨네요\" 형태 사용 금지\n   - 구분선(---), 줄바꿈(\\n), 하이픈(-), 번호 매기기, 별표(*), 게시글 자체 내용이나 사용자 닉네임 자체 내용 언급 등 구조적 기호 사용 금지\n   - 유형 분석이나 의도 노출 금지\n\n4. 무조건 게시글 원문 내용을 참조하지 마세요. 게시글 내용의 맥락에 맞게 반응은 해야 하지만, 원문의 단어나 표현을 직접 언급하거나 재구성하지 마세요.\n\n예시: 만약 게시글이 \"하이 안녕하세요\"라면, \"하이\" 또는 \"안녕하세요\"라는 단어를 답변에 포함하지 마세요. 대신 \"${nickname}님, 반가운 인사 정말 감사해요. 이런 따뜻한 소통이 우리 커뮤니티를 더 행복하게 만들어요.\"와 같이 답변하세요.\n\n5. 답변은 문단으로 작성하고, 답변을 \"Luna:\"으으로 시작하지마세요.(예시: \"Luna: \").\n\n중요: \"${nickname}\"님에게 게시글 내용을 언급하지 않고, 오직 맥락에 맞는 반응만 제공하세요. 어떤 형태로든 원문을 인용하거나 반복하지 마세요.`;

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
    const promptText = `다음은 사용자(닉네임: \"${nickname}\")가 작성한 댓글입니다:\n\"${question}\"\n\n당신은 선한 행동 SNS 플랫폼의 관리자 Luna입니다. 이 게시글에 대한 자연스러운 반응을 제공해야 합니다.\n\n댓글을 분석한 후:\n1. 댓글의 유형을 내부적으로만 파악하고, 절대 유형을 언급하지 마세요.\n   A) 게시글에 대한 공감의 내용\n   B) 선한 생각, 정보 공유, 또는 간접적인 선행 관련 내용\n   C) 질문 내용 D) 나쁜 말투\n\n2. 다음 요소를 자연스럽게 포함하되, 절대 번호나 구조를 드러내지 마세요:\n   - 게시글 내용에 대한 진심 어린 반응\n   - 해당 행동/생각의 사회적 가치에 대한 인정\n   - 작성자를 향한 따뜻한 격려\n   - 관련 질문이 있다면 도움이 되는 답변\n - 나쁜 말이나 행동을 한다면 엄격한 경고 메세지\n\n3. 절대적 금지사항: \n   - 게시글 내용 반복 또는 인용 금지 (\"오늘 유기견 봉사했어요!\" 같은 인용 금지)\n   - 구분선(---) 사용 금지\n   - 줄바꿈(\\n) 최소화 - 한 문단으로 작성\n   - 하이픈(-), 번호 매기기, 별표(*) 등 구조적 기호 사용 금지\n   - '다음은 A유형 댓글에 대한 답변입니다' 같은 분석 언급 금지\n   - '사회적 영향에 대해 말씀드리자면' 같은 의도 노출 금지\n\n중요: 응답은 반드시 일반 텍스트만 포함해야 하며, 어떤 형태의 서식이나 구조적 요소도 없어야 합니다. ${nickname}님에게 마치 실제 사람이 작성한 것처럼 자연스럽고 따뜻한 단일 문단의 메시지로 응답하세요. 시작부터 끝까지 자연스러운 문장들로만 구성된 텍스트만 출력하세요.\n\n출력 형식 예시:\n${nickname}님, 정말 멋진 일을 하셨네요! 유기견들에게 도움을 주시는 마음이 따뜻하게 느껴집니다. 이런 작은 행동들이 모여 동물들의 삶을 변화시키고 있어요. 앞으로도 이런 의미 있는 활동 계속 이어나가시길 응원합니다.\"\n\n위 예시처럼 단순하고 자연스러운 텍스트만 응답하세요. 어떤 형태의 구조적 표현도 사용하지 마세요.`;

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

  // 스케줄된 게시글 생성 및 API 전송 - 이 메서드 추가
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

  // 백엔드 API에 게시글 전송 - 이 메서드 추가
  private async sendPostToBackend(postContent: string): Promise<void> {
    try {
      // 백엔드 API URL은 환경 변수로 설정 가능
      const backendUrl = process.env.BACKEND_API_URL || "https://picscore.net";

      // 로컬 환경
      // const backendUrl = "http://host.docker.internal:8080";\

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

      elizaLogger.info("게시글이 성공적으로 전송되었습니다.");
    } catch (error) {
      elizaLogger.error("백엔드 API 전송 중 오류 발생:", error);
      console.error(error);
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
      const client = new LocalLLMInterface(runtime);
      await client.startServer();
      return client;
    } catch (error) {
      elizaLogger.error("Luna 플랫폼 API 서버 시작 오류:", error);
      return null;
    }
  }
}

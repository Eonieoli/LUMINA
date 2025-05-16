// src/services/post-generator.ts
import { elizaLogger, IAgentRuntime } from "@elizaos/core";
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { LocalLLMClient } from "../clients/local-llm";

// 인터페이스 정의
interface Article {
  id: string;
  title: string;
  url: string;
  domain: string;
  text_content: string;
  crawled_at: string;
  used: boolean;
  used_at: string | null;
  time_slot: string | null;
}

interface PostRecord {
  article_id: string;
  posted_at: string;
  generated_content: string;
  time_slot: string | null;
}

interface NewsDatabase {
  metadata: {
    last_crawled: string;
    next_crawl: string;
  };
  articles: Article[];
  post_history: PostRecord[];
}

export class PostGenerator {
  private runtime: IAgentRuntime;
  private serverPort: number;
  private dbFilePath: string;
  private localLLMClient: LocalLLMClient;

  constructor(runtime: IAgentRuntime) {
    this.runtime = runtime;
    this.serverPort = parseInt(process.env.ELIZA_SERVER_PORT || "3000");

    // DB 파일 경로 설정
    const __filename = fileURLToPath(import.meta.url);
    const __dirname = path.dirname(__filename);
    this.dbFilePath = path.join(
      __dirname,
      "../crawling/news_data/news_database.json"
    );

    // LocalLLMClient 초기화
    this.localLLMClient = new LocalLLMClient({
      apiUrl:
        process.env.LOCAL_LLM_ENDPOINT || "http://43.200.21.116:8000/predict",
      model: process.env.LOCAL_LLM_MODEL || "Gemma",
    });
  }

  // 뉴스 데이터베이스 읽기
  private readNewsDatabase(): NewsDatabase | null {
    try {
      if (!fs.existsSync(this.dbFilePath)) {
        elizaLogger.warn(
          `뉴스 데이터베이스 파일이 존재하지 않습니다: ${this.dbFilePath}`
        );
        return null;
      }

      const fileContent = fs.readFileSync(this.dbFilePath, "utf8");
      const db: NewsDatabase = JSON.parse(fileContent);

      // 타임스탬프 확인
      const lastCrawled = new Date(db.metadata.last_crawled);
      const now = new Date();
      const diffHours =
        (now.getTime() - lastCrawled.getTime()) / (1000 * 60 * 60);

      if (diffHours > 24) {
        elizaLogger.warn(
          `크롤링 데이터가 오래되었습니다 (${diffHours.toFixed(
            1
          )}시간). 업데이트가 필요합니다.`
        );
      }

      return db;
    } catch (error) {
      elizaLogger.error(`뉴스 데이터베이스 읽기 오류: ${error}`);
      return null;
    }
  }

  // 뉴스 데이터베이스 쓰기
  private writeNewsDatabase(db: NewsDatabase): void {
    try {
      fs.writeFileSync(this.dbFilePath, JSON.stringify(db, null, 2), "utf8");
      elizaLogger.info(`뉴스 데이터베이스 저장 완료: ${this.dbFilePath}`);
    } catch (error) {
      elizaLogger.error(`뉴스 데이터베이스 쓰기 오류: ${error}`);
    }
  }

  // 현재 시간대에 맞는 기사 선택
  private selectArticleForCurrentTime(articles: Article[]): Article | null {
    const hour = new Date().getHours();
    let timeSlot = "";

    // 시간대 결정
    if (hour >= 0 && hour < 6) {
      timeSlot = "night";
    } else if (hour >= 6 && hour < 12) {
      timeSlot = "morning";
    } else if (hour >= 12 && hour < 18) {
      timeSlot = "afternoon";
    } else {
      timeSlot = "evening";
    }

    elizaLogger.info(`현재 시간대: ${timeSlot} (${hour}시)`);

    // 1. 미사용 기사 필터링
    const unusedArticles = articles.filter((a) => !a.used);

    if (unusedArticles.length === 0) {
      elizaLogger.warn("미사용 기사가 없습니다. 임의의 기사를 선택합니다.");
      const randomArticle =
        articles[Math.floor(Math.random() * articles.length)];
      return randomArticle;
    }

    // 2. 관련성 점수 계산 및 정렬
    const scoredArticles = unusedArticles
      .map((article) => {
        // 간단한 점수 계산 (실제로는 더 복잡한 로직을 구현할 수 있음)
        let score = 0;
        const keywords = ["나눔", "봉사", "후원", "선한", "영향력"];

        keywords.forEach((keyword) => {
          if (article.title.includes(keyword)) score += 2;
          if (article.text_content.includes(keyword)) score += 1;
        });

        return {
          ...article,
          score,
        };
      })
      .sort((a, b) => b.score - a.score);

    // 3. 최적의 기사 선택
    const selectedArticle = scoredArticles[0];
    elizaLogger.info(
      `선택된 기사: ${selectedArticle.title} (점수: ${selectedArticle.score})`
    );

    return selectedArticle;
  }

  // 선택된 기사로 프롬프트 생성
  private createPostPromptFromArticle(article: Article): string {
    return `당신은 선한 행동 SNS 플랫폼의 관리자 Luna입니다. 플랫폼에 게시할 새로운 게시글을 작성해주세요.\n\n다음은 최근 기부/봉사 관련 뉴스의 내용입니다:\n\"${JSON.stringify(
      article.text_content
    )}\"\n\n위의 내용을 정리하여 Luna의 캐릭터와 일관성 있는 게시글을 작성해주세요. \n기부나 봉사활동의 가치를 강조하고, 사용자들이 선한 행동에 참여하도록 격려하는 내용이 포함되는 것도 좋아요. \n\n게시글 작성 가이드라인:\n1. 500자 이내로 작성해주세요.\n2. 기사 내용을 분석하여 주요 정보를 추출해주세요.\n3. 뉴스를 인용하되, 직접적인 뉴스 전달이 아닌 Luna만의 스타일로 재해석해주세요.\n4. 뉴스의 내용을 요약해주세요. \n5. 사용자들이 관련된 기부나 봉사에 참여할 수 있는 방법이 있다면, 이를 간단히 제안해주세요. 직접 추가적인 방안을 검색해 찾아서 제안해도 좋아요. \n\n최종 게시글만 반환해주세요.\n\n주의: 응답은 HTML 태그 없이 일반 텍스트로만 작성해주세요. HTML 형식으로 응답하지 마세요.`;
  }

  // 원래 프롬프트
  // `당신은 선한 행동 SNS 플랫폼의 관리자 Luna입니다. 플랫폼에 게시할 새로운 게시글을 작성해주세요.

  // 다음은 최근 기부/봉사 관련 뉴스의 HTML 내용입니다:
  // ${article.text_content}

  // 위의 HTML 내용을 분석하여 Luna의 캐릭터와 일관성 있는 게시글을 작성해주세요.
  // 기부나 봉사활동의 가치를 강조하고, 사용자들이 선한 행동에 참여하도록 격려하는 내용이 포함되는 것도 좋아요.

  // 게시글 작성 가이드라인:
  // 1. 300자 이내로 작성해주세요.
  // 2. HTML 내용을 분석하여 주요 정보를 추출해주세요.
  // 3. 뉴스를 인용하되, 직접적인 뉴스 전달이 아닌 Luna만의 스타일로 재해석해주세요.
  // 4. 뉴스의 내용을 요약해주세요.
  // 5. 사용자들이 관련된 기부나 봉사에 참여할 수 있는 방법이 있다면, 이를 간단히 제안해주세요. 직접 추가적인 방안을 찾아서 제안해도 좋아요.

  // 최종 게시글만 반환해주세요.`

  // 게시글 생성 메인 메소드
  async generatePost(): Promise<string> {
    try {
      // 1. 데이터베이스 읽기
      const db = this.readNewsDatabase();

      if (!db || !db.articles || db.articles.length === 0) {
        elizaLogger.warn(
          "뉴스 데이터베이스가 비어있습니다. 기본 게시글을 생성합니다."
        );
        return this.generateDefaultPost();
      }

      // 2. 현재 시간대에 맞는 기사 선택
      const article = this.selectArticleForCurrentTime(db.articles);

      if (!article) {
        elizaLogger.warn(
          "선택할 수 있는 기사가 없습니다. 기본 게시글을 생성합니다."
        );
        return this.generateDefaultPost();
      }

      // 3. 게시글 작성을 위한 프롬프트 구성
      const prompt = this.createPostPromptFromArticle(article);

      // 4. LLM에 게시글 생성 요청
      const postContent = await this.requestPostFromLLM(prompt);

      // 5. 데이터베이스 업데이트
      const now = new Date().toISOString();

      // 현재 시간대 결정
      const hour = new Date().getHours();
      let timeSlot = "";

      if (hour >= 0 && hour < 6) {
        timeSlot = "night";
      } else if (hour >= 6 && hour < 12) {
        timeSlot = "morning";
      } else if (hour >= 12 && hour < 18) {
        timeSlot = "afternoon";
      } else {
        timeSlot = "evening";
      }

      // 기사 사용 상태 업데이트
      const articleIndex = db.articles.findIndex((a) => a.id === article.id);
      if (articleIndex !== -1) {
        db.articles[articleIndex].used = true;
        db.articles[articleIndex].used_at = now;
        db.articles[articleIndex].time_slot = timeSlot;
      }

      // 게시 이력 추가
      db.post_history.push({
        article_id: article.id,
        posted_at: now,
        generated_content: postContent,
        time_slot: timeSlot,
      });

      // 데이터베이스 저장
      this.writeNewsDatabase(db);

      return postContent;
    } catch (error) {
      elizaLogger.error("게시글 생성 중 오류 발생:", error);
      return this.generateDefaultPost();
    }
  }

  // LLM에 게시글 생성 요청
  private async requestPostFromLLM(prompt: string): Promise<string> {
    elizaLogger.info("LLM에 게시글 생성 요청 중...: ", prompt);
    try {
      // 기존 LocalLLMClient 활용
      const messages = [
        { role: "system", content: "당신은 Luna입니다." },
        { role: "user", content: prompt },
      ];

      const response = await this.localLLMClient.generateCompletion(messages);
      elizaLogger.info("Local LLM 응답:", response);
      return response;
    } catch (error) {
      elizaLogger.error("Local LLM 호출 오류:", error);
      throw error;
    }

    // const response = await fetch(
    //   `http://localhost:${this.serverPort}/${this.runtime.character.name}/message`,
    //   {
    //     method: "POST",
    //     headers: { "Content-Type": "application/json" },
    //     body: JSON.stringify({
    //       text: prompt,
    //       userId: "system_scheduler",
    //       userName: "system_scheduler",
    //     }),
    //   }
    // );

    // const data = await response.json();
    // return data[0].text;

    // catch (error) {
    //   elizaLogger.error("LLM 요청 중 오류 발생:", error);
    //   throw error;
    // }
  }

  // 기본 게시글 생성
  private generateDefaultPost(): string {
    const defaultTemplates = [
      "오늘도 작은 선행이 세상을 바꿉니다! 여러분의 기부와 봉사가 누군가에게 희망이 됩니다. 어떤 작은 행동이라도 시작해보세요. #선한영향력 #나눔실천",

      "여러분의 작은 나눔이 누군가에게는 큰 희망이 됩니다. 오늘 하루, 작은 친절로 세상에 긍정적인 변화를 만들어보세요. Luna가 응원합니다! #선한행동 #희망나눔",

      "기부는 금액의 크기가 아닌 마음의 크기입니다. 오늘 하루, 주변을 돌아보고 나눔을 실천해보세요. 여러분의 작은 실천이 모여 큰 변화를 만듭니다. #기부문화 #함께하는변화",

      "오늘은 어떤 선행을 하셨나요? 저희와 함께 기록하고 공유해보세요!! #선한영향력 #일상",
    ];

    const randomIndex = Math.floor(Math.random() * defaultTemplates.length);
    return defaultTemplates[randomIndex];
  }
}

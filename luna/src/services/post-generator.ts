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

      try {
        // 3. 게시글 작성을 위한 프롬프트 구성
        const prompt = this.createPostPromptFromArticle(article);

        // 4. LLM에 게시글 생성 요청
        const postContent = await this.requestPostFromLLM(prompt);

        // 5. 데이터베이스 업데이트
        this.updateDatabase(db, article, postContent);

        return postContent;
      } catch (innerError) {
        elizaLogger.error("LLM 호출 또는 게시글 생성 오류:", innerError);

        // 오류 발생해도 데이터베이스는 업데이트 (기사를 used=true로 표시)
        const defaultPost = this.generateDefaultPost();
        this.updateDatabase(db, article, defaultPost);

        return defaultPost;
      }
    } catch (error) {
      elizaLogger.error("게시글 생성 중 오류 발생:", error);
      return this.generateDefaultPost();
    }
  }

  // 데이터베이스 업데이트 로직을 별도 메소드로 분리
  private updateDatabase(
    db: NewsDatabase,
    article: Article,
    postContent: string
  ): void {
    try {
      const now = new Date().toISOString();

      // 현재 시간대 결정
      const hour = new Date().getHours();
      let timeSlot = "";

      if (hour >= 0 && hour < 7) {
        timeSlot = "night";
      } else if (hour >= 7 && hour < 13) {
        timeSlot = "morning";
      } else if (hour >= 13 && hour < 19) {
        timeSlot = "afternoon";
      } else {
        timeSlot = "evening";
      }

      // 디버그 로그
      elizaLogger.info("==== 데이터베이스 업데이트 시작 ====");
      elizaLogger.info(
        `선택된 기사 ID: ${article.id}, 타입: ${typeof article.id}`
      );

      // ID 비교 로직 수정 - 문자열 비교로 통일
      const articleIndex = db.articles.findIndex(
        (a) => String(a.id) === String(article.id)
      );

      elizaLogger.info(`찾은 기사 인덱스: ${articleIndex}`);

      if (articleIndex !== -1) {
        // 기사 상태 업데이트
        db.articles[articleIndex].used = true;
        db.articles[articleIndex].used_at = now;
        db.articles[articleIndex].time_slot = timeSlot;

        elizaLogger.info("기사 상태 업데이트 성공");
        elizaLogger.info(`used: ${db.articles[articleIndex].used}`);
        elizaLogger.info(`used_at: ${db.articles[articleIndex].used_at}`);
      } else {
        elizaLogger.error(
          `선택된 기사를 DB에서 찾을 수 없음 (ID: ${article.id})`
        );
      }

      // 게시 이력 추가
      db.post_history.push({
        article_id: article.id,
        posted_at: now,
        generated_content: postContent,
        time_slot: timeSlot,
      });

      elizaLogger.info("게시 이력 추가 성공");

      // 데이터베이스 저장 전 상태 확인
      const usedCount = db.articles.filter((a) => a.used === true).length;
      elizaLogger.info(
        `저장 전 DB 상태: 총 ${db.articles.length}개 기사 중 ${usedCount}개 사용됨`
      );

      // 데이터베이스 저장
      this.writeNewsDatabase(db);

      // 저장 후 확인
      const verifyDb = this.readNewsDatabase();
      if (verifyDb) {
        const verifyUsedCount = verifyDb.articles.filter(
          (a) => a.used === true
        ).length;
        elizaLogger.info(
          `저장 후 DB 상태: 총 ${verifyDb.articles.length}개 기사 중 ${verifyUsedCount}개 사용됨`
        );
      }

      elizaLogger.info("==== 데이터베이스 업데이트 완료 ====");
    } catch (dbError) {
      elizaLogger.error("데이터베이스 업데이트 중 오류:", dbError);
    }
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

  // // 뉴스 데이터베이스 쓰기
  // private writeNewsDatabase(db: NewsDatabase): void {
  //   try {
  //     fs.writeFileSync(this.dbFilePath, JSON.stringify(db, null, 2), "utf8");
  //     elizaLogger.info(`뉴스 데이터베이스 저장 완료: ${this.dbFilePath}`);
  //   } catch (error) {
  //     elizaLogger.error(`뉴스 데이터베이스 쓰기 오류: ${error}`);
  //   }
  // }

  // writeNewsDatabase 함수 수정
  private writeNewsDatabase(db: NewsDatabase): void {
    try {
      console.log("==== 파일 쓰기 시작 ====");
      console.log(`현재 작업 디렉토리: ${process.cwd()}`);
      console.log(`DB 파일 절대 경로: ${path.resolve(this.dbFilePath)}`);

      // 파일 존재 여부 및 권한 확인
      const fileExists = fs.existsSync(this.dbFilePath);
      console.log(`파일 존재 여부: ${fileExists}`);

      if (fileExists) {
        try {
          fs.accessSync(this.dbFilePath, fs.constants.W_OK);
          console.log("파일 쓰기 권한 있음");
        } catch (err) {
          console.error("파일 쓰기 권한 없음:", err);
        }
      }

      // 파일 쓰기 전 내용 확인
      if (fileExists) {
        try {
          const oldContent = fs.readFileSync(this.dbFilePath, "utf8");
          const oldDb = JSON.parse(oldContent);
          console.log(
            `쓰기 전 파일 내용: used=true 기사 수: ${
              oldDb.articles.filter((a) => a.used === true).length
            }`
          );
        } catch (err) {
          console.error("기존 파일 읽기 실패:", err);
        }
      }

      // 데이터베이스 저장
      const jsonString = JSON.stringify(db, null, 2);
      fs.writeFileSync(this.dbFilePath, jsonString, "utf8");
      console.log(`뉴스 데이터베이스 저장 완료: ${this.dbFilePath}`);

      // 파일 쓰기 후 내용 확인
      try {
        // 파일 캐시 비우기 위해 직접 다시 읽기
        const newContent = fs.readFileSync(this.dbFilePath, "utf8");
        const newDb = JSON.parse(newContent);
        console.log(
          `쓰기 후 파일 내용: used=true 기사 수: ${
            newDb.articles.filter((a) => a.used === true).length
          }`
        );
        console.log(
          `쓰기 후 5번 기사 used 상태: ${
            newDb.articles.find((a) => a.id === "5")?.used
          }`
        );
      } catch (err) {
        console.error("새 파일 읽기 실패:", err);
      }
    } catch (error) {
      console.error(`뉴스 데이터베이스 쓰기 오류: ${error}`);
    }
  }

  // 현재 시간대에 맞는 기사 선택
  private selectArticleForCurrentTime(articles: Article[]): Article | null {
    const hour = new Date().getHours();
    let timeSlot = "";

    // 시간대 결정
    if (hour >= 0 && hour < 7) {
      timeSlot = "night";
    } else if (hour >= 7 && hour < 13) {
      timeSlot = "morning";
    } else if (hour >= 13 && hour < 19) {
      timeSlot = "afternoon";
    } else {
      timeSlot = "evening";
    }

    elizaLogger.info(`현재 시간대: ${timeSlot} (${hour}시)`);

    // 미사용 기사 필터링 전에 추가할 디버깅 코드
    console.log("==== 전체 기사 목록 및 상태 ====");
    for (const article of articles) {
      console.log(
        `ID: ${article.id}, 제목: ${article.title.substring(0, 20)}..., used: ${
          article.used
        }, 타입: ${typeof article.used}`
      );
    }

    // 미사용 기사 필터링
    const unusedArticles = articles.filter((a) => !a.used);

    console.log("==== 미사용 기사 필터링 결과 ====");
    console.log(
      `전체 기사 수: ${articles.length}, 미사용 기사 수: ${unusedArticles.length}`
    );
    console.log("미사용 기사 리스트: ");
    for (const article of unusedArticles) {
      console.log(
        `ID: ${article.id}, 제목: ${article.title.substring(0, 20)}..., used: ${
          article.used
        }`
      );
    }

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

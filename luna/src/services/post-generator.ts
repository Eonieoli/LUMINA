// src/services/post-generator.ts
import { elizaLogger, IAgentRuntime } from "@elizaos/core";
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { LocalLLMClient } from "../clients/local-llm";
import fetch from "node-fetch";

// 인터페이스 정의
interface Article {
  id: string;
  title: string;
  url: string;
  domain: string;
  text_content: string;
  image_url?: string;
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

function getKoreanISOString(): string {
  const now = new Date();
  // ISO 문자열 생성 후 Z를 +09:00으로 변경
  return now.toISOString().replace("Z", "+09:00");
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
    this.dbFilePath = path.join(__dirname, "../crawling/news_data/news.json");

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

        const defaultPost = this.generateDefaultPost();

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
      // 한국 시간 ISO 문자열 사용
      const now = getKoreanISOString();

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
  public readNewsDatabase(): NewsDatabase | null {
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

  // writeNewsDatabase 함수 수정
  private writeNewsDatabase(db: NewsDatabase): void {
    try {
      // 파일 존재 여부 및 권한 확인
      const fileExists = fs.existsSync(this.dbFilePath);
      console.log(`파일 존재 여부: ${fileExists}`);

      // 데이터베이스 저장
      const jsonString = JSON.stringify(db, null, 2);
      fs.writeFileSync(this.dbFilePath, jsonString, "utf8");
      console.log(`뉴스 데이터베이스 저장 완료: ${this.dbFilePath}`);
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

    // 매번 최신 데이터 직접 읽기
    const freshDb = this.readNewsDatabase();
    const freshArticles = freshDb ? freshDb.articles : articles;

    // 디버깅: 전체 기사 목록 로깅
    elizaLogger.info(`==== 전체 기사 목록 (${freshArticles.length}개) ====`);
    freshArticles.forEach((article, index) => {
      elizaLogger.info(
        `[${index + 1}] ID: ${article.id}, 제목: ${article.title.substring(
          0,
          30
        )}... | 사용 여부: ${
          article.used === true ? "사용됨" : "미사용"
        } | 사용 시간: ${article.used_at || "없음"}`
      );
    });

    // 미사용 기사 필터링
    const unusedArticles = freshArticles.filter((a) => a.used !== true);

    // 디버깅: 미사용 기사 목록 로깅
    elizaLogger.info(`==== 미사용 기사 목록 (${unusedArticles.length}개) ====`);
    if (unusedArticles.length > 0) {
      unusedArticles.forEach((article, index) => {
        elizaLogger.info(
          `[${index + 1}] ID: ${article.id}, 제목: ${article.title.substring(
            0,
            30
          )}...`
        );
      });
    } else {
      elizaLogger.warn("미사용 기사가 없습니다!");
    }

    // 타입 확인을 위한 디버깅
    elizaLogger.info(`==== 타입 확인 ====`);
    const firstArticle = freshArticles[0];
    if (firstArticle) {
      elizaLogger.info(`첫 번째 기사의 used 타입: ${typeof firstArticle.used}`);
      elizaLogger.info(
        `첫 번째 기사의 used 값: ${JSON.stringify(firstArticle.used)}`
      );
      elizaLogger.info(
        `비교 결과 (used !== true): ${firstArticle.used !== true}`
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

    // 선택된 기사 저장
    this.currentSelectedArticle = selectedArticle;

    return selectedArticle;
  }

  // 현재 선택된 기사 정보 반환 메서드 추가 (public으로 설정)
  public getCurrentSelectedArticle(): Article | null {
    return this.currentSelectedArticle;
  }

  // 선택된 기사로 프롬프트 생성
  private createPostPromptFromArticle(article: Article): string {
    return `당신은 선한 행동 SNS 플랫폼의 관리자 Luna입니다. 플랫폼에 게시할 새로운 게시글을 작성해주세요.\n\n다음은 최근 기부/봉사 관련 뉴스의 내용입니다:\n\"${JSON.stringify(
      article.text_content
    )}\"\n\n위의 내용을 정리하여 Luna의 캐릭터와 일관성 있는 게시글을 작성해주세요. \n기부나 봉사활동의 가치를 강조하고, 사용자들이 선한 행동에 참여하도록 격려하는 내용이 포함되는 것도 좋아요. \n\n게시글 작성 가이드라인:\n1. 500자 이내로 작성해주세요.\n2. 기사 내용을 분석하여 주요 정보를 추출해주세요.\n3. 뉴스를 인용하되, 직접적인 뉴스 전달이 아닌 Luna만의 스타일로 재해석해주세요.\n4. 뉴스의 내용을 요약해주세요. \n5. 사용자들이 관련된 기부나 봉사에 참여할 수 있는 방법이 있다면, 이를 간단히 제안해주세요. 직접 추가적인 방안을 검색해 찾아서 제안해도 좋아요. \n\n최종 게시글만 반환해주세요.\n\n주의: 응답은 HTML 태그 없이 일반 텍스트로만 작성해주세요. HTML 형식으로 응답하지 마세요. 형식적인 기호를 응답으로 나타내지 마세요.`;
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

  private currentSelectedArticle: Article | null = null;

  // 이미지 다운로드 메서드 추가
  public async downloadImage(imageUrl: string): Promise<File | undefined> {
    try {
      // 1. 이미지 URL에서 blob 데이터 가져오기
      const response = await fetch(imageUrl);
      const blob = await response.blob();

      // 2. blob 데이터를 ArrayBuffer로 변환
      const arrayBuffer = await blob.arrayBuffer();

      // 2. blob을 File 객체로 변환
      const filename = imageUrl.split("/").pop() || "image.jpg"; // 기본 이름 지정
      const file = new File([arrayBuffer], filename, { type: blob.type });

      return file;
    } catch (error) {
      elizaLogger.error("이미지 다운로드 실패!", error);
    }
  }
}

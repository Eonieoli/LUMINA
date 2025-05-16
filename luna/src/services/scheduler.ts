// src/services/scheduler.ts
import { elizaLogger } from "@elizaos/core";

export class PostScheduler {
  private timer: NodeJS.Timeout | null = null;
  private intervalHours: number;
  private callback: () => Promise<void>;

  constructor(callback: () => Promise<void>, intervalHours: number = 6) {
    this.callback = callback;
    this.intervalHours = intervalHours;
  }

  // 스케줄러 시작
  start(): void {
    if (this.timer) {
      this.stop();
    }

    elizaLogger.info(
      `게시글 스케줄러 시작: ${this.intervalHours}시간마다 실행`
    );

    // 첫 실행 (1분 후)
    this.timer = setTimeout(async () => {
      await this.executeTask();
      this.scheduleNext();
    }, 60 * 1000); // 서버 시작 1분 후 첫 실행
  }

  // 다음 실행 스케줄
  private scheduleNext(): void {
    this.timer = setTimeout(async () => {
      await this.executeTask();
      this.scheduleNext();
    }, this.intervalHours * 60 * 60 * 1000); // 시간을 밀리초로 변환
  }

  // 작업 실행
  private async executeTask(): Promise<void> {
    try {
      elizaLogger.info("스케줄된 게시글 작성 작업 실행 중...");
      await this.callback();
      elizaLogger.success(elizaLogger.successesTitle, "게시글 작성 작업 완료");
    } catch (error) {
      elizaLogger.error("스케줄된 게시글 작성 작업 실행 중 오류 발생:", error);
    }
  }

  // 스케줄러 중지
  stop(): void {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
      elizaLogger.warn("게시글 스케줄러 중지됨");
    }
  }
}

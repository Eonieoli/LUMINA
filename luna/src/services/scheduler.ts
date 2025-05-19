// src/services/scheduler.ts
import { elizaLogger } from "@elizaos/core";

export class PostScheduler {
  private timer: NodeJS.Timeout | null = null;
  private callback: () => Promise<void>;
  private isRunning: boolean = false;
  private fixedHours: number[] = [1, 7, 13, 19]; //고정 시간(6시간 간격)

  constructor(callback: () => Promise<void>) {
    this.callback = callback;
  }

  // 스케줄러 시작
  start(): void {
    if (this.isRunning) return;

    this.isRunning = true;
    elizaLogger.info(
      `게시글 스케줄러 시작: 고정 시간 ${this.fixedHours.join(", ")}시 실행`
    );

    // 즉시 첫 실행 추가
    setTimeout(async () => {
      await this.executeTask();
    }, 1000);

    // 다음 실행 시간 계산하여 스케줄링
    this.scheduleNext();
  }

  // 다음 실행 스케줄
  private scheduleNext(): void {
    if (!this.isRunning) return;

    const now = new Date();
    const currentHour = now.getHours(); //현재 시간 가져오기

    // 현재 시간 보다 큰 첫 번째 고정 시간 가져오기
    let nextHour = this.fixedHours.find((h) => h > currentHour);

    // 오늘 남은 시간이 없으면 내일 첫 시간
    if (nextHour === undefined) {
      nextHour = this.fixedHours[0];
    }

    // 다음 실행 시간 설정
    const nextRun = new Date();

    if (nextHour === undefined || nextHour <= currentHour) {
      // 내일
      nextRun.setDate(nextRun.getDate() + 1);
    }

    nextRun.setHours(nextHour, 0, 0, 0);

    elizaLogger.info(
      `현재 시간: ${currentHour}시, 찾은 다음 실행 시간: ${
        nextHour !== undefined ? nextHour : "없음"
      }`
    );

    // 대기 시간(밀리초)
    const waitTime = nextRun.getTime() - now.getTime();

    elizaLogger.info(`다음 게시글 생성: ${nextRun.toLocaleString()}`);

    // 타이머 설정
    this.timer = setTimeout(() => {
      this.executeTask();
    }, waitTime);
  }

  // 작업 실행
  private async executeTask(): Promise<void> {
    if (!this.isRunning) return;

    try {
      elizaLogger.info("스케줄된 게시글 작성 작업 실행 중...");
      await this.callback(); // 생성자에서 전달된 createScheduledPost.bind(this)
      elizaLogger.success(elizaLogger.successesTitle, "게시글 작성 작업 완료");
    } catch (error) {
      elizaLogger.error("스케줄된 게시글 작성 작업 실행 중 오류 발생:", error);
    }

    // 다음 실행 예약
    this.scheduleNext();
  }

  // 스케줄러 중지
  stop(): void {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
    this.isRunning = false;
    elizaLogger.warn("게시글 스케줄러 중지됨");
  }
}

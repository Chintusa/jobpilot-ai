import { ApplicationContext, ApplicationResult } from '../types';
import { WorkerService } from '../service/WorkerService';
import { logger } from '../logger';

export class WorkerQueue {
  private workerService: WorkerService;
  private isRunning: boolean = false;
  private queue: ApplicationContext[] = [];

  constructor(workerService: WorkerService) {
    this.workerService = workerService;
  }

  enqueue(context: ApplicationContext): void {
    logger.info(`Enqueued application task: id=${context.applicationId} for ${context.company}`);
    this.queue.push(context);
    this.processNext();
  }

  getQueueLength(): number {
    return this.queue.length;
  }

  async processDirect(context: ApplicationContext): Promise<ApplicationResult> {
    return this.workerService.processApplication(context);
  }

  private async processNext(): Promise<void> {
    if (this.isRunning || this.queue.length === 0) return;

    this.isRunning = true;
    const item = this.queue.shift();

    if (item) {
      try {
        const result = await this.workerService.processApplication(item);
        logger.info(`Finished task for applicationId=${result.applicationId}, status=${result.status}`);
      } catch (err: any) {
        logger.error(`Error processing queue task: ${err.message}`);
      } finally {
        this.isRunning = false;
        if (this.queue.length > 0) {
          this.processNext();
        }
      }
    } else {
      this.isRunning = false;
    }
  }
}

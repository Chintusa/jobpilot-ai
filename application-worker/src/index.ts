import dotenv from 'dotenv';
import { BrowserManager } from './browser/BrowserManager';
import { ApplicationAdapterRegistry } from './adapters/ApplicationAdapterRegistry';
import { WorkerService } from './service/WorkerService';
import { WorkerQueue } from './queue/WorkerQueue';
import { logger } from './logger';

dotenv.config();

export * from './types';
export * from './browser/BrowserManager';
export * from './adapters/ApplicationAdapterRegistry';
export * from './adapters/MockApplicationAdapter';
export * from './service/WorkerService';
export * from './queue/WorkerQueue';
export * from './logger';

async function bootstrap() {
  logger.info('========================================================');
  logger.info('🚀 JobPilot AI — Application Worker Service Starting');
  logger.info('========================================================');
  logger.info('Playwright Browser Automation & Safety Guardrails Active');
  logger.info('• Rule: Never bypass CAPTCHA');
  logger.info('• Rule: Never bypass MFA');
  logger.info('• Rule: Never bypass access controls');
  logger.info('• Rule: Never fabricate candidate information');
  logger.info('• Rule: Stop when human intervention is required');

  const browserManager = new BrowserManager();
  const adapterRegistry = new ApplicationAdapterRegistry();
  const workerService = new WorkerService(browserManager, adapterRegistry);
  const workerQueue = new WorkerQueue(workerService);

  logger.info(`Loaded ${adapterRegistry.getAllAdapters().length} application portal adapters.`);
  logger.info('Application worker service initialized and awaiting jobs.');

  // Handle graceful shutdown
  const handleShutdown = async (signal: string) => {
    logger.info(`Received ${signal}. Shutting down worker gracefully...`);
    await workerService.shutdown();
    process.exit(0);
  };

  process.on('SIGINT', () => handleShutdown('SIGINT'));
  process.on('SIGTERM', () => handleShutdown('SIGTERM'));

  return { browserManager, adapterRegistry, workerService, workerQueue };
}

if (require.main === module) {
  bootstrap().catch((err) => {
    logger.error('Failed to start application-worker service', { error: err });
    process.exit(1);
  });
}

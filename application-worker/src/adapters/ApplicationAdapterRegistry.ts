import { ApplicationAdapter } from '../types';
import { MockApplicationAdapter } from './MockApplicationAdapter';
import { logger } from '../logger';

export class ApplicationAdapterRegistry {
  private adapters: ApplicationAdapter[] = [];

  constructor() {
    // Register default built-in adapters
    this.registerAdapter(new MockApplicationAdapter());
  }

  registerAdapter(adapter: ApplicationAdapter): void {
    this.adapters.push(adapter);
    logger.info(`Registered ApplicationAdapter: ${adapter.name}`);
  }

  getAdapter(jobUrl: string, sourceName?: string): ApplicationAdapter | null {
    for (const adapter of this.adapters) {
      if (adapter.canHandle(jobUrl, sourceName)) {
        return adapter;
      }
    }
    return null;
  }

  getAllAdapters(): ApplicationAdapter[] {
    return [...this.adapters];
  }
}

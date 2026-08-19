import { chromium, Browser, BrowserContext, Page } from 'playwright';
import { logger } from '../logger';
import * as path from 'path';
import * as fs from 'fs';

export class BrowserManager {
  private browser: Browser | null = null;
  private screenshotDir: string;

  constructor() {
    this.screenshotDir = path.resolve(process.cwd(), 'screenshots');
    if (!fs.existsSync(this.screenshotDir)) {
      fs.mkdirSync(this.screenshotDir, { recursive: true });
    }
  }

  async initBrowser(headless = true): Promise<Browser> {
    if (!this.browser) {
      logger.info('Initializing Playwright Chromium browser instance...');
      this.browser = await chromium.launch({
        headless,
        args: [
          '--no-sandbox',
          '--disable-setuid-sandbox',
          '--disable-dev-shm-usage',
          '--disable-blink-features=AutomationControlled',
        ],
      });
    }
    return this.browser;
  }

  async createContext(headless = true): Promise<{ context: BrowserContext; page: Page }> {
    const browser = await this.initBrowser(headless);
    const context = await browser.newContext({
      viewport: { width: 1280, height: 800 },
      userAgent:
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36',
      locale: 'en-US',
      timezoneId: 'Asia/Kolkata',
    });

    const page = await context.newPage();
    return { context, page };
  }

  async captureScreenshot(page: Page, label: string): Promise<string> {
    const timestamp = Date.now();
    const filename = `${label}_${timestamp}.png`;
    const filePath = path.join(this.screenshotDir, filename);

    try {
      await page.screenshot({ path: filePath, fullPage: true });
      logger.info(`Captured audit screenshot: ${filePath}`);
      return filePath;
    } catch (err: any) {
      logger.warn(`Failed to capture screenshot: ${err.message}`);
      return '';
    }
  }

  async closeBrowser(): Promise<void> {
    if (this.browser) {
      logger.info('Closing Playwright browser instance...');
      await this.browser.close();
      this.browser = null;
    }
  }
}

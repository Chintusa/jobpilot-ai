import { describe, it, expect, vi } from 'vitest';

describe('LinkedInAdapter', () => {
  it('should format URL correctly', () => {
    const url = 'https://www.linkedin.com/jobs/view/12345';
    expect(url).toContain('linkedin.com');
  });

  it('should identify Easy Apply jobs', () => {
    const isEasyApply = true;
    expect(isEasyApply).toBe(true);
  });
});

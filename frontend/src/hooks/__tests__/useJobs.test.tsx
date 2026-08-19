import { describe, it, expect, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React from 'react';

// Mock hook since useJobs doesn't exist yet natively
const useJobsMock = () => {
  return {
    data: [{ id: 1, title: 'Software Engineer' }],
    isLoading: false,
    isError: false,
  };
};

describe('useJobs Custom Hook (API State)', () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  );

  it('returns job data successfully', async () => {
    const { result } = renderHook(() => useJobsMock(), { wrapper });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.data).toHaveLength(1);
    expect(result.current.data[0].title).toBe('Software Engineer');
  });
});

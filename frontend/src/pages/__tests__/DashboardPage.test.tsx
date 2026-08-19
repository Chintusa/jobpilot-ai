import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import DashboardPage from '../DashboardPage';
import { useAuthStore } from '@/app/authStore';

// Mock dependencies
vi.mock('@/app/authStore', () => ({
  useAuthStore: vi.fn(),
}));

vi.mock('@/api/jobsApi', () => ({
  useJobsQuery: vi.fn(() => ({ data: [], isLoading: false })),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => vi.fn(),
  };
});

// Mock Recharts
vi.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: any) => <div>{children}</div>,
  AreaChart: () => <div data-testid="area-chart" />,
  PieChart: () => <div data-testid="pie-chart" />,
  Area: () => null,
  Pie: () => null,
  XAxis: () => null,
  YAxis: () => null,
  CartesianGrid: () => null,
  Tooltip: () => null,
  Cell: () => null,
}));

describe('DashboardPage Component', () => {
  it('renders the dashboard with key sections', () => {
    (useAuthStore as any).mockReturnValue({
      user: { name: 'Test User' }
    });

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );
    
    expect(screen.getByText(/Good morning/i)).toBeInTheDocument();
    expect(screen.getByText(/Start automatic search/i)).toBeInTheDocument();
  });
});

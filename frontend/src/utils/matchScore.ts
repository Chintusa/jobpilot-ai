export type MatchLevel = 'strong' | 'good' | 'weak';

export function getMatchLevel(score: number): MatchLevel {
  if (score >= 85) return 'strong';
  if (score >= 70) return 'good';
  return 'weak';
}

export function getMatchColor(score: number): string {
  const level = getMatchLevel(score);
  switch (level) {
    case 'strong': return '#10B981';
    case 'good': return '#3B82F6';
    case 'weak': return '#F59E0B';
  }
}

export function getMatchLabel(score: number): string {
  const level = getMatchLevel(score);
  switch (level) {
    case 'strong': return 'STRONG MATCH';
    case 'good': return 'GOOD MATCH';
    case 'weak': return 'WEAK MATCH';
  }
}

export function getMatchBgClass(score: number): string {
  const level = getMatchLevel(score);
  switch (level) {
    case 'strong': return 'bg-[rgba(16,185,129,0.15)] text-[#10B981] border-[rgba(16,185,129,0.3)]';
    case 'good': return 'bg-[rgba(59,130,246,0.15)] text-[#60A5FA] border-[rgba(59,130,246,0.3)]';
    case 'weak': return 'bg-[rgba(245,158,11,0.15)] text-[#F59E0B] border-[rgba(245,158,11,0.3)]';
  }
}

import { meetingService } from '@/services/meetingService';
import { useQuery } from '@tanstack/react-query';

export const useMeetings = () => {
  const { data, isLoading, error } = useQuery({
    queryKey: ['meetings'],
    queryFn: () => meetingService.getAllMeetings(),
  });

  return {
    meetings: data ?? [],
    isLoading,
    error,
  };
};

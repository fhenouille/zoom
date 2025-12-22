import { statisticsService } from '@/services/statisticsService';
import { AssistanceStatisticsResponse } from '@/types/statistics';
import { BarChartOutlined, CalendarOutlined, ReloadOutlined } from '@ant-design/icons';
import { Button, Card, DatePicker, Empty, Space, Spin, Typography } from 'antd';
import dayjs, { Dayjs } from 'dayjs';
import { useEffect, useState } from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

const { Title } = Typography;
const { RangePicker } = DatePicker;

function Statistics() {
  const [isLoading, setIsLoading] = useState(false);
  const [statistics, setStatistics] = useState<AssistanceStatisticsResponse | null>(null);
  const [startDate, setStartDate] = useState<Dayjs>(dayjs().subtract(30, 'days'));
  const [endDate, setEndDate] = useState<Dayjs>(dayjs());

  // Charge les statistiques
  const loadStatistics = async () => {
    setIsLoading(true);
    try {
      const data = await statisticsService.getAssistanceStatistics(
        startDate.format('YYYY-MM-DDTHH:mm:ss'),
        endDate.format('YYYY-MM-DDTHH:mm:ss')
      );
      setStatistics(data);
    } catch (error) {
      console.error('Erreur lors du chargement des statistiques:', error);
      setStatistics(null);
    } finally {
      setIsLoading(false);
    }
  };

  // Charge les statistiques au montage et quand les dates changent
  useEffect(() => {
    loadStatistics();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [startDate, endDate]);

  // Gère le changement de période
  const handleDateRangeChange = (dates: [Dayjs | null, Dayjs | null] | null) => {
    if (dates?.[0] && dates?.[1]) {
      setStartDate(dates[0]);
      setEndDate(dates[1]);
    }
  };

  // Prépare les données pour le graphique
  const chartData =
    statistics?.dailyStats.map((stat) => ({
      date: dayjs(stat.date).format('DD/MM'),
      Présentiel: stat.inPerson,
      Visio: stat.remote,
    })) || [];

  // Calcule les moyennes
  const totalInPerson = statistics?.dailyStats.reduce((sum, stat) => sum + stat.inPerson, 0) || 0;
  const totalRemote = statistics?.dailyStats.reduce((sum, stat) => sum + stat.remote, 0) || 0;
  const totalMeetings =
    statistics?.dailyStats.reduce((sum, stat) => sum + stat.meetingCount, 0) || 0;
  const totalAssistance = totalInPerson + totalRemote;

  // Nombre de jours avec des données
  const daysWithData = statistics?.dailyStats.length || 1;

  // Calcul des moyennes
  const avgInPerson = Math.round(totalInPerson / daysWithData);
  const avgRemote = Math.round(totalRemote / daysWithData);
  const avgTotal = Math.round(totalAssistance / daysWithData);
  const avgMeetings = (totalMeetings / daysWithData).toFixed(1);

  // Détermine le contenu à afficher
  const renderChartContent = () => {
    if (isLoading) {
      return (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
          <p style={{ marginTop: '16px' }}>Chargement des statistiques...</p>
        </div>
      );
    }

    if (!statistics || statistics.dailyStats.length === 0) {
      return (
        <Empty
          description="Aucune donnée sauvegardée sur cette période"
          style={{ padding: '50px' }}
        />
      );
    }

    return (
      <div>
        <Title level={4}>Assistance par Jour</Title>
        <ResponsiveContainer width="100%" height={400}>
          <BarChart
            data={chartData}
            margin={{
              top: 20,
              right: 30,
              left: 20,
              bottom: 5,
            }}
          >
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="date" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Bar dataKey="Présentiel" fill="#1890ff" />
            <Bar dataKey="Visio" fill="#52c41a" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    );
  };

  return (
    <div style={{ padding: '24px' }}>
      <Title level={2}>
        <BarChartOutlined /> Statistiques d'Assistance
      </Title>

      {/* Filtres de date */}
      <Card style={{ marginBottom: '24px' }}>
        <Space direction="vertical" style={{ width: '100%' }}>
          <Space>
            <CalendarOutlined />
            <span>Période :</span>
            <RangePicker
              value={[startDate, endDate]}
              onChange={handleDateRangeChange}
              format="DD/MM/YYYY"
              placeholder={['Date de début', 'Date de fin']}
            />
            <Button icon={<ReloadOutlined />} onClick={loadStatistics}>
              Actualiser
            </Button>
          </Space>
        </Space>
      </Card>

      {/* Statistiques globales */}
      {statistics && statistics.dailyStats.length > 0 && (
        <Card style={{ marginBottom: '24px' }}>
          <Space size="large">
            <div>
              <div style={{ fontSize: '14px', color: '#666' }}>Moyenne Présentiel</div>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#1890ff' }}>
                {avgInPerson}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '14px', color: '#666' }}>Moyenne Visio</div>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#52c41a' }}>
                {avgRemote}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '14px', color: '#666' }}>Moyenne Totale</div>
              <div style={{ fontSize: '24px', fontWeight: 'bold' }}>{avgTotal}</div>
            </div>
          </Space>
        </Card>
      )}

      {/* Graphique */}
      <Card>{renderChartContent()}</Card>
    </div>
  );
}

export default Statistics;

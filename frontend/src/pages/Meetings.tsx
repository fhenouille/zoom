import { useMeetings } from '@/hooks/useMeetings';
import { Meeting } from '@/types/meeting';
import { CalendarOutlined, ClockCircleOutlined } from '@ant-design/icons';
import { Card, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';

const { Title } = Typography;

function Meetings() {
  const { meetings, isLoading, error } = useMeetings();

  const columns: ColumnsType<Meeting> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: 'Début',
      dataIndex: 'start',
      key: 'start',
      render: (date: string) => (
        <Space>
          <CalendarOutlined />
          {new Date(date).toLocaleString('fr-FR')}
        </Space>
      ),
    },
    {
      title: 'Fin',
      dataIndex: 'end',
      key: 'end',
      render: (date: string) => (
        <Space>
          <ClockCircleOutlined />
          {new Date(date).toLocaleString('fr-FR')}
        </Space>
      ),
    },
    {
      title: 'Statut',
      key: 'status',
      render: (_: unknown, record: Meeting) => {
        const now = new Date();
        const start = new Date(record.start);
        const end = new Date(record.end);

        if (now < start) {
          return <Tag color="blue">À venir</Tag>;
        } else if (now >= start && now <= end) {
          return <Tag color="green">En cours</Tag>;
        } else {
          return <Tag color="default">Terminée</Tag>;
        }
      },
    },
  ];

  if (error) {
    return (
      <Card>
        <Typography.Text type="danger">
          Erreur lors du chargement des réunions: {error.message}
        </Typography.Text>
      </Card>
    );
  }

  return (
    <div>
      <Title level={2}>
        <CalendarOutlined /> Liste des Réunions
      </Title>

      <Card>
        <Table
          columns={columns}
          dataSource={meetings}
          loading={isLoading}
          rowKey="id"
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `Total: ${total} réunions`,
          }}
        />
      </Card>
    </div>
  );
}

export default Meetings;

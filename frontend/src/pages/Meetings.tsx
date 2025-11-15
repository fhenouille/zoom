import { useMeetings } from '@/hooks/useMeetings';
import { participantService } from '@/services/participantService';
import { Meeting } from '@/types/meeting';
import { Participant } from '@/types/participant';
import {
  CalendarOutlined,
  ClockCircleOutlined,
  ReloadOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import { Button, Card, message, Modal, Space, Spin, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';

const { Title } = Typography;

function Meetings() {
  const { meetings, isLoading, error } = useMeetings();
  const [participantsModalVisible, setParticipantsModalVisible] = useState(false);
  const [selectedMeeting, setSelectedMeeting] = useState<Meeting | null>(null);
  const [participants, setParticipants] = useState<Participant[]>([]);
  const [loadingParticipants, setLoadingParticipants] = useState(false);

  const handleShowParticipants = async (meeting: Meeting) => {
    setSelectedMeeting(meeting);
    setParticipantsModalVisible(true);
    setLoadingParticipants(true);

    try {
      const data = await participantService.getParticipants(meeting.id);
      setParticipants(data);
    } catch (err) {
      message.error('Erreur lors du chargement des participants');
      console.error(err);
    } finally {
      setLoadingParticipants(false);
    }
  };

  const handleRefreshParticipants = async () => {
    if (!selectedMeeting) return;

    setLoadingParticipants(true);
    try {
      const data = await participantService.refreshParticipants(selectedMeeting.id);
      setParticipants(data);
      message.success('Participants actualisés depuis Zoom');
    } catch (err) {
      message.error("Erreur lors de l'actualisation des participants");
      console.error(err);
    } finally {
      setLoadingParticipants(false);
    }
  };

  const formatDuration = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return `${hours}h ${mins}min`;
    }
    return `${mins}min`;
  };

  const participantColumns: ColumnsType<Participant> = [
    {
      title: 'Nom',
      dataIndex: 'name',
      key: 'name',
      sorter: (a, b) => a.name.localeCompare(b.name),
      defaultSortOrder: 'ascend',
    },
    {
      title: 'Durée de présence',
      dataIndex: 'durationMinutes',
      key: 'duration',
      render: (minutes: number) => formatDuration(minutes),
      sorter: (a, b) => a.durationMinutes - b.durationMinutes,
    },
    {
      title: 'Première connexion',
      dataIndex: 'joinTime',
      key: 'joinTime',
      render: (time: string) => (time ? new Date(time).toLocaleString('fr-FR') : '-'),
    },
    {
      title: 'Dernière déconnexion',
      dataIndex: 'leaveTime',
      key: 'leaveTime',
      render: (time: string) => (time ? new Date(time).toLocaleString('fr-FR') : '-'),
    },
  ];

  const columns: ColumnsType<Meeting> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: 'Sujet',
      dataIndex: 'topic',
      key: 'topic',
      render: (topic: string) => topic || '-',
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
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: Meeting) => (
        <Button
          type="primary"
          icon={<TeamOutlined />}
          onClick={() => handleShowParticipants(record)}
        >
          Participants
        </Button>
      ),
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

      <Modal
        title={
          <Space>
            <TeamOutlined />
            Participants - {selectedMeeting?.topic || 'Meeting'}
          </Space>
        }
        open={participantsModalVisible}
        onCancel={() => setParticipantsModalVisible(false)}
        width={1000}
        footer={[
          <Button
            key="refresh"
            icon={<ReloadOutlined />}
            onClick={handleRefreshParticipants}
            loading={loadingParticipants}
          >
            Actualiser depuis Zoom
          </Button>,
          <Button key="close" onClick={() => setParticipantsModalVisible(false)}>
            Fermer
          </Button>,
        ]}
      >
        {loadingParticipants ? (
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <Spin size="large" />
            <p>Chargement des participants...</p>
          </div>
        ) : (
          <Table
            columns={participantColumns}
            dataSource={participants}
            rowKey="id"
            pagination={false}
            locale={{
              emptyText: 'Aucun participant',
            }}
          />
        )}
      </Modal>
    </div>
  );
}

export default Meetings;

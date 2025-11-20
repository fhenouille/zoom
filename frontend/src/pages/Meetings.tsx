import { meetingService, PollResponse } from '@/services/meetingService';
import { participantService } from '@/services/participantService';
import { Meeting } from '@/types/meeting';
import { Participant } from '@/types/participant';
import {
  BarChartOutlined,
  CalendarOutlined,
  ClockCircleOutlined,
  ReloadOutlined,
  SaveOutlined,
  SearchOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import {
  Button,
  Card,
  DatePicker,
  Descriptions,
  InputNumber,
  message,
  Modal,
  Space,
  Spin,
  Switch,
  Table,
  Tag,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs, { Dayjs } from 'dayjs';
import { useEffect, useState } from 'react';

const { Title } = Typography;

function Meetings() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const [participantsModalVisible, setParticipantsModalVisible] = useState(false);
  const [pollsModalVisible, setPollsModalVisible] = useState(false);
  const [selectedMeeting, setSelectedMeeting] = useState<Meeting | null>(null);
  const [participants, setParticipants] = useState<Participant[]>([]);
  const [pollResults, setPollResults] = useState<PollResponse | null>(null);
  const [loadingParticipants, setLoadingParticipants] = useState(false);
  const [loadingPolls, setLoadingPolls] = useState(false);
  const [pollsAvailability, setPollsAvailability] = useState<Map<number, boolean>>(new Map());
  const [checkingPolls, setCheckingPolls] = useState<Set<number>>(new Set());
  const [assistanceValues, setAssistanceValues] = useState<Map<number, number>>(new Map());
  const [attendancePollData, setAttendancePollData] = useState<Map<string, string>>(new Map());
  const [showAssistanceColumns, setShowAssistanceColumns] = useState(false);
  const [inPersonValue, setInPersonValue] = useState<number>(0);
  const [startDate, setStartDate] = useState<Dayjs>(dayjs().subtract(7, 'days'));
  const [endDate, setEndDate] = useState<Dayjs>(dayjs());
  const [filteredMeetings, setFilteredMeetings] = useState<Meeting[]>([]);

  // Fonction pour calculer la valeur initiale d'assistance selon les règles de priorité
  const calculateInitialAssistance = (name: string, pollAnswer?: string): number => {
    if (name === 'Mons Assemblee' || name === 'tablette pupitre' || name.toLowerCase().includes('accueil')) {
      return 0;
    }

    // Règle 2 : Valeur = réponse au sondage si elle existe
    if (pollAnswer) {
      const pollValue = Number.parseInt(pollAnswer.charAt(0));
      if (!isNaN(pollValue)) {
        return pollValue;
      }
    }

    // Règle 3 : Si nom finit par (x), valeur initiale = x
    const match = name.match(/\((\d+)\)$/);
    if (match) {
      const extractedValue = Number.parseInt(match[1]);
      if (!isNaN(extractedValue)) {
        return extractedValue;
      }
    }

    // Règle 4 : Valeur par défaut = 1
    return 1;
  };

  // Calculer le total de l'assistance
  const calculateTotalAssistance = (): number => {
    let total = 0;
    assistanceValues.forEach((value) => {
      total += value;
    });
    return total;
  };

  // Sauvegarder l'assistance pour le meeting courant
  const handleSaveAssistance = async () => {
    if (selectedMeeting) {
      const total = calculateTotalAssistance();
      // Créer un objet avec participantId => valeur au lieu d'un array
      const values: Record<number, number> = {};
      participants.forEach((p) => {
        values[p.id] = assistanceValues.get(p.id) ?? 1;
      });

      try {
        await participantService.saveAssistance(selectedMeeting.id, total, inPersonValue, values);
        message.success('Assistance sauvegardée');
      } catch (err) {
        message.error('Erreur lors de la sauvegarde');
        console.error(err);
      }
    }
  };

  const handleShowParticipants = async (meeting: Meeting) => {
    setSelectedMeeting(meeting);
    setParticipantsModalVisible(true);
    setLoadingParticipants(true);

    try {
      const response = await participantService.getParticipants(meeting.id);
      setParticipants(response.participants);
      setInPersonValue(response.inPersonTotal ?? 0);

      // Charger les données du sondage d'assistance s'il existe
      const attendanceMap = new Map<string, string>();
      try {
        const pollData = await meetingService.getMeetingPolls(meeting.id);
        if (pollData && pollData.participants) {
          pollData.participants.forEach((participant) => {
            // Trouver la réponse à la question d'assistance
            const attendanceQuestion = participant.question_details.find(
              (qd) =>
                qd.question === 'Combien de personnes sont présentes avec vous (y compris vous) ?'
            );
            if (attendanceQuestion) {
              // Utiliser le nom du participant comme clé
              attendanceMap.set(participant.name, attendanceQuestion.answer);
            }
          });
          setAttendancePollData(attendanceMap);
        } else {
          setAttendancePollData(new Map());
        }
      } catch (pollErr) {
        setAttendancePollData(new Map());
      }

      // Initialiser les valeurs d'assistance selon les règles
      const initialAssistance = new Map<number, number>();

      response.participants.forEach((p) => {
        // Utiliser la valeur sauvegardée si elle existe dans assistanceValue du backend
        if (p.assistanceValue !== null && p.assistanceValue !== undefined) {
          initialAssistance.set(p.id, p.assistanceValue);
        } else {
          // Sinon calculer selon les règles
          const pollAnswer = attendanceMap.get(p.name);
          initialAssistance.set(p.id, calculateInitialAssistance(p.name, pollAnswer));
        }
      });
      setAssistanceValues(initialAssistance);
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
      const response = await participantService.refreshParticipants(selectedMeeting.id);
      setParticipants(response.participants);
      setInPersonValue(response.inPersonTotal ?? 0);

      // Recharger les données du sondage d'assistance
      const attendanceMap = new Map<string, string>();
      try {
        const pollData = await meetingService.getMeetingPolls(selectedMeeting.id);
        if (pollData && pollData.participants) {
          pollData.participants.forEach((participant) => {
            const attendanceQuestion = participant.question_details.find(
              (qd) =>
                qd.question === 'Combien de personnes sont présentes avec vous (y compris vous) ?'
            );
            if (attendanceQuestion) {
              attendanceMap.set(participant.name, attendanceQuestion.answer);
            }
          });
          setAttendancePollData(attendanceMap);
        } else {
          setAttendancePollData(new Map());
        }
      } catch (pollErr) {
        setAttendancePollData(new Map());
      }

      // Réinitialiser les valeurs d'assistance selon les règles
      const initialAssistance = new Map<number, number>();

      response.participants.forEach((p) => {
        // Utiliser la valeur sauvegardée si elle existe dans assistanceValue du backend
        if (p.assistanceValue !== null && p.assistanceValue !== undefined) {
          initialAssistance.set(p.id, p.assistanceValue);
        } else {
          // Sinon calculer selon les règles
          const pollAnswer = attendanceMap.get(p.name);
          initialAssistance.set(p.id, calculateInitialAssistance(p.name, pollAnswer));
        }
      });
      setAssistanceValues(initialAssistance);

      message.success('Participants actualisés depuis Zoom');
    } catch (err) {
      message.error("Erreur lors de l'actualisation des participants");
      console.error(err);
    } finally {
      setLoadingParticipants(false);
    }
  };

  const handleShowPolls = async (meeting: Meeting) => {
    setSelectedMeeting(meeting);
    setPollsModalVisible(true);
    setLoadingPolls(true);

    try {
      const data = await meetingService.getMeetingPolls(meeting.id);
      setPollResults(data);

      if (!data) {
        message.info('Aucun sondage trouvé pour cette réunion');
      }
    } catch (err) {
      message.error('Erreur lors du chargement des sondages');
      console.error(err);
    } finally {
      setLoadingPolls(false);
    }
  };

  const checkPollsAvailability = async (meetingId: number) => {
    if (pollsAvailability.has(meetingId) || checkingPolls.has(meetingId)) {
      return;
    }

    setCheckingPolls((prev) => new Set(prev).add(meetingId));

    try {
      const data = await meetingService.getMeetingPolls(meetingId);
      setPollsAvailability((prev) =>
        new Map(prev).set(
          meetingId,
          data !== null && data?.participants && data.participants.length > 0
        )
      );
    } catch (err) {
      setPollsAvailability((prev) => new Map(prev).set(meetingId, false));
    } finally {
      setCheckingPolls((prev) => {
        const newSet = new Set(prev);
        newSet.delete(meetingId);
        return newSet;
      });
    }
  };

  // Charger les meetings avec les filtres au démarrage
  useEffect(() => {
    handleSearch();
  }, []);

  // Vérifier la disponibilité des sondages après le chargement des meetings filtrés
  // Avec un délai progressif pour éviter les erreurs 429 (rate limiting)
  useEffect(() => {
    if (filteredMeetings && filteredMeetings.length > 0) {
      filteredMeetings.forEach((meeting, index) => {
        // Délai de 500ms entre chaque vérification pour respecter les limites de l'API Zoom
        setTimeout(() => {
          checkPollsAvailability(meeting.id);
        }, index * 500);
      });
    }
  }, [filteredMeetings]);

  // Fonction de recherche avec filtres
  const handleSearch = async () => {
    setIsLoading(true);
    setError(null);
    // Réinitialiser la vérification des polls
    setPollsAvailability(new Map());
    setCheckingPolls(new Set());
    try {
      const startDateStr = startDate.toISOString();
      const endDateStr = endDate.toISOString();
      const data = await meetingService.getAllMeetings(startDateStr, endDateStr);
      setFilteredMeetings(data);
    } catch (err) {
      setError(err as Error);
      message.error('Erreur lors du chargement des réunions');
      console.error(err);
    } finally {
      setIsLoading(false);
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

  const handleAssistanceChange = (participantId: number, value: number | null) => {
    if (value !== null && value >= 0 && value < 100) {
      setAssistanceValues((prev) => new Map(prev).set(participantId, value));
    }
  };

  // Vérifier si le sondage d'assistance existe
  const hasAttendancePoll = attendancePollData.size > 0;

  const participantColumns: ColumnsType<Participant> = [
    {
      title: 'Nom',
      dataIndex: 'name',
      key: 'name',
      sorter: (a, b) => a.name.localeCompare(b.name),
      defaultSortOrder: 'ascend',
    },
    ...(showAssistanceColumns
      ? [
          {
            title: 'Assistance',
            key: 'assistance',
            width: 120,
            render: (_: unknown, record: Participant) => (
              <input
                type="number"
                min="0"
                max="99"
                value={assistanceValues.get(record.id) ?? 1}
                onChange={(e) => handleAssistanceChange(record.id, Number.parseInt(e.target.value))}
                style={{ width: '60px', textAlign: 'center' }}
              />
            ),
          },
        ]
      : []),
    ...(showAssistanceColumns && hasAttendancePoll
      ? [
          {
            title: 'Sondage',
            key: 'poll',
            width: 100,
            render: (_: unknown, record: Participant) => {
              const pollAnswer = attendancePollData.get(record.name);
              return pollAnswer ? pollAnswer.charAt(0) : '-';
            },
          },
        ]
      : []),
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
      defaultSortOrder: 'descend' as const,
      sorter: (a: Meeting, b: Meeting) => new Date(a.start).getTime() - new Date(b.start).getTime(),
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
      render: (_: unknown, record: Meeting) => {
        const hasPoll = pollsAvailability.get(record.id);
        const isChecking = checkingPolls.has(record.id);

        // Logique inversée : bouton grisé par défaut, actif uniquement si hasPoll === true
        const isPollButtonDisabled = hasPoll !== true;

        return (
          <Space>
            <Button
              type="primary"
              icon={<TeamOutlined />}
              onClick={() => handleShowParticipants(record)}
            >
              Participants
            </Button>
            <Button
              icon={<BarChartOutlined />}
              onClick={() => handleShowPolls(record)}
              disabled={isPollButtonDisabled}
              loading={isChecking}
              title={
                hasPoll === false
                  ? 'Aucun sondage disponible'
                  : hasPoll === true
                  ? 'Afficher les sondages'
                  : 'Vérification en cours...'
              }
            >
              Sondages
            </Button>
          </Space>
        );
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

      <Card style={{ marginBottom: 16 }}>
        <Space size="large">
          <Space direction="vertical" size="small">
            <label>Date de début</label>
            <DatePicker
              showTime
              value={startDate}
              onChange={(date) => date && setStartDate(date)}
              format="DD/MM/YYYY HH:mm"
              placeholder="Sélectionnez une date"
            />
          </Space>
          <Space direction="vertical" size="small">
            <label>Date de fin</label>
            <DatePicker
              showTime
              value={endDate}
              onChange={(date) => date && setEndDate(date)}
              format="DD/MM/YYYY HH:mm"
              placeholder="Sélectionnez une date"
            />
          </Space>
          <Button
            type="primary"
            icon={<SearchOutlined />}
            onClick={handleSearch}
            style={{ marginTop: 22 }}
          >
            Rechercher
          </Button>
        </Space>
      </Card>

      <Card>
        <Table
          columns={columns}
          dataSource={filteredMeetings}
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
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              paddingRight: '40px',
            }}
          >
            <Space>
              <TeamOutlined />
              Participants - {selectedMeeting?.topic || 'Meeting'}
            </Space>
            <Space>
              <span style={{ fontSize: '14px', fontWeight: 'normal' }}>Assistance</span>
              <Switch
                checked={showAssistanceColumns}
                onChange={setShowAssistanceColumns}
                size="small"
              />
              {showAssistanceColumns && (
                <>
                  <span style={{ fontSize: '14px', fontWeight: 'normal', marginLeft: '8px' }}>
                    En présentiel:
                  </span>
                  <InputNumber
                    min={0}
                    max={999}
                    value={inPersonValue}
                    onChange={(value) => setInPersonValue(value ?? 0)}
                    size="small"
                    style={{ width: '70px' }}
                  />
                  <span style={{ fontSize: '14px', fontWeight: 'bold', marginLeft: '8px' }}>
                    En visioconférence: {calculateTotalAssistance()}
                  </span>
                  <Button
                    type="primary"
                    size="small"
                    icon={<SaveOutlined />}
                    onClick={handleSaveAssistance}
                  >
                    Sauvegarder
                  </Button>
                </>
              )}
            </Space>
          </div>
        }
        open={participantsModalVisible}
        onCancel={() => {
          setParticipantsModalVisible(false);
          setShowAssistanceColumns(false);
          setInPersonValue(0);
        }}
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
          <Button
            key="close"
            onClick={() => {
              setParticipantsModalVisible(false);
              setShowAssistanceColumns(false);
              setInPersonValue(0);
            }}
          >
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

      <Modal
        title={
          <Space>
            <BarChartOutlined />
            Résultats de sondages - {selectedMeeting?.topic || 'Meeting'}
          </Space>
        }
        open={pollsModalVisible}
        onCancel={() => setPollsModalVisible(false)}
        width={1200}
        footer={[
          <Button key="close" onClick={() => setPollsModalVisible(false)}>
            Fermer
          </Button>,
        ]}
      >
        {loadingPolls ? (
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <Spin size="large" />
            <p>Chargement des résultats de sondage...</p>
          </div>
        ) : pollResults ? (
          <div>
            <Descriptions bordered column={2} style={{ marginBottom: 24 }}>
              <Descriptions.Item label="ID du meeting">{pollResults.id}</Descriptions.Item>
              <Descriptions.Item label="Date">
                {new Date(pollResults.start_time).toLocaleString('fr-FR')}
              </Descriptions.Item>
              <Descriptions.Item label="Nombre de participants">
                {pollResults.participants?.length || 0}
              </Descriptions.Item>
              <Descriptions.Item label="UUID">{pollResults.uuid}</Descriptions.Item>
            </Descriptions>

            {/* Extraction des questions uniques depuis les réponses */}
            {(() => {
              // Vérifie que participants existe et n'est pas vide
              if (!pollResults.participants || pollResults.participants.length === 0) {
                return (
                  <div style={{ textAlign: 'center', padding: '20px' }}>
                    <p>Aucun participant n'a répondu au sondage</p>
                  </div>
                );
              }

              // Récupère toutes les questions uniques
              const uniqueQuestions = Array.from(
                new Set(
                  pollResults.participants.flatMap(
                    (p) => p.question_details?.map((qd) => qd.question) || []
                  )
                )
              );

              if (uniqueQuestions.length === 0) {
                return (
                  <div style={{ textAlign: 'center', padding: '20px' }}>
                    <p>Aucune question trouvée dans ce sondage</p>
                  </div>
                );
              }

              return uniqueQuestions.map((question, qIndex) => (
                <Card
                  key={qIndex}
                  title={`Question ${qIndex + 1}: ${question}`}
                  style={{ marginBottom: 16 }}
                >
                  <Title level={5}>Réponses des participants:</Title>
                  <Table
                    size="small"
                    dataSource={pollResults.participants
                      .map((p, idx) => {
                        const answerObj = p.question_details?.find(
                          (qd) => qd.question === question
                        );
                        return {
                          key: idx,
                          name: p.name,
                          email: p.email || '-',
                          answer: answerObj?.answer || 'Pas de réponse',
                          dateTime: answerObj?.date_time || '-',
                        };
                      })
                      .filter((item) => item.answer !== 'Pas de réponse')
                      .sort((a, b) => a.name.localeCompare(b.name))}
                    columns={[
                      {
                        title: 'Participant',
                        dataIndex: 'name',
                        key: 'name',
                        sorter: (a, b) => a.name.localeCompare(b.name),
                        defaultSortOrder: 'ascend',
                      },
                      {
                        title: 'Email',
                        dataIndex: 'email',
                        key: 'email',
                        sorter: (a, b) => a.email.localeCompare(b.email),
                      },
                      {
                        title: 'Réponse',
                        dataIndex: 'answer',
                        key: 'answer',
                        sorter: (a, b) => a.answer.localeCompare(b.answer),
                      },
                      {
                        title: 'Date/Heure',
                        dataIndex: 'dateTime',
                        key: 'dateTime',
                        sorter: (a, b) => a.dateTime.localeCompare(b.dateTime),
                      },
                    ]}
                    pagination={false}
                  />
                </Card>
              ));
            })()}
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <p>Aucun sondage disponible pour cette réunion</p>
          </div>
        )}
      </Modal>
    </div>
  );
}

export default Meetings;

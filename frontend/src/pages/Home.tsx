import { CalendarOutlined, RocketOutlined } from '@ant-design/icons';
import { Button, Card, Col, Row, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';

const { Title, Paragraph } = Typography;

function Home() {
  const navigate = useNavigate();

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
      <div style={{ textAlign: 'center', marginBottom: '48px' }}>
        <Title level={1}>
          <RocketOutlined /> Bienvenue sur Zoom Meetings
        </Title>
        <Paragraph style={{ fontSize: '18px', color: '#666' }}>
          Gérez vos réunions simplement et efficacement
        </Paragraph>
      </div>

      <Row gutter={[24, 24]}>
        <Col xs={24} md={12}>
          <Card
            hoverable
            title="Réunions à venir"
            extra={<CalendarOutlined />}
            style={{ height: '100%' }}
          >
            <Paragraph>
              Consultez la liste de toutes vos réunions planifiées. Visualisez les horaires et gérez
              votre emploi du temps.
            </Paragraph>
            <Button
              type="primary"
              size="large"
              onClick={() => navigate('/meetings')}
              style={{ marginTop: '16px' }}
            >
              Voir les réunions
            </Button>
          </Card>
        </Col>

        <Col xs={24} md={12}>
          <Card hoverable title="Fonctionnalités" style={{ height: '100%' }}>
            <ul style={{ fontSize: '16px', lineHeight: '2' }}>
              <li>📅 Gestion des réunions</li>
              <li>🔍 Recherche et filtrage</li>
              <li>⚡ Interface moderne et réactive</li>
              <li>🔐 Sécurisé et fiable</li>
            </ul>
          </Card>
        </Col>
      </Row>

      <Card style={{ marginTop: '32px', background: '#f0f2f5' }}>
        <Title level={3}>Technologies utilisées</Title>
        <Row gutter={[16, 16]}>
          <Col span={12}>
            <strong>Frontend:</strong>
            <ul>
              <li>React 18 + TypeScript</li>
              <li>Vite</li>
              <li>Ant Design</li>
              <li>React Router v6</li>
              <li>Jotai + TanStack Query</li>
            </ul>
          </Col>
          <Col span={12}>
            <strong>Backend:</strong>
            <ul>
              <li>Spring Boot 3.x</li>
              <li>Spring Data JPA</li>
              <li>Base de données H2</li>
              <li>Maven</li>
            </ul>
          </Col>
        </Row>
      </Card>
    </div>
  );
}

export default Home;

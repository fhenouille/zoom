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
          Gérez l'assistance simplement et efficacement
        </Paragraph>
      </div>

      <Row gutter={[24, 24]}>
        <Col xs={24} md={12}>
          <Card
            hoverable
            title="Affichage des réunions"
            extra={<CalendarOutlined />}
            style={{ height: '100%' }}
          >
            <Paragraph>
              Consultez la liste de vos réunions. Visualisez les horaires et gérez votre assistance.
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
              <li>📅 Affichage des réunions</li>
              <li>🔍 Recherche et filtrage</li>
              <li>⚡ Comptage</li>
            </ul>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

export default Home;

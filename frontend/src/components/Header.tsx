import { CalendarOutlined, HomeOutlined } from '@ant-design/icons';
import { Layout, Menu } from 'antd';
import { useLocation, useNavigate } from 'react-router-dom';

const { Header: AntHeader } = Layout;

function Header() {
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: 'Accueil',
    },
    {
      key: '/meetings',
      icon: <CalendarOutlined />,
      label: 'Réunions',
    },
  ];

  return (
    <AntHeader style={{ display: 'flex', alignItems: 'center' }}>
      <div
        style={{
          color: 'white',
          fontSize: '20px',
          fontWeight: 'bold',
          marginRight: '50px',
        }}
      >
        Zoom Meetings
      </div>
      <Menu
        theme="dark"
        mode="horizontal"
        selectedKeys={[location.pathname]}
        items={menuItems}
        onClick={({ key }) => navigate(key)}
        style={{ flex: 1, minWidth: 0 }}
      />
    </AntHeader>
  );
}

export default Header;

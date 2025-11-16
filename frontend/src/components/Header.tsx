import { useAuth } from '@/contexts/AuthContext';
import { CalendarOutlined, HomeOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons';
import { Avatar, Dropdown, Layout, Menu, Space } from 'antd';
import { useLocation, useNavigate } from 'react-router-dom';

const { Header: AntHeader } = Layout;

function Header() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, user, logout } = useAuth();

  // v3: Vite define() for environment variables
  const apiUrl =
    typeof __API_BASE_URL__ !== 'undefined'
      ? __API_BASE_URL__
      : import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
  console.log('🎯 Header - API from Vite define:', apiUrl);

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

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const userMenuItems = [
    {
      key: 'user',
      icon: <UserOutlined />,
      label: user?.username || 'Utilisateur',
      disabled: true,
    } as const,
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Déconnexion',
      onClick: handleLogout,
    } as const,
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
      {isAuthenticated && (
        <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
          <Space style={{ cursor: 'pointer', marginLeft: 16 }}>
            <Avatar icon={<UserOutlined />} />
          </Space>
        </Dropdown>
      )}
    </AntHeader>
  );
}

export default Header;

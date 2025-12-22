import { useAuth } from '@/contexts/AuthContext';
import {
  CalendarOutlined,
  HomeOutlined,
  LogoutOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Avatar, Dropdown, Layout, Menu, Space } from 'antd';
import { useLocation, useNavigate } from 'react-router-dom';

const { Header: AntHeader } = Layout;

function Header() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, user, logout } = useAuth();

  // Production mode detection
  console.log('🔧 Header - Production Mode:', !import.meta.env.DEV);

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
    {
      key: '/guide',
      label: '📚 Guide',
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
      key: 'role',
      label: `Rôle: ${user?.role || 'USER'}`,
      disabled: true,
    } as const,
    {
      type: 'divider' as const,
    },
    // Afficher l'option Admin uniquement si l'utilisateur a le rôle ADMIN
    ...(user?.role === 'ADMIN'
      ? [
          {
            key: 'admin',
            icon: <TeamOutlined />,
            label: 'Gestion des Utilisateurs',
            onClick: () => navigate('/admin'),
          } as const,
          {
            type: 'divider' as const,
          },
        ]
      : []),
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

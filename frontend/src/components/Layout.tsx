import { Layout as AntLayout } from 'antd';
import { ReactNode } from 'react';
import Footer from './Footer';
import Header from './Header';

const { Content } = AntLayout;

interface LayoutProps {
  children: ReactNode;
}

function Layout({ children }: LayoutProps) {
  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Header />
      <Content style={{ padding: '24px 50px' }}>{children}</Content>
      <Footer />
    </AntLayout>
  );
}

export default Layout;

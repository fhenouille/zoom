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
      <Content className="main-content">{children}</Content>
      <Footer />
    </AntLayout>
  );
}

export default Layout;

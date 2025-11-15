import { Layout } from 'antd';

const { Footer: AntFooter } = Layout;

function Footer() {
  const currentYear = new Date().getFullYear();

  return <AntFooter style={{ textAlign: 'center' }}>Zoom Meetings ©{currentYear}</AntFooter>;
}

export default Footer;

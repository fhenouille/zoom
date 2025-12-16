import { Layout } from 'antd';

const { Footer: AntFooter } = Layout;

function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <AntFooter style={{ textAlign: 'center' }}>
      Zoom Meetings ©{currentYear} - v1.0.1 (Unraid)
    </AntFooter>
  );
}

export default Footer;

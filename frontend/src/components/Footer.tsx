import { Layout } from 'antd';

const { Footer: AntFooter } = Layout;

function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <AntFooter style={{ textAlign: 'center' }}>
      Zoom Meetings ©{currentYear} - v2.0.0 (Unraid)
    </AntFooter>
  );
}

export default Footer;

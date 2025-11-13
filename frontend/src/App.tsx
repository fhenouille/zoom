import { Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import Home from './pages/Home';
import Meetings from './pages/Meetings';

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/meetings" element={<Meetings />} />
      </Routes>
    </Layout>
  );
}

export default App;

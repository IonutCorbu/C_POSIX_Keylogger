import React, { useState } from 'react';
import api from '../api';

const SendEmail = () => {
  const [to, setTo] = useState('');
  const [subject, setSubject] = useState('');
  const [body, setBody] = useState('');
  const [status, setStatus] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/v1/email/send', { to, subject, body });
      setStatus('Email sent successfully!');
      setTo('');
      setSubject('');
      setBody('');
    } catch (error) {
      console.error(error);
      setStatus('Failed to send email.');
    }
  };

  return (
    <div className="container mt-4">
      <h2>Send Email</h2>
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label className="form-label">To:</label>
          <input type="email" className="form-control" value={to} onChange={e => setTo(e.target.value)} required />
        </div>
        <div className="mb-3">
          <label className="form-label">Subject:</label>
          <input type="text" className="form-control" value={subject} onChange={e => setSubject(e.target.value)} required />
        </div>
        <div className="mb-3">
          <label className="form-label">Body:</label>
          <textarea className="form-control" rows="5" value={body} onChange={e => setBody(e.target.value)} required />
        </div>
        <button type="submit" className="btn btn-primary">Send</button>
        {status && <p className="mt-3">{status}</p>}
      </form>
    </div>
  );
};

export default SendEmail;

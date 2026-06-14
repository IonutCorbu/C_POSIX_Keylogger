import React, { useEffect, useState } from 'react';
import { useParams, Link, useLocation } from 'react-router-dom';
import api from '../api';

const HeartbeatAnalyse = () => {
  const { id } = useParams(); 
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const type = searchParams.get('type');
  const endpoint = type === 'ollama' ? `/v1/heartbeat/analyze_ollama/${id}` : `/v1/heartbeat/analyze/${id}`;

  const [loading, setLoading] = useState(true);
  const [words, setWords] = useState([]);

  useEffect(() => {
    setLoading(true);
    api.get(endpoint)
      .then(res => {
        setWords(res.data.words || []);
      })
      .catch(err => console.error('Error fetching analysis:', err))
      .finally(() => setLoading(false));
  }, [id, endpoint]);

  if (loading) return <p className="mt-4">Loading...</p>;

  return (
    <div className="container mt-4">
      <h2>{type === 'ollama' ? 'Local LLM Password Prediction' : 'Heartbeat Analysis'}</h2>
      <p><strong>Heartbeat ID:</strong> {id}</p>

      <h3>Possible passwords</h3>

      {words.length === 0 ? (
        <div className="alert alert-info">
          no passwords detected
        </div>
      ) : (
        <table className="table table-striped table-bordered">
          <thead className="table-dark">
            <tr>
              <th>Passwords</th>
            </tr>
          </thead>
          <tbody>
            {words.map((w, idx) => (
              <tr key={idx}>
                <td>{w}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <Link to={-1} className="btn btn-secondary">
        Back
      </Link>
    </div>
  );
};

export default HeartbeatAnalyse;

import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import DataTable from 'react-data-table-component';
import api from '../api';

const Dashboard = () => {
  const [victims, setVictims] = useState([]);

  useEffect(() => {
    api.get('/v1/victims')
      .then(res => setVictims(res.data))
      .catch(err => console.error(err));
  }, []);

  const columns = [
    {
      name: 'ID',
      selector: row => row.id,
      sortable: true,
      width: '100px'
    },
    {
      name: 'IP Address',
      cell: row => <Link to={`/victim/${row.id}`}>{row.ipAddress}</Link>,
      sortable: true
    }
  ];

  return (
    <div className="container">
      <h2 className="mb-4">Victim List</h2>
      <DataTable
        columns={columns}
        data={victims}
        pagination
        highlightOnHover
        striped
      />
    </div>
  );
};

export default Dashboard;

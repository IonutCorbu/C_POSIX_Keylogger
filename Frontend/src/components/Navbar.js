import React from 'react';
import { Link } from 'react-router-dom';

const Navbar = () => (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
    <div className="container-fluid">
      <a className="navbar-brand" href="/">Keylogger Dashboard</a>
      <div className="collapse navbar-collapse">
        <ul className="navbar-nav me-auto mb-2 mb-lg-0">
          <li className="nav-item">
            <a className="nav-link" href="/">Dashboard</a>
          </li>
          <li className="nav-item">
            <a className="nav-link" href="/send-email">Send Email</a>
          </li>
        </ul>
      </div>
    </div>
  </nav>
  
);

export default Navbar;
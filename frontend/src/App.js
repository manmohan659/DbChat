import React, { useState, useEffect } from 'react';
import './App.css';
import ChatComponent from './ChatComponent';

function App() {
  const [connectionStatus, setConnectionStatus] = useState('');
  const [dbConfig, setDbConfig] = useState({
    host: 'localhost',
    port: '3306',
    username: 'root',
    password: '',
    database: 'testdb'
  });

  useEffect(() => {
    testBackendConnection();
  }, []);

  const testBackendConnection = async () => {
    try {
      const response = await fetch('/api/database/test');
      if (response.ok) {
        console.log('Backend is reachable');
      } else {
        console.error('Failed to reach backend');
      }
    } catch (error) {
      console.error('Error testing backend connection:', error);
    }
  };

  const handleInputChange = (e) => {
    setDbConfig({ ...dbConfig, [e.target.name]: e.target.value });
  };

  const handleConnect = async () => {
    try {
      const sessionId = localStorage.getItem('sessionId') || 'session-' + Date.now();
      localStorage.setItem('sessionId', sessionId);

      const response = await fetch('/api/database/connect', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'X-Session-ID': sessionId
        },
        body: JSON.stringify(dbConfig)
      });
      
      if (response.ok) {
        setConnectionStatus('success');
      } else {
        const errorText = await response.text();
        console.error('Connection failed:', errorText);
        setConnectionStatus('error');
      }
    } catch (error) {
      console.error('Connection error:', error);
      setConnectionStatus('error');
    }
  };

  return (
    <div className="app-container">
      <div className="sidebar">
        <div className="sidebar-content">
          <h2>Database Connection</h2>
          <p>Connect to your MySQL database</p>
          <div className="form-group">
            <label>Host</label>
            <input type="text" name="host" value={dbConfig.host} onChange={handleInputChange} />
          </div>
          <div className="form-group">
            <label>Port</label>
            <input type="text" name="port" value={dbConfig.port} onChange={handleInputChange} />
          </div>
          <div className="form-group">
            <label>Username</label>
            <input type="text" name="username" value={dbConfig.username} onChange={handleInputChange} />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" name="password" value={dbConfig.password} onChange={handleInputChange} />
          </div>
          <div className="form-group">
            <label>Database</label>
            <input type="text" name="database" value={dbConfig.database} onChange={handleInputChange} />
          </div>
          <button 
            className={`connect-button ${connectionStatus}`} 
            onClick={handleConnect}
          >
            {connectionStatus === 'success' ? 'Connected' : 'Connect'}
          </button>
        </div>
      </div>
      <main className="main-content">
        <h1>Chat with MySQL</h1>
        <ChatComponent connectionStatus={connectionStatus} />
      </main>
    </div>
  );
}

export default App;
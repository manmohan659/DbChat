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
      console.log('Attempting to connect with config:', dbConfig);
      const response = await fetch('/api/database/connect', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dbConfig)
      });
      const responseText = await response.text();
      console.log('Response:', responseText);
      if (response.ok) {
        setConnectionStatus('success');
      } else {
        console.error('Connection failed:', responseText);
        setConnectionStatus('error');
      }
    } catch (error) {
      console.error('Connection error:', error);
      setConnectionStatus('error');
    }
  };

  return (
    <div className="App">
      <div className="sidebar">
        <h2>Settings</h2>
        <p>Bored!Talk to your database.</p>
        <input type="text" name="host" placeholder="Host" value={dbConfig.host} onChange={handleInputChange} />
        <input type="text" name="port" placeholder="Port" value={dbConfig.port} onChange={handleInputChange} />
        <input type="text" name="username" placeholder="User" value={dbConfig.username} onChange={handleInputChange} />
        <input type="password" name="password" placeholder="Password" value={dbConfig.password} onChange={handleInputChange} />
        <input type="text" name="database" placeholder="Database" value={dbConfig.database} onChange={handleInputChange} />
        <button 
          className={`connect-button ${connectionStatus}`} 
          onClick={handleConnect}
        >
          Connect
        </button>
      </div>
      <main className="main-content">
        <h1>Chat with MySQL</h1>
        <ChatComponent connectionStatus={connectionStatus} />
      </main>
    </div>
  );
}

export default App;
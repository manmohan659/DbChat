import React, { useState, useEffect } from 'react';
import './App.css';
import ChatComponent from './ChatComponent';

function App() {
  const [connectionStatus, setConnectionStatus] = useState('');
  const [dbConfig, setDbConfig] = useState({
    host: '',
    port: '',
    username: '',
    password: '',
    database: '',
  });
  const [sessions, setSessions] = useState([]);
  const [activeSessionId, setActiveSessionId] = useState(localStorage.getItem('sessionId') || null);
  const [showConfirmation, setShowConfirmation] = useState(false);

  useEffect(() => {
    if (!activeSessionId) {
      const newSessionId = 'session-' + Date.now();
      setActiveSessionId(newSessionId);
      localStorage.setItem('sessionId', newSessionId);
    }
  }, [activeSessionId]);

  useEffect(() => {
    testBackendConnection();
    fetchSessions();
  }, []);

  useEffect(() => {
    if (activeSessionId) {
      localStorage.setItem('sessionId', activeSessionId);
    }
  }, [activeSessionId]);

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

  const fetchSessions = async () => {
    try {
      const response = await fetch('/api/sessions');
      if (response.ok) {
        const data = await response.json();
        setSessions(data);
        if (!activeSessionId && data.length > 0) {
          setActiveSessionId(data[0].sessionId);
          localStorage.setItem('sessionId', data[0].sessionId);
        }
      } else {
        console.error('Failed to fetch sessions');
      }
    } catch (error) {
      console.error('Error fetching sessions:', error);
    }
  };

  const handleInputChange = (e) => {
    setDbConfig({ ...dbConfig, [e.target.name]: e.target.value });
  };

  const handleConnect = async () => {
    try {
      const sessionId = activeSessionId || 'session-' + Date.now();
      setActiveSessionId(sessionId);
      localStorage.setItem('sessionId', sessionId);

      console.log('Connecting with session ID:', sessionId);
      const response = await fetch('/api/database/connect', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Session-ID': sessionId,
        },
        body: JSON.stringify(dbConfig),
      });

      if (response.ok) {
        setConnectionStatus('success');
        console.log('Connection successful');
        await fetchSessions(); // Update the sessions list
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

  const handleNewSession = () => {
    const newSessionId = 'session-' + Date.now();
    setActiveSessionId(newSessionId);
    localStorage.setItem('sessionId', newSessionId);
    setConnectionStatus('');
    setDbConfig({
      host: '',
      port: '',
      username: '',
      password: '',
      database: '',
    });
  };
  const clearSessions = async () => {
    try {
      const response = await fetch('/api/sessions/clear', {
        method: 'DELETE',
      });
      if (response.ok) {
        setSessions([]);
        setActiveSessionId(null);
        localStorage.removeItem('sessionId');
        setShowConfirmation(false);
        setConnectionStatus('');
        setDbConfig({
          host: 'localhost',
          port: '3306',
          username: 'root',
          password: '',
          database: 'testdb',
        });
      } else {
        console.error('Failed to clear sessions');
      }
    } catch (error) {
      console.error('Error clearing sessions:', error);
    }
  };

  const handleSessionChange = (e) => {
    const sessionId = e.target.value;
    setActiveSessionId(sessionId);
    localStorage.setItem('sessionId', sessionId);
    setConnectionStatus('');
  };

  return (
      <div className="app-container">
        <div className="sidebar">
          {/* Sessions Area */}
          <div className="sidebar-content session-card">
            <h3>Sessions</h3>
            <div className="session-buttons">
              <button className="session-btn" onClick={handleNewSession}>New Session</button>
              <button className="session-btn" onClick={() => setShowConfirmation(true)} disabled={!sessions.length}>Clear Sessions</button>
            </div>
            <div className="form-group">
              <label>Select Session</label>
              <select
                  className="session-select"
                  value={activeSessionId || ''}
                  onChange={handleSessionChange}
              >
                <option value="" disabled>Select a session</option>
                {sessions.map((session) => (
                    <option key={session.sessionId} value={session.sessionId}>{session.sessionId}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Database Connection Area */}
          <div className="sidebar-content database-connection-card">
            <h3>Database Connection</h3>
            <p>Connect to your database</p>
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
            <button className={`connect-button ${connectionStatus}`} onClick={handleConnect} disabled={!activeSessionId}>
              {connectionStatus === 'success' ? 'Connected' : 'Connect'}
            </button>
          </div>
        </div>

        {/* Main Content Area */}
        <main className="main-content">
          <h1>Chat with Database</h1>
          <ChatComponent
              connectionStatus={connectionStatus}
              activeSessionId={activeSessionId}
              setActiveSessionId={setActiveSessionId} // Add this line
          />
        </main>



        {showConfirmation && (
            <div className="popup-overlay">
              <div className="popup">
                <p>You are about to clear all sessions. Click "Yes" if you wish to delete all previous messages.</p>
                <button onClick={clearSessions}>Yes</button>
                <button onClick={() => setShowConfirmation(false)}>No</button>
              </div>
            </div>
        )}
      </div>
  );
}

export default App;
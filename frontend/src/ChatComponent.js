
// Import Material-UI components
import { DataGrid } from '@mui/x-data-grid';
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { CircularProgress } from '@mui/material';
import './App.css';


function ChatComponent({ connectionStatus, activeSessionId, setActiveSessionId }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const chatWindowRef = useRef(null);
  const [schemaUploaded, setSchemaUploaded] = useState(false);
  const [showPopup, setShowPopup] = useState(false);

  // Load chat history when activeSessionId or connectionStatus changes
  useEffect(() => {
    const initializeChat = async () => {
      if (activeSessionId) {
        try {
          const response = await fetch('/api/query/history', {
            headers: {
              'X-Session-ID': activeSessionId,
            },
          });

          if (response.ok) {
            const history = await response.json();
            const formattedHistory = history.map((msg) => ({
              text: msg.message,
              isUser: msg.isUser,
              type: msg.type,
              sql: msg.generatedSql,
              tableData: msg.tableData,
            }));

            setMessages(formattedHistory);
          } else {
            setMessages([]);
          }
        } catch (error) {
          console.error('Error loading chat history:', error);
          setMessages([]);
        }
      } else {
        setMessages([]);
      }
    };

    initializeChat();
  }, [activeSessionId]);

  // Scroll to bottom when messages change
  const scrollToBottom = useCallback(() => {
    if (messagesEndRef.current) {
      const chatWindow = chatWindowRef.current;
      const isScrolledToBottom =
          chatWindow.scrollHeight - chatWindow.scrollTop <=
          chatWindow.clientHeight + 100;

      if (isScrolledToBottom) {
        messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
      }
    }
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  const fileInputRef = useRef(null);

  // **Handler to Simulate Click on Hidden File Input**
  const handleFileButtonClick = () => {
    if (fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  // **Handler for File Selection and Upload**
  const handleFileChange = async (event) => {
    const file = event.target.files[0];
    let sessionId = activeSessionId;

    if (!sessionId) {
      // Create a new session ID if none exists
      sessionId = 'session-' + Date.now();
      setActiveSessionId(sessionId);
      localStorage.setItem('sessionId', sessionId);
    }

    if (file) {
      const formData = new FormData();
      formData.append('file', file);
      try {
        const response = await fetch('/api/schema/upload', {
          method: 'POST',
          headers: {
            'X-Session-ID': sessionId,
          },
          body: formData,
        });
        if (response.ok) {
          setSchemaUploaded(true);  // Schema uploaded successfully
          const successMessage = {
            text: 'Schema uploaded successfully!',
            isUser: false,
            type: 'SYSTEM',
          };
          setMessages((prev) => [...prev, successMessage]);
        } else {
          const errorText = await response.text();
          const errorMessage = {
            text: `Schema upload failed: ${errorText}`,
            isUser: false,
            type: 'ERROR',
          };
          setMessages((prev) => [...prev, errorMessage]);
        }
      } catch (error) {
        const errorMessage = {
          text: `Error uploading schema: ${error.message}`,
          isUser: false,
          type: 'ERROR',
        };
        setMessages((prev) => [...prev, errorMessage]);
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMessage = { text: input, isUser: true, type: 'QUERY' };
    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);

    try {
      const sessionId = activeSessionId;
      const response = await fetch('/api/query/nl-to-sql', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Session-ID': sessionId,
        },
        body: JSON.stringify({ query: input }),
      });

      const data = await response.json();

      if (response.ok) {
        const aiMessage = {
          text: data.message,
          isUser: false,
          type: data.type || 'RESPONSE',
          sql: data.generatedSql,
          tableData: data.tableData, // Include tableData
        };
        setMessages((prev) => [...prev, aiMessage]);
      } else {
        throw new Error(data.message || 'Failed to process query');
      }
    } catch (error) {
      console.error('Error:', error);
      const errorMessage = {
        text:
            error.message || 'Sorry, there was an error processing your request.',
        isUser: false,
        type: 'ERROR',
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const getMessageClassName = useCallback((message) => {
    let baseClass = 'message';
    if (message.isUser) {
      baseClass += ' user-message';
    } else {
      baseClass += ' ai-message';
      if (message.type === 'ERROR') baseClass += ' error-message';
      if (message.type === 'SYSTEM') baseClass += ' system-message';
    }
    return baseClass;
  }, []);

  // Function to render the table using Material-UI Data Grid
  const renderTable = (tableData) => {
    if (!tableData || !tableData.headers || !tableData.rows) return null;

    // Prepare columns for Data Grid
    const columns = tableData.headers.map((header, index) => ({
      field: header,
      headerName: header,
      width: 150,
    }));

    // Prepare rows for Data Grid
    const rows = tableData.rows.map((row, index) => {
      const rowData = { id: index }; // Each row must have a unique id
      tableData.headers.forEach((header, idx) => {
        rowData[header] = row[idx];
      });
      return rowData;
    });

    return (
        <div style={{ height: 400, width: '100%', marginTop: '16px' }}>
          <DataGrid
              rows={rows}
              columns={columns}
              pageSize={10}
              rowsPerPageOptions={[10, 20, 50]}
              sx={{
                backgroundColor: '#1e1e1e',
                color: '#fff',
                '& .MuiDataGrid-cell': {
                  borderBottom: '1px solid #333',
                },
                '& .MuiDataGrid-columnHeaders': {
                  backgroundColor: '#252526',
                  borderBottom: '1px solid #333',
                },
                '& .MuiDataGrid-footerContainer': {
                  backgroundColor: '#252526',
                  borderTop: '1px solid #333',
                },
                '& .MuiTablePagination-root': {
                  color: '#fff',
                },
                '& .MuiIconButton-root': {
                  color: '#fff',
                },
                '& .MuiCheckbox-root': {
                  color: '#fff',
                },
              }}
          />
        </div>
    );
  };

  return (
      <div className="chat-container">
        <div className="chat-window" ref={chatWindowRef}>
          {messages.map((message, index) => (
              <div key={index} className={getMessageClassName(message)}>
            <span className="message-icon">
              {message.isUser
                  ? '👤'
                  : message.type === 'ERROR'
                      ? '⚠️'
                      : '🤖'}
            </span>
                <div className="message-content">
                  <div className="message-text">{message.text}</div>
                  {message.sql && (
                      <div className="sql-query">
                        <span className="sql-label">Generated SQL:</span>
                        <code>{message.sql}</code>
                      </div>
                  )}
                  {message.tableData && (
                      <div className="table-container">
                        {renderTable(message.tableData)}
                      </div>
                  )}
                </div>
              </div>
          ))}
          {isLoading && (
              <div className="loading-indicator">
                <CircularProgress size={24} color="inherit"/>
              </div>
          )}
          <div ref={messagesEndRef}/>
        </div>
        <form onSubmit={handleSubmit} className="input-area">
          <button
              type="button"
              className="upload-button"
              onClick={handleFileButtonClick}
              disabled={isLoading}
          >
            📎
          </button>
          <input
              ref={fileInputRef}
              type="file"
              style={{display: 'none'}}
              onChange={handleFileChange}
              accept=".sql,.txt"
          />
          <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onClick={() => {
                if (!(connectionStatus === 'success' || schemaUploaded)) {
                  setShowPopup(true);
                  setTimeout(() => setShowPopup(false), 4000);  // Hide after 4 seconds
                }
              }}
              placeholder={
                connectionStatus === 'success' || schemaUploaded
                    ? 'Type your query...'
                    : 'Connect to database or upload schema first...'
              }
              disabled={!(connectionStatus === 'success' || schemaUploaded) || isLoading}
          />

          <button
              type="submit"
              disabled={
                  !(connectionStatus === 'success' || schemaUploaded) ||
                  isLoading ||
                  !input.trim()
              }
          >
            {isLoading ? 'Processing...' : 'Send'}
          </button>
        </form>
        {showPopup && (
            <div className="floating-popup">
              <p>Please connect to a database or upload a schema to proceed.</p>
            </div>
        )}
      </div>
  );
}

export default ChatComponent;
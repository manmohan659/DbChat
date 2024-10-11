# SQL Assistant Chatbot

This project is a SQL assistant chatbot built with **Spring Boot** and **MySQL**. It allows users to interact with a chatbot that generates and executes SQL queries (only `SELECT` queries) on a database. The project also integrates with an LLM (e.g., OpenAI's GPT) to generate SQL queries based on natural language inputs.

## Features

- **Natural Language to SQL Conversion**: Converts user queries into `SELECT` SQL statements using LLM.
- **Execute SQL Queries**: Executes `SELECT` queries on the connected MySQL database.
- **Database Schema Parsing**: Fetches and processes the database schema for use in SQL query generation.
- **Result Display**: Fetches results from the database and generates a natural language response.
- **Case-insensitive SQL Query Generation**: Only generates and executes `SELECT` queries, ignoring other commands like `UPDATE`, `DELETE`, etc.

## Technologies Used

- **Java (Spring Boot)**: Backend application framework.
- **MySQL**: Database for storing and retrieving data.
- **LangChain4j**: Integration for handling LLM interactions.
- **React**: Frontend for user interaction with the chatbot.
- **OpenAI GPT-4**: LLM used for converting natural language to SQL.
- **Regex**: Used for post-processing the LLM response to extract valid SQL queries.

## Setup Instructions

### Prerequisites

- **Java 17** or higher installed.
- **MySQL** database installed and running.
- **Node.js** and **npm** installed for running the React frontend.
- **OpenAI API Key** for LLM integration.

### 1. Clone the Repository

```bash
git clone https://github.com/your-repo/sql-assistant-chatbot.git
cd sql-assistant-chatbot

-- database-per-service (see ARCHITECTURE.md §6)
CREATE DATABASE surplus_db;
CREATE DATABASE need_db;
CREATE DATABASE coordination_db;

\c surplus_db
CREATE EXTENSION IF NOT EXISTS postgis;

\c need_db
CREATE EXTENSION IF NOT EXISTS postgis;

\c coordination_db
CREATE EXTENSION IF NOT EXISTS postgis;

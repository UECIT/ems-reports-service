CREATE DATABASE IF NOT EXISTS cdss_reports;

USE cdss_reports;

CREATE TABLE IF NOT EXISTS cdss_reports.counter (
  name              VARCHAR(255),
  value BIGINT UNSIGNED,
  PRIMARY KEY (name)
);

-- ===================================================================================
-- FICHERO: src\database\esquema.sql
-- DESCRIPCION: Esquema de la base de datos (tabla CODIGO)
-- COPYRIGHT: (c) 2026 Raúl Sanabria Rodado
-- FECHA: 2026-10-15
-- AUTOR: Raúl Sanabria Rodado
-- APORTACION: DDL de la tabla CODIGO
-- ===================================================================================
-- ---------------------------------------------------------------
-- ---------------------------------------------------------------
--
-- Esquema de la base de datos biometria
--
-- crea la tabla CODIGO que almacena el codigo recibido del beacon
--
-- ---------------------------------------------------------------
-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS CODIGO (
  id     INT      NOT NULL AUTO_INCREMENT,
  valor  INT      NOT NULL,
  fecha  DATETIME NOT NULL,
  PRIMARY KEY (id)
);

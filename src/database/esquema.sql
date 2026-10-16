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

-- ---------------------------------------------------------------
-- creacion de la base de datos y de la tabla CODIGO
--
-- sin el CREATE DATABASE el fichero no sirve para montar el
-- proyecto en otra maquina: hay que crearla a mano antes
-- ---------------------------------------------------------------

CREATE DATABASE IF NOT EXISTS biometria
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE biometria;

CREATE TABLE IF NOT EXISTS CODIGO (
  id     INT      NOT NULL AUTO_INCREMENT,
  valor  INT      NOT NULL,
  fecha  DATETIME NOT NULL,
  PRIMARY KEY (id)
);

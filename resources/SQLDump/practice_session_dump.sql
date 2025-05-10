-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: practice_session
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `practice_session`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `practice_session` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `practice_session`;

--
-- Table structure for table `active_practice_session`
--

DROP TABLE IF EXISTS `active_practice_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `active_practice_session` (
  `session_token` varchar(255) NOT NULL,
  `user_id` int NOT NULL,
  `practice_session_id` bigint NOT NULL AUTO_INCREMENT,
  `level` int NOT NULL,
  `math_category` varchar(255) DEFAULT NULL,
  `category_settings_id` bigint DEFAULT NULL,
  `solution_detail` varchar(255) DEFAULT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`practice_session_id`),
  UNIQUE KEY `session_token` (`session_token`),
  KEY `active_practice_session_category_fk` (`category_settings_id`),
  KEY `active_practice_session_user_fk` (`user_id`),
  CONSTRAINT `active_practice_session_category_fk` FOREIGN KEY (`category_settings_id`) REFERENCES `category_settings` (`id`) ON DELETE SET NULL,
  CONSTRAINT `active_practice_session_user_fk` FOREIGN KEY (`user_id`) REFERENCES `finalproject`.`users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `arithmetic_settings`
--

DROP TABLE IF EXISTS `arithmetic_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `arithmetic_settings` (
  `id` bigint NOT NULL,
  `practice_session_id` bigint NOT NULL,
  `solution_detail` text,
  `number_types` text,
  `question_types` text,
  PRIMARY KEY (`id`),
  KEY `practice_session_id` (`practice_session_id`),
  CONSTRAINT `arithmetic_settings_ibfk_1` FOREIGN KEY (`id`) REFERENCES `category_settings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `category_settings`
--

DROP TABLE IF EXISTS `category_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `practice_session_id` bigint DEFAULT NULL,
  `math_category` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `geometry_settings`
--

DROP TABLE IF EXISTS `geometry_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `geometry_settings` (
  `id` bigint NOT NULL,
  `practice_session_id` bigint NOT NULL,
  `solution_detail` text,
  `shape_types` text,
  `shapes` text,
  `question_type_types` text,
  PRIMARY KEY (`id`),
  KEY `practice_session_id` (`practice_session_id`),
  CONSTRAINT `geometry_settings_ibfk_1` FOREIGN KEY (`id`) REFERENCES `category_settings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `math_problem`
--

DROP TABLE IF EXISTS `math_problem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `math_problem` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `practice_session_id` bigint NOT NULL,
  `problem_level` int NOT NULL,
  `problem_content` text,
  `solution_content` text,
  `fails` int NOT NULL,
  `requested_solution` tinyint(1) NOT NULL DEFAULT '0',
  `requested_hint` tinyint(1) NOT NULL,
  `time_taken` bigint NOT NULL,
  `created_at` timestamp NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `practice_session`
--

DROP TABLE IF EXISTS `practice_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `practice_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `level` int NOT NULL,
  `math_category` varchar(255) DEFAULT NULL,
  `category_settings_id` bigint DEFAULT NULL,
  `solution_detail` varchar(255) DEFAULT NULL,
  `date` timestamp NOT NULL,
  `time_taken` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `practice_session_ibfk_1` (`category_settings_id`),
  CONSTRAINT `practice_session_ibfk_1` FOREIGN KEY (`category_settings_id`) REFERENCES `category_settings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `statistics`
--

DROP TABLE IF EXISTS `statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `statistics` (
  `statistics_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `geometry_sessions` int NOT NULL DEFAULT '0',
  `geometry_problems` int NOT NULL DEFAULT '0',
  `geometry_fails` int NOT NULL DEFAULT '0',
  `geometry_hints_used` int NOT NULL DEFAULT '0',
  `geometry_solutions_requested` int NOT NULL DEFAULT '0',
  `geometry_time_spent` bigint NOT NULL DEFAULT '0',
  `arithmetics_sessions` int NOT NULL DEFAULT '0',
  `arithmetics_problems` int NOT NULL DEFAULT '0',
  `arithmetics_fails` int NOT NULL DEFAULT '0',
  `arithmetics_hints_used` int NOT NULL DEFAULT '0',
  `arithmetics_solutions_requested` int NOT NULL DEFAULT '0',
  `arithmetics_time_spent` bigint NOT NULL DEFAULT '0',
  `last_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`statistics_id`),
  KEY `statistics_user_fk` (`user_id`),
  CONSTRAINT `statistics_user_fk` FOREIGN KEY (`user_id`) REFERENCES `finalproject`.`users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'practice_session'
--

--
-- Dumping routines for database 'practice_session'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-03-30 15:25:09

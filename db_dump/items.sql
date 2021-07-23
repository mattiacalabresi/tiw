-- MySQL dump 10.13  Distrib 8.0.23, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: market_db
-- ------------------------------------------------------
-- Server version	8.0.23

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `items`
--

DROP TABLE IF EXISTS `items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(45) NOT NULL,
  `name` varchar(45) NOT NULL,
  `description` varchar(100) NOT NULL,
  `image_name` varchar(23) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `image_name_UNIQUE` (`image_name`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `items`
--

LOCK TABLES `items` WRITE;
/*!40000 ALTER TABLE `items` DISABLE KEYS */;
INSERT INTO `items` VALUES (38,'C8QVD93F','iPhone 11','64 GB, charger included, good conditions.','20210722_120259300.png'),(39,'CNPX-E470','Canon Printer','Model: Pixma E470, no cartridges included!!! Best deal!','20210722_120956310.jpg'),(40,'PGVQ-14i256-S','Asus Notebook','i5 processor, 256 GB SSD, all accessories are included. Barely used, original box, free shipping.','20210722_121508155.jpg'),(41,'SMSG-UW29K','Samsung Monitor','Ultra Wide! Power cord included. New never used!','20210722_122159192.jpg'),(42,'TFD72JT7Y','Gray iPad','Used iPad Air, 64GB, no pencil','20210722_122931523.jfif'),(43,'OBN22BK','Office Chair','Very comfortable, adjustable and tiltable. No original box, free shipping','20210722_123547138.jpg'),(44,'WT324766','Wooden Table','Only a few scratches, very robust','20210722_144541397.jpg'),(45,'83736525835','Sunglasses','Blue lens, no scratches, cover included, free shipping','20210722_145119546.jpg'),(46,'OPWTC2','Smartwatch','New, packed, never opened, 2 years warranty.','20210722_145650401.jpg'),(47,'4082C003','Canon Eos r6','Good camera, very durable, very good conditions. Free shipping.','20210722_150344346.png'),(48,'ISBN-978-3-16-148410-0','The Lord of the Rings','Collection books, extremely rare! Very good conditions','20210722_150812440.png'),(49,'123','Phone','new, free shipping','20210723_091324683.jpg');
/*!40000 ALTER TABLE `items` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-07-23 10:02:21

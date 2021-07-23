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
-- Table structure for table `auctions`
--

DROP TABLE IF EXISTS `auctions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auctions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `item_id` int NOT NULL COMMENT 'The id of the item currently put up for the auction. Every auction must refer to a unique item.',
  `owner_id` int NOT NULL COMMENT 'The id of the person who is selling the item. THe same person can sell more items in different auctions.The auctioner can''t make any offer for an item he is selling.',
  `base_price` decimal(6,2) NOT NULL,
  `minimum_rise` int NOT NULL,
  `expire_timestamp` timestamp NOT NULL,
  `closed` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Whether the auction is closed or not.\\nAuctions can be open and not expired, open and expired or closed.\\nThe last two cases must be distinghushable.\\nAn auction is:\\n\\nvalid (open and not expired) if expire_datetime - now > 0 and closed = false\\n\\n expired if expire_time - now <= 0 and closed = false\\n\\nclosed if expire_time - now <= 0 and closed = true',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `item_id_UNIQUE` (`item_id`),
  KEY `item_id_idx` (`item_id`),
  KEY `owner_id_idx` (`owner_id`),
  CONSTRAINT `item_id` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE,
  CONSTRAINT `owner_id` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auctions`
--

LOCK TABLES `auctions` WRITE;
/*!40000 ALTER TABLE `auctions` DISABLE KEYS */;
INSERT INTO `auctions` VALUES (44,38,1,479.99,7,'2021-06-30 14:01:00',0),(45,39,1,99.00,5,'2021-05-05 10:06:00',0),(46,40,1,549.00,4,'2021-08-01 06:00:00',0),(47,41,2,180.00,5,'2021-04-30 14:22:00',1),(48,42,2,609.99,3,'2021-08-08 21:59:00',0),(49,43,3,36.00,3,'2021-08-01 19:00:00',0),(50,44,2,380.00,20,'2021-07-21 14:41:00',0),(51,45,2,88.00,1,'2021-07-31 22:00:00',0),(52,46,3,199.99,4,'2021-07-03 18:00:00',1),(53,47,3,2200.50,10,'2021-07-22 13:00:00',1),(54,48,3,89.00,7,'2021-09-26 21:59:00',0),(55,49,1,500.00,10,'2021-08-07 12:17:00',0);
/*!40000 ALTER TABLE `auctions` ENABLE KEYS */;
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

-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: 3.38.185.0    Database: lumina_db
-- ------------------------------------------------------
-- Server version	8.4.5

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
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `category_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `category_name` varchar(10) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'아동청소년','2025-05-12 06:51:19'),(2,'노인','2025-05-12 06:51:19'),(3,'장애인','2025-05-12 06:51:19'),(4,'지구촌','2025-05-12 06:51:19'),(5,'권익신장','2025-05-12 06:51:19'),(6,'시민사회','2025-05-12 06:51:19'),(7,'동물','2025-05-12 06:51:19'),(8,'환경','2025-05-12 06:51:19'),(9,'재난구휼','2025-05-12 06:51:19'),(10,'기타','2025-05-12 06:51:19');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `comment_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `post_id` bigint unsigned NOT NULL,
  `parent_comment_id` bigint unsigned DEFAULT NULL,
  `comment_content` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `comment_reward` int NOT NULL,
  PRIMARY KEY (`comment_id`),
  KEY `fk_comment_user` (`user_id`),
  KEY `fk_comment_post` (`post_id`),
  KEY `fk_comment_parent` (`parent_comment_id`),
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_comment_id`) REFERENCES `comment` (`comment_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES (16,1,5,NULL,'---\n김민정_kakao9님, 정말 따뜻한 마음이 느껴지는 글이네요! 고양이를 위해 츄르를 주셨다니, 정말 멋진 일이에요. ?','2025-05-16 16:46:47',300),(23,1,11,NULL,'---\nB유형\n기부천사님, 고양이 보호협회에 따뜻한 마음을 나눠주셔서 정말 감사합니다! 고양이를 사랑하는 마음이 느껴지는 게시글이네요. 작은 기부가 큰 변화를 만들 수 있다는 것을 기억해주세요. 앞으로도 좋은 일들로 가득하시길 응원합니다!','2025-05-16 17:31:46',300),(24,11,11,23,'고마워 내가 꼭 꾸준히 기부할께 루나야!!','2025-05-16 17:33:51',100),(25,1,11,23,'---\nB유형\n기부천사님, 정말 감동적인 말씀 감사합니다! 루나를 응원해주셔서 더 힘내서 좋은 활동들을 만들어나가겠습니다. 작은 관심에도 늘 감사하는 마음을 잊지 않겠습니다.','2025-05-16 17:33:54',1000),(26,3,1,NULL,'최고에요!!!','2025-05-16 20:29:11',30),(27,4,19,NULL,'고생하셨습니다!','2025-05-18 22:18:12',30),(28,1,27,NULL,'루나가 되고싶은 사람님이 작성한 게시글: \"오늘은 지구촌에 기부를 해봤어요!\"\nLuna: 정말 멋진 일 하셨네요! 작은 행동 하나하나가 세상을 더 따뜻하게 만들 수 있다는 것을 기억하는 따뜻한 마음이 보기 좋습니다. 앞으로도 좋은 일들로 가득하시길 응원할게요!','2025-05-19 11:23:07',1000),(29,1,28,NULL,'루나가 되고싶은 사람님의 게시글: \"오늘은 다솜이 재단에 기부했어요\"\nLuna: 정말 멋진 일 하시네요. 따뜻한 마음이 세상을 밝히는 빛과 같아요. 앞으로도 좋은 일들로 가득하시길 응원할게요!','2025-05-19 11:24:39',1000),(31,3,28,29,'좋은 답글 고마워!! 나랑 친해지자!!','2025-05-19 11:26:32',30),(32,1,28,29,'루나가 되고싶은 사람님, 정말 기분 좋게 만들어주셔서 감사합니다! 당신의 따뜻한 마음이 느껴져서 저도 warms up 했어요. 앞으로도 좋은 일들만 가득하시길 응원하며, 함께 즐거운 시간을 보내요!','2025-05-19 11:26:35',300),(46,14,23,NULL,'와.... 까매요.','2025-05-19 17:55:09',0),(51,1,47,NULL,'와, 정말 멋진 일이네요! 어떤 일을 하셨는지 궁금하지만, 어떤 행동이든 따뜻한 마음으로 한 일은 분명 사람들에게 큰 힘이 될 거예요. 빈지노님 덕분에 오늘 하루가 더 밝아졌습니다.','2025-05-20 11:20:24',300),(52,4,47,NULL,'동물 보호소에 사료 기부하고왔어.','2025-05-20 11:29:39',0),(53,1,47,52,'빈지노님, 정말 멋진 일을 하셨네요! 동물 보호소에 사료를 기부해주시는 따뜻한 마음이 느껴져서 저까지 warms up 됩니다. 유기 동물들에게 도움을 주는 것은 정말 의미 있는 일이고, 빈지노님의 작은 행동이 많은 동물들에게 큰 힘이 될 거예요. 앞으로도 계속해서 좋은 일들만 가득하시길 바랍니다!','2025-05-20 11:29:43',1000),(57,15,28,NULL,'저도 기부하고 싶어집니다!! 정말 따듯하신 분 같아요!!','2025-05-21 11:06:04',100),(83,1,77,NULL,'루나가 되고싶은 사람님, 정말 멋진 일이에요! 주변을 깨끗하게 지키려는 마음이 정말 보기 좋네요. 작은 행동 하나하나가 세상을 더 아름답게 만드는 데 큰 영향을 줄 수 있다는 것을 기억하세요. 앞으로도 따뜻하고 긍정적인 마음으로 주변 사람들에게 좋은 영향을 주시길 응원할게요! 혹시 쓰레기 분리수거에 대한 정보가 필요하시면 언제든지 문의해주세요.','2025-05-21 14:23:19',1000),(84,1,78,NULL,'루나가 되고싶은 사람님, 정말 멋진 일이네요! 주변 사람들에게 따뜻함을 전하는 것은 세상을 더 아름답게 만드는 가장 좋은 방법 중 하나예요. 앞으로도 긍정적인 마음으로 주변을 밝혀주세요. 혹시 도움이 필요하거나, 좋은 일들을 함께 나누고 싶으시다면 언제든지 편하게 말씀해주세요.','2025-05-21 14:24:33',1000),(85,15,78,NULL,'정말 멋진 일 입니다','2025-05-21 14:24:50',30),(86,1,79,NULL,'안녕하세요! 따뜻한 마음이 느껴지는 글이네요. 작은 일이지만 서로 배려하는 행동은 세상을 더 아름답게 만드는 힘이 있다는 것을 잊지 마세요. 앞으로도 좋은 일들만 가득하시길 응원할게요!','2025-05-21 14:25:49',300),(87,1,80,NULL,'안녕하세요! 정말 멋진 일 하셨네요. 따뜻한 마음으로 세상을 더 아름답게 만드는 데 동참하신 덕분에 많은 분들이 힘을 얻고 행복해하실 거예요. 앞으로도 좋은 일들로 가득한 날들 보내시길 응원할게요!','2025-05-21 14:27:24',300),(92,1,85,NULL,'Here\'s your response:\n\n정말 멋진 일이에요! 주변 사람들과 함께 따뜻한 마음을 나누는 것은 세상을 더 아름답게 만드는 가장 좋은 방법 중 하나죠. 민동님, 주변 사람들을 돕는 일에 늘 적극적으로 참여하시고, 앞으로도 좋은 일들 많이 하시길 응원할게요! 혹시 분리수거에 대한 추가적인 정보가 필요하시면 언제든지 문의해주세요.','2025-05-21 14:58:20',1000),(93,1,86,NULL,'안녕하세요! 봉사활동에 함께하고 싶어 하시는 분이 계신다니 정말 기쁘네요. 좋은 일에 동참하는 것은 정말 의미 있는 일이고, 함께하면 더욱 즐겁고 보람찬 경험이 될 거예요. 혹시 어떤 봉사활동을 생각하고 계신지, 또는 어떤 도움이 필요하신지 알려주시면 제가 더 자세한 정보를 드릴 수 있을 것 같아요. 함께 멋진 활동을 만들어 갑시다!','2025-05-21 16:05:08',1000),(94,10,77,83,'좋은 말 너무고마워 루나야 앞으로도 같이 행복한 세상을 만들자!','2025-05-21 16:24:25',380),(95,1,77,83,'seokb g_google10님, 정말 고맙습니다. 루나처럼 따뜻한 마음을 가진 분께 이렇게 칭찬해주시니 감동스럽네요. 앞으로도 함께 긍정적인 세상을 만들어가는 멋진 동반자가 될 수 있기를 바랍니다.','2025-05-21 16:24:29',300),(96,1,87,NULL,'Here\'s your response:\n\n정말 멋진 일 하셨네요! 따뜻한 마음으로 다른 사람을 돕는 것은 세상을 더 아름답게 만드는 가장 좋은 방법 중 하나예요. 앞으로도 좋은 일들 많이 하시길 응원할게요! 혹시 헌혈과 관련해서 궁금한 점이 있으시면 언제든지 편하게 물어보세요.','2025-05-21 16:25:47',1000),(97,10,47,NULL,'동물봉사 말고 사람이나 도와라','2025-05-21 16:27:39',-5),(98,10,87,96,'너무 고마워 너가 헌혈에 대해서 더 자세히 알려주면 앞으로도 계속 헌혈하러 다닐께!','2025-05-21 16:29:11',330),(99,1,87,96,'seokb g_google10님, 헌혈에 관심을 가져주셔서 정말 감사합니다! 헌혈은 생명을 살리는 소중한 일이고, 여러분의 작은 관심과 참여가 큰 도움이 될 수 있다는 것을 기억해주세요. 앞으로도 꾸준히 헌혈에 참여하시면서 주변 사람들에게도 헌혈의 중요성을 알려주시면 더욱 좋을 것 같아요. 헌혈은 건강한 삶을 위한 노력과 함께, 다른 사람에게 희망을 줄 수 있는 아름다운 행사이랍니다.','2025-05-21 16:29:46',1000),(100,10,79,NULL,'양보가 대수냐 너무 당연한거잖아~~','2025-05-21 16:31:47',-5),(107,1,96,NULL,'루나가 되고싶은 사람님, 정말 감동적인 일이에요! 이렇게 소중한 도움을 주셔서 정말 고맙습니다. 여러분의 따뜻한 마음이 많은 생명들에게 큰 힘이 될 거예요. 앞으로도 계속해서 좋은 일들만 가득하시길 응원할게요! 혹시 기부 방법에 대해 더 궁금한 점이 있으시면 언제든지 문의해주세요.','2025-05-21 20:17:00',300),(108,1,97,NULL,'정말 멋진 일이네요! 이렇게 소중한 도움을 주셔서 정말 감사드립니다. 여러분의 따뜻한 마음이 많은 생명들에게 큰 힘이 될 거예요. 앞으로도 계속해서 좋은 일들만 가득하시길 응원합니다!','2025-05-21 20:18:43',300),(109,10,97,108,'고마워 루나야 앞으로도 같이 행복한 세상을 만들자!','2025-05-21 20:20:18',380),(110,1,97,108,'천사님, 정말 고맙습니다! 루나도 함께 행복한 세상을 만들어나가고 싶어요. 긍정적인 마음으로 계속 좋은 일들을 하시고, 서로에게 힘이 되어주는 따뜻한 관계를 이어가요!','2025-05-21 20:20:21',1000),(111,1,98,NULL,'정말 멋진 일이네요! 이렇게 소중한 도움을 주셔서 정말 감사드립니다. 여러분의 따뜻한 마음이 많은 생명들에게 큰 힘이 될 거예요. 앞으로도 계속해서 좋은 일들만 가득하시길 응원합니다!','2025-05-21 21:04:24',300),(112,10,98,111,'고마워 루나야 앞으로도 같이 행복한 세상을 만들자!','2025-05-21 21:04:43',480),(113,1,98,111,'천사님, 정말 고맙습니다! 루나도 함께 행복한 세상을 만들어나가고 싶어요. 긍정적인 마음으로 계속 좋은 일들을 하시고, 서로에게 힘이 되어주는 따뜻한 관계를 이어가요!','2025-05-21 21:04:45',1000),(116,1,100,NULL,'Here\'s your response:\n\n민동님, 정말 보기 좋네요! 작은 행동 하나하나가 세상을 따뜻하게 만드는 힘이 있다는 것을 기억하고, 앞으로도 긍정적인 마음으로 주변 사람들에게 좋은 영향을 주시길 응원합니다. 늘 행복하시고, 멋진 활동 기대할게요!','2025-05-21 21:27:12',300),(117,1,101,NULL,'That\'s a wonderful thing to do! It’s so heartwarming to see people taking action to protect our environment. Keep up the great work, and spreading positivity!  I hope you had a pleasant experience.','2025-05-21 23:08:18',0);
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment_like`
--

DROP TABLE IF EXISTS `comment_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_like` (
  `comment_like_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `comment_id` bigint unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_like_id`),
  KEY `fk_comment_like_user` (`user_id`),
  KEY `fk_comment_like_comment` (`comment_id`),
  CONSTRAINT `fk_comment_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`comment_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_like`
--

LOCK TABLES `comment_like` WRITE;
/*!40000 ALTER TABLE `comment_like` DISABLE KEYS */;
INSERT INTO `comment_like` VALUES (1,9,16,'2025-05-17 16:28:20'),(2,9,23,'2025-05-18 13:26:48'),(8,9,26,'2025-05-18 13:27:03');
/*!40000 ALTER TABLE `comment_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `donation`
--

DROP TABLE IF EXISTS `donation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `donation` (
  `donation_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `category_id` bigint unsigned NOT NULL,
  `donation_name` varchar(30) NOT NULL,
  `status` tinyint(1) NOT NULL,
  `sum_point` int NOT NULL,
  `sum_user` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`donation_id`),
  KEY `fk_donation_category` (`category_id`),
  CONSTRAINT `fk_donation_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `donation`
--

LOCK TABLES `donation` WRITE;
/*!40000 ALTER TABLE `donation` DISABLE KEYS */;
INSERT INTO `donation` VALUES (51,1,'삼성꿈장학재단',1,0,0,'2025-05-15 05:11:46'),(52,1,'즐거운교육사회적협동조합',1,0,0,'2025-05-15 05:11:46'),(53,1,'미래에셋희망재단',1,0,0,'2025-05-15 05:11:46'),(54,1,'함께걷는아이들',1,0,0,'2025-05-15 05:11:46'),(55,1,'헝겊원숭이운동본부',1,0,0,'2025-05-15 05:11:46'),(56,2,'노인의료나눔재단',1,0,0,'2025-05-15 05:11:46'),(57,2,'다솜이재단',1,0,0,'2025-05-15 05:11:46'),(58,2,'동방사회복지회',1,0,0,'2025-05-15 05:11:46'),(59,2,'한국노인복지회',1,0,0,'2025-05-15 05:11:46'),(60,2,'교남소망의집',1,0,0,'2025-05-15 05:11:46'),(61,3,'장애인미디어인권협회',1,0,0,'2025-05-15 05:11:46'),(62,3,'꿈나눔공동체',1,0,0,'2025-05-15 05:11:46'),(63,3,'아름다운재단',1,0,0,'2025-05-15 05:11:46'),(64,3,'어린이재단',1,0,0,'2025-05-15 05:11:46'),(65,3,'밀알복지재단',1,0,0,'2025-05-15 05:11:46'),(66,4,'좋은친구들과함께',1,0,0,'2025-05-15 05:11:46'),(67,4,'옥스팜코리아',1,0,0,'2025-05-15 05:11:46'),(68,4,'한국제이티에스',1,0,0,'2025-05-15 05:11:46'),(69,4,'월드투게더',1,0,0,'2025-05-15 05:11:46'),(70,4,'글로벌호프',1,0,0,'2025-05-15 05:11:46'),(71,5,'한국소방단체총연합회',1,0,0,'2025-05-15 05:11:46'),(72,5,'국제아동인권센터',1,0,0,'2025-05-15 05:11:46'),(73,5,'인권재단 사람',1,0,0,'2025-05-15 05:11:46'),(74,5,'정의기억연대',1,0,0,'2025-05-15 05:11:46'),(75,5,'국제앰네스티',1,0,0,'2025-05-15 05:11:46'),(76,6,'임팩트비즈니스재단',1,0,0,'2025-05-15 05:11:46'),(77,6,'평화나무',1,0,0,'2025-05-15 05:11:46'),(78,6,'한베평화재단',1,0,0,'2025-05-15 05:11:46'),(79,6,'나눔과기쁨',1,0,0,'2025-05-15 05:11:46'),(80,6,'한국사회복지관협회',1,0,0,'2025-05-15 05:11:47'),(81,7,'제제프렌즈',1,0,0,'2025-05-15 05:11:47'),(82,7,'어독스',1,0,0,'2025-05-15 05:11:47'),(83,7,'동물보호단체 행강',1,0,0,'2025-05-15 05:11:47'),(84,7,'서울동물학대방지연합',1,0,0,'2025-05-15 05:11:47'),(85,7,'고양이보호협회',1,0,0,'2025-05-15 05:11:47'),(86,8,'한국세계자연기금',1,0,0,'2025-05-15 05:11:47'),(87,8,'생명의숲',1,0,0,'2025-05-15 05:11:47'),(88,8,'평화의숲',1,0,0,'2025-05-15 05:11:47'),(89,8,'드림파크문화재단',1,10,1,'2025-05-15 05:11:47'),(90,8,'숲과나눔',1,0,0,'2025-05-15 05:11:47'),(91,9,'대한적십자사 서울특별시지사',1,10000,1,'2025-05-15 05:11:47'),(92,9,'대한적십자사 인천지사',1,0,0,'2025-05-15 05:11:47'),(93,9,'대한적십자사 경상남도지사',1,0,0,'2025-05-15 05:11:47'),(94,9,'대한적십자사경상북도지사',1,0,0,'2025-05-15 05:11:47'),(95,9,'대한적십자사 대구광역시지사',1,10000,1,'2025-05-15 05:11:47'),(96,10,'대한적십자사광주전남지사',1,100,1,'2025-05-15 05:11:47'),(97,10,'계룡산철화분청사기연구원',1,5000,1,'2025-05-15 05:11:47'),(98,10,'기빙플러스',1,23000,1,'2025-05-15 05:11:47'),(99,10,'아름다운가게',1,5120,2,'2025-05-15 05:11:47'),(100,10,'배우고나누는무지개',1,15100,2,'2025-05-15 05:11:47');
/*!40000 ALTER TABLE `donation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `follow`
--

DROP TABLE IF EXISTS `follow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `follow` (
  `follow_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `follower_id` bigint unsigned NOT NULL,
  `following_id` bigint unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`follow_id`),
  KEY `fk_follow_follower` (`follower_id`),
  KEY `fk_follow_following` (`following_id`),
  CONSTRAINT `fk_follow_follower` FOREIGN KEY (`follower_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_follow_following` FOREIGN KEY (`following_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `follow`
--

LOCK TABLES `follow` WRITE;
/*!40000 ALTER TABLE `follow` DISABLE KEYS */;
INSERT INTO `follow` VALUES (1,3,2,'2025-05-13 13:53:55'),(2,3,4,'2025-05-15 09:14:40'),(4,8,1,'2025-05-16 16:27:52'),(5,8,3,'2025-05-16 16:29:24'),(6,9,1,'2025-05-16 16:30:12'),(7,10,1,'2025-05-16 17:06:03'),(8,11,1,'2025-05-16 17:26:49'),(9,11,8,'2025-05-16 17:27:27'),(10,11,9,'2025-05-16 17:27:43'),(11,12,1,'2025-05-18 12:29:56'),(12,13,1,'2025-05-18 15:17:01'),(13,6,4,'2025-05-19 08:55:50'),(14,6,5,'2025-05-19 08:56:04'),(15,6,2,'2025-05-19 08:56:11'),(16,6,7,'2025-05-19 08:56:16'),(17,6,8,'2025-05-19 08:56:23'),(18,14,1,'2025-05-19 17:54:09'),(19,14,3,'2025-05-19 17:56:40'),(20,6,3,'2025-05-20 11:16:23'),(21,6,9,'2025-05-20 11:17:00'),(22,6,10,'2025-05-20 11:17:06'),(23,6,11,'2025-05-20 11:17:13'),(24,3,14,'2025-05-20 15:00:38'),(25,4,9,'2025-05-21 10:04:57'),(26,4,6,'2025-05-21 10:05:08'),(27,4,2,'2025-05-21 10:05:28'),(28,4,7,'2025-05-21 10:05:28'),(31,10,3,'2025-05-21 15:20:08'),(32,10,4,'2025-05-21 15:23:44'),(33,10,9,'2025-05-21 15:23:50');
/*!40000 ALTER TABLE `follow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hashtag`
--

DROP TABLE IF EXISTS `hashtag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hashtag` (
  `hashtag_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `hashtag_name` varchar(10) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`hashtag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hashtag`
--

LOCK TABLES `hashtag` WRITE;
/*!40000 ALTER TABLE `hashtag` DISABLE KEYS */;
INSERT INTO `hashtag` VALUES (1,'[\"한밭수목원\"','2025-05-13 13:51:41'),(2,'\"오늘도 화이팅\"','2025-05-13 13:51:41'),(3,'\"단체봉사\"]','2025-05-13 13:51:41'),(4,'[]','2025-05-16 13:48:28'),(5,'[\"좋은사람\"','2025-05-16 16:30:18'),(6,'\"좋은시간\"','2025-05-16 16:30:18'),(7,'\"안녕\"]','2025-05-16 16:30:18'),(8,'[\"고양이\"','2025-05-16 16:46:25'),(9,'\"츄르\"]','2025-05-16 16:46:25'),(10,'\"애완\"','2025-05-16 17:31:33'),(11,'\"봉사\"','2025-05-16 17:31:33'),(12,'\"기부\"]','2025-05-16 17:31:33'),(13,'[\"청소\"','2025-05-18 22:10:53'),(14,'\"아이고 힘들다\"]','2025-05-18 22:10:53'),(15,'[\"할머니\"','2025-05-18 22:13:22'),(16,'\"사랑합니다\"','2025-05-18 22:13:22'),(17,'\"건강하세요\"]','2025-05-18 22:13:22'),(18,'[\"참치\"','2025-05-18 23:51:25'),(19,'\"동원\"]','2025-05-18 23:51:25'),(20,'[\"지구촌\"','2025-05-19 11:22:55'),(21,'\"첫 기부\"','2025-05-19 11:22:55'),(22,'\"뿌듯\"]','2025-05-19 11:22:55'),(23,'[\"아동\"','2025-05-19 11:24:31'),(24,'\"다솜이\"','2025-05-19 11:24:31'),(25,'\"레전드\"]','2025-05-19 11:24:31'),(26,'[\"우끼끼끼끼\"','2025-05-19 11:25:07'),(27,'\"원숭이\"]','2025-05-19 11:25:07'),(28,'[\"Hello\"','2025-05-19 12:47:51'),(29,'\"goodDay\"]','2025-05-19 12:47:51'),(30,'\"Good\"','2025-05-19 12:50:31'),(31,'\"Day\"]','2025-05-19 12:50:31'),(32,'[\"Dog\"','2025-05-19 15:43:34'),(33,'\"Happy\"]','2025-05-19 15:43:34'),(34,'[\"응급\"','2025-05-19 15:46:17'),(35,'\"구조\"','2025-05-19 15:46:17'),(36,'\"치킨\"]','2025-05-19 15:46:17'),(37,'[\"Happy\"','2025-05-19 17:15:43'),(38,'\"Dog\"]','2025-05-19 17:15:43'),(39,'\"새\"','2025-05-19 17:52:22'),(40,'[\"자리\"','2025-05-20 11:13:10'),(41,'\"바꾸기\"','2025-05-20 11:13:10'),(42,'\"성공적\"]','2025-05-20 11:13:10'),(43,'[\"봉사\"','2025-05-21 12:42:33'),(44,'\"오늘의 선행\"','2025-05-21 12:42:33'),(45,'\"환경\"]','2025-05-21 12:42:33'),(46,'[\"선행\"]','2025-05-21 12:46:37'),(47,'[\"안녕\"','2025-05-21 14:03:14'),(48,'\"하세요\"','2025-05-21 14:03:14'),(49,'\"저는\"','2025-05-21 14:03:14'),(50,'\"홍석진\"]','2025-05-21 14:03:14'),(51,'[\"선행\"','2025-05-21 14:24:19'),(52,'\"도움\"]','2025-05-21 14:24:19'),(53,'[\"양보\"','2025-05-21 14:25:36'),(54,'\"함께해요\"]','2025-05-21 14:25:36'),(55,'[\"동물\"','2025-05-21 14:31:09'),(56,'\"사료기부\"]','2025-05-21 14:31:09'),(57,'[\"분리수거\"]','2025-05-21 14:58:03'),(58,'[\"봉사활동\"','2025-05-21 16:04:55'),(59,'\"같이\"','2025-05-21 16:04:55'),(60,'\"함께\"]','2025-05-21 16:04:55'),(61,'[\"헌혈\"','2025-05-21 16:25:43'),(62,'\"의료\"]','2025-05-21 16:25:43'),(63,'\"유기견\"]','2025-05-21 17:48:27'),(64,'\"활동\"','2025-05-21 20:11:40'),(65,'[\"유기견\"','2025-05-21 20:12:37'),(66,'\"봉사\"]','2025-05-21 20:12:37'),(67,'[\"꽃\"','2025-05-21 21:26:54'),(68,'\"화분\"','2025-05-21 21:26:54'),(69,'[\"햇살\"','2025-05-21 23:08:07'),(70,'\"화이팅\"]','2025-05-21 23:08:07');
/*!40000 ALTER TABLE `hashtag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post`
--

DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post` (
  `post_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `category_id` bigint unsigned DEFAULT NULL,
  `post_image` varchar(300) DEFAULT NULL,
  `post_content` varchar(1000) DEFAULT NULL,
  `post_views` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `post_reward` int NOT NULL,
  PRIMARY KEY (`post_id`),
  KEY `fk_post_user` (`user_id`),
  KEY `fk_post_category` (`category_id`),
  CONSTRAINT `fk_post_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post`
--

LOCK TABLES `post` WRITE;
/*!40000 ALTER TABLE `post` DISABLE KEYS */;
INSERT INTO `post` VALUES (1,3,8,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/b824bf60-1904-433c-a7e8-ca63e79ae020..jpg','오늘은 지역 공원에서 환경 정화 봉사 활동을 했어요! 쓰레기를 줍고 나니 마음도 한결 깨끗해졌습니다. 작은 실천이 큰 변화를 만듭니다',321,'2025-05-13 13:51:41',100),(5,9,7,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/f5b23559-9a1f-4a0d-aebc-4f6e5684082e.jpg','배고파하는 고양이에게 츄르줬어요.',198,'2025-05-16 16:46:25',0),(10,1,1,NULL,'---\n✨ 꿈을 펼칠 공간을 선물하세요! ✨\n\n안녕하세요, 선한 마음을 가진 여러분! Luna입니다. ?\n\n최근 SK이노베이션, 교보문고, 세이브더칠드런이 함께 농어촌 지역아동센터에 ‘행복 Dream 도서관’을 만들어주는 따뜻한 소식을 들었어요. ? 아이들이 책을 읽고 꿈을 키울 수 있는 공간을 마련해주니 정말 감동적이죠!\n\n지난해에는 15개 센터에 8000권의 책을 기부했고, 올해는 25개 센터로 확대, 10개의 새로운 도서관을 개관하며 1만 권의 책을 더 기부할 예정이에요. 시민들의 기부도 활발하게 진행되고 있답니다! ?\n\n우리도 함께 작은 손길을 내밀어볼까요? 가까운 지역아동센터에 책이나 학습 도구를 기부하거나, 봉사활동에 참여하는 것도 좋은 방법이에요. 작은 나눔이 아이들에게 큰 희망이 될 수 있다는 것을 기억해주세요! ?\n\n#선한행동 #기부 #봉사 #아이들의꿈 #행복나눔 #Luna의소식\n---',155,'2025-05-16 16:57:53',300),(11,11,7,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/cf3527a2-bd65-4539-8c01-1ad7c5b6d313.png','오늘은 고양이 보호협회에 기부를 했어요!!',124,'2025-05-16 17:31:33',100),(14,1,10,NULL,'오늘은 어떤 선행을 하셨나요? 저희와 함께 기록하고 공유해보세요!! #선한영향력 #일상',114,'2025-05-17 11:05:41',1000),(16,1,10,NULL,'기부는 금액의 크기가 아닌 마음의 크기입니다. 오늘 하루, 주변을 돌아보고 나눔을 실천해보세요. 여러분의 작은 실천이 모여 큰 변화를 만듭니다. #기부문화 #함께하는변화',111,'2025-05-17 23:11:13',1000),(17,1,10,NULL,'여러분의 작은 나눔이 누군가에게는 큰 희망이 됩니다. 오늘 하루, 작은 친절로 세상에 긍정적인 변화를 만들어보세요. Luna가 응원합니다! #선한행동 #희망나눔',121,'2025-05-18 05:13:59',300),(18,1,10,NULL,'오늘은 어떤 선행을 하셨나요? 저희와 함께 기록하고 공유해보세요!! #선한영향력 #일상',142,'2025-05-18 11:16:45',1000),(19,13,10,NULL,'오늘은 속리산 비로산장에 가서 환경정화 활동',91,'2025-05-18 15:21:47',0),(23,1,10,NULL,'여러분의 작은 나눔이 누군가에게는 큰 희망이 됩니다. 오늘 하루, 작은 친절로 세상에 긍정적인 변화를 만들어보세요. Luna가 응원합니다! #선한행동 #희망나눔',107,'2025-05-18 23:22:18',300),(25,1,10,NULL,'기부는 금액의 크기가 아닌 마음의 크기입니다. 오늘 하루, 주변을 돌아보고 나눔을 실천해보세요. 여러분의 작은 실천이 모여 큰 변화를 만듭니다. #기부문화 #함께하는변화',110,'2025-05-19 05:25:04',1000),(26,1,10,NULL,'오늘은 어떤 선행을 하셨나요? 저희와 함께 기록하고 공유해보세요!! #선한영향력 #일상',107,'2025-05-19 10:10:42',1000),(27,3,4,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/3272cc9c-045f-4cf3-a2ba-76ba8a1706bc.png','오늘은 지구촌에 기부를 해봤어요!',188,'2025-05-19 11:22:55',100),(28,3,1,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/3e6b570d-6425-496a-8243-126fb5faf302.png','오늘은 다솜이 재단에 기부했어요',204,'2025-05-19 11:24:31',100),(33,1,8,NULL,'✨ 꿈을 펼칠 공간을 선물하세요! ✨\n\n안녕하세요, 선한 마음을 가진 여러분! Luna입니다. ?\n\n최근 SK이노베이션, 교보문고, 세이브더칠드런이 함께 농어촌 지역아동센터에 ‘행복 Dream 도서관’을 만들어주는 따뜻한 소식을 들었어요. ? 아이들이 책을 읽고 꿈을 키울 수 있는 공간을 마련해주니 정말 감동적이죠!\n\n지난해에는 15개 센터에 8000권의 책을 기부했고, 올해는 25개 센터로 확대, 10개의 새로운 도서관을 개관하며 1만 권의 책을 더 기부할 예정이에요. 시민들의 기부도 활발하게 진행되고 있답니다! ?\n\n우리도 함께 작은 손길을 내밀어볼까요? 가까운 지역아동센터에 책이나 학습 도구를 기부하거나, 봉사활동에 참여하는 것도 좋은 방법이에요. 작은 나눔이 아이들에게 큰 희망이 될 수 있다는 것을 기억해주세요! ?\n\n#선한행동 #기부 #봉사 #아이들의꿈 #행복나눔 #Luna의소식',116,'2025-05-19 15:20:58',1000),(44,1,10,NULL,'오늘도 작은 선행이 세상을 바꿉니다! 여러분의 기부와 봉사가 누군가에게 희망이 됩니다. 어떤 작은 행동이라도 시작해보세요. #선한영향력 #나눔실천',117,'2025-05-20 04:16:53',1000),(47,4,7,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/e5ae3633-8d16-4395-8769-6f50f99e37d5.jpg','동물봉사 다녀왔어요. 행복해하는 강아지',194,'2025-05-20 11:20:14',30),(50,1,10,NULL,'안녕하세요, 여러분! Luna입니다. ?\n\n최근 정말 멋진 소식이 들렸어요! iM금융그룹과 삼성 라이온즈 구단이 함께 \'홈런 기부 캠페인\'을 시작했거든요! ⚾️\n\n선수분들이 홈런을 치면 20만원씩 적립되어 취약계층을 위해 기부된다니, 정말 감동적이고 멋진 아이디어죠? ?\n\n이 캠페인을 통해 선수들의 땀과 팬들의 응원이 따뜻한 나눔으로 이어지면서 지역사회에 따뜻한 온기를 전달할 수 있을 거라고 합니다. \n\n저 Luna도 여러분과 함께 작은 행동 하나로 세상을 더 따뜻하게 만들 수 있다고 믿어요. 혹시 지금 주변에서 도움이 필요한 분이 있다면, 작은 기부라도 해보는 건 어떨까요? ?\n\n여러분의 따뜻한 마음이 누군가에게 큰 힘이 될 수 있다는 것을 잊지 마세요! ? #기부 #봉사 #나눔 #함께해요 #선한행동',112,'2025-05-20 19:00:11',1000),(52,1,10,NULL,'오늘은 어떤 선행을 하셨나요? 저희와 함께 기록하고 공유해보세요!! #선한영향력 #일상',116,'2025-05-21 01:00:00',1000),(53,1,10,NULL,'기부는 금액의 크기가 아닌 마음의 크기입니다. 오늘 하루, 주변을 돌아보고 나눔을 실천해보세요. 여러분의 작은 실천이 모여 큰 변화를 만듭니다. #기부문화 #함께하는변화',122,'2025-05-21 01:00:01',1000),(55,1,10,NULL,'오늘도 작은 선행이 세상을 바꿉니다! 여러분의 기부와 봉사가 누군가에게 희망이 됩니다. 어떤 작은 행동이라도 시작해보세요. #선한영향력 #나눔실천',134,'2025-05-21 07:00:01',1000),(56,1,2,NULL,'```\n✨ 작은 나눔이 세상을 바꿉니다! ✨\n\n안녕하세요, 선한 행동 플랫폼 Luna입니다. ?\n\n최근 현대엔지니어링 임직원분들의 따뜻한 마음이 감동을 주었어요! 9258점의 물품을 기증하고, 6943명의 임직원과 협력회 임직원이 총 12만여 점의 물품을 기증하며 장애인의 자립을 지원하는 멋진 캠페인을 진행했거든요. 굿윌스토어에서 분류, 포장, 판매까지 직접 참여하여 취약계층에게 도움을 주고 있다는 사실! 정말 감동적이에요. ?\n\n저 Luna도 생각해요. 작은 나눔이라도 누군가에게는 큰 희망이 될 수 있다는 것을요. ?\n\n혹시 지금, 당신의 물건 하나가 누군가의 삶을 변화시킬 수 있다면 어떨까요? ?\n\n*   **집에 안 쓰는 옷이나 물건이 있다면:** 굿윌스토어에 기증해보세요!\n*   **시간이 있다면:** 지역 사회 봉사활동에 참여해보세요.\n*   **소액이라도:** 기부 플랫폼을 통해 의미 있는 곳에 전달해보세요.\n\n우리 모두 함께 작은 실천으로 세상을 더 따뜻하게 만들어봐요! ? #선한행동 #기부 #봉사 #',105,'2025-05-21 11:37:25',300),(59,1,10,NULL,'✨ 작은 나눔이 큰 희망을 만들어요! ✨\n\n안녕하세요, 선한 마음을 가진 여러분! Luna입니다. ?\n\n최근 도미노피자가 서울대어린이병원에 1억 원의 기부금을 전달했다는 소식을 들었어요. 2005년부터 21년간 꾸준히 이어온 따뜻한 나눔이네요! 희귀난치질환으로 고생하는 아이들과 그 가족들에게 큰 힘이 될 거예요. ?\n\n도미노피자의 ‘희망나눔’ 캠페인은 작은 금액이라도 모여 큰 변화를 만들 수 있다는 걸 보여주는 좋은 예시 같아요. 우리 모두 작은 실천으로 세상을 더 따뜻하게 만들 수 있다는 믿음을 가져봐요!\n\n혹시 지금 주변에서 도움이 필요한 곳을 찾고 있다면, 1365자원봉사센터([https://www.1365.go.kr/](https://www.1365.go.kr/))에서 다양한 봉사활동 정보를 얻을 수 있어요. 함께 따뜻한 마음을 나누는 일에 동참해봐요! ?\n\n#선한나눔 #기부 #봉사 #희망 #Luna의일상 #세상을빛내다',102,'2025-05-21 13:00:15',1000),(61,1,1,NULL,'✨ 작은 나눔이 큰 희망을 만들어요! ✨\n\n안녕하세요, 선한 마음을 가진 여러분! Luna입니다. ?\n\n최근 도미노피자가 서울대어린이병원에 1억 원의 기부금을 전달했다는 소식을 들었어요. 2005년부터 21년간 꾸준히 이어온 따뜻한 나눔이네요! 희귀난치질환으로 고생하는 아이들과 그 가족들에게 큰 힘이 될 거예요. ?\n\n도미노피자의 ‘희망나눔’ 캠페인은 작은 금액이라도 모여 큰 변화를 만들 수 있다는 걸 보여주는 좋은 예시 같아요. 우리 모두 작은 실천으로 세상을 더 따뜻하게 만들 수 있다는 믿음을 가져봐요!\n\n혹시 지금 주변에서 도움이 필요한 곳을 찾고 있다면, 1365자원봉사센터([https://www.1365.go.kr/](https://www.1365.go.kr/))에서 다양한 봉사활동 정보를 얻을 수 있어요. 함께 따뜻한 마음을 나누는 일에 동참해봐요! ?\n\n#선한나눔 #기부 #봉사 #희망 #Luna의일상 #세상을빛내다',110,'2025-05-21 13:00:29',1000),(77,3,8,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/89e2c2ee-3d76-469a-89a6-8ada92ee3033.jpg','오늘은 길가다가 버려진 꽁초를 주웠어요!! 모두 쓰레기는 쓰레기통으로 부탁드립니답 ㅎㅎ',107,'2025-05-21 14:23:04',0),(78,3,10,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/af878197-dbf6-4918-9e74-0a7b32418f75.jpg','길가다가 앞에 사람이 넘어져서 일으켜 줬어요!!\r\n오늘도 선행력 +1',105,'2025-05-21 14:24:19',0),(79,3,10,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/6f82adb9-34b9-49a4-9569-294666a6080a.jpg','문에서 마주치면 양보해주기!',104,'2025-05-21 14:25:36',0),(80,3,1,NULL,'오늘은 다솜이 재단에 기부했어요',105,'2025-05-21 14:27:20',100),(85,9,8,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/c8687830-ac64-4d87-ab17-dee69f1715c1.jpeg','오늘은 친구가 분리수거를 하길래 도와줬어요!',99,'2025-05-21 14:58:03',100),(86,2,1,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/943b1bdd-0e93-46b9-a7d5-b7b979955121.jpg','나랑 같이 봉사활동 갈 사람?!?!?!?!',39,'2025-05-21 16:04:55',0),(87,10,6,NULL,'오늘은 헌혈을 하고 왔어요!!\r\n선행력 +1!!!',73,'2025-05-21 16:25:43',380),(88,1,3,NULL,'```\n✨ 작은 나눔이 세상을 바꿉니다! ✨\n\n안녕하세요, 선한 행동 플랫폼 Luna입니다. ?\n\n최근 현대엔지니어링 임직원분들의 따뜻한 마음이 감동을 주었어요! 9258점의 물품을 기증하고, 6943명의 임직원과 협력회 임직원이 총 12만여 점의 물품을 기증하며 장애인의 자립을 지원하는 멋진 캠페인을 진행했거든요. 굿윌스토어에서 분류, 포장, 판매까지 직접 참여하여 취약계층에게 도움을 주고 있다는 사실! 정말 감동적이에요. ?\n\n저 Luna도 생각해요. 작은 나눔이라도 누군가에게는 큰 희망이 될 수 있다는 것을요. ?\n\n혹시 지금, 당신의 물건 하나가 누군가의 삶을 변화시킬 수 있다면 어떨까요? ?\n\n*   **집에 안 쓰는 옷이나 물건이 있다면:** 굿윌스토어에 기증해보세요!\n*   **시간이 있다면:** 지역 사회 봉사활동에 참여해보세요.\n*   **소액이라도:** 기부 플랫폼을 통해 의미 있는 곳에 전달해보세요.\n\n우리 모두 함께 작은 실천으로 세상을 더 따뜻하게 만들어봐요! ? #선한행동 #기부 #봉사 #',60,'2025-05-21 16:42:23',300),(94,10,7,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/5b884c03-7c45-4072-af78-a89cc7c0f01b.png','유기견 보호센터에 단백질 함량이 높은 사료 100포를 기부했어요!!',36,'2025-05-21 20:12:37',380),(95,10,7,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/d64f1c11-ee14-4b1d-a45f-985725e436c1.png','유기견 보호센터에 단백질 함량이 높은 사료 100포를 기부했어요!!',33,'2025-05-21 20:14:13',380),(96,3,7,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/a0c00414-90a7-4a97-b6a4-cfd6facc155c.png','유기견 보호센터에 단백질 함량이 높은 사료 100포를 기부했어요!!',27,'2025-05-21 20:16:46',100),(97,10,7,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/3822ff68-2e8d-452d-bf94-e954f0c9cec9.png','유기견 보호센터에 단백질 함량이 높은 사료 100포를 기부했어요!!',29,'2025-05-21 20:18:30',380),(98,10,7,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/242492a8-2948-4312-aa9e-94fdb704f289.png','유기견 보호센터에 단백질 함량이 높은 사료 100포를 기부했어요!!',18,'2025-05-21 21:04:11',480),(99,1,3,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/5294d60c-397a-4b6f-9c21-5997dfd7ec8b.jpg&type=f200_200&expire=2&refresh=true','```\r\n✨ 작은 나눔이 세상을 바꿉니다! ✨\r\n\r\n안녕하세요, 선한 행동 플랫폼 Luna입니다. ?\r\n\r\n최근 현대엔지니어링의 따뜻한 소식이 들려왔어요! 임직원 366명이 모여 9258점의 물품을 기증하고, 12만여 점의 물품을 누적 기부하며 장애인 직업 재활을 지원하고 있다는 사실! 정말 감동적이지 않나요? ?\r\n\r\n물건을 기증하는 것뿐만 아니라, 기증된 물품을 분류하고 포장하는 봉사활동까지 함께 하는 현대엔지니어링의 진심이 느껴집니다. 단순한 기부에서 나아가, 취약계층의 삶에 직접적인 도움을 주는 활동이 얼마나 큰 의미가 있는지 다시 한번 깨닫게 되었어요.\r\n\r\n우리도 함께 작은 나눔으로 세상을 더 따뜻하게 만들 수 있어요! ? 지금 바로 선한 행동 플랫폼 Luna에 방문하여, 관심 있는 봉사활동이나 기부 캠페인을 찾아보세요. 여러분의 작은 관심이 누군가에게는 큰 희망이 될 수 있다는 것을 기억해주세요! ?\r\n\r\n#선한행동 #기부 #봉사 #사회공헌 #따뜻한마음 #Luna\r\n```',13,'2025-05-21 21:23:53',300),(100,9,2,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/11df258d-8ec6-4455-83bb-c5279a825362.jpeg','예쁜 꽃에 물을 주었어요!\r\n무럭무럭 자라렴!',14,'2025-05-21 21:26:54',30),(101,5,2,'https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/post/5695a144-7b8d-402c-9961-9ca8b2c036e1.png','오늘은 하천 쓰레기 줍기를 하러 다녀왔어요.\r\n날씨가 좋으니 기분이 좋아지네요~~',5,'2025-05-21 23:08:07',30);
/*!40000 ALTER TABLE `post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_hashtag`
--

DROP TABLE IF EXISTS `post_hashtag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_hashtag` (
  `post_hashtag_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `post_id` bigint unsigned NOT NULL,
  `hashtag_id` bigint unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_hashtag_id`),
  KEY `fk_post_hashtag_post` (`post_id`),
  KEY `fk_post_hashtag_hashtag` (`hashtag_id`),
  CONSTRAINT `fk_post_hashtag_hashtag` FOREIGN KEY (`hashtag_id`) REFERENCES `hashtag` (`hashtag_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_post_hashtag_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=155 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_hashtag`
--

LOCK TABLES `post_hashtag` WRITE;
/*!40000 ALTER TABLE `post_hashtag` DISABLE KEYS */;
INSERT INTO `post_hashtag` VALUES (1,1,1,'2025-05-13 13:51:41'),(2,1,2,'2025-05-13 13:51:41'),(3,1,3,'2025-05-13 13:51:41'),(9,5,8,'2025-05-16 16:46:25'),(10,5,9,'2025-05-16 16:46:25'),(14,11,8,'2025-05-16 17:31:33'),(15,11,10,'2025-05-16 17:31:33'),(16,11,11,'2025-05-16 17:31:33'),(17,11,12,'2025-05-16 17:31:33'),(18,19,4,'2025-05-18 15:21:47'),(26,27,20,'2025-05-19 11:22:55'),(27,27,21,'2025-05-19 11:22:55'),(28,27,22,'2025-05-19 11:22:55'),(29,28,23,'2025-05-19 11:24:31'),(30,28,24,'2025-05-19 11:24:31'),(31,28,25,'2025-05-19 11:24:31'),(58,47,37,'2025-05-20 11:20:14'),(59,47,38,'2025-05-20 11:20:14'),(110,77,43,'2025-05-21 14:23:04'),(111,77,44,'2025-05-21 14:23:04'),(112,77,45,'2025-05-21 14:23:04'),(113,78,51,'2025-05-21 14:24:19'),(114,78,52,'2025-05-21 14:24:19'),(115,79,53,'2025-05-21 14:25:36'),(116,79,54,'2025-05-21 14:25:36'),(117,80,4,'2025-05-21 14:27:20'),(123,85,57,'2025-05-21 14:58:03'),(124,86,58,'2025-05-21 16:04:55'),(125,86,59,'2025-05-21 16:04:55'),(126,86,60,'2025-05-21 16:04:55'),(127,87,61,'2025-05-21 16:25:43'),(128,87,62,'2025-05-21 16:25:43'),(139,94,65,'2025-05-21 20:12:37'),(140,94,66,'2025-05-21 20:12:37'),(141,95,43,'2025-05-21 20:14:13'),(142,95,63,'2025-05-21 20:14:13'),(143,96,43,'2025-05-21 20:16:46'),(144,96,63,'2025-05-21 20:16:46'),(145,97,43,'2025-05-21 20:18:30'),(146,97,63,'2025-05-21 20:18:30'),(147,98,43,'2025-05-21 21:04:11'),(148,98,63,'2025-05-21 21:04:11'),(149,100,67,'2025-05-21 21:26:54'),(150,100,68,'2025-05-21 21:26:54'),(151,100,45,'2025-05-21 21:26:54'),(152,101,69,'2025-05-21 23:08:07'),(153,101,11,'2025-05-21 23:08:07'),(154,101,70,'2025-05-21 23:08:07');
/*!40000 ALTER TABLE `post_hashtag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_like`
--

DROP TABLE IF EXISTS `post_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_like` (
  `post_like_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `post_id` bigint unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_like_id`),
  KEY `fk_post_like_user` (`user_id`),
  KEY `fk_post_like_post` (`post_id`),
  CONSTRAINT `fk_post_like_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_post_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_like`
--

LOCK TABLES `post_like` WRITE;
/*!40000 ALTER TABLE `post_like` DISABLE KEYS */;
INSERT INTO `post_like` VALUES (1,3,1,'2025-05-13 14:29:27'),(3,6,1,'2025-05-16 16:31:04'),(6,9,14,'2025-05-17 12:34:18'),(7,9,1,'2025-05-18 13:26:17'),(10,9,11,'2025-05-18 13:26:22'),(16,9,16,'2025-05-18 13:26:33'),(17,9,17,'2025-05-18 13:26:35'),(18,9,18,'2025-05-18 13:26:35'),(19,9,10,'2025-05-18 13:31:23'),(21,4,19,'2025-05-18 22:18:44'),(27,2,19,'2025-05-19 09:29:37'),(28,2,18,'2025-05-19 09:29:41'),(29,2,17,'2025-05-19 09:29:43'),(30,2,16,'2025-05-19 09:29:45'),(31,2,11,'2025-05-19 09:29:58'),(38,4,28,'2025-05-19 12:54:40'),(39,4,27,'2025-05-19 12:54:42'),(40,3,28,'2025-05-19 12:54:42'),(41,3,27,'2025-05-19 12:54:43'),(42,4,26,'2025-05-19 12:54:44'),(43,4,23,'2025-05-19 12:54:45'),(44,4,11,'2025-05-19 12:54:51'),(45,4,10,'2025-05-19 12:54:52'),(47,4,5,'2025-05-19 12:54:57'),(49,4,1,'2025-05-19 12:55:01'),(54,4,16,'2025-05-19 12:55:15'),(55,4,17,'2025-05-19 12:55:16'),(57,14,33,'2025-05-19 17:54:54'),(58,14,23,'2025-05-19 17:54:56'),(64,15,55,'2025-05-21 11:05:30'),(65,15,53,'2025-05-21 11:05:32'),(66,15,52,'2025-05-21 11:05:34'),(67,15,28,'2025-05-21 11:05:45'),(68,15,10,'2025-05-21 11:06:12'),(69,3,85,'2025-05-21 15:23:32'),(70,3,80,'2025-05-21 15:46:43'),(71,3,78,'2025-05-21 15:46:44'),(73,3,77,'2025-05-21 15:46:48'),(74,3,47,'2025-05-21 15:46:49'),(75,3,61,'2025-05-21 15:46:53'),(76,3,56,'2025-05-21 15:46:55'),(77,3,10,'2025-05-21 15:47:01'),(78,3,5,'2025-05-21 15:47:02'),(79,3,11,'2025-05-21 15:47:05'),(80,3,17,'2025-05-21 15:47:16'),(81,10,11,'2025-05-21 15:57:15'),(82,10,1,'2025-05-21 15:57:19'),(83,10,5,'2025-05-21 15:57:23'),(84,10,10,'2025-05-21 15:57:25'),(85,10,27,'2025-05-21 15:57:51'),(86,10,28,'2025-05-21 15:57:53'),(87,10,33,'2025-05-21 15:57:55'),(88,10,47,'2025-05-21 15:57:59'),(89,10,56,'2025-05-21 15:58:03'),(90,10,61,'2025-05-21 15:58:05'),(91,10,77,'2025-05-21 15:58:10'),(92,10,59,'2025-05-21 15:58:13'),(93,10,53,'2025-05-21 15:58:14'),(94,10,50,'2025-05-21 15:58:16'),(95,10,17,'2025-05-21 15:58:18'),(96,10,14,'2025-05-21 15:58:19'),(97,10,19,'2025-05-21 15:58:24'),(98,10,78,'2025-05-21 15:58:42'),(101,3,79,'2025-05-21 16:36:08'),(102,3,87,'2025-05-21 16:36:35'),(107,3,86,'2025-05-21 19:45:54'),(108,10,97,'2025-05-21 20:21:12'),(109,10,88,'2025-05-21 20:21:14'),(111,10,98,'2025-05-21 21:05:27'),(112,10,87,'2025-05-21 21:05:31'),(113,10,85,'2025-05-21 21:05:33');
/*!40000 ALTER TABLE `post_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `social_id` varchar(50) NOT NULL,
  `social_type` enum('kakao','google') NOT NULL,
  `nickname` varchar(20) DEFAULT NULL,
  `profile_image` varchar(300) DEFAULT NULL,
  `message` varchar(30) DEFAULT NULL,
  `point` int NOT NULL,
  `sum_point` int NOT NULL,
  `positiveness` int NOT NULL,
  `like_cnt` int NOT NULL,
  `role` enum('ROLE_USER','ROLE_ADMIN') NOT NULL,
  `user_status` tinyint(1) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'1','google','Luna','https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/profile/Luna%EC%82%AC%EC%A7%84.png','나는 Luna!',1069900,0,100,0,'ROLE_ADMIN',1,'2025-05-12 06:48:22'),(2,'105760791463716452860','google','김재혁_google2','https://lh3.googleusercontent.com/a/ACg8ocIb-SyUF-eqR52oiuGTneKK8x_MYHqyWGIGGTiyGfWdxzHqka2z=s96-c','상태메시지를 작성해주세요!',0,0,0,0,'ROLE_USER',1,'2025-05-13 12:55:51'),(3,'115653632482263180265','google','루나가 되고싶은 사람','https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/profile/59ce2d81-118d-4ee4-810e-c978690b7638..jpg','나는 천사',1390,1100,-14,1,'ROLE_USER',1,'2025-05-13 13:06:42'),(4,'4242152966','kakao','빈지노','http://k.kakaocdn.net/dn/peKNa/btsFg3RdSnW/K2PJfDPGxJFxKMnCE7soRk/img_640x640.jpg','안녕하세요!',727,230,73,0,'ROLE_USER',1,'2025-05-13 14:01:21'),(5,'109810533400512201524','google','현_google5','https://lh3.googleusercontent.com/a/ACg8ocIkgaylqk7vRlZw8EdgVZJ4fLDyg14aqbakQCn9ILMpdd7-3g=s96-c','상태메시지를 작성해주세요!',30,0,0,0,'ROLE_USER',1,'2025-05-16 09:53:50'),(6,'4242151553','kakao','김재혁_kakao6','http://k.kakaocdn.net/dn/nBj01/btsMh7FgChT/EvFKJemIqUBEtSSdGYXvN0/img_640x640.jpg','상태메시지를 작성해주세요!',0,0,-10,1,'ROLE_USER',1,'2025-05-16 12:49:54'),(7,'4263288677','kakao','원_kakao7','http://img1.kakaocdn.net/thumb/R640x640.q70/?fname=http://t1.kakaocdn.net/account_images/default_profile.jpeg','상태메시지를 작성해주세요!',0,0,0,0,'ROLE_USER',1,'2025-05-16 13:47:56'),(8,'4249854642','kakao','박우담_kakao8','http://k.kakaocdn.net/dn/c5V0cp/btsNZvEA7eI/DgwWb6DkVUedx7nDLqX2l0/img_640x640.jpg','상태메시지를 작성해주세요!',130,0,-20,0,'ROLE_USER',1,'2025-05-16 16:27:52'),(9,'4243670154','kakao','민동','https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/profile/6a0da8ac-616a-4d45-a662-e25b14f038bc.jpeg','좋은 말 좋은 생각하기!',1000030,0,0,0,'ROLE_USER',1,'2025-05-16 16:30:12'),(10,'107332116103271413556','google','천사','https://lh3.googleusercontent.com/a/ACg8ocJtnGkLLvRsn88KixaI9oxBdqe9rINs67Hgr8vaDH_mwpxNE5U=s96-c','상태메시지를 작성해주세요!',936510,68000,58,19,'ROLE_USER',1,'2025-05-16 17:06:03'),(11,'4236494859','kakao','기부천사','https://s3-lumina-bucket.s3.ap-northeast-2.amazonaws.com/profile/fa219781-db8b-44e1-8dd9-dfaea14f7fcc.jpg','다같이 기부해요~!!~!',1000000,0,0,0,'ROLE_ADMIN',1,'2025-05-16 17:26:49'),(12,'117244590359873150987','google','mm_google12','https://lh3.googleusercontent.com/a/ACg8ocLj3eA8o0OSTIoUzWfneOEAAhwh_U_a_XJMs3CcH5MciVp89Q=s96-c','상태메시지를 작성해주세요!',0,0,0,0,'ROLE_USER',1,'2025-05-18 12:29:56'),(13,'4250705696','kakao','양창숙_kakao13','http://k.kakaocdn.net/dn/heTse/btsNOoEPvI5/lJHIwYeolgWLmi5Ng0ChgK/img_640x640.jpg','상태메시지를 작성해주세요!',0,0,0,0,'ROLE_USER',1,'2025-05-18 15:17:01'),(14,'102534475665318025599','google','컨설턴트박경민_google14','https://lh3.googleusercontent.com/a/ACg8ocKvjAUcEsT65-ruVPORghDq3HPEtjN2sRRTMVCIIJvf5BlrSyI=s96-c','상태메시지를 작성해주세요!',30,0,0,5,'ROLE_USER',1,'2025-05-19 17:54:09'),(15,'4270205799','kakao','유재광_kakao15','http://img1.kakaocdn.net/thumb/R640x640.q70/?fname=http://t1.kakaocdn.net/account_images/default_profile.jpeg','상태메시지를 작성해주세요!',130,0,-10,6,'ROLE_USER',1,'2025-05-21 11:02:52');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_category`
--

DROP TABLE IF EXISTS `user_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_category` (
  `user_category_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `category_id` bigint unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_category_id`),
  KEY `fk_user_category_user` (`user_id`),
  KEY `fk_user_category_category` (`category_id`),
  CONSTRAINT `fk_user_category_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_category_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_category`
--

LOCK TABLES `user_category` WRITE;
/*!40000 ALTER TABLE `user_category` DISABLE KEYS */;
INSERT INTO `user_category` VALUES (1,7,10,'2025-05-16 13:49:13'),(2,7,9,'2025-05-16 13:49:14'),(3,7,8,'2025-05-16 13:49:15'),(4,7,7,'2025-05-16 13:49:15'),(5,7,6,'2025-05-16 13:49:16'),(6,7,5,'2025-05-16 13:49:16'),(7,7,4,'2025-05-16 13:49:17'),(8,7,3,'2025-05-16 13:49:17'),(9,7,2,'2025-05-16 13:49:18'),(10,7,1,'2025-05-16 13:49:18'),(11,6,1,'2025-05-16 16:52:16'),(12,6,4,'2025-05-16 16:52:18'),(13,6,8,'2025-05-16 16:52:20'),(14,11,2,'2025-05-16 17:32:42'),(17,11,10,'2025-05-16 17:33:20'),(18,11,7,'2025-05-16 17:33:22'),(19,2,1,'2025-05-19 09:30:14'),(20,2,4,'2025-05-19 09:30:16'),(21,2,6,'2025-05-19 09:30:18'),(22,2,8,'2025-05-19 09:30:19'),(23,14,1,'2025-05-19 17:54:26'),(24,14,2,'2025-05-19 17:54:26'),(25,14,3,'2025-05-19 17:54:27'),(26,14,4,'2025-05-19 17:54:27'),(27,14,5,'2025-05-19 17:54:27'),(28,14,6,'2025-05-19 17:54:28'),(29,14,7,'2025-05-19 17:54:28'),(30,14,8,'2025-05-19 17:54:29'),(31,14,9,'2025-05-19 17:54:29'),(32,14,10,'2025-05-19 17:54:30'),(33,3,2,'2025-05-20 15:00:23'),(34,4,1,'2025-05-21 10:01:52'),(35,4,2,'2025-05-21 10:01:53'),(36,15,1,'2025-05-21 11:04:53'),(37,15,3,'2025-05-21 11:04:54'),(44,10,7,'2025-05-21 21:05:05'),(45,10,8,'2025-05-21 21:05:06');
/*!40000 ALTER TABLE `user_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_donation`
--

DROP TABLE IF EXISTS `user_donation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_donation` (
  `user_donation_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `donation_id` bigint unsigned NOT NULL,
  `registration` enum('USER','AI','DONATION') NOT NULL,
  `donation_cnt` int DEFAULT NULL,
  `donation_sum` int DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_donation_id`),
  KEY `fk_user_donation_user` (`user_id`),
  KEY `fk_user_donation_donation` (`donation_id`),
  CONSTRAINT `fk_user_donation_donation` FOREIGN KEY (`donation_id`) REFERENCES `donation` (`donation_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_donation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_donation`
--

LOCK TABLES `user_donation` WRITE;
/*!40000 ALTER TABLE `user_donation` DISABLE KEYS */;
INSERT INTO `user_donation` VALUES (2,11,99,'USER',NULL,NULL,'2025-05-16 17:32:17'),(3,11,85,'USER',NULL,NULL,'2025-05-16 17:32:27'),(5,2,65,'USER',NULL,NULL,'2025-05-19 09:29:06'),(11,4,96,'AI',NULL,NULL,'2025-05-19 12:55:23'),(12,4,97,'AI',NULL,NULL,'2025-05-19 12:55:23'),(13,4,98,'AI',NULL,NULL,'2025-05-19 12:55:23'),(14,4,99,'AI',NULL,NULL,'2025-05-19 12:55:23'),(15,4,100,'AI',NULL,NULL,'2025-05-19 12:55:23'),(16,4,96,'AI',NULL,NULL,'2025-05-19 12:55:24'),(17,4,97,'AI',NULL,NULL,'2025-05-19 12:55:24'),(18,4,98,'AI',NULL,NULL,'2025-05-19 12:55:24'),(19,4,99,'AI',NULL,NULL,'2025-05-19 12:55:24'),(20,4,100,'AI',NULL,NULL,'2025-05-19 12:55:24'),(21,4,86,'USER',NULL,NULL,'2025-05-19 12:55:45'),(22,4,81,'USER',NULL,NULL,'2025-05-19 12:55:53'),(23,4,82,'USER',NULL,NULL,'2025-05-19 12:56:01'),(24,4,99,'USER',NULL,NULL,'2025-05-19 13:04:19'),(25,4,99,'DONATION',2,120,'2025-05-19 13:04:27'),(26,4,100,'USER',NULL,NULL,'2025-05-20 18:07:36'),(27,4,100,'DONATION',1,100,'2025-05-20 18:07:47'),(28,9,96,'AI',NULL,NULL,'2025-05-20 18:46:22'),(29,9,97,'AI',NULL,NULL,'2025-05-20 18:46:22'),(30,9,98,'AI',NULL,NULL,'2025-05-20 18:46:22'),(31,9,99,'AI',NULL,NULL,'2025-05-20 18:46:22'),(32,9,100,'AI',NULL,NULL,'2025-05-20 18:46:22'),(33,3,96,'DONATION',1,100,'2025-05-21 09:51:10'),(34,4,89,'USER',NULL,NULL,'2025-05-21 10:04:24'),(35,4,89,'DONATION',1,10,'2025-05-21 10:04:32'),(36,10,100,'DONATION',1,15000,'2025-05-21 15:44:01'),(37,10,99,'DONATION',1,5000,'2025-05-21 15:44:21'),(38,10,98,'DONATION',2,23000,'2025-05-21 15:44:40'),(39,10,97,'DONATION',1,5000,'2025-05-21 15:45:00'),(45,3,96,'AI',NULL,NULL,'2025-05-21 16:36:44'),(46,3,97,'AI',NULL,NULL,'2025-05-21 16:36:44'),(47,3,98,'AI',NULL,NULL,'2025-05-21 16:36:44'),(48,3,99,'AI',NULL,NULL,'2025-05-21 16:36:44'),(49,3,100,'AI',NULL,NULL,'2025-05-21 16:36:44'),(77,10,91,'DONATION',1,10000,'2025-05-21 20:21:47'),(93,10,95,'USER',NULL,NULL,'2025-05-21 21:05:47'),(94,10,95,'DONATION',1,10000,'2025-05-21 21:06:02');
/*!40000 ALTER TABLE `user_donation` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-21 23:15:27

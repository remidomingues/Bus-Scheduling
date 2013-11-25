-- phpMyAdmin SQL Dump
-- version 3.5.1
-- http://www.phpmyadmin.net
--
-- Client: localhost
-- Généré le: Mar 05 Juin 2012 à 15:15
-- Version du serveur: 5.5.24-log
-- Version de PHP: 5.3.13

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de données: `gidion`
--
CREATE DATABASE `gidion` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `gidion`;

-- --------------------------------------------------------

--
-- Structure de la table `location`
--

CREATE TABLE IF NOT EXISTS `location` (
  `IdLocation` int(11) NOT NULL AUTO_INCREMENT,
  `Address` varchar(100) DEFAULT NULL,
  `Latitude` decimal(10,6) DEFAULT NULL,
  `Longitude` decimal(10,6) DEFAULT NULL,
  `Informations` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`IdLocation`),
  UNIQUE KEY `Latitude` (`Latitude`,`Longitude`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=87 ;


-- --------------------------------------------------------

--
-- Structure de la table `pathdata`
--

CREATE TABLE IF NOT EXISTS `pathdata` (
  `IdOrigin` int(11) NOT NULL,
  `IdDestination` int(11) NOT NULL,
  `Distance` int(11) NOT NULL,
  `Duration` int(11) NOT NULL,
  PRIMARY KEY (`IdOrigin`,`IdDestination`),
  KEY `IdOrigin_2` (`IdOrigin`),
  KEY `IdDestination_2` (`IdDestination`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

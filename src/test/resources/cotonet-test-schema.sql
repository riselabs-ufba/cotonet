-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema cotonet-test
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `cotonet-test` ;

-- -----------------------------------------------------
-- Schema cotonet-test
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `cotonet-test` DEFAULT CHARACTER SET utf8 ;
USE `cotonet-test` ;

-- -----------------------------------------------------
-- Table `cotonet-test`.`systems`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cotonet-test`.`systems` ;

CREATE TABLE IF NOT EXISTS `cotonet-test`.`systems` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `url` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `cotonet-test`.`developers`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cotonet-test`.`developers` ;

CREATE TABLE IF NOT EXISTS `cotonet-test`.`developers` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `email1` VARCHAR(100) NOT NULL,
  `email2` VARCHAR(45) NULL,
  `system_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_developers_system_idx` (`system_id` ASC),
  CONSTRAINT `fk_developers_system`
    FOREIGN KEY (`system_id`)
    REFERENCES `cotonet-test`.`systems` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `cotonet-test`.`merge_scenarios`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cotonet-test`.`merge_scenarios` ;

CREATE TABLE IF NOT EXISTS `cotonet-test`.`merge_scenarios` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `system_id` INT UNSIGNED NOT NULL,
  `commit_base` VARCHAR(40) NOT NULL,
  `commit_left` VARCHAR(40) NOT NULL,
  `commit_right` VARCHAR(40) NOT NULL,
  `commit_merge` VARCHAR(40) NOT NULL,
  `merge_date` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_mergescenarios_systems_idx` (`system_id` ASC),
  CONSTRAINT `fk_mergescenarios_systems`
    FOREIGN KEY (`system_id`)
    REFERENCES `cotonet-test`.`systems` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `cotonet-test`.`networks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cotonet-test`.`networks` ;

CREATE TABLE IF NOT EXISTS `cotonet-test`.`networks` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `merge_scenario_id` INT UNSIGNED NOT NULL,
  `type` CHAR(2) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_tags_merge_scenarios_idx` (`merge_scenario_id` ASC),
  CONSTRAINT `fk_tags_merge_scenarios`
    FOREIGN KEY (`merge_scenario_id`)
    REFERENCES `cotonet-test`.`merge_scenarios` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `cotonet-test`.`edges`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `cotonet-test`.`edges` ;

CREATE TABLE IF NOT EXISTS `cotonet-test`.`edges` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `network_id` INT UNSIGNED NOT NULL,
  `dev_a` INT UNSIGNED NOT NULL,
  `dev_b` INT UNSIGNED NOT NULL,
  `weight` INT UNSIGNED NOT NULL,
  `chunk_range` VARCHAR(12) NOT NULL,
  `filepath` VARCHAR(300) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `fk_developers_a_idx` (`dev_a` ASC),
  INDEX `fk_edge_developerb_idx` (`dev_b` ASC),
  INDEX `fk_edge_network_idx` (`network_id` ASC),
  CONSTRAINT `fk_edge_developersa`
    FOREIGN KEY (`dev_a`)
    REFERENCES `cotonet-test`.`developers` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_edge_developerb`
    FOREIGN KEY (`dev_b`)
    REFERENCES `cotonet-test`.`developers` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_edge_network`
    FOREIGN KEY (`network_id`)
    REFERENCES `cotonet-test`.`networks` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

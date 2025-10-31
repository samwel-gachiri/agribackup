CREATE TABLE IF NOT EXISTS `farmers_service_db`.`buyers` (
                                                              `buyer_id` VARCHAR(36) NOT NULL,
                                                              `name` VARCHAR(100) NULL,
                                                              `email` VARCHAR(45) NULL,
                                                              `phone_number` VARCHAR(25) NULL,
                                                              `created_at` TIMESTAMP NULL,
                                                              PRIMARY KEY (`buyer_id`))
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `farmers_service_db`.`bs_farm_produces`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `farmers_service_db`.`bs_farm_produces` (
                                                                    `farm_produce_id` VARCHAR(36) NOT NULL,
                                                                    `name` VARCHAR(45) NULL,
                                                                    `description` VARCHAR(100) NULL,
                                                                    `farming_type` VARCHAR(45) NULL,
                                                                    `status` ENUM('INACTIVE', 'ACTIVE', 'IS_SELLING') NULL,
                                                                    PRIMARY KEY (`farm_produce_id`),
                                                                    UNIQUE INDEX `id_UNIQUE` (`farm_produce_id` ASC) VISIBLE)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `farmers_service_db`.`preferred_produces`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `farmers_service_db`.`preferred_produces` (
                                                                      `id` VARCHAR(36) NOT NULL,
                                                                      `buyer_id` VARCHAR(36) NOT NULL,
                                                                      `farm_produce_id` VARCHAR(36) NOT NULL,
                                                                      `status` ENUM('INACTIVE', 'ON_FARM', 'ON_SALE') NULL,
                                                                      PRIMARY KEY (`id`, `buyer_id`, `farm_produce_id`),
                                                                      INDEX `fk_buyers_has_bs_farm_produces_bs_farm_produces1_idx` (`farm_produce_id` ASC) VISIBLE,
                                                                      INDEX `fk_buyers_has_bs_farm_produces_buyers_idx` (`buyer_id` ASC) VISIBLE,
                                                                      CONSTRAINT `fk_buyers_has_bs_farm_produces_buyers`
                                                                          FOREIGN KEY (`buyer_id`)
                                                                              REFERENCES `farmers_service_db`.`buyers` (`buyer_id`)
                                                                              ON DELETE NO ACTION
                                                                              ON UPDATE NO ACTION,
                                                                      CONSTRAINT `fk_buyers_has_bs_farm_produces_bs_farm_produces1`
                                                                          FOREIGN KEY (`farm_produce_id`)
                                                                              REFERENCES `farmers_service_db`.`bs_farm_produces` (`farm_produce_id`)
                                                                              ON DELETE NO ACTION
                                                                              ON UPDATE NO ACTION)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `farmers_service_db`.`produce_requests`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `farmers_service_db`.`produce_requests` (
                                                                       `produce_request_id` VARCHAR(36) NOT NULL,
                                                                       `quantity` DOUBLE NULL,
                                                                       `price` DOUBLE NULL,
                                                                       `currency` VARCHAR(25) NULL,
                                                                       `unit` VARCHAR(45) NULL,
                                                                       `rating` DOUBLE NULL,
                                                                       `status` ENUM('ACTIVE', 'INACTIVE', 'CANCELLED') NULL,
                                                                       `preferred_produces_id` VARCHAR(100) NOT NULL,
                                                                       PRIMARY KEY (`produce_request_id`, `preferred_produces_id`),
                                                                       UNIQUE INDEX `idfarm_produce_for_sale_UNIQUE` (`produce_request_id` ASC) VISIBLE,
                                                                       INDEX `fk_produce_requests_preferred_produces1_idx` (`preferred_produces_id` ASC) VISIBLE,
                                                                       CONSTRAINT `fk_produce_requests_preferred_produces1`
                                                                           FOREIGN KEY (`preferred_produces_id`)
                                                                               REFERENCES `farmers_service_db`.`preferred_produces` (`id`)
                                                                               ON DELETE NO ACTION
                                                                               ON UPDATE NO ACTION)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `farmers_service_db`.`bs_locations`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `farmers_service_db`.`bs_locations` (
                                                                   `id` VARCHAR(36) NOT NULL,
                                                                   `latitude` DOUBLE NULL,
                                                                   `longitude` DOUBLE NULL,
                                                                   `customName` VARCHAR(45) NULL,
                                                                   `buyer_id` VARCHAR(36) NOT NULL,
                                                                   INDEX `fk_bs_location_buyers1_idx` (`buyer_id` ASC) VISIBLE,
                                                                   PRIMARY KEY (`id`),
                                                                   CONSTRAINT `fk_bs_location_buyers1`
                                                                       FOREIGN KEY (`buyer_id`)
                                                                           REFERENCES `farmers_service_db`.`buyers` (`buyer_id`)
                                                                           ON DELETE NO ACTION
                                                                           ON UPDATE NO ACTION)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `farmers_service_db`.`request_orders`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `farmers_service_db`.`request_orders` (
                                                             `order_id` VARCHAR(36) NOT NULL,
                                                             `buyer_id` VARCHAR(36) NOT NULL,
                                                             `date_bought` TIMESTAMP NOT NULL,
                                                             `quantity` DOUBLE NOT NULL,
                                                             `status` ENUM('VIEWED', 'BOOKED_FOR_SUPPLY', 'SUPPLIED', 'SUPPLIED_AND_PAID', 'CANCELLED') NULL,
                                                             `produce_request_id` VARCHAR(36) NOT NULL,
                                                             PRIMARY KEY (`order_id`, `produce_request_id`),
                                                             INDEX `fk_produce_request_buyers_produce_requests1_idx` (`produce_request_id` ASC) VISIBLE,
                                                             CONSTRAINT `fk_produce_request_buyers_produce_requests1`
                                                                 FOREIGN KEY (`produce_request_id`)
                                                                     REFERENCES `farmers_service_db`.`produce_requests` (`produce_request_id`)
                                                                     ON DELETE NO ACTION
                                                                     ON UPDATE NO ACTION)
    ENGINE = InnoDB;
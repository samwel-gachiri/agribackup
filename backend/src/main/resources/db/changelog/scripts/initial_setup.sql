CREATE TABLE IF NOT EXISTS `farmers_service_db`.`farmers` (
                                                              `farmer_id` VARCHAR(36) NOT NULL,
                                                              `name` VARCHAR(100) NULL,
                                                              `email` VARCHAR(45) NULL,
                                                              `phone_number` VARCHAR(25) NULL,
                                                              `created_at` TIMESTAMP NULL,
                                                              PRIMARY KEY (`farmer_id`))
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `farmers_service_db`.`farm_produces`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `farmers_service_db`.`farm_produces` (
                                                                    `farm_produce_id` VARCHAR(36) NOT NULL,
                                                                    `name` VARCHAR(45) NULL,
                                                                    `description` VARCHAR(100) NULL,
                                                                    `farming_type` VARCHAR(45) NULL,
                                                                    `status` ENUM('INACTIVE', 'ACTIVE', 'IS_SELLING') NULL,
                                                                    PRIMARY KEY (`farm_produce_id`),
                                                                    UNIQUE INDEX `id_UNIQUE` (`farm_produce_id` ASC) VISIBLE)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `farmers_service_db`.`farmer_produces`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `farmers_service_db`.`farmer_produces` (
                                                                      `id` VARCHAR(36) NOT NULL,
                                                                      `farmer_id` VARCHAR(36) NOT NULL,
                                                                      `farm_produce_id` VARCHAR(36) NOT NULL,
                                                                      `status` ENUM('INACTIVE', 'ON_FARM', 'ON_SALE') NULL,
                                                                      PRIMARY KEY (`id`, `farmer_id`, `farm_produce_id`),
                                                                      INDEX `fk_farmers_has_farm_produces_farm_produces1_idx` (`farm_produce_id` ASC) VISIBLE,
                                                                      INDEX `fk_farmers_has_farm_produces_farmers_idx` (`farmer_id` ASC) VISIBLE,
                                                                      CONSTRAINT `fk_farmers_has_farm_produces_farmers`
                                                                          FOREIGN KEY (`farmer_id`)
                                                                              REFERENCES `farmers_service_db`.`farmers` (`farmer_id`)
                                                                              ON DELETE NO ACTION
                                                                              ON UPDATE NO ACTION,
                                                                      CONSTRAINT `fk_farmers_has_farm_produces_farm_produces1`
                                                                          FOREIGN KEY (`farm_produce_id`)
                                                                              REFERENCES `farmers_service_db`.`farm_produces` (`farm_produce_id`)
                                                                              ON DELETE NO ACTION
                                                                              ON UPDATE NO ACTION)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `farmers_service_db`.`produce_listings`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `farmers_service_db`.`produce_listings` (
                                                                       `produce_listing_id` VARCHAR(36) NOT NULL,
                                                                       `quantity` DOUBLE NULL,
                                                                       `price` DOUBLE NULL,
                                                                       `currency` VARCHAR(25) NULL,
                                                                       `unit` VARCHAR(45) NULL,
                                                                       `rating` DOUBLE NULL,
                                                                       `status` ENUM('ACTIVE', 'INACTIVE', 'CANCELLED') NULL,
                                                                       `farmer_produces_id` VARCHAR(100) NOT NULL,
                                                                       PRIMARY KEY (`produce_listing_id`, `farmer_produces_id`),
                                                                       UNIQUE INDEX `idfarm_produce_for_sale_UNIQUE` (`produce_listing_id` ASC) VISIBLE,
                                                                       INDEX `fk_produce_listings_farmer_produces1_idx` (`farmer_produces_id` ASC) VISIBLE,
                                                                       CONSTRAINT `fk_produce_listings_farmer_produces1`
                                                                           FOREIGN KEY (`farmer_produces_id`)
                                                                               REFERENCES `farmers_service_db`.`farmer_produces` (`id`)
                                                                               ON DELETE NO ACTION
                                                                               ON UPDATE NO ACTION)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `farmers_service_db`.`fs_locations`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `farmers_service_db`.`fs_locations` (
                                                                   `id` VARCHAR(36) NOT NULL,
                                                                   `latitude` DOUBLE NULL,
                                                                   `longitude` DOUBLE NULL,
                                                                   `customName` VARCHAR(45) NULL,
                                                                   `farmer_id` VARCHAR(36) NOT NULL,
                                                                   INDEX `fk_fs_location_farmers1_idx` (`farmer_id` ASC) VISIBLE,
                                                                   PRIMARY KEY (`id`),
                                                                   CONSTRAINT `fk_fs_location_farmers1`
                                                                       FOREIGN KEY (`farmer_id`)
                                                                           REFERENCES `farmers_service_db`.`farmers` (`farmer_id`)
                                                                           ON DELETE NO ACTION
                                                                           ON UPDATE NO ACTION)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `farmers_service_db`.`orders`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `farmers_service_db`.`orders` (
                                                             `order_id` VARCHAR(36) NOT NULL,
                                                             `buyer_id` VARCHAR(36) NOT NULL,
                                                             `date_bought` TIMESTAMP NOT NULL,
                                                             `quantity` DOUBLE NOT NULL,
                                                             `status` ENUM('VIEWED', 'BOOKED_FOR_SUPPLY', 'SUPPLIED', 'SUPPLIED_AND_PAID', 'CANCELLED') NULL,
                                                             `produce_listing_id` VARCHAR(36) NOT NULL,
                                                             PRIMARY KEY (`order_id`, `produce_listing_id`),
                                                             INDEX `fk_produce_listing_buyers_produce_listings1_idx` (`produce_listing_id` ASC) VISIBLE,
                                                             CONSTRAINT `fk_produce_listing_buyers_produce_listings1`
                                                                 FOREIGN KEY (`produce_listing_id`)
                                                                     REFERENCES `farmers_service_db`.`produce_listings` (`produce_listing_id`)
                                                                     ON DELETE NO ACTION
                                                                     ON UPDATE NO ACTION)
    ENGINE = InnoDB;
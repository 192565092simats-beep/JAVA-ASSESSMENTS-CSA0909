-- =============================================================================
-- Task B3: Bus-Pass Relational Schema (schema.sql)
-- Target Database: MySQL 8.0+ / MariaDB / PostgreSQL
-- Student: Thejas Balaji (Register Number: 192565092)
-- Course: Programming in Java (CO5: Data Persistence Engineering)
-- =============================================================================

DROP TABLE IF EXISTS bus_pass;

CREATE TABLE bus_pass (
    pass_id VARCHAR(20) PRIMARY KEY,
    reg_no VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    route_no INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_bus_pass_route (route_no),
    INDEX idx_bus_pass_reg (reg_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample Verification Insert
INSERT INTO bus_pass (pass_id, reg_no, name, route_no, amount)
VALUES ('P1001', '192565092', 'Thejas Balaji', 12, 4500.00);

-- Verification Query
SELECT * FROM bus_pass;

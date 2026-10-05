-- ============================================================
-- Campus Canteen Pre-Order & Pickup System - database schema
-- MySQL 8. Run:  mysql -u root -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_canteen
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_canteen;

-- App login (least privilege: only this DB, no admin rights)
CREATE USER IF NOT EXISTS 'canteen'@'localhost' IDENTIFIED BY 'canteen123';
GRANT ALL PRIVILEGES ON campus_canteen.* TO 'canteen'@'localhost';
FLUSH PRIVILEGES;

-- ---------------- users ----------------
CREATE TABLE IF NOT EXISTS users (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  email         VARCHAR(150) NOT NULL UNIQUE,
  password_hash CHAR(64)     NOT NULL,   -- SHA-256(salt + password), hex
  salt          VARCHAR(64)  NOT NULL,
  phone         VARCHAR(20),
  role          ENUM('STUDENT','ADMIN') NOT NULL DEFAULT 'STUDENT',
  email_verified BOOLEAN NOT NULL DEFAULT FALSE,
  verify_code   CHAR(6),
  verify_expires TIMESTAMP NULL,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------- menu_items ----------------
CREATE TABLE IF NOT EXISTS menu_items (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(120) NOT NULL,
  description TEXT,
  price       DECIMAL(10,2) NOT NULL,
  category    VARCHAR(60) NOT NULL,
  veg         BOOLEAN NOT NULL DEFAULT TRUE,
  available   BOOLEAN NOT NULL DEFAULT TRUE,
  image_url   VARCHAR(500)
) ENGINE=InnoDB;

-- ---------------- pickup_slots ----------------
CREATE TABLE IF NOT EXISTS pickup_slots (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  slot_date  DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time   TIME NOT NULL,
  max_orders INT NOT NULL DEFAULT 20,
  active     BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE KEY uq_slot (slot_date, start_time, end_time)
) ENGINE=InnoDB;

-- ---------------- orders ----------------
CREATE TABLE IF NOT EXISTS orders (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  user_id       INT NOT NULL,
  slot_id       INT NOT NULL,
  status        ENUM('PLACED','ACCEPTED','PREPARING','READY','PICKED_UP','REJECTED')
                NOT NULL DEFAULT 'PLACED',
  total         DECIMAL(10,2) NOT NULL,
  payment_mode  VARCHAR(20) NOT NULL DEFAULT 'PAY_AT_CANTEEN',
  note          VARCHAR(255),
  reject_reason VARCHAR(255),
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (slot_id) REFERENCES pickup_slots(id)
) ENGINE=InnoDB;

-- ---------------- order_items ----------------
CREATE TABLE IF NOT EXISTS order_items (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  order_id       INT NOT NULL,
  menu_item_id   INT NOT NULL,
  qty            INT NOT NULL,
  price_at_order DECIMAL(10,2) NOT NULL,  -- frozen at order time
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
) ENGINE=InnoDB;

-- ---------------- feedback ----------------
CREATE TABLE IF NOT EXISTS feedback (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  order_id   INT NOT NULL UNIQUE,   -- one feedback per order
  user_id    INT NOT NULL,
  rating     INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment    TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- ============================================================
-- SEED DATA
-- ============================================================

-- Admin account: admin@canteen.local / admin123
-- !!! CHANGE THIS PASSWORD (or delete the row) after first login !!!
-- Admin account: admin@canteen.local / admin123
-- (hash = SHA-256( salt_hex_string + "admin123" ), exactly as
--  PasswordUtil.hash() computes it. CHANGE this password after first login.)
INSERT IGNORE INTO users (name, email, password_hash, salt, phone, role, email_verified) VALUES
('Canteen Admin', 'admin@canteen.local',
 'd4d13358f4ce677cf7532d2633c39ad8987a331c02409c79c5a338e9ff4af8f7',
 'a2916edd003ffb0d2f2cd5d322f1ca06', '9000000000', 'ADMIN', TRUE);

-- Sample menu (run once - no unique key, re-running would duplicate rows)
INSERT INTO menu_items (name, description, price, category, veg, available) VALUES
('Masala Dosa', 'Crispy dosa with potato masala, chutney & sambar', 50.00, 'Breakfast', TRUE, TRUE),
('Poha', 'Flattened rice with peanuts, curry leaves & lemon', 30.00, 'Breakfast', TRUE, TRUE),
('Veg Thali', 'Roti, rice, dal, 2 sabzi, salad & papad', 90.00, 'Lunch', TRUE, TRUE),
('Chicken Biryani', 'Hyderabadi style with mirchi ka salan', 120.00, 'Lunch', FALSE, TRUE),
('Paneer Butter Masala + Naan', 'Rich paneer curry with 2 butter naans', 110.00, 'Lunch', TRUE, TRUE),
('Samosa (2 pc)', 'With tamarind & mint chutney', 25.00, 'Snacks', TRUE, TRUE),
('Veg Sandwich', 'Grilled triple-layer with mint mayo', 40.00, 'Snacks', TRUE, TRUE),
('Cold Coffee', 'Blended with vanilla ice cream', 60.00, 'Beverages', TRUE, TRUE),
('Masala Chai', 'Kadak cutting chai', 20.00, 'Beverages', TRUE, TRUE),
('Egg Curry + Rice', 'Homestyle egg curry with steamed rice', 95.00, 'Lunch', FALSE, FALSE);

-- Pickup slots for today + next 2 days (safe to re-run any day: INSERT IGNORE + date-based)
INSERT IGNORE INTO pickup_slots (slot_date, start_time, end_time, max_orders, active) VALUES
(CURDATE(),                 '08:00', '08:30', 25, TRUE),
(CURDATE(),                 '12:00', '12:30', 40, TRUE),
(CURDATE(),                 '12:30', '13:00', 40, TRUE),
(CURDATE(),                 '16:00', '16:30', 25, TRUE),
(CURDATE() + INTERVAL 1 DAY,'08:00', '08:30', 25, TRUE),
(CURDATE() + INTERVAL 1 DAY,'12:00', '12:30', 40, TRUE),
(CURDATE() + INTERVAL 1 DAY,'12:30', '13:00', 40, TRUE),
(CURDATE() + INTERVAL 1 DAY,'16:00', '16:30', 25, TRUE),
(CURDATE() + INTERVAL 2 DAY,'08:00', '08:30', 25, TRUE),
(CURDATE() + INTERVAL 2 DAY,'12:00', '12:30', 40, TRUE),
(CURDATE() + INTERVAL 2 DAY,'12:30', '13:00', 40, TRUE),
(CURDATE() + INTERVAL 2 DAY,'16:00', '16:30', 25, TRUE);

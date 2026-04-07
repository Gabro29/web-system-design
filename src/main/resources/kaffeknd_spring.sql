
--
-- Struttura della tabella utente
--

CREATE TABLE IF NOT EXISTS utente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    credito DECIMAL(10, 2) DEFAULT 0.00,
    ruolo VARCHAR(15) NOT NULL
);

INSERT IGNORE INTO utente (email, password, credito, ruolo) VALUES
('mario.rossi@domain.it', '$2a$12$zf6sn.na1wXTs1j.iOpG1OQJlKQXOfJOBry6jr9nhzv9o66ot5Nt.', 5.50, 'CLIENTE'),
('luigi.verdi@unipa.it', '$2a$12$zf6sn.na1wXTs1j.iOpG1OQJlKQXOfJOBry6jr9nhzv9o66ot5Nt.', 10.00, 'CLIENTE'),
('admin@kaffe.it', '$2a$12$N0DTBAde1Xv8RwEJkM3Fbuyt0EsM8nvzvn1c7TOfbU4J8nlaAaKuC', 0.00, 'GESTORE'),
('tecnico@kaffe.it', '$2a$12$5zQ74ihhlk.NuRuhaB5k8.gKMQTFihzWAiXQaEe765Ewe.oGu3TWm', 0.00, 'ADDETTO'),
('giovanni.rana@kaffe.it', '$2a$12$5zQ74ihhlk.NuRuhaB5k8.gKMQTFihzWAiXQaEe765Ewe.oGu3TWm', 0.00, 'ADDETTO'),
('gianfranco.fini@kaffe.it', '$2a$12$5zQ74ihhlk.NuRuhaB5k8.gKMQTFihzWAiXQaEe765Ewe.oGu3TWm', 0.00, 'ADDETTO'),
('carlo.buttita@kaffe.it', '$2a$12$5zQ74ihhlk.NuRuhaB5k8.gKMQTFihzWAiXQaEe765Ewe.oGu3TWm', 0.00, 'ADDETTO'),
('silvio.berlusconi@kaffe.it', '$2a$12$5zQ74ihhlk.NuRuhaB5k8.gKMQTFihzWAiXQaEe765Ewe.oGu3TWm', 0.00, 'ADDETTO');

-- --------------------------------------------------------

--
-- Struttura della tabella macchinetta
--

CREATE TABLE IF NOT EXISTS macchinetta (
    code VARCHAR(7) PRIMARY KEY,
    status VARCHAR(11) DEFAULT 'ACTIVE',
    luogo VARCHAR(255),
    current_user_id BIGINT UNIQUE,
    last_user_interaction DATETIME,
    CONSTRAINT fk_macchinetta_user FOREIGN KEY (current_user_id) REFERENCES utente(id)
);

INSERT IGNORE INTO macchinetta (code, status, luogo) VALUES
('FE716XW', 'ACTIVE', 'Edificio 12 - Piano 1'),
('DR529QP', 'MAINTENANCE', 'Edificio 8 - Piano Terra'),
('AB123CD', 'MAINTENANCE', 'Edificio 19 - Piano 2'),
('GH890LM', 'ERROR', 'Edificio 6 - Piano 3');

-- --------------------------------------------------------

--
-- Struttura della tabella prodotto
--

CREATE TABLE IF NOT EXISTS prodotto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) UNIQUE NOT NULL,
    categoria VARCHAR(50),
    prezzo DECIMAL(10, 2) NOT NULL,
    image_path VARCHAR(255),
    available BOOLEAN DEFAULT TRUE
);

INSERT IGNORE INTO prodotto (id, nome, categoria, prezzo, image_path, available) VALUES
(1, 'Espresso', 'Bevanda Calda', 1.00, '/data/images/products/espresso.png', true),
(2, 'Cappuccino', 'Bevanda Calda', 1.50, '/data/images/products/cappuccino.png', true),
(3, 'Latte', 'Bevanda Calda', 1.50, '/data/images/products/milk.png', true),
(4, 'Cioccolata', 'Bevanda Calda', 2.00, '/data/images/products/chocolate.png', true),
(5, 'Tè', 'Bevanda Calda', 1.00, '/data/images/products/tea.png', true),
(6, 'Acqua', 'Bevanda Fredda', 0.50, '/data/images/products/water.png', true);

-- --------------------------------------------------------

--
-- Struttura della tabella statistica_erogazione
--

CREATE TABLE IF NOT EXISTS statistica_erogazione (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    macchinetta_code VARCHAR(7) NOT NULL,
    prodotto_id BIGINT NOT NULL,
    conteggio INT DEFAULT 0,
    CONSTRAINT uq_statistica UNIQUE (macchinetta_code, prodotto_id),
    CONSTRAINT fk_stat_macchina FOREIGN KEY (macchinetta_code) REFERENCES macchinetta(code) ON DELETE CASCADE,
    CONSTRAINT fk_stat_prodotto FOREIGN KEY (prodotto_id) REFERENCES prodotto(id) ON DELETE CASCADE
);

-- --------------------------------------------------------

--
-- Struttura della tabella fornitura
--

CREATE TABLE IF NOT EXISTS fornitura (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    livello INT NOT NULL,
    capacita_massima INT DEFAULT 100,
    soglia_attenzione INT DEFAULT 20,
    unita VARCHAR(2) DEFAULT 'ML',
    macchinetta_code VARCHAR(7) NOT NULL,
    CONSTRAINT fk_fornitura_macchinetta FOREIGN KEY (macchinetta_code) REFERENCES macchinetta(code) ON DELETE CASCADE,
    CONSTRAINT uq_fornitura_macchina UNIQUE (nome, macchinetta_code)
);

INSERT IGNORE INTO fornitura (id, nome, livello, capacita_massima, soglia_attenzione, unita, macchinetta_code) VALUES
(1, 'Acqua', 9500, 10000, 1500, 'ML', 'FE716XW'),
(2, 'Caffè', 4200, 5000, 1000, 'G', 'FE716XW'),
(3, 'Zucchero', 2800, 3000, 500, 'G', 'FE716XW'),
(4, 'Bicchieri', 4500, 5000, 50, 'G', 'FE716XW'),
(5, 'Latte', 4100, 5000, 1000, 'ML', 'FE716XW'),
(6, 'Tè', 850, 1000, 200, 'G', 'FE716XW'),
(7, 'Cioccolata', 1800, 2000, 400, 'G', 'FE716XW'),
(8, 'Acqua', 5200, 10000, 1500, 'ML', 'DR529QP'),
(9, 'Caffè', 2500, 5000, 1000, 'G', 'DR529QP'),
(10, 'Zucchero', 1200, 3000, 500, 'G', 'DR529QP'),
(11, 'Bicchieri', 2000, 5000, 50, 'G', 'DR529QP'),
(12, 'Latte', 800, 5000, 1000, 'ML', 'DR529QP'),
(13, 'Tè', 400, 1000, 200, 'G', 'DR529QP'),
(14, 'Cioccolata', 900, 2000, 400, 'G', 'DR529QP'),
(15, 'Acqua', 8000, 10000, 1500, 'ML', 'GH890LM'),
(16, 'Caffè', 3000, 5000, 1000, 'G', 'GH890LM'),
(17, 'Zucchero', 100, 3000, 500, 'G', 'GH890LM'),
(18, 'Bicchieri', 0, 5000, 50, 'G', 'GH890LM'),
(19, 'Latte', 3000, 5000, 1000, 'ML', 'GH890LM'),
(20, 'Tè', 600, 1000, 200, 'G', 'GH890LM'),
(21, 'Cioccolata', 1200, 2000, 400, 'G', 'GH890LM'),
(22, 'Acqua', 7500, 10000, 1500, 'ML', 'AB123CD'),
(23, 'Caffè', 3500, 5000, 1000, 'G', 'AB123CD'),
(24, 'Zucchero', 0, 3000, 500, 'G', 'AB123CD'),
(25, 'Bicchieri', 2000, 5000, 50, 'G', 'AB123CD'),
(26, 'Latte', 0, 5000, 1000, 'ML', 'AB123CD'),
(27, 'Tè', 700, 1000, 200, 'G', 'AB123CD'),
(28, 'Cioccolata', 800, 2000, 400, 'G', 'AB123CD');

-- --------------------------------------------------------

--
-- Struttura della tabella ingrediente
--

CREATE TABLE IF NOT EXISTS ingrediente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prodotto_id BIGINT NOT NULL,
    nome_fornitura VARCHAR(50) NOT NULL,
    quantita INT NOT NULL,
    CONSTRAINT fk_ricetta_prodotto FOREIGN KEY (prodotto_id) REFERENCES prodotto(id) ON DELETE CASCADE
);

INSERT IGNORE INTO ingrediente (prodotto_id, nome_fornitura, quantita) VALUES
(1, 'Caffè', 7),
(1, 'Acqua', 30),
(2, 'Caffè', 7),
(2, 'Latte', 50),
(2, 'Acqua', 30),
(3, 'Latte', 150),
(4, 'Cioccolata', 20),
(4, 'Acqua', 100),
(5, 'Tè', 1),
(5, 'Acqua', 150),
(6, 'Acqua', 200);

-- --------------------------------------------------------

--
-- Struttura della tabella messaggio
--

CREATE TABLE IF NOT EXISTS messaggio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    testo VARCHAR(255),
    tipo VARCHAR(20),
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    macchinetta_code VARCHAR(7),
    CONSTRAINT fk_messaggio_macchinetta FOREIGN KEY (macchinetta_code) REFERENCES macchinetta(code) ON DELETE CASCADE
);

INSERT IGNORE INTO messaggio (id, testo, tipo, timestamp, macchinetta_code) VALUES
(1, 'Latte in esaurimento', 'WARNING', NOW(), 'DR529QP'),
(2, 'Bicchieri terminato', 'ERROR', NOW(), 'GH890LM'),
(3, 'Zucchero terminato', 'ERROR', NOW() - INTERVAL 5 HOUR, 'AB123CD'),
(4, 'Latte terminato', 'ERROR', NOW() - INTERVAL 7 HOUR, 'AB123CD');

-- Insert the default user only if the table is empty
INSERT INTO users (id, username, password)
SELECT 1, 'admin', '$2a$10$l.AZZDGpGnA1tsVAjhs5ju59n7tbF.bwRQaAig38/kCHqa4PC.nf2'
    WHERE NOT EXISTS (SELECT 1 FROM users);

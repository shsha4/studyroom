SET SESSION lock_wait_timeout = 5;

INSERT INTO room (uuid, room_name, capacity) VALUES
    (UUID_TO_BIN('01a1a17e-bc80-7d74-b0dc-9e4f66d13344'), 'A룸', 4),
    (UUID_TO_BIN('01a1a17e-bc81-7046-a5ee-42efbaf69c2c'), 'B룸', 6),
    (UUID_TO_BIN('01a1a17e-bc82-78c2-bf75-3ea9e88d9ffd'), '세미나실', 12);

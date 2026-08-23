INSERT INTO test_users (
    id,
    username,
    role
) VALUES (
    1,
    'admin',
    'tester'
)
ON DUPLICATE KEY UPDATE
    username = 'admin',
    role = 'tester';

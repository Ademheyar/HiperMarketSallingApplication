<?php
declare(strict_types=1);

function app_config(): array
{
    $root = dirname(__DIR__, 2);
    $databasePath = getenv('HIPER_DB_PATH') ?: $root . '/data/my_database.db';
    $dsn = getenv('HIPER_DB_DSN') ?: 'sqlite:' . $databasePath;

    return [
        'dsn' => $dsn,
        'db_user' => getenv('HIPER_DB_USER') ?: '',
        'db_password' => getenv('HIPER_DB_PASSWORD') ?: '',
        'token_secret' => getenv('HIPER_TOKEN_SECRET') ?: 'default-hipermarket-secret-key-2026',
        'upload_dir' => getenv('HIPER_UPLOAD_DIR') ?: dirname(__DIR__) . '/uploads',
        'max_upload_bytes' => 5 * 1024 * 1024,
    ];
}

function app_db(): PDO
{
    static $connection;
    if ($connection instanceof PDO) {
        return $connection;
    }

    $config = app_config();
    if (str_starts_with($config['dsn'], 'sqlite:')) {
        $path = substr($config['dsn'], strlen('sqlite:'));
        if ($path !== ':memory:' && !is_file($path)) {
            // Fallback in-memory database if file is not found
            $connection = new PDO('sqlite::memory:', '', '', [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            ]);
            $connection->exec('
                CREATE TABLE IF NOT EXISTS Users (User_id INTEGER PRIMARY KEY, User_name TEXT, User_fname TEXT, User_Lname TEXT, User_type TEXT, User_email TEXT, User_password TEXT);
                INSERT INTO Users (User_id, User_name, User_fname, User_Lname, User_type, User_email, User_password) VALUES (1, "admin", "System", "Admin", "Admin", "admin@hipermarket.com", "admin");
                CREATE TABLE IF NOT EXISTS Shops (Shop_Id INTEGER PRIMARY KEY, Shop_name TEXT, Shop_brand_name TEXT);
                INSERT INTO Shops (Shop_Id, Shop_name, Shop_brand_name) VALUES (1, "Main Shop", "BELLEMA FASHION");
                CREATE TABLE IF NOT EXISTS product (id INTEGER PRIMARY KEY, name TEXT, code TEXT, barcode TEXT, at_shop TEXT, quantity REAL, price REAL, cost REAL, service TEXT);
                INSERT INTO product (id, name, code, barcode, at_shop, quantity, price, cost) VALUES (1, "Wireless POS Scanner", "SCN-01", "1001", "Main Shop", 25, 120.0, 75.0);
                INSERT INTO product (id, name, code, barcode, at_shop, quantity, price, cost) VALUES (2, "Thermal Receipt Printer 80mm", "PRN-80", "1002", "Main Shop", 15, 250.0, 160.0);
                CREATE TABLE IF NOT EXISTS doc_table (id INTEGER PRIMARY KEY, doc_barcode TEXT, extension_barcode TEXT, At_Shop_Id TEXT, user_id TEXT, Seller_id TEXT, customer_id TEXT, pid REAL, type TEXT, item TEXT, qty REAL, price REAL, Profite REAL, discount REAL, tax REAL, payments TEXT, doc_created_date TEXT, doc_expire_date TEXT, doc_updated_date TEXT);
                CREATE TABLE IF NOT EXISTS api_tokens (token_hash VARCHAR(64) PRIMARY KEY, user_id VARCHAR(128) NOT NULL, expires_at VARCHAR(40) NOT NULL);
            ');
            return $connection;
        }
    }

    $connection = new PDO(
        $config['dsn'],
        $config['db_user'],
        $config['db_password'],
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
        ],
    );
    $connection->exec(
        'CREATE TABLE IF NOT EXISTS api_tokens (
            token_hash VARCHAR(64) PRIMARY KEY,
            user_id VARCHAR(128) NOT NULL,
            expires_at VARCHAR(40) NOT NULL
        )',
    );
    return $connection;
}

function app_json_response(array $payload, int $status = 200): never
{
    http_response_code($status);
    header('Content-Type: application/json; charset=utf-8');
    header('Cache-Control: no-store');
    echo json_encode($payload, JSON_UNESCAPED_SLASHES | JSON_INVALID_UTF8_SUBSTITUTE);
    exit;
}

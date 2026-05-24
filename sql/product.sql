CREATE TABLE product (
    id VARCHAR(100) PRIMARY KEY,

    title TEXT NOT NULL,

    price NUMERIC(19, 2),
    original_price NUMERIC(19, 2),

    currency_id VARCHAR(10),

    permalink TEXT,
    thumbnail TEXT,

    condition VARCHAR(50),

    free_shipping BOOLEAN,

    available_quantity INTEGER,
    seller_id BIGINT,

    category_id VARCHAR(50),

    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);
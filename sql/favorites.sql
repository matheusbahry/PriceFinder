CREATE TABLE favorites (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    product_name TEXT NOT NULL,

    product_url TEXT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_favorites_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT uk_user_product_url
        UNIQUE (user_id, product_url)
);
CREATE TABLE IF NOT EXISTS orders (
                                       order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       username VARCHAR(255) NOT NULL,
                                       cart_id UUID NOT NULL,
                                       payment_id UUID,
                                       delivery_id UUID,
                                       order_state VARCHAR(50) NOT NULL,
                                       weight double precision,
                                       volume double precision,
                                       fragile boolean,
                                       total_price double precision,
                                       delivery_price double precision,
                                       product_price double precision
);

CREATE TABLE IF NOT EXISTS order_products (
    order_id UUID REFERENCES orders(order_id),
    product_id UUID,
    quantity integer,
    PRIMARY KEY (order_id, product_id)
);
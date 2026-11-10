-- =========================================================================
-- Farm-to-Table PostgreSQL Stored Procedures & Functions Initialization
-- Database: farm_to_table
-- =========================================================================

-- 1. Category Stored Procedures & Functions
CREATE OR REPLACE PROCEDURE sp_insert_category(
    IN p_name VARCHAR,
    IN p_description VARCHAR,
    IN p_status VARCHAR,
    OUT p_id BIGINT
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO category_entity (name, description, status, created_at, updated_at)
    VALUES (p_name, p_description, p_status, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id INTO p_id;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_update_category(
    IN p_id BIGINT,
    IN p_name VARCHAR,
    IN p_description VARCHAR,
    IN p_status VARCHAR
)
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE category_entity
    SET name = p_name, description = p_description, status = p_status, updated_at = CURRENT_TIMESTAMP
    WHERE id = p_id;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_delete_category(IN p_id BIGINT)
LANGUAGE plpgsql AS $$
BEGIN
    DELETE FROM category_entity WHERE id = p_id;
END;
$$;

CREATE OR REPLACE FUNCTION fn_get_category_by_id(p_id BIGINT)
RETURNS SETOF category_entity
LANGUAGE sql AS $$
    SELECT * FROM category_entity WHERE id = p_id;
$$;

CREATE OR REPLACE FUNCTION fn_get_all_categories()
RETURNS SETOF category_entity
LANGUAGE sql AS $$
    SELECT * FROM category_entity ORDER BY id ASC;
$$;

-- 2. Product Stored Procedures & Functions
CREATE OR REPLACE PROCEDURE sp_insert_product(
    IN p_user_id VARCHAR,
    IN p_category_id VARCHAR,
    IN p_name VARCHAR,
    IN p_qty INTEGER,
    IN p_unit_price DOUBLE PRECISION,
    IN p_farmer_price DOUBLE PRECISION,
    IN p_expected_date DATE,
    IN p_expired_date DATE,
    IN p_location VARCHAR,
    OUT p_id BIGINT
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO product_entity (user_id, category_id, name, qty, unit_price, farmer_price,
                                expected_havest_date, expired_date, location, created_at, updated_at)
    VALUES (p_user_id, p_category_id, p_name, p_qty, p_unit_price, p_farmer_price,
            p_expected_date, p_expired_date, p_location, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id INTO p_id;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_update_product(
    IN p_id BIGINT,
    IN p_user_id VARCHAR,
    IN p_category_id VARCHAR,
    IN p_name VARCHAR,
    IN p_qty INTEGER,
    IN p_unit_price DOUBLE PRECISION,
    IN p_farmer_price DOUBLE PRECISION,
    IN p_expected_date DATE,
    IN p_expired_date DATE,
    IN p_location VARCHAR
)
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE product_entity
    SET user_id = p_user_id, category_id = p_category_id, name = p_name, qty = p_qty,
        unit_price = p_unit_price, farmer_price = p_farmer_price,
        expected_havest_date = p_expected_date, expired_date = p_expired_date,
        location = p_location, updated_at = CURRENT_TIMESTAMP
    WHERE id = p_id;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_delete_product(IN p_id BIGINT)
LANGUAGE plpgsql AS $$
BEGIN
    DELETE FROM product_entity WHERE id = p_id;
END;
$$;

CREATE OR REPLACE FUNCTION fn_get_product_by_id(p_id BIGINT)
RETURNS SETOF product_entity
LANGUAGE sql AS $$
    SELECT * FROM product_entity WHERE id = p_id;
$$;

CREATE OR REPLACE FUNCTION fn_get_all_products()
RETURNS SETOF product_entity
LANGUAGE sql AS $$
    SELECT * FROM product_entity ORDER BY id DESC;
$$;

-- 3. Order Details Stored Procedures & Functions
CREATE OR REPLACE PROCEDURE sp_insert_order_detail(
    IN p_order_id VARCHAR,
    IN p_product_id VARCHAR,
    IN p_qty INTEGER,
    IN p_unit_price DOUBLE PRECISION,
    IN p_other_method_id VARCHAR,
    IN p_payment_method_id VARCHAR,
    OUT p_id BIGINT
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO order_detail_entity (order_id, product_id, qty, unit_price, sub_total,
                                     other_method_id, payment_method_id, created_at, updated_at)
    VALUES (p_order_id, p_product_id, p_qty, p_unit_price, (p_qty * p_unit_price),
            p_other_method_id, p_payment_method_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id INTO p_id;
END;
$$;

CREATE OR REPLACE FUNCTION fn_get_order_detail_by_id(p_id BIGINT)
RETURNS TABLE (
    id BIGINT, order_id VARCHAR, product_id VARCHAR, qty INTEGER, unit_price DOUBLE PRECISION,
    sub_total DOUBLE PRECISION, other_method_id VARCHAR, payment_method_id VARCHAR,
    created_at TIMESTAMP, updated_at TIMESTAMP
) LANGUAGE sql AS $$
    SELECT id, order_id, product_id, qty, unit_price, sub_total,
           other_method_id, payment_method_id, created_at, updated_at
    FROM order_detail_entity WHERE id = p_id;
$$;

CREATE OR REPLACE FUNCTION fn_list_order_detail(p_order_id VARCHAR, p_limit INTEGER, p_offset INTEGER)
RETURNS TABLE (
    id BIGINT, order_id VARCHAR, product_id VARCHAR, qty INTEGER, unit_price DOUBLE PRECISION,
    sub_total DOUBLE PRECISION, other_method_id VARCHAR, payment_method_id VARCHAR,
    created_at TIMESTAMP, updated_at TIMESTAMP
) LANGUAGE sql AS $$
    SELECT id, order_id, product_id, qty, unit_price, sub_total,
           other_method_id, payment_method_id, created_at, updated_at
    FROM order_detail_entity
    WHERE (p_order_id IS NULL OR order_id = p_order_id)
    ORDER BY id ASC
    LIMIT p_limit OFFSET p_offset;
$$;


DROP TABLE IF EXISTS usage_logs CASCADE;
DROP TABLE IF EXISTS project_products CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- think about if I want to leave out the user by now because security is really difficult.
-- users will be created for now, but there will not be any authorisation yet(no password), anyone can enter if you know the username
CREATE TABLE users (
   id BIGSERIAL PRIMARY KEY,
   username VARCHAR(50) UNIQUE NOT NULL,
   email VARCHAR(100) UNIQUE NOT NULL,
-- password_hash VARCHAR(255) NOT NULL,
   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE products (
      id BIGSERIAL PRIMARY KEY,
      user_id INTEGER NOT NULL,
      category_id INTEGER NOT NULL,
      name VARCHAR(100) NOT NULL,
      brand VARCHAR(100),
      purchase_date DATE,
      opening_date DATE NOT NULL,
      period_after_opening_months INTEGER NOT NULL CHECK (period_after_opening_months >= 0),

      starting_weight_grams NUMERIC(5,2),
      current_weight_grams NUMERIC(5,2),

      rating INTEGER CHECK (rating BETWEEN 1 AND 10),
      is_finished BOOLEAN NOT NULL DEFAULT FALSE,
-- optional: image_url VARCHAR(255),

      CONSTRAINT fk_product_user
          FOREIGN KEY (user_id)
              REFERENCES users(id)
              ON DELETE CASCADE,

      CONSTRAINT fk_product_category
          FOREIGN KEY (category_id)
              REFERENCES categories(id)
              ON DELETE RESTRICT
);

CREATE TABLE projects (
      id BIGSERIAL PRIMARY KEY,
      user_id INTEGER NOT NULL,
      name VARCHAR(100) NOT NULL,
      description TEXT,
      start_date DATE NOT NULL DEFAULT CURRENT_DATE,
      end_date DATE,

      CONSTRAINT fk_project_user
          FOREIGN KEY (user_id)
              REFERENCES users(id)
              ON DELETE CASCADE
);

CREATE TABLE project_products (
      project_id INTEGER NOT NULL,
      product_id INTEGER NOT NULL,

      goal_type VARCHAR(30) NOT NULL DEFAULT 'FINISH_COMPLETELY',
      target_uses INTEGER CHECK (target_uses > 0),

      PRIMARY KEY (project_id, product_id),

      CONSTRAINT fk_junction_project
          FOREIGN KEY (project_id)
              REFERENCES projects(id)
              ON DELETE CASCADE,

      CONSTRAINT fk_junction_product
          FOREIGN KEY (product_id)
              REFERENCES products(id)
              ON DELETE CASCADE
);

CREATE TABLE usage_logs (
    id BIGSERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL,
    project_id INTEGER,
    use_date DATE NOT NULL DEFAULT CURRENT_DATE,
    weight_recorded NUMERIC(5,2),
    notes TEXT,

    CONSTRAINT fk_log_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_log_project
        FOREIGN KEY (project_id)
            REFERENCES projects(id)
            ON DELETE SET NULL
);
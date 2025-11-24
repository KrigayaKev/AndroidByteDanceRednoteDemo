CREATE TABLE post_image (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    post_id INTEGER NOT NULL,                -- 所属帖子 ID
    image_url TEXT NOT NULL,                 -- 图片 URL
    sort_order INTEGER DEFAULT 0,            -- 排序（0, 1, 2...），用于控制展示顺序
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
);
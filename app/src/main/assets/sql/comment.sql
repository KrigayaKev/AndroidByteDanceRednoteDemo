CREATE TABLE comment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    post_id INTEGER NOT NULL,                -- 帖子 ID，外键
    user_id INTEGER NOT NULL,                -- 评论者 ID
    content TEXT NOT NULL,                   -- 评论内容
    parent_comment_id INTEGER DEFAULT NULL,  -- 回复某条评论（支持嵌套）
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post(id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (parent_comment_id) REFERENCES comment(id)
);
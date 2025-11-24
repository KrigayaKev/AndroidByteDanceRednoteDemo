CREATE TABLE post (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,                     -- 标题，不可为空
    content TEXT,                            -- 正文内容，可为空
    video_url TEXT,                          -- 视频地址，可为空
    is_video BOOLEAN NOT NULL DEFAULT FALSE, -- 是否为视频帖
    cover_url TEXT NOT NULL,                 -- 封面图 URL，不可为空
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user(id)
);
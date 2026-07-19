CREATE TABLE notifications (
    id         UUID         NOT NULL PRIMARY KEY,
    user_id    UUID         NOT NULL,
    upload_id  UUID         NOT NULL,
    email      VARCHAR(255) NOT NULL,
    subject    VARCHAR(512) NOT NULL,
    message    TEXT,
    type       VARCHAR(50)  NOT NULL,
    sent       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user_id  ON notifications (user_id);
CREATE INDEX idx_notifications_upload_id ON notifications (upload_id);

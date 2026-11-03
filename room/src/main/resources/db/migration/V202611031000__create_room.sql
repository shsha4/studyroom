SET SESSION lock_wait_timeout = 5;

CREATE TABLE room (
    id          BIGINT          NOT NULL    AUTO_INCREMENT,
    uuid        BINARY(16)      NOT NULL    COMMENT '외부 노출 식별자. UUIDv7',
    room_name   VARCHAR(50)     NOT NULL    COMMENT '방 이름',
    capacity    INT             NOT NULL    COMMENT '최대 수용 인원',
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_room_uuid UNIQUE (uuid),
    CONSTRAINT ck_room_capacity CHECK (capacity > 0)
) COMMENT = '스터디 룸';

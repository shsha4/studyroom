SET SESSION lock_wait_timeout = 5;

CREATE TABLE reservation (
    id                  BIGINT      NOT NULL    AUTO_INCREMENT,
    uuid                BINARY(16)  NOT NULL    COMMENT '외부 노출 식별자. UUIDv7',
    room_id             BIGINT      NOT NULL    COMMENT '예약한 방. room.id',
    member_uuid         BINARY(16)  NOT NULL    COMMENT '예약한 회원 식별자. member 서비스 소유',
    start_at            DATETIME(6) NOT NULL    COMMENT '이용 시작 시각. 정시',
    end_at              DATETIME(6) NOT NULL    COMMENT '이용 종료 시각. 정시이고 이 시각은 포함하지 않는다',
    reservation_status  VARCHAR(30) NOT NULL    COMMENT '예약 상태. RESERVED/CANCELLED',
    cancelled_at        DATETIME(6) NULL        COMMENT '취소 시각. 취소 전에는 NULL',
    created_at          DATETIME(6) NOT NULL    DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6) NOT NULL    DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_reservation_uuid UNIQUE (uuid),
    INDEX ix_reservation_room_id_start_at (room_id, start_at)
) COMMENT = '스터디룸 예약';

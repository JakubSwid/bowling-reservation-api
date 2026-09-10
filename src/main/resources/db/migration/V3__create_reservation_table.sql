CREATE TABLE reservation(
    id BIGSERIAL PRIMARY KEY,
    lane_id BIGINT NOT NULL REFERENCES lane(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    reservation_status VARCHAR(50) NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    customer_email VARCHAR(200) NOT NULL,
    version INTEGER,
    CONSTRAINT check_time_range CHECK ( start_time < end_time )
);

CREATE INDEX index_reservation_lane_time ON reservation reservation(lane_id, start_time, end_time);
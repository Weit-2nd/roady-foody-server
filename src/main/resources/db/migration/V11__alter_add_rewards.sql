ALTER TABLE REWARDS
    ADD CONSTRAINT UQ_REWARDS_ON_FOOD_SPOTS_HISTORIES
        UNIQUE (food_spots_history_id);

ALTER TABLE REWARDS
    ADD reward_type NUMBER(1) NOT NULL;

ALTER TABLE REWARDS ADD reward_reason VARCHAR2(20) NOT NULL;

ALTER TABLE rewards
DROP CONSTRAINT FK_REWARDS_ON_FOOD_SPOTS_HISTORIES;

ALTER TABLE rewards
    ADD CONSTRAINT FK_REWARDS_ON_FOOD_SPOTS_HISTORIES
        FOREIGN KEY (food_spots_history_id) REFERENCES food_spots_histories(id)
            ON DELETE SET NULL;

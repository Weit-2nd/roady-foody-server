DROP INDEX FOOD_SPOTS_HISTORIES_ID_USER_ID_INDEX;

CREATE INDEX FOOD_SPOTS_HISTORIES_USER_ID_CREATED_DATETIME_INDEX
    ON FOOD_SPOTS_HISTORIES(USER_ID, CREATED_DATETIME)
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE appointment
    ADD CONSTRAINT ex_appointment_no_overlap
    EXCLUDE USING gist (
        physiotherapist_id WITH =,
        tstzrange(start_time, end_time, '[)') WITH &&
    )
    WHERE (status IN ('BOOKED', 'CONFIRMED'));
CREATE TABLE kvstore
(
  "key" TEXT PRIMARY KEY,
  "value" TEXT,
  "lastModified" timestamp default CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION update_lastmodified_column()
RETURNS TRIGGER AS '
  BEGIN
      IF NEW."value" != OLD."value" THEN
        NEW."lastModified" = NOW();
      END IF;
    RETURN NEW;
  END;
' LANGUAGE 'plpgsql';

-- AFTER UPDATE cannot alter the row, so we use BEFORE UPDATE
-- https://www.postgresql.org/docs/current/plpgsql-trigger.html
CREATE TRIGGER update_kvstore_lastmodified 
BEFORE UPDATE ON kvstore 
FOR EACH ROW EXECUTE PROCEDURE update_lastmodified_column();

CREATE TABLE contact
(
  "resourceName"  varchar(255) PRIMARY KEY,
    "name"          varchar(255),
    "email"         varchar(255),
    "phone"         varchar(255)
);
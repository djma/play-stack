CREATE TABLE kvstore
(
	"key" TEXT PRIMARY KEY,
	"value" TEXT,
	"lastModified" timestamp default CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION update_lastmodified_column()
RETURNS TRIGGER AS '
  BEGIN
    NEW."lastModified" = NOW();
    RETURN NEW;
  END;
' LANGUAGE 'plpgsql';

-- BEFORE UPDATE will always update the timestamp, even if the row is not changed, 
-- AFTER UPDATE will only update the timestamp if the row is changed
CREATE TRIGGER update_kvstore_lastmodified 
AFTER UPDATE ON kvstore 
FOR EACH ROW EXECUTE PROCEDURE update_lastmodified_column();

CREATE TABLE contact
(
	"resourceName"  varchar(255) PRIMARY KEY,
    "name"          varchar(255),
    "email"         varchar(255),
    "phone"         varchar(255)
);
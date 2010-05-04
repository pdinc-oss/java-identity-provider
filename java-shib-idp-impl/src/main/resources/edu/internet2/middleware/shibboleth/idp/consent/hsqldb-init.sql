DROP TABLE AttributeReleaseConsent IF EXISTS;
DROP TABLE AgreedTermsOfUse IF EXISTS;
DROP TABLE RelyingParty IF EXISTS;
DROP TABLE Principal IF EXISTS;

CREATE TABLE Principal (
    id                  INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1),
    uniqueId            VARCHAR(256)    NOT NULL,
    firstAccess         TIMESTAMP       DEFAULT 'NOW' NOT NULL,
    lastAccess          TIMESTAMP       DEFAULT 'NOW' NOT NULL,
    globalConsent       BIT             DEFAULT FALSE NOT NULL,
 
    PRIMARY KEY(id),
 
    UNIQUE (uniqueId)
);

CREATE TABLE RelyingParty (
    id                  INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1),
    entityId            VARCHAR(256)                  NOT NULL,
    
    PRIMARY KEY(id),
    
    UNIQUE (entityId)
);

CREATE TABLE AgreedTermsOfUse (
    principalId         INTEGER                       NOT NULL,
    version             VARCHAR(256)    DEFAULT '0.0'	NOT NULL,
    fingerprint         VARCHAR(256)    DEFAULT ''    NOT NULL,
    agreeDate           TIMESTAMP       DEFAULT 'NOW' NOT NULL,
    
    PRIMARY KEY (principalId, version),
    
    FOREIGN KEY (principalId) REFERENCES Principal(id) ON DELETE CASCADE
);

CREATE TABLE AttributeReleaseConsent (
    principalId         INTEGER                       NOT NULL,
    relyingPartyId      INTEGER                       NOT NULL,
    attributeId         VARCHAR(256)                  NOT NULL,
    attributeValuesHash VARCHAR(256)    DEFAULT ''    NOT NULL,
    releaseDate         TIMESTAMP       DEFAULT 'NOW' NOT NULL,

    PRIMARY KEY (principalId, relyingPartyId, attributeId),
    
    FOREIGN KEY (principalId) REFERENCES Principal(id) ON DELETE CASCADE,
    FOREIGN KEY (relyingPartyId) REFERENCES RelyingParty(id) ON DELETE CASCADE
);
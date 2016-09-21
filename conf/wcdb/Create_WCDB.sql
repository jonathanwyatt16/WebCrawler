CREATE DATABASE WCDB
GO

USE WCDB
GO

CREATE LOGIN WCDB_User WITH PASSWORD = 'WCDB_Pass', DEFAULT_DATABASE = [WCDB]
CREATE USER WCDB_User FOR LOGIN WCDB_User
ALTER ROLE db_owner ADD MEMBER WCDB_User
GO

CREATE TABLE WebCrawlRun
(
	wcrId int IDENTITY(1,1),
	wcrStart datetime NOT NULL,
	wcrEnd datetime,
	wcrType varchar(255) NOT NULL,
	wcrStatus int NOT NULL,
	wcrComment varchar(255),
	wcrPagesFound int,
	wcrPageCrawlSuccess int,
	wcrPageCrawlError int,
	CONSTRAINT pk_WebCrawlRun PRIMARY KEY (wcrId),
	CONSTRAINT uk_wcrStart UNIQUE (wcrStart)
)
GO
	
CREATE TABLE PageCrawlRun
(
	pcrId int IDENTITY(1,1),
	pcrWcrId int NOT NULL,
	pcrPageTitle varchar(255) NOT NULL,
	pcrStart datetime,
	pcrEnd datetime,
	pcrStatus int,
	pcrComment varchar(255),
	CONSTRAINT pk_PageCrawlRun PRIMARY KEY (pcrId),
	CONSTRAINT fk_pcrWcrId FOREIGN KEY (pcrWcrId) REFERENCES WebCrawlRun(wcrId)
)
GO

CREATE INDEX idx_pcrWcrId ON PageCrawlRun (pcrWcrId)
CREATE INDEX idx_pcrPageTitle ON PageCrawlRun (pcrPageTitle)
GO

CREATE TABLE QueryAudit
(
	qaId int IDENTITY(1,1),
	qaDate datetime NOT NULL,
	qaSource varchar(255),
	qaQuery varchar(1000),
	qaComment varchar(255),
	CONSTRAINT pk_QueryAudit PRIMARY KEY (qaId)
)
GO

CREATE TABLE JiraCustomer
(
	cusId int IDENTITY(1,1),
	cusName varchar(255) NOT NULL,
	CONSTRAINT pk_JiraCustomer PRIMARY KEY (cusId),
	CONSTRAINT uk_cusName UNIQUE (cusName)
)
GO

CREATE TABLE JiraEmployee
(
	eId int IDENTITY(1,1),
	eFullName varchar(255) NOT NULL,
	eAlias varchar(255),
	CONSTRAINT pk_JiraEmployee PRIMARY KEY (eId),
	CONSTRAINT uk_eFullName UNIQUE (eFullName)
)
GO

CREATE TABLE JiraProject
(
	pId int IDENTITY(1,1),
	pAbbrev varchar(255) NOT NULL,
	pName varchar(255) NOT NULL,
	pLeadEmployeeId int,
	CONSTRAINT pk_JiraProject PRIMARY KEY (pId),
	CONSTRAINT uk_pAbbrev UNIQUE (pAbbrev),
	CONSTRAINT fk_pLeadEmployeeId FOREIGN KEY (pLeadEmployeeId) REFERENCES JiraEmployee(eId)
)
GO

CREATE TABLE JiraTicket
(
	tId int IDENTITY(1,1),
	tKey varchar(225),
	tProjectId int NOT NULL,
	tNumber int NOT NULL,
	tTitle varChar(255),
	tType varchar(255),
	tPriority varchar(255),
	tCustomerId int,
	tOperatingSystem varchar(255),
	tMailServer varchar(255),
	tZLVersion varchar(255),
	tZLBuild int,
	tStatus varchar(255),
	tResolution varchar(255),
	tAssigneeId int, 
	tReporterId int,
	tCreated datetime,
	tUpdated datetime,
	tResolved datetime,
	tLastCrawlId int,
	tDescription nvarchar(1000),
	CONSTRAINT pk_JiraTicket PRIMARY KEY (tId),
	CONSTRAINT uk_tKey UNIQUE (tKey),
	CONSTRAINT fk_tProjectId FOREIGN KEY (tProjectId) REFERENCES JiraProject(pId),
	CONSTRAINT fk_tCustomerId FOREIGN KEY (tCustomerId) REFERENCES JiraCustomer(cusId),
	CONSTRAINT fk_tAssigneeId FOREIGN KEY (tAssigneeId) REFERENCES JiraEmployee(eId),
	CONSTRAINT fk_tReporterId FOREIGN KEY (tReporterId) REFERENCES JiraEmployee(eId)
)
GO

CREATE INDEX idx_tKey ON JiraTicket (tKey)
CREATE INDEX idx_tProjectId ON JiraTicket (tProjectId)
CREATE INDEX idx_tCustomerId ON JiraTicket (tCustomerId)
CREATE INDEX idx_tAssigneeId ON JiraTicket (tAssigneeId)
GO

CREATE TABLE JiraTicketWorkLog
(
	wlId int NOT NULL,
	wlTicketId int NOT NULL,
	wlEmployeeId int NOT NULL,
	wlDate datetime NOT NULL,
	wlMinutes int NOT NULL,
	wlComment nvarchar(1000),
	CONSTRAINT pk_JiraTicketWorkLog PRIMARY KEY (wlId),
	CONSTRAINT fk_wlTicketId FOREIGN KEY (wlTicketId) REFERENCES JiraTicket(tId) ON DELETE CASCADE,
	CONSTRAINT fk_wlEmployeeId FOREIGN KEY (wlEmployeeId) REFERENCES JiraEmployee(eId)
)
GO

CREATE INDEX idx_TicketId ON JiraTicketWorkLog (wlTicketId)
CREATE INDEX idx_EmployeeId ON JiraTicketWorkLog (wlEmployeeId)
GO

CREATE TABLE JiraTicketComment
(
	comId int NOT NULL,
	comTicketId int NOT NULL,
	comEmployeeId int NOT NULL,
	comDate datetime NOT NULL,
	comComment nvarchar(1000),
	CONSTRAINT pk_JiraTicketComment PRIMARY KEY (comId),
	CONSTRAINT fk_comTicketId FOREIGN KEY (comTicketId) REFERENCES JiraTicket(tId) ON DELETE CASCADE,
	CONSTRAINT fk_comEmployeeId FOREIGN KEY (comEmployeeId) REFERENCES JiraEmployee(eId)
)
GO

CREATE INDEX idx_comTicketId ON JiraTicketComment (comTicketId)
CREATE INDEX idx_comEmployeeId ON JiraTicketComment (comEmployeeId)
GO

CREATE TABLE JiraTicketWatcher
(
	wEmployeeId int NOT NULL,
	wTicketId int NOT NULL,
	CONSTRAINT pk_JiraTicketWatcher PRIMARY KEY (wEmployeeId, wTicketId),
	CONSTRAINT fk_wTicketId FOREIGN KEY (wTicketId) REFERENCES JiraTicket(tId) ON DELETE CASCADE,
	CONSTRAINT fk_wEmployeeId FOREIGN KEY (wEmployeeId) REFERENCES JiraEmployee(eId)
)
GO

CREATE INDEX idx_wTicketId ON JiraTicketWatcher (wTicketId)
CREATE INDEX idx_wEmployeeId ON JiraTicketWatcher (wEmployeeId)
GO
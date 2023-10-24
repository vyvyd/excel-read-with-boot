# Excel Boot 

## Purpose 

This project is a working example of how to read an excel file through a web endpoint and update the contents on a Postgres Table 

## Tasks 

- [ ] Create a sample excel file and check it in 
- [ ] Create database setup instructions
- [ ] Controller Endpoint for reading an excel file 
- [ ] Verify that the data has been written to the database

### Out of scope 

- Progressive load of contents. Every call to the excel reading endpoint creates a full-replace of the contents in the database.

## Running locally

### Database setup 

Have a locally running instance of PostgreSQL. You could choose to run a docker image or run the MacOS installer if you don't have PostgreSQL

#### Create a user and database

```sql
CREATE ROLE "helloexcel";
CREATE DATABASE "helloexcel" with OWNER "helloexcel";   
```

#### Create a schema 

Connect to the database `helloexcel` as role `helloexcel`

```sql
CREATE SCHEMA "helloexcel";
CREATE TABLE "helloexcel"."contacts"(
      name VARCHAR(255),
      email VARCHAR(255)
);
```
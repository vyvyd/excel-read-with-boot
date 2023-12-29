# Excel Boot 

## Purpose 

This project is a working example of how to read an excel file through a web endpoint and update the contents on a Postgres Table 

## Tasks 

<details>
  <summary>Phase 1</summary>
- [X] Create a sample excel file and check it in 
- [X] Create database setup instructions
- [X] Controller Endpoint for reading an excel file 
- [X] Verify that the data has been written to the database
</details>

### Phase 2 

- [ ] Convert the excel format to take an list of ISBNs
  - [ ] README changes for DB schema
- [ ] Use a backend-job setup to fetch book details from ISBN + write to DB
- [ ] Display a final list of books with details
- [ ] Display progressive update on UI when fetching book details

### Out of scope 

- Progressive load of contents. Every call to the excel reading endpoint creates a full-replace of the contents in the database.

## Running locally

### Database setup 

Have a locally running instance of PostgreSQL. You could choose to run a docker image or run the MacOS installer if you don't have PostgreSQL

#### Create a user and database

```sql
CREATE ROLE "helloexcel" SUPERUSER LOGIN PASSWORD 'password';;
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

#### Run the test

There is a single `ApplicationTest.kt` in the project. Now we should be able to run it and see the test pass.

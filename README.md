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
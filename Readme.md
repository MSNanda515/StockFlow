# Stock Flow
Track the flow of your goods using this Inventory tracking web application.

This project uses a CI/CD pipeline to test and deploy the project on heroku and can be accessed 
[here](https://dashboard.heroku.com/apps/stock-flow-msn)

## Inspiration

With the massive increase in e-commerce, there is a surreal amount of inventory that might need to be managed. 
The intention behind creating this application is to have a base application that helps solve the inventory tracking 
needs of a business. The application automates many of the inventory arrangement tasks in a warehouse, with an ability
to manage multiple warehouses from a single platform.

Moreover, all transactions are recorded which could be analyzed to make better decisions regarding sales and placement of
inventory items.


Additionally, I wanted to learn *Kotlin* and this project gave me the perfect opportunity to write good software using the
language.

## Technology Stack:

The backend is developed using Kotlin spring boot framework
The frontend of the web application is developed using Thymeleaf template engine
The application uses a document store for persistence which is managed on Mongodb Atlas

## Running Instructions:
Prerequisites: Make sure that you have Java version 11 or higher installed on your machine.

The easiest way to run the project is to use IntelliJ IDE to open the project. It should detect the gradle scripts
and provide a run configuration for the spring boot application.

To run the application from the command line, navigate to the root of the project and execute the following command:
```shell
./gradlew bootRun 
```
This should start a gradle daemon, download dependencies and start the tomcat server.
The web server starts on port 8080 by default.

Alternatively, you can build a docker image with all the code built and packaged. You can use the docker build command
to create an image using the Dockerfile given.

## Built With:
Kotlin, Spring boot, MongoDB, Thymeleaf, Html, CSS, Bootstrap, Postman
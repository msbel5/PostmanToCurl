# Postman Collection to cURL Converter

This Java application converts Postman collection files (in JSON format) into cURL commands. It's designed to simplify the process of generating cURL commands from complex Postman collections, especially useful for testing and automation purposes.

## Features

- Converts entire Postman collections to cURL commands.
- Supports variable extraction and replacement from Postman collections.
- Handles headers, body, and other request attributes.
- Generates cURL commands from "originalRequest" within Postman's response items.

## Prerequisites

Before you begin, ensure you have met the following requirements:

- Java JDK 11 or higher installed.
- Basic understanding of Java and JSON structures.

## Installation

To install this project, follow these steps:

1. Clone the repository to your local machine:

   ```bash
   git clone https://github.com/your-username/postman-to-curl.git
   ```

2. Navigate to the cloned directory:

   ```bash
   cd postman-to-curl
   ```

## Usage

To use the application, follow these steps:

1. Place your Postman collection JSON files in the `src/main/resources/postman` directory.

2. Run the application:

   ```bash
   java -jar postman-to-curl.jar
   ```

3. The cURL commands will be generated in the `src/main/resources/generated` directory, with each collection's commands in a separate `.txt` file.

## Contributing

Contributions to this project are welcome. To contribute:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Make your changes and commit them (`git commit -am 'Add some feature'`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Create a new Pull Request.

## License

This project is licensed under the [MIT License](LICENSE).

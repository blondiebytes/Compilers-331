#CMPU 331 Assignment 1

Create a new directory for your compilers project. All examples will assume the project
directory is named `cmpu-331`

##Clone this repository

Clone this repository into your `cmpu-331` directory.
```bash
$> cd cmpu-331
$> git clone <URL> LexicalAnalyzer
```
This will clone your private repository into `cmpu-331/LexicalAnalyzer`

The `URL` above is the URL to your private GitHub repository. This is the URL sent to you when you accepted the assignment. You can also find this URL in the GitHub UI.

##Maven

Move the settings.xml file to your ~/.m2 directory. You can remove the settings.xml file from your project directory.

## Validate

Run `mvn compile` to ensure everything is working.

Write code until `mvn package` reports `BUILD SUCCESS`.

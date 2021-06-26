# AWS Lambda

### PROJECT DESCRIPTION

This repository contains password reset function that is utilized by [Online Bookstore](https://github.com/V-Abhishek/online-bookstore) web application. The function is built and deployed on AWS Lambda, by leveraging CircleCI pipeline, for every commit. The lambda function is triggered every time a request is published to `password_reset` topic on **Amazon Simple Notification Service(AWS SNS)**. The Lambda function validates the user email and checks for its record on Dynamo DB, if present ensures that the user has expired the TTL of 15 mins else creates a unique reset link, using UUID, and sends it to requestor email leveraging **Amazon Simple Email Service(AWS SES)** and later update the record on ** Dynamo DB** with new TTL  

---

### ARCHITECTURE

<img alt="Lambda" src="https://github.com/V-Abhishek/aws-lambda/blob/main/images/Lambda.png" />

---

### INFRASTRUCTURE

The infrastructure required by the Lambda function is provisioned and built using **Terraform**. You can find more information regarding infrastructure in this [link](https://github.com/V-Abhishek/aws-infrastructure)



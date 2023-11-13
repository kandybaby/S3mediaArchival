# AWS Setup Guide

In order for the application to work properly, you need to be able to set up an AWS account, create an IAM user, and S3 buckets.

## Step 1: Create an AWS Account
If you don't already have an AWS account, you can create one by following these steps:
- **Visit the AWS Sign-Up Page**: Go to the AWS sign-up page by visiting [https://aws.amazon.com/](https://aws.amazon.com/).
- **Click "Create an AWS Account"**: Click on the "Create an AWS Account" button to start the registration process.
- **Follow the Registration Process**: Follow the on-screen instructions to complete the registration process, providing the required information, including your email address and payment details.

## Step 2: Create an IAM User
To securely interact with AWS services, you should create an IAM (Identity and Access Management) user with the necessary permissions. This user will only have access to S3, so if somehow your access keys were compromised, the damage would be limited. Here's how to set this up:
- **Log in to the AWS Management Console**: Log in to your AWS account if you're not already logged in.
- **Navigate to IAM**: In the AWS Management Console, click on "Services" and select "IAM" under the "Security, Identity, & Compliance" section.

![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/f33c8855-4eb4-42d1-9d4c-206349827de0)

- **Enable 2FA for the Root Account**: You technically do not need to do this, but I highly recommend it. A compromised root account is very dangerous.
- **Create a New User**: In the IAM dashboard, click on "Users" in the left navigation pane and then click the "create user" button.

![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/fd43bf9f-e789-433a-a0df-7ea9439a0515)


- **Choose User Name**: Select a name for your user, do not check “give s3 console access”. Hit next.
- **Set Permissions**: In the "Set permissions" step, select "Attach existing policies directly" and search for and attach the "AmazonS3FullAccess" policy.

![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/f1eb5892-5226-449d-b736-379f4a1dbe6a)


- **Review and Create User**: Review your settings, and then click "Create user."
- **Select Created User**: Return to the users table using the left side column if you’re not there automatically. Select the user you just created.
- **Create Access Keys**: On the main user screen, there will be a summary at the top with “Access Key 1” and “Access Key 2” and a link underneath to create a key. Click the key creation link. For the use case, select “Application Running outside of AWS” and click next. Anything is fine for tag, put “s3-media-archival” if you would like. Then click create.
- **Save your Access Keys**: AWS will then present to you an access key and a secret access key. Save those values to your computer; you will need them as environment variables for the application. Please keep in mind whoever has these access keys can upload or download anything from your S3 buckets and incur the costs of doing so onto you. It’s best practice to return to the IAM service in the console and regenerate your keys every once in a while. Never upload your keys anywhere else. Save them to your local machine for now; you will need them when we install the application.

## Step 3: Create an S3 Bucket
You will be uploading your media into S3 buckets. I recommend you create one bucket to correspond to each type of media you will be uploading. I have one bucket for movies, one bucket for tv shows, one bucket for music, etc.
- **Navigate to S3**: Click on "Services" and select "S3" under the "Storage" section.
- **Create a Bucket**: Click the "Create bucket" button.
- **Configure the Bucket**:
    - Enter a unique and meaningful name for your bucket, using the prefix of your choice. Could be something like yourname_mediaarchive_movies.
    - Select a region for your bucket. There are multiple things to consider here. [Different regions have different prices](https://aws.amazon.com/s3/pricing) for storage. Many, but not all regions are $1/TB/Month for glacier deep archive. The further away you are from the region you pick, the slower your upload and download speed will be. I recommend picking the closest region to you, where the storage price for deep archive is $1/TB/Month.
    - Leave all other settings at their default, hit create.


![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/57ff0036-0c5b-4339-b634-c02097beaec1)



- **Enable abort multipart lifecycle rule**: If you cancel an upload part way through or the application crashes part way through an upload, the unused data will sit, invisible in S3 forever, and you will still pay for that. The application has logic to ask S3 to delete any leftover data every day at midnight. However, I recommend enabling a rule on the bucket itself to do the same thing. Click on the bucket and navigate to the “management” tab. Then select “create lifecycle rule”. Use the following screenshots to create the settings for the rule, then click save.

![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/a51edf2b-bb36-4f3a-a8c3-5cdc172567ae)
![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/6b654668-4d15-4dd6-814a-b37e0f56a290)


If you are creating multiple buckets for different media types or any other form of organization, I recommend you create the exact same life cycle rule on each new bucket. Also ensure each bucket is created in the same region.

## Other Important AWS Considerations
If you want to delete an archive from S3, you must do that from the console; there is no way of doing it from the app. If you want to delete an entire bucket, you must do it from the console. Check out the official [AWS pricing guide](https://aws.amazon.com/s3/pricing) to better understand AWS pricing.

For a less concise overview of AWS pricing, I have also written a [brief overview of S3 pricing.](./AWSPricing.md)

# AWS S3 Pricing Guide

This guide provides a rough overview of how to determine your approximate S3 costs for both storage and egress.

## 1. Storage Costs

Amazon S3 provides several storage classes, each with different pricing models. Here's an overview of some of the standard storage class and the deep archive storage class.

- **S3 Standard**: This class offers high durability and availability and is suitable for frequently accessed data. Pricing typically ranges from $0.023 to $0.030 per GB/month, depending on the region.

- **S3 Glacier Deep Archive**: Designed for long-term archiving, this class offers the lowest storage cost at approximately $0.00099 per GB/month.

**Important Note**: AWS S3 pricing may vary by region, and Amazon occasionally updates its pricing structure. Always check the AWS website for the most current pricing information.

Generally, for this application I recommend using glacier deep archive. There is no harm in waiting 2 days for your data to be ready to download, especially given how much cheaper it is. If you want instant downloads, you can use S3 standard but I would not store large media data in that class, because its very expensive.There are many other storage tiers, but generally they are more complicated and better suited to specific business use cases. Keep it simple, upload to deep archive, or standard unless you know exactly what you’re doing.

## 2. Data Transfer Costs

Data transferred into S3 is free, so no need to worry about that. Data transferred out is subject to egress fees. Data transfer costs depend on the region, but right now for US East 1, the cost is $0.09 per GB, or $100 per TB.

##  3. API Request Costs
There is a small cost to asking S3 to send us information, or initiating an upload. It is hard to say exactly what this will cost you, because uploading large files triggers a lot of API requests. But generally this cost is small. Last march I uploaded 10TB of data to S3, the API request cost was $6. Some operations in the application will ask S3 for information about objects, these requests may cost a few cents.

## 4. Data Restoration Cost
The application is configured to always use the "bulk" restoration process when downloading objects. This is why it takes up to two days to restore an archive to make it available for download. Bulk requests are free. However, when you restore an object for download, that object will exist for 3 days at the S3 "standard" storage price. So if you restored a 100gb file for download, you would incur 0.1 months, of 100gb storage, at $0.023 a GB per month, for a total of 100 * 0.023 * 0.1 = $0.23 cents.

## 5. Early Deletion Costs
If you upload an object to Glacier Deep Archive, you will pay for its storage for a minimum of six months, whether you use it or not. If you upload a 100gb file, that would normally cost you $0.10 a month. If you were to overwrite this file after 3 months, S3 would delete the old one, and replace it with your new one. The old file would then be charged $0.30 cents in early deletion fees, since you need to pay for 6 months total storage.

That means if you upload a file to deep archive, and immediately delete it, you will be charged the full six months storage cost for that file.
If you delete a file after 6 months has passed, you will not be charged an early deletion fee.

There is no early deletion fee for standard storage class, but of course the storage cost itself is 20-30x the price. 

## 6. AWS Snowball
AWS Snowball is a service where they send you a harddrive with files from S3. Working out the exact pricing is...complicated, but depending on where you live, once you are restoring more than 5TB in one go, Snowball is probably significantly cheaper than downloading your files.

I have never used snowball and cannot provide a guide, but if you are looking at recovering an entire archive, it might be worth reaching out to AWS support and doing some research into getting snowball to work. You could end up reducing the total cost by as much as half depending on the size of the recovery.




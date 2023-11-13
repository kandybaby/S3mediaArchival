# S3 Media Archival Application

The S3 Media Archival Application is a self-hosted web application designed to make archiving media data to S3 Deep Archive both accessible and easy.  Bulk archiving in S3, especially for non standard storage classes, usually requires, at a minimum, experience scripting and working with AWS CLI or SDK. This is not accessible to many people, who might still want to be able to use S3 for media archiving.



As of the time of writing (Nov, 2023), bulk storage costs in S3 is some of the cheapest cloud storage available, at $1/TB/Month in the Deep Archive storage class. The two main drawbacks are that media stored in Deep Archive can take up to 2 days to be made available for download, and AWS data egress fees can drive the cost of getting your data *out* of S3, approximately $100 a TB.

Because of this pricing fact, an S3 Media Archive is designed ideally, to never be used. It should not be your primary line of defence against data loss, it should be your last. Keep at least two copies of your data locally. However, an S3 archive is a great disaster recovery scenario, if you lose all your local data and local backups. It's expensive to get your data back, but at least it's an option. For more information on data safety and the rational for an S3 Archive, check out the [Why S3 documentation](./guides/WhyS3.md)

![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/24102a4b-b043-41da-95e6-760f90cfb2e0)


## Features

- Scan directories for any kind of media (movies, tv, music, ebooks, comics, photos, ect)
- Bulk upload media to S3 buckets in a desired storage class
- Re-scan directories to identify when archives are out of date
- Bulk download media from S3 buckets, even if that media needs to be restored first

For a full account of features and instructions, check out the [user guide.](./guides/UserGuide.md)

## Installation

S3 Media Archival is designed to be hosted using **docker**. At present moment, I do not support running the application any other way. It is possible to run the application by building the source code and running the JAR directly, but if you wish to try this I will not at this time provide support for issues. In the future, I may provide support for other methods of hosting.

To get the application up and running, please first visit the [AWS guide](./guides/AWSSetupGuide.md). This will help set up the relevant AWS resources and permissions to allow the application to work.

Once your AWS setup is complete, you can follow the [installation guide](./guides/Installation.md). This guide will also cover installing and setting up docker as well.

## Support
The app is starting its first release at 0.1.0, it is possible there are bugs and issues. I am hopeful everything will work well, and so far in my own use and testing, I have fixed any issue I've encountered.

If you are using the app and encounter a bug, or have a feature request, please open an issue on the github repository! I will do my best to support, though at present I am just one person.  I cannot guarantee I will be able to implement your feature, or immediately fix your bug, but I will try.

## Contributing
If you would like to fix a bug or implement a feature yourself, open an issue and have at it! I will review any pull requests that get opened, though please let me respond to an issue before you work on it, just so you don't waste your time on a feature or bug fix that I will reject. If you disagree with my choice, I would encourage forking the repository!

## Disclaimer
As I mentioned, downloading from AWS can be up to $100/TB. Do not download anything from your archive without being prepared to pay this price. I am not responsible if you use this application irresponsibly and unknowingly rack up AWS fees. This application is use at your own risk. For more details on pricing, check out the AWS website or for a less precise overview, my [AWS pricing guide.](./guides/AWSPricing.md)







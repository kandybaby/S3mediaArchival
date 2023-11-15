# Installation 

## Quick Installation: Docker-Compose

You can use the application with docker-compose, this is actually my preferred method of installation.

Here is a sample docker-compose file you can use. See the Application Configuration section of this guide for explanations of the env variables and volume mapping. 

```
version: '3'
services:
  media-archival:
    image: ghcr.io/kandybaby/s3mediaarchival:latest
    environment:
      - TRANSFER_THROUGHPUT=0.5
      - AWS_ACCESS_KEY_ID=your_aws_key
      - AWS_SECRET_ACCESS_KEY=your_aws_secret_key
      - AWS_REGION=your_aws_region
    ports:
      - "your_port:8080"
    volumes:
      - /path/to/your/appdata:/appdata
      - /path/to/your/downloads:/downloadDirectory
      - /path/to/your/temporary:/temporaryDirectory
      - /path/to/your/data:/data

```

Using docker-compose can simplify managing multiple self-hosted apps in docker. Explaining its set up is beyond the scope of this guide, but I encourage anybody to look into it further if that sounds interesting.

If you do not know how to use docker or docker-compose, the rest of this guide will walk you through getting the application hosted and running. 

## Application Configuration

Before you can use the application, you will need to come up with values for environment variables, a port mapping, and volume mappings. This section will walk through the necessary information you need to acquire to continue. Once you have all the relevant variables and volumes, continue on to installing docker.

#### Environment Variables

The App has 3 mandatory, and one optional environment variables.

###### TRANSFER_THROUGHPUT
The transfer throughput is optional, and it is the maximum amount of bandwidth (upload + download combined) that the application will use, in GbPS. The default value if not provided is 0.5 GbPS

###### AWS_ACCESS_KEY_ID
This is mandatory, and should be the AWS access key you saved during the AWS set up.

###### AWS_SECRET_ACCESS_KEY
This is mandatory, and should be the AWS secret key you saved during the AWS set up.

###### AWS_REGION
This is mandatory, and should be the formal code of the region you created your S3 buckets in. For example, us-east-1 or eu-west-1

#### Port Mapping
The application will run at localhost:[port], and you need to select which one. Any number higher than 1000 works, if unsure, just pick 8080. This would mean you could use the app at localhost:8080

#### Volume Mapping
In order for the application to write or read data from your host machine, we need to map directories from outside the docker container, to directories inside the docker container. This application requires four mappings to work

###### Appdata
This volume is where the application will store its database and logs.

You must map this volume to ```/appdata```, so you can set it like so ``` /host/path/to/appdata:/appdata```

###### Temporary Upload Volume

The application TARs your media before sending it S3. This allows us to upload complete seasons or albums as one object, minimizing management overhead. The downside of this approach, is that the application has to copy all the data into a TAR file before uploading. You need to select a directory where these temporary files will be created. Keep in mind, that there needs to be enough available disk space to handle up to 5 concurrent uploads, of your 5 largest individual archives. For example, my largest archives are 70GB, so my temporary volume should have at least 350GB of free space so that those five files could be copied and uploaded concurrently.

You must map this volume to ```/temporaryDirectory``` so you can set it like so ``` /host/path/to/temp:/temporaryDirectory ```

###### Download Directory

If you use the app to recover media from S3, you will need a download directory.

You must map this volume to ```/downloadDirectory``` so you can set it like so ```/host/path/to/download:/downloadDirectory ```

###### Data Directory

The directory that contains the media you would like to archive.

You can map this volume to any path inside the container, but I recommend using ```/data``` so you can set it like so ```/host/path/to/data:/data```


## Install Docker

If you already have docker, you can skip to the next section. 

If you are not familiar with docker, it is a special software that always creates a consistent runtime environment for a piece of software. Docker creates containers based off of images, and these images contain not only the application code, but the operating system that code will run on. This is important for the S3 Media Archival application, because it allows me, the developer, to depend on a predictable runtime environment for the application. 

Using docker is actually quite simple. After we install docker itself, you just need to "pull" the image I have published, and then run it to start the application. 

My guide for docker installation is [located here.](./DockerInstallation.md)

## Running The Application 

After successfully installing Docker, the next step is to pull and run the S3 Media Archival application. The process is largely the same across Windows, macOS, and Linux, with minor differences in setting up automatic start-up.

### Pulling the Image

1. Open your Terminal (Linux/macOS) or Command Prompt/Powershell (Windows).
2. Pull the image using the following command: ```docker pull ghcr.io/kandybaby/s3mediaarchival:latest``` 
3. You could alternatively use ```docker pull ghcr.io/kandybaby/s3mediaarchival:0.1.0```, or whatever your desired version of the application is. 

### Docker Run

You are now ready to run the application. You will need to construct the following command to run in your cmd or terminal. 

``` bash
docker run -d \
-e TRANSFER_THROUGHPUT=0.5 \
-e AWS_ACCESS_KEY_ID=your_access_key \
-e AWS_SECRET_ACCESS_KEY=your_secret_access_key \
-e AWS_REGION=your_aws_region \
-p your_port:8080 \
-v your/appdata/path:/appdata \
-v your/temporary/folder/path:/temporaryDirectory \
-v your/download/folder:/downloadDirectory \
-v your/data/folder:/data \
--restart unless-stopped \
--name s3-media-archival ghcr.io/kandybaby/s3mediaarchival:latest
```

_Note: if you pulled s3mediaarchival:0.1.0, you should use that exactly. For example ``` --name s3-media-archival ghcr.io/kandybaby/s3mediaarchival:0.1.0```_

Once you have substituted in all your own configurations from the application configuration section of this guide, you can run this command in your cmd or terminal. When it finishes,  the application should now be running at ```localhost:8080``` or whichever port you picked.

The default username and password is ```admin``` and ```password``` respectively 

You're now ready to check out the [User Guide!](./UserGuide.md)


### A Note on Volume Permissions

You could end up with issues where docker attempts to read or write to the directories you have mapped, if docker does not have permission to do so. This probably won't be a problem, but if you are on MacOS or Windows, you can navigate to the "Resources" > "File Sharing" section of the settings on docker desktop to add the volumes. On windows, running docker desktop as admin can help.

Permission issues tend to be different on linux, and if you are using linux I am going to assume you understand how users and groups work. You are able to use --user flag in the docker run command to set which user:group the container will use. Alternatively, if you are using linux and do not understand users / groups, I would highly recommend it you look it up, but for now, if you add ```--user "$(id -u):$(id -g)``` to your docker run command will probably fix any permission issues.

MacOS users can also attach the ```--user "$(id -u):$(id -g)``` flag to their docekr run if they continue to have issues.


## Troubleshooting Issues 
If you are unable to get the application to run, please open an issue on the github repo, I will do my best to offer support.
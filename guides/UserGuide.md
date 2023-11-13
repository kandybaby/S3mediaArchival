# User Guide

## Table of Contents

1. [Login](#login)
2. [Updating Login Information](#updating-login-information)
3. [Dashboard](#dashboard)
    1. [Adding a Library](#adding-a-library)
        1. [Library Name](#library-name)
        2. [Library Path](#library-path)
        3. [Storage Class](#storage-class)
        4. [Bucket Name](#bucket-name)
        5. [TV Series](#tv-series)
        6. [Complete Library Addition](#complete-library-addition)
    2. [Scanning a Library](#scanning-a-library)
    3. [Upload, Download and Delete](#upload-download-and-delete)
    4. [Library Details](#library-details)
        1. [Delete Library](#delete-library)
        2. [Synchronize Library](#synchronize-library)
        3. [Upload Library](#upload-library)
4. [Upload Jobs](#upload-jobs)
    1. [Cancelling Uploads](#cancelling-uploads)
5. [Download Jobs](#download-jobs)
    1. [Clearing Downloads](#clearing-downloads)
    2. [Cancelling Downloads](#cancelling-downloads)



## Login

![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/c2f18edc-3dd3-44b7-9f27-e324a292d0ca)


The default username for new apps is ```admin``` and the default password is ```password```

## Updating Login Information

![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/01491d27-3f07-4b14-b56a-642babf281b0)


The very first thing you should do is click settings in the navbar, and update your username and password to something more secure. The application does not support multiple users.

## Dashboard

Use the navbar to head back to the ```dashboard```. Your UI will look like this

![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/1fd3ef6c-25af-45e1-a671-19b593391b1a)

### Adding a library

If you click the ```+``` button at the top left of the UI, you will open a modal that will let you add a library to your applicaiton

![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/f2e27922-a1e9-4367-ad62-a47a6a0f26c6)

#### Library Name
Library name is simply the display name we use for the library on the UI. I recommend using simple identifiers like "TV Shows" or "Movies"

#### Library Path
The library path is one of the more complicated concepts the manage in the app. It signals the directory the application will read for this library, and everything in that directory will be treated as one "object"

For example, you might organize your movies like so

```
--data 
---movies 
----Everything Everywhere All At Once (2022)
------Everything Everywhere All At Once.mkv 
------poster.jpg 
------trailer.mkv
```
You would probably want to upload the movie, the trailer and the poster as one file to S3. It reduces the amount of management you need to do. If you uploaded everything separately, you would need to download and compile it all separately as well. Uploading as one object keeps all that relevant information together.

So in this case, your movie library would use the path ```/data/movies/```. When you scan, the application will scan every folder in /data/movies and treat each folder as its own media object, based on its name. When you go to upload an object, everything within that folder will be TARed together and uploaded as one object.

But things get more complicated if we look at a hypothetical music library

```
--data 
----music
------Chapelle Roan
--------The Rise and Fall of a mid-west princess
----------01-feminenomenom
------Carly Rae Jepsen 
--------The Lovliest Time 
----------06-Kamikaze
```

This music library is organized first my artist, then my album. The most convienent way to upload music would probably be albums. If you upload each artist as one file, if you add a new album, you'll need to re-upload the new artist object with the new album. But if you have an album, that won't ever need to be updated or changed.

But there's a problem. We can tell the application to get all the folders in ```/data/music/``` but that will package up each artist, not each album. So the solution here is to use a special tag in your path like so

```/data/music/${artist}```

The ```${}``` tag, instructs the application to go one level deeper, and traverse to every file or folder *inside* the preceding path. So a path of ```/data/music/${artist}``` would treat your albums as individual objects. If you used a path of ```/data/music/${artist}/${album}``` it would treat your *songs* as the media object.

For another example, I could organize my comic books like this

```
--data
----comics
------marvel
--------The Amazing Spiderman 
------DC
--------The Flash
------Manga
--------Golden Kamuy
```

If I wanted to store the individual comic book runs as media objects, I would use the path ```/data/comics/${type}```

In the end, you can use this variable to choose how granular you want your storage to be. If you want to archive artist discographies as one object, or albums as one object, for example. You can use the ```${}``` feature to organize that how would like.

I archive my music my album, movies by movie, ebooks by book, comics by volume, and TV shows are a special case I will address talking about the TV series checkbox.

### Important Note!
Remember that you had to map an external folder to an internal folder in your docker container. You must use the path *inside* the docker container

So if you mapped ```/your/media/archive:/data``` the paths for your libraries should all start with ```/data```

#### Storage Class
I discussed two of the various storage classes in the [pricing guide](./AWSPricing.md). But basically, unless you know exactly what you are doing, always select glacier deep archive. You could alternatively use standard, if you were uploading **small** amounts of data, that you wanted instantly available, but if that's you're use case, you are way better off using a consumer product like google drive.

#### Bucket Name
This is the name of the S3 bucket you want the files in this library to upload to.

#### TV Series
If you have a plex library for your TV, you probably organize your TV shows along these lines

```
--data
---shows
-----Our Flag Means Death
------Season 1
--------Episode 1.mkv 
------Season 2
--------Episode 1.mkv 
------Poster.jpg
------tvshow.nfo 
```

To me, the ideal unit for archiving TV shows is the season. If you have an ongoing TV show, you can archive only the completed seasons, and not be forced to re-archive the entire series. Plus, I have a few TV shows, like Charmed or Buffy the Vampire Slayer, that take up about 500GB each. Uploading the entire series as one object is very large.

But there's a problem, often in our TV show folders there is additional metadata like poster.jpg or tvshow.nfo. If you used

```/data/shows/${season}``` as your path

then all those other little things in each series root directory will get archived as separate objects alongside the seasons.

The solution, is the TV Series checkbox. If you store your TV shows in the above format, you can use the path

```/data/shows/``` and check the TV series textbox on library creation. This will modify the scan library logic to treat folders within the show with substring "season" as  their own objects, and package everything else as "series metadata"

So in the above example, with the path ```data/shows``` and tv series check box filled, the app would scan the library and give you

- Our Flag Means Death Metadata
- Our Flag Means Death Season 1
- Our Flag Means Death Season 2

Our Flag Means Death Metadata would cover everything in the Our Flag Means Death Folder, that is not a Season folder.

#### Complete Library Addition
Simply click the blue add library button to finish adding your library after you filled in the required information.

You can add as many libraries as you would like with the ```+``` button.

### Scanning a Library

Once you have added a library, it will be empty like this

![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/8b377dee-d14b-473b-a712-0c53929214d1)

To populate it with your media data, you only need to click the ```scan``` button. This will trigger the application to scan the path you provided in library creation. Scanning can take up to a few minutes depending on library size, but will usually be quicker.

After your library is scanned, you should see your media data like so

![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/7555a6d7-344b-444a-a3c4-978f0092a044)

You can use the search box to filter by name, and the select archive status box to filter by archive status.

There are three archive statuses

- Not Archived
- Archived
- Out of Date

Initially, all of your data should read Not Archived. Once you upload something to S3, its status will change to Archived. If you scan your library again, the last modified date of the file or folder being archived will be recorded. If the date is more recent than the archived date, the file will be marked Out of Date.

Every once in a while you should re-scan your libraries to see if there is new media that hasn't yet been archived, or if archived media has changed since it was last updated. Re-scanning a library will only modify existing objects by changing their date last modified and archived status. It will not delete or duplicate existing objects.


### Upload, Download and Delete

You can select objects for operations by selecting their checkbox. Alternatively, you can select everything on a page by using the header checkbox. When you click upload or download, if the media is eligible, a job to upload or download it will be added to a queue to run.

Jobs are eligible for download if they have a status of Archived or Out of Date, and do not already have an active upload or download job in the queue.

Jobs are eligible for upload if they do not have an active upload or download job in the queue.

If you click delete, objects will be deleted from the application. Delete will not remove an object from your file system or from S3. If you delete an object on the app, but not in your file system, it will be re-added during your next scan. If you want to remove an archive from S3, you will have to use the AWS S3 Console for that operation.

### Library details

If you click the library details button, it will open a summary view modal with library actions as seen below

![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/8a64c52b-8bda-450c-a96e-ff99842527a5)

Here you can review your library information.

#### Delete Library
If you wish to delete your whole library, you can do so with this button. This will also delete all media objects from that library from the application database. Again, this will not delete any archives in S3, or any files from your file system.

Currently, the application does not support updating a library. If you wish to update library information, you have to delete the existing library and create a new one.

#### Synchronize Library
If you have deleted a library or created a fresh install of the application, you can synchronize a library with its S3 bucket to get existing archive information. This is currently a limited operation that depends on matching the paths of your existing media with the paths of the archived media.

To synchronize a library, make sure you scan it first. Then head to library details and click the synchronize button. The application will get all the objects from the listed library S3 bucket, and if the path of those objects, ie ```/data/shows/Our Flag Means Death/Season 1```, matches an object in your library, it will update the Archived date of that object, and depending on that date, give the object a status of Archived or Out of Date.

#### Upload Library
This button will trigger an upload job for every object in a library that has an archive status of not archived or out of date. Use with caution, and only if you truly want every object in a library in S3.

## Upload Jobs
![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/eb95f34f-3618-4ac3-8e34-3be04450af88)

Uploading media has 3 stages. The first stage is the ```Waiting...``` stage, when the server is waiting for the resources to be able to do the upload.

Once a dedicated thread has picked up the job, the first thing it will begin doing is ```Tarring media```, creating a copy of the media object in one uploadable TAR file. So for example, a TV show season would be Tarred into one file that can be extracted on download. This can take a long time or a short time depending on the file size.

Finally, once a progress bar appears that means the TARed media is being uploaded to S3. Once the upload finishes, the archive date and new status of Archived will be recorded for that media object.

### Cancelling Uploads
![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/5f58b397-0f19-462a-973a-5c643a4ef179)

If you wish to cancel an upload while it is in progress, you can select the upload using a checkbox and click cancel, or you can cancel all active uploads at once on every page.

When an upload is cancelled, you will see a message saying ```Canceled, awaiting removal and cleanup```

Sometimes, this can take quite awhile. There are technical reasons for this, essentially I don't interrupt ongoing processes, but wait for them to finish before checking if a job is canceled. You will not be able to re-add an upload or download job until the cancellation logic has finished cleaning up the job.

## Download Jobs
![image](https://github.com/kandybaby/S3mediaArchival/assets/149127739/511b57f8-1942-4ecc-a422-a6f7e762e2c8)

Downloading media also has multiple stages. For the first stage, a restore request is sent out to AWS, asking for the object to made available to download. This only applies to objects in GLACIER or DEEP_ARCHIVE storage classes, but you should be using deep archive anyways.

The media will take up to 2 days to restore, then it will be marked as restored, and put into a queue to begin downloading. Once the media is downloaded, the TAR file that was downloaded will be automatically unzipped.

Media in not glacier storage classes will being downloading instantly, but remember those classes can cause 20-30 times the price of glacier classes for storage.

Restored media will be available for 3 days. Do not restore more at once than you can download within that time window, or you risk paying for restores you could not finish downloading.

## Clearing Downloads

When a download has either succeeded or failed, the status will change to show either success, or failed. To remove finished jobs from the board, you can click ```clear All Done```, or you can select specific jobs and use the ```clear``` button.

This functionality exists for downloads to help in keeping tracking of successful downloads for larger scale restores. You are able to manually see the final status of each download, and won't be able to add another download for that object until you clear it.

## Cancelling Downloads

If you cancel a download after sending out the restore, the object will still restore in S3, the application just won't check for it, and won't download it when it's available. You will still pay the restore cost on the object, but not the data transfer cost on the download.

Similar to uploads, cancelling a job fully may take some time, and if you cancel a job or a batch of jobs you may need to give them a few hours before you can start a new job for that media. 
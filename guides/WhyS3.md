# Why Archive to S3 ?

## 3-2-1 Back ups
The 3-2-1 back up strategy is the oft repeated rule for securing data you care about and don't want to lose. It requires 3 copies of your data, 2 different storage medias, and 1 off-site copy. Data that is properly backed up with the 3-2-1 method is extremely unlikely to be lost.

3-2-1 is not the most robust data loss prevention rule out there, but it is very good. Unfortunately, it can also get very expensive when scaling to multiple TBs. Most people are able to easily get two local copies of their data, you just need an external HDD. But getting a second copy your data outside of your house can be difficult and expensive. You can, for example, buy external HDDs and ship them to friends or family, but you need to periodically retrieve them and update your back ups, and make sure your hardware hasn't degraded.

You can buy and build an entirely new NAS that runs at a friend or family member's house. This is probably the most cost effective method, no other off-site backup is going to be cheaper over 10 years, assuming you need to do at least one off-site restore. But of course, this is still a decent amount of work in terms of building a second server, and maintaining it. Swapping out disks as needed, debugging any issues remotely, etc. And you need a willing friend or family member who doesn't mind you using their power and bandwidth.

You can use cloud back ups as well, but the cheapest cloud back up providers, like Backblaze B2, are usually in the neighbourhood of 5$/TB/Month. For a 10TB archive, you're already looking at $50 a month.

Many people simply choose not to 3-2-1 TBs of media date due to the cost. They keep two or more local copies, and if they experience a simultaneous failure, they will accept the loss and begin rebuilding their archives again.

## 2.5-2-1 Back up ?
A 2.5-2-1 backup, to be clear, is not a real thing. It's just the term I've made up to describe my own personal archive to S3. I have a local back up of my data on a separate device, and an offsite S3 archive.

This a 2.5.-2-1 and not a 3-2-1 because the S3 archive is crucially not an offsite back up. A proper back up has the capacity for incremental restores, ie you can roll back your data to X point in time, and you should be able to run full test restores as well. Archiving your media to S3 does not allow you to do this, since you are not storing incremental data, and full test restores are prohibitively expensive.

However, I still believe this is a good solution for people desiring increased data protection, with reasonably low effort. Building a secondary NAS at your parents house will be cheaper, but it won't be less work. Also, the long-term unchanging nature of media makes it well suited for this kind of archive. When you download media, say a movie, the file will likely never change unless you replace it with something of a different quality or size. This means I am normally not worried about not having incremental restores.

## The restores cost an insane amount of money
Yes, they do. A full restore of 15TB from S3 could cost you as much as $1500, though it would probably be closer to $1000 if you used AWS Snowball (they ship you a harddrive instead of you downloading it).

The whole point of an offsite S3 archive is that its a back up of last resort. It's an insurance policy if you lose both copies of your local data. If there is a fire, or your kid knocks a pitcher of juice onto your home lab. AWS S3 Deep Archive is currently $1/TB/Month, if you were to use a cloud service without egress fees, you would probably pay $5/TB/Month. If you were to store, for example, 10TB of data, you would need to do somewhere around 5 or 6 full restores for the other option to be cheaper.

In other words, the costs of the restores are high, but offset by the very low storage cost combined wth the fact that restores should be extremely rare.

If you do not keep two local copies of your data, and only rely on an S3 archive, you are going to have a bad time. You could easily be stuck with a high number of expensive restores.

## I can just rebuild my media library
Yes, you probably can. But I also think people sometimes overestimate how easy it can be to re-acquire media they found years ago. Also, in my case for example, I have 1400 albums that I acquired one by one. That's a huge amount of work to rebuild that.

This is actually a big advantage of this S3 archive to me. You can do a partial restore, pick and choose exactly which media to download. The storage is very cheap, so it's not a huge cost, but if you loose everything you can just restore your music library, or you can just restore the movies you can no longer find anywhere else.

## Is this the best solution ?
That wholly depends on your priorities. It is not the cheapest solution, its also not the most protected solution you could possibly have. Personally I find the costs low, the security high, and more importantly, the effort is low. I really don't want to be driving to my parents house to replace a failed disk on my off-site NAS. And my media data is important to me, I truly never want to lose it.

You might feel differently, and that's okay. Only you can decide if this is the right strategy for you.





Build for the usage in conjunction with Google Guice.

Example usage:

```List<String> scopes = Lists.newArrayList();
scopes.add("https://www.googleapis.com/auth/youtube.upload");

YouTube youTube = new YoutubeFactory("path/To/Client/Secret",
"path/To/Refresh/Storage/Folder",
scopes,
"applicationName").getService();

UploadService uploadService = new UploadService(youTube);
String videoId = uploadService.upload(...);
uploadService.updateThumbnail(ThumbnailFile, videoId);```
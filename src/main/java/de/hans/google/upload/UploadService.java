package de.hans.google.upload;

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.Lists;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.inject.Inject;
import de.hans.google.upload.data.VideoMetaData;
import de.hans.google.upload.data.YoutubeCategory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class UploadService {

    private YouTube youTube;

    @Inject
    public UploadService(YouTube youTube) {
        this.youTube = youTube;
    }

    /**
     * upload a video-file to youtube
     * language is english
     *
     * @param downloadedVideoMetaData
     * @param fileToUpload            maximum size is 128GB
     * @param publishAt               if null, video will be published immediately
     * @param tags
     * @param videoPrivate
     * @param embeddable
     * @param madeForKids
     * @param notifySubscribers
     * @return the id of the uploaded video
     * @throws IOException
     */
    public String upload(
            VideoMetaData downloadedVideoMetaData,
            File fileToUpload,
            DateTime publishAt,
            List<String> tags,
            YoutubeCategory category,
            boolean videoPrivate,
            boolean embeddable,
            boolean madeForKids,
            boolean notifySubscribers
    ) throws IOException {

        // Define the Video object, which will be uploaded as the request body.
        Video video = createToVideo(
                downloadedVideoMetaData,
                publishAt,
                tags,
                category,
                videoPrivate,
                embeddable,
                madeForKids);

        Video response = upload(downloadedVideoMetaData, fileToUpload, notifySubscribers, video);

        return response.getId();
    }

    private Video upload(
            VideoMetaData downloadedVideoMetaData,
            File fileToUpload,
            boolean notifySubscribers,
            Video video) throws IOException {
        // The maximum file size for this operation is 137438953472.
        InputStreamContent mediaContent = new InputStreamContent(
                "application/octet-stream",
                new BufferedInputStream(new FileInputStream(fileToUpload))
        );
        mediaContent.setLength(fileToUpload.length());

        // Define and execute the API request
        List<String> parts = Lists.newArrayList();
        parts.add("snippet");
        parts.add("status");
        YouTube.Videos.Insert request = youTube.videos().insert(parts, video, mediaContent);
        request = request.setNotifySubscribers(notifySubscribers);
        System.out.println("... going to upload: " + downloadedVideoMetaData.getTitle());
        Video response = request.execute();
        System.out.println("done uploading: " + downloadedVideoMetaData.getTitle());
        return response;
    }

    private Video createToVideo(
            VideoMetaData downloadedVideoMetaData,
            DateTime publishAt,
            List<String> tags,
            YoutubeCategory category,
            boolean videoPrivate,
            boolean embeddable,
            boolean madeForKids) {
        Video video = new Video();

        // Add the snippet object property to the Video object.
        VideoSnippet snippet = new VideoSnippet();
        snippet.setCategoryId(Integer.toString(category.getCode()));
        snippet.setTitle(downloadedVideoMetaData.getTitle());
        snippet.setDescription(downloadedVideoMetaData.getDescription());

        snippet.setTags(tags);

        snippet.setDefaultLanguage("en");
        snippet.setDefaultAudioLanguage("en");

        video.setSnippet(snippet);

        // Add the status object property to the Video object.
        VideoStatus status = new VideoStatus();
        if (videoPrivate) {
            status.setPrivacyStatus("private");
        } else {
            status.setPrivacyStatus("public");
        }

        status.setEmbeddable(embeddable);
        status.setMadeForKids(madeForKids);
        status.setSelfDeclaredMadeForKids(madeForKids);

        status.setPublishAt(publishAt);

        video.setStatus(status);
        return video;
    }

    public void updateThumbnail(File thumbnail, String videoId) throws Exception {

        InputStreamContent mediaContent = new InputStreamContent(
                "application/octet-stream",
                new BufferedInputStream(new FileInputStream(thumbnail)));
        mediaContent.setLength(thumbnail.length());

        // Define and execute the API request
        YouTube.Thumbnails.Set request = this.youTube.thumbnails().set(videoId, mediaContent);
        System.out.println("...going to change thumbnail of: " + videoId + " to: " + thumbnail.getAbsolutePath());
        request.execute();
        System.out.println("done changeing thumbnail of: " + videoId + " to: " + thumbnail.getAbsolutePath());
    }
}

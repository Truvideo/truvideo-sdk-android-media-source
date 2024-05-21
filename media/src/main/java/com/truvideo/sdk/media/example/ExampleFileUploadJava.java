package com.truvideo.sdk.media.example;

import androidx.annotation.NonNull;

import com.truvideo.sdk.media.TruvideoSdkMedia;
import com.truvideo.sdk.media.builder.TruvideoSdkMediaFileUploadRequestBuilder;
import com.truvideo.sdk.media.exception.TruvideoSdkMediaException;
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaCallback;
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaFileUploadCallback;
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest;

import java.util.HashMap;
import java.util.Map;


class ExampleFileUploadJava {

    private void uploadFile(String filePath) {
        final TruvideoSdkMediaFileUploadRequestBuilder builder = TruvideoSdkMedia.getInstance().FileUploadRequestBuilder(filePath);

        // Tags
        builder.addTag("key", "value");
        builder.addTag("color", "red");
        builder.addTag("order-number", "123");

        // Metadata
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");
        metadata.put("key1", 1);
        HashMap<String, Object> metadataNested = new HashMap<>();
        metadataNested.put("key2", 2);
        metadata.put("nested", metadataNested);
        builder.setMetadata(metadata);

        // Build request
        builder.build(new TruvideoSdkMediaCallback<TruvideoSdkMediaFileUploadRequest>() {
            @Override
            public void onComplete(TruvideoSdkMediaFileUploadRequest data) {
                // Request ready

                // Send to upload
                data.upload(new TruvideoSdkMediaFileUploadCallback() {
                    @Override
                    public void onComplete(@NonNull String id, @NonNull TruvideoSdkMediaFileUploadRequest response) {
                        // Handle result
                        String remoteId = response.getRemoteId();
                        String remoteUrl = response.getRemoteUrl();
                        String transcriptionUrl = response.getTranscriptionUrl();
                        Map<String, String> tags = response.getTags();
                        Map<String, Object> metadata = response.getMetadata();
                    }

                    @Override
                    public void onProgressChanged(@NonNull String id, float progress) {
                        // Handle progress changed
                    }

                    @Override
                    public void onError(@NonNull String id, @NonNull TruvideoSdkMediaException ex) {
                        // Handle error uploading the file
                    }
                });
            }

            @Override
            public void onError(@NonNull TruvideoSdkMediaException exception) {
                // Handle error creating the request
            }
        });
    }
}

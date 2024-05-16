package com.truvideo.sdk.media.example;

import androidx.annotation.NonNull;

import com.truvideo.sdk.media.TruvideoSdkMedia;
import com.truvideo.sdk.media.builder.TruvideoSdkMediaFileUploadRequestBuilder;
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaFileUploadCallback;
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest;

import java.util.Map;

import truvideo.sdk.common.exception.TruvideoSdkException;

class ExampleFileUploadJava {

    private void uploadFile(String filePath) {
        // Create file upload request builder
        final TruvideoSdkMediaFileUploadRequestBuilder builder = TruvideoSdkMedia.getInstance().FileUploadRequestBuilder(filePath);
        builder.addTag("key", "value");
        builder.addTag("color", "red");
        builder.addTag("order-number", "123");

        // Build request
//        builder.build(request -> {
//            // Here the request its ready to be used
//
//            // Upload file
//            request.upload(new TruvideoSdkMediaFileUploadCallback() {
//                @Override
//                public void onComplete(@NonNull String id, @NonNull TruvideoSdkMediaFileUploadRequest response) {
//                    // Handle complete upload
//                    String url = response.getUrl();
//                    String transcriptionUrl = response.getTranscriptionUrl();
//                    Map<String, String> tags = response.getTags();
//                }
//
//                @Override
//                public void onProgressChanged(@NonNull String id, float progress) {
//                    // Handle progress upload
//
//                }
//
//                @Override
//                public void onError(@NonNull String id, @NonNull TruvideoSdkException ex) {
//                    // Handle error upload
//                }
//            });
//        });
    }
}

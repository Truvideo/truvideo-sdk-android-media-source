Latest version:

[![](https://jitpack.io/v/Truvideo/truvideo-sdk-android-media.svg)](https://jitpack.io/#Truvideo/truvideo-sdk-android-media)

Add the dependency on the app build gradle
```gradle
implementation 'com.github.Truvideo:truvideo-sdk-android-camera:0.0.3'
```

Make sure to add the jitpack repository in the settings.graddle
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url 'https://jitpack.io'
        }
        // rest of your repositories
    }
}
```

# Using the media module

First you must get the media URI you want to upload and with this URI, you have to call the TruvideoSdkMedia.upload(...) method
```dart
override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  
  // rest of your code

  val pickMedia =
      registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
          if (uri != null) {
              fileUri = uri
          }
      }

  buttonPickFile.setOnClickListener {
      pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
  }

  buttonUploadFile.setOnClickListener {
      uploadFile()
  }
}


private fun uploadFile() {
    val uri = fileUri ?: return

    TruvideoSdkMedia.upload(
        this, object : TruvideoSdkTransferListener {
            override fun onComplete(id: String, url: String) {
                // Called when the transfer finishes correctly.
            }

            override fun onProgressChanged(id: String, progress: Int) {
                // Called when more bytes are transferred.
            }

            override fun onError(id: String, ex: Exception) {
                // Called when an exception happens.
            }
        },
        uri
    )
}
```

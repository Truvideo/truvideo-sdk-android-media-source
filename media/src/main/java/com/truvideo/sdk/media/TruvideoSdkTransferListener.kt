package com.truvideo.sdk.media

interface TruvideoSdkTransferListener {
    /**
     * Called when the transfer finishes correctly.
     *
     * @param id The id of the transfer record.
     */
    fun onComplete(id: Int)

    /**
     * Called when more bytes are transferred.
     *
     * @param id The id of the transfer record.
     * @param progress the percentage transferred currently.
     */
    fun onProgressChanged(id: Int, progress: Int)

    /**
     * Called when an exception happens.
     *
     * @param id The id of the transfer record.
     * @param ex An exception object.
     */
    fun onError(id: Int, ex: Exception)
}

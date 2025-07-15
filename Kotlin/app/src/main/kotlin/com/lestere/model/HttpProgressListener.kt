package com.lestere.model

import com.lestere.utils.CommandLineProgressBar
import io.ktor.client.content.*

object HttpProgressListener {
    class DownloadCommandLineOutputListener(label: String) : ProgressListener {
        private val progressBar: CommandLineProgressBar = CommandLineProgressBar("Download $label", 100)

        override suspend fun onProgress(bytesSentTotal: Long, contentLength: Long?) {
            progressBar.update(bytesSentTotal, contentLength)
        }
    }

    class UploadCommandLineOutputListener(label: String) : ProgressListener {
        private val progressBar: CommandLineProgressBar = CommandLineProgressBar("Upload $label", 100)

        override suspend fun onProgress(bytesSentTotal: Long, contentLength: Long?) {
            progressBar.update(bytesSentTotal, contentLength)
        }
    }
}

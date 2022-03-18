/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.network.utils

import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.Response
import top.limbang.mcmod.network.model.DownloadStatus
import java.io.*

/**
 * ### 开始下载文件
 */
fun Response<ResponseBody>.startDownload(file: File) = flow {
    body()?.let { body ->
        val contentLength = body.contentLength()
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = body.byteStream()
            outputStream = file.outputStream()
            // 当前下载的长度
            var currentLength = 0L
            // 缓冲区
            val bufferSize = 1024 * 8
            val buffer = ByteArray(bufferSize)
            val bufferedInputStream = BufferedInputStream(inputStream, bufferSize)
            var readLength: Int
            while (bufferedInputStream.read(buffer, 0, bufferSize).also { readLength = it } != -1) {
                outputStream.write(buffer, 0, readLength)
                currentLength += readLength
                emit(
                    DownloadStatus.Process(
                        currentLength,
                        contentLength,
                        String.format("%.2f", currentLength.toFloat() / contentLength.toFloat() * 100)
                    )
                )
            }
            outputStream.flush()
        } catch (e: IOException) {
            emit(DownloadStatus.Error(e))
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
        emit(DownloadStatus.Success(file))
    } ?: emit(DownloadStatus.Error(RuntimeException("下载出错")))
}
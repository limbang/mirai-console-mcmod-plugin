package top.limbang.mcmod.network.model

import top.limbang.mcmod.network.model.DownloadStatus.*
import java.io.File

/**
 * ### 下载状态
 * - [Process] 下载中
 * - [Error] 下载出错
 * - [Success] 下载成功
 */
sealed class DownloadStatus {
    /**
     * ### 下载中
     * @param currentLength 当前下载长度
     * @param contentLength 文件总长度
     * @param progress 当前下载的百分比 精确到 0.01
     */
    class Process(val currentLength: Long,val contentLength: Long,val progress:String) : DownloadStatus()

    /**
     * ### 下载报错
     * @param error 错误原因
     */
    class Error(val error: Throwable) : DownloadStatus()

    /**
     * ### 下载成功
     */
    class Success(val file: File) : DownloadStatus()
}
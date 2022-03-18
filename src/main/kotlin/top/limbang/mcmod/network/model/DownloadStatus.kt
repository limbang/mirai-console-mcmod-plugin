/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

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
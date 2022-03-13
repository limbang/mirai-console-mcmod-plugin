package top.limbang.mcmod.network.model

/**
 * ### 搜索过滤分类
 * - [ALL] 全部
 * - [MODULE] 模组
 * - [MODULE_PACKAGE] 整合包
 * - [ITEM] 资料
 * - [COURSE] 教程
 * - [AUTHOR] 作者
 * - [USER] 用户
 * - [COMMUNITY] 社群
 * - [SERVER] 服务器
 */
enum class SearchFilter {
    ALL,
    MODULE,
    MODULE_PACKAGE,
    ITEM,
    COURSE,
    AUTHOR,
    USER,
    COMMUNITY,
    SERVER
}
# Minecraft 百科查询

可以戳一戳机器人获取帮助

## 指令

```shell
# 修改搜索标签的命令
/mcmod setQueryCommand <type> <command>
```

type有如下类型

- [ALL] 全部
- [MODULE] 模组
- [MODULE_PACKAGE] 整合包
- [ITEM] 物品
- [COURSE] 教程
- [AUTHOR] 作者
- [USER] 用户
- [COMMUNITY] 社群
- [SERVER] 服务器

现只支持 `MODULE` `ITEM` `COURSE` `MODULE_PACKAGE` `SERVER`

默认命令：

```shell
MODULE = ssm
ITEM = ssi
COURSE = ssc
MODULE_PACKAGE = ssp
SERVER = sss
```

其他配置直接更改配置文件:

路径:`config/top.limbang.mirai-console-mcmod-plugin/mcmod.yml`

```yaml
# 是否启用戳一戳回复功能 true:启用 false:禁用
isNudgeEnabled: true
# 是否启用群消息回复功能,默认回复群消息 true:启用 false:禁用
isGroupMessagesEnabled: true
# 是否启用好友消息回复功能,默认回复群消息 true:启用 false:禁用
isFriendMessagesEnabled: false
# 是否启用临时消息回复功能,默认回复群消息 true:启用 false:禁用
isTempMessagesEnabled: false
# 是否启用陌生人消息回复功能,默认回复群消息 true:启用 false:禁用
isStrangerMessagesEnabled: false
# 是否启动显示原Url功能,默认不启用 true:启用 false:禁用
isShowOriginalUrlEnabled: false
# 每页显示多少条目,默认为 6
pageSize: 6
```
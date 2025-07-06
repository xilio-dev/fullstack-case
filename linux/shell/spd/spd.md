## 用于查询端口号进程
```shell
(base) liuxin@xilio Desktop % spd
Usage: /usr/local/bin/spd <port_range>
Examples:
  /usr/local/bin/spd 8000-    : List processes with ports below 8000
  /usr/local/bin/spd 8000+    : List processes with ports above 8000
  /usr/local/bin/spd 8000-9000: List processes with ports between 8000 and 9000 (inclusive)
  /usr/local/bin/spd 8000     : List processes using port 8000
```

将[spd.sh](spd.sh)脚本保存在`/usr/local/bin`目录下，便于直接在控制台调用，下面是mac电脑配置。
```shell
chmod +x spd.sh
sudo mv spd.sh /usr/local/bin/spd
```

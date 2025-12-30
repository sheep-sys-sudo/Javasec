# SSRF
## jar协议
```
jar协议是Java为访问压缩包内资源设计的标准协议，支持直接读取.jar、.apk等zip格式文件中的内容而无需解压。其格式为jar:<协议>:<压缩包路径>!/<包内路径>，通过JarURLConnection等类实现自动解析和文件读取。该协议具有无需解压、安全性高和跨平台等优势，被Android和Unity等平台原生支持，适用于快速访问压缩包内的特定资源文件。
```
语法格式： 
jar:<嵌套协议>:<压缩包路径>!/<包内路径>  
`jar:file:///data/app/com.xxx.xxx-1/base.apk!/assets/myAsset.txt`  
现在比如  
`jar:file:///proc/self/cwd/app.jar!/BOOT-INF/classes/application.properties`绕过file协议开头的WAF，读取了源码文件  
注意这里使用的是`/proc/self/cwd`  
```
/proc/self/cwd/是一个指向当前进程工作目录的符号链接。通过访问这个路径，可以获取当前进程的运行目录信息。具体来说，/proc/self目录允许进程直接访问其自身的信息，而cwd文件则提供了当前工作目录的路径。 
```
Java里有JarURLConnection、ZipFile等类专门处理这种协议。
### 参考
 - [https://blog.csdn.net/qq_33060405/article/details/150093266](https://blog.csdn.net/qq_33060405/article/details/150093266)
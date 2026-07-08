## 添加本地jar包到maven仓库
### 收费版本aspose
Aspose.Words是一款先进的类库，通过它可以直接在各个应用程序中执行各种文档处理任务。Aspose.Words支持DOC，OOXML，RTF，HTML，OpenDocument, PDF,XPS,EPUB和其他格式。使用Aspose.Words，可以生成，更改，转换，渲染和打印文档而不使用Microsoft Word。
#### 1.下载jar包
链接:https://pan.baidu.com/s/1BT_MCZYAR3hXhpibPb1DbQ  密码:2zyi
#### 2.导入maven
下载好jar包后将其放入maven本地仓库，便于后期在项目pom文件中引用，前提本地已有maven环境。在cmd中输入以下命令：
~~~
mvn install:install-file -DgroupId=com.aspose -DartifactId=aspose-words -Dversion=15.8.0 -Dpackaging=jar -Dfile=/Users/zhaoxin/Downloads/aspose-words-15.8.0-jdk16.jar
~~~
#### 在项目中引用
~~~
        <dependency>
            <groupId>com.aspose</groupId>
            <artifactId>aspose-words</artifactId>
            <version>15.8.0</version>
        </dependency>
~~~
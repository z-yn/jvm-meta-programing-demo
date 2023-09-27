# jvm-meta-programing-demo

JVM上元编程测试。
## 注意事项
注，对于JDK9之前，com.sun.tools包是外部jar。需要使用systemPath引入

```xml
<dependency>
    <groupId>com.sun</groupId>
    <artifactId>tools</artifactId>
    <version>1.8</version>
    <scope>system</scope>
    <systemPath>${java.home}/../lib/tools.jar</systemPath>
</dependency>
```

而对于JDK9之后。需要从模块导出这些包。
```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <fork>true</fork>
                    <compilerArgs>
                        <arg>--add-exports</arg>
                        <arg>jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
                        <arg>--add-exports</arg>
                        <arg>jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                        <arg>--add-exports</arg>
                        <arg>jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                        <arg>--add-exports</arg>
                        <arg>jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                        <arg>--add-exports</arg>
                        <arg>jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                    </compilerArgs>
                </configuration>
            </plugin>
        </plugins>
    </build>
```
目前测试JDK11是可以的。

但是使用JDK17以及JDK21测试本项目是会编译报错的。应该是因为[JEP 403: Strongly Encapsulate JDK Internals](https://openjdk.org/jeps/403)

测试下来。直接使用是不行的。应该是可以使用反射使用。

## KAPT
于是我好奇使用KAPT来处理，意外发现。对于Kotlin来说，对于这种export的问题，只需要对整个文件开始一行添加注解。Kotlin编译器就会忽略这些模块导出包的引用。不过这个只是编译阶段，运行阶段还是需要添加--add-exports的。不过我们这边的都是编译阶段使用。
```kotlin
@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")
```


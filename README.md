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
目前测试JDK11是可以的。但是使用JDK17测试本项目是会编译报错的。待排查问题
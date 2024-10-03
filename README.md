仓库内容为Java Panama教程代码相关

Java部分代码需要至少Java 22，native部分代码本机包含Rust相关工具链

推荐使用graalvm23进行测试，本项目使用graalvm23以展示涉及到的API和对应的native-image构建

相关平台native-image兼容性请参考[graalvm官方文档](https://www.graalvm.org/latest/reference-manual/native-image/native-code-interoperability/foreign-interface/)

如想运行测试，请在仓库根目录使用以下命令

maven会自动构建对应的Rust代码并放置在正确位置
```shell
mvn clean test
````
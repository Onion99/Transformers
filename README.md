# Essential Plugin for Google App Clones Upload - Android Resource Review Plugin 
# 出海谷歌马甲包上架必备插件 - Android 资源去重插件

[English](#english) | [中文](#chinese)

[![](https://jitpack.io/v/Onion99/Transformers.svg)](https://jitpack.io/#Onion99/Transformers)

## English

### Introduction
Android Resource Review Plugin is a powerful Gradle plugin designed to help Android developers identify and modify resources during the build process. It effectively modifies asset files' SHA256 values to prevent duplicate apk/aab

### Features
- 🔍 Automatically detects resources in your Android project
- 🛠 Modifies asset files' SHA256 values without affecting functionality
- 🛠 Modifies resources files' SHA256 values without affecting functionality
- 🛠 Modifies java/kotlin files' SHA256 values without affecting functionality

### Usage

1. Add the plugin to your project-level build.gradle:

```groovy
dependencies {
    classpath 'com.github.Onion99:Transformers:1.7'
}
```

2. Apply the plugin in your app-level build.gradle:

```groovy
apply plugin: 'com.onion.plugin'
```

3. The plugin will automatically run during the build process

### How It Works
The plugin works by:
1. Scanning asset files during the merge assets phase
2. Modifying file SHA256 values by appending random data
3. Maintaining original file functionality while preventing duplicate conflicts
4. Automatically restoring files to their original state after processing

### Things to note
- The plugin only modifys the file during the build process and will not affect the source file.
- It is recommended to fully test it in the development environment before using it in the production environment.
- If you encounter any problems, you can provide feedback through GitHub Issues

### More spam content generation

https://github.com/Onion99/AndroidJunkCode

## License

Copyright (c) 2024 Nova

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Chinese

### 简介
Android Resource Review Plugin 是一个强大的 Gradle 插件，专门用于帮助 Android 开发者在构建过程中识别和修改资源。它通过有效修改资源文件的 SHA256 值来防止重复资源。

### 特性
- 🔍 自动检测项目中的资源
- 🛠 修改Assets文件的 SHA256 值，同时保持功能完整性
- 🛠 修改资源文件的 SHA256 值，同时保持功能完整性
- 🛠 修改代码文件的 SHA256 值，同时保持功能完整性

### 使用方法

1. 在项目级 build.gradle 中添加插件：
```groovy
dependencies {
    classpath 'com.github.Onion99:Transformers:1.7'
}
```

2. 在应用级 build.gradle 中应用插件：
```groovy
apply plugin: 'com.onion.plugin'
```

3. 插件将在构建过程中自动运行

### 工作原理
插件通过以下步骤工作：
1. 在合并资源阶段扫描资源文件
2. 通过追加随机数据修改文件 SHA256 值
3. 在防止重复冲突的同时保持原始文件功能
4. 处理完成后自动还原文件到原始状态

### 注意事项
- 插件仅在构建过程中修改文件，不会影响源文件
- 建议在开发环境中充分测试后再在生产环境使用
- 如遇到问题，可以通过 GitHub Issues 反馈


### 更多垃圾内容生成

https://github.com/Onion99/AndroidJunkCode

## License

Copyright (c) 2024 Nova

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
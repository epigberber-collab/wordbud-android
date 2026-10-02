# 词芽输入法 · WordBud 0.1.0

安卓离线拼音输入法实验版：中文候选词下面显示英文词典释义。
独立非官方实现，灵感来自青简，不是青简官方安卓版。

## 安装与启用（Android 9 或更新）

1. 下载 `wordbud-0.1.0.apk`，打开文件并安装；如果系统要求允许此来源安装，按手机提示为下载或文件管理应用开启。
2. 打开桌面的「词芽输入法」，点「1 启用词芽输入法」，在系统页面打开词芽。
3. 返回应用，点「2 切换到词芽」，选择词芽输入法。
4. 点击测试框，输入 `nihao`，点「你好」。再试 `kaifa` 和 `xuexi`。

安装包使用开发签名，非应用商店发行。已通过编译、APK 签名验证和词库引擎测试；尚未在真机或模拟器验证。系统对输入法显示的启用提示由 Android 提供。本应用无网络权限、不存储输入记录、不包含广告。

## 操作

| 操作 | 结果 |
|---|---|
| 输入全拼 | 显示中文候选及英文释义 |
| 点击候选 | 输入中文 |
| 长按候选 | 输入该词的英文词典释义（可能含两个词义） |
| 横向滑动候选条 | 查看其他候选 |
| 空格 | 选择第一个候选；没有拼音时输入空格 |
| 回车 | 有拼音时确认原始拼音；否则换行或执行搜索等动作 |
| 中 / EN | 切换中文、英文模式 |
| 123 / ABC | 切换数字符号、字母键盘 |
| ⇧ | 英文模式切换大小写 |
| ⌫ | 删除一个拼音字母或光标前字符 |
| 长按 ⌫ | 清空当前待选拼音 |
| 地球键 | 切换其他输入法，长按打开输入法选择器 |
| `v` | 表示 ü，例如 `lv` 可找到「绿」 |

密码、邮箱、网址和数字类输入框默认直接输入，不显示译词。应用不保存输入习惯或密码。

## 已实现与限制

- 内置 115,107 条经过转换的离线中英词典条目，无需下载词库。
- 支持全拼词语查询、基础候选排序、逐词选字。`woxiangxuexi` 可以依次选「我」「想」「学习」。
- 首次词库加载在后台完成，避免阻塞键盘启动。
- 长词优先及少量常用词优先，未使用统计词频模型；多音字和多义词可能需要滑动找候选。
- 本版没有智能整句预测、拼写纠错、简拼、双拼、五笔、语音、手写或自动学习。
- 释义是词典释义，不是结合当前句子生成的翻译。
- 不适合作为已经验证稳定的主力输入法；保留手机原有输入法便于切换。

## 源码与构建

用 Android Studio 打开本目录，安装 Android SDK 35，同步项目，然后 Build APK。
项目使用 Android Gradle Plugin 8.7.3，需要兼容的 Gradle（8.9）和 JDK 17。没有附带 Gradle Wrapper，请使用 Android Studio 或自行安装 Gradle。

也可使用不依赖 Gradle 的脚本，前提是安装 Java 编译器、SDK Platform 35、Build Tools 35.0.0、zip 和 keytool：

```bash
export WORDBUD_ANDROID_JAR=/path/to/android-sdk/platforms/android-35/android.jar
export WORDBUD_BUILD_TOOLS=/path/to/android-sdk/build-tools/35.0.0
bash tools/build-apk.sh
```

如果只有 Java 运行时，可额外设置 `WORDBUD_ECJ_JAR` 指向 Eclipse ECJ 3.37.0 编译器。脚本生成的开发签名密钥在 `out/development.jks`；更新同一安装时必须保留同一密钥。

词库引擎的 JVM 检查：

```bash
mkdir -p out/test
javac -encoding UTF-8 -d out/test app/src/main/java/app/wordbud/ime/Lexicon.java tools/LexiconTest.java
java -cp out/test LexiconTest app/src/main/assets/lexicon.tsv.gz
```

## 许可证和致谢

应用原创代码：MIT，见 LICENSE。
词库：CC-CEDICT（Paul Denisowski、CC-CEDICT contributors / MDBG），CC BY-SA 4.0。
来源：https://www.mdbg.net/chinese/dictionary?page=cc-cedict
许可：https://creativecommons.org/licenses/by-sa/4.0/

改编过程：保留简体中文词条、去除拼音声调、用 v 代替 ü、整理前两个主要释义、基础排序。
转换后的 `app/src/main/assets/lexicon.tsv.gz` 继续采用 CC BY-SA 4.0，转换脚本为 `tools/make_lexicon.py`。下载的原始词典也随源码包提供在 `data/cedict-source.txt.gz`。

青简项目：https://github.com/qingjian-team/qingjian 。本项目没有使用其代码、logo 或品牌名，不代表其维护者。

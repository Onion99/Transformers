# -*- coding: utf-8 -*-
import json

import lief
import os
import sys
import random

mapping = []
NEW_NAME = "newName"
# 不能改名字的库
WHITE_LIST = ["liblog.so", "libdl.so", "libz.so", "libstdc++.so", "libc.so", "libm.so", "libdu.so",
              "libfdk-aac.so", "libijkffmpeg.so", "libopenh264.so", "libyuv.so", "libGLESv2.so", "libEGL.so",
              "libandroid.so", "libjnigraphics.so", "libOpenSLES.so", "libm.so", "libc.so", "libZegoLiveRoom.so",
              "libc++_shared.so"]


# 根据文件名找到处理方式，返回none表示不处理
def findRecord(fileName):
    detail = None
    for record in mapping:
        if record['name'] == fileName:
            detail = record
            break
    return detail


# 遍历不能改名字的名单
def stripRecord(keepList):
    for keepSo in keepList:
        detail = findRecord(keepSo)
        if detail is not None:
            # print(f"恢复名字:{keepSo} from {detail[NEW_NAME]} to {keepSo}")
            detail[NEW_NAME] = keepSo


def generateData(size):
    data = [] * size
    for i in range(0, size):
        data.append(random.randint(0, 255))
    return data


def addSection(binary, filename):
    detail = findRecord(filename)
    if detail is None:
        return

    try:
        sections = detail['sections']
        if len(sections) == 0:
            sections.append({
                'name': ".obdata",
                'data': generateData(128)
            })

        for sec in sections:
            secName = sec['name']
            content = sec['data']

            # 移除
            if binary.has_section(secName):
                binary.remove_section(secName)
            # 添加
            section = lief.ELF.Section()
            section.name = secName
            section.type = lief.ELF.SECTION_TYPES.PROGBITS
            section.content = content
            binary.add(section, True)
    except BaseException as e:
        print("添加section异常: %s, %s" % (filename, e))


def modifyDepend(binary, filename, cacheFile):
    try:
        # 修改名称
        if lief.ELF.DYNAMIC_TAGS.SONAME in binary:
            soname = binary[lief.ELF.DYNAMIC_TAGS.SONAME]
            detail = findRecord(soname.name)
            if detail is not None:
                # print(soname)
                soname.name = detail[NEW_NAME]

        # 修改依赖
        for entry in binary.dynamic_entries:
            if entry.tag == lief.ELF.DYNAMIC_TAGS.NEEDED:
                # print(entry)
                detail = findRecord(entry.name)
                if detail is not None:
                    entry.name = detail[NEW_NAME]
        return True
    except BaseException as e:
        print("修改依赖名称异常: %s, %s" % (filename, e))
        removeDictFile(filename, cacheFile)
        return False


def outputNewFile(binary, filename, cacheFile, root, filePath):
    try:
        detail = findRecord(filename)
        if detail is None:
            print("not change elf file: " + filename)
            return
        newFileName = detail[NEW_NAME]
        newFilePath = os.path.join(root, newFileName)
        binary.write(newFilePath)
        if newFileName != filename:
            os.remove(filePath)
        print(filePath + " --> " + newFileName)

    except BaseException as e:
        print("输出文件异常: %s, %s" % (filename, e))
        removeDictFile(filename, cacheFile)


def removeDictFile(filename, cacheFile):
    try:
        detail = findRecord(filename)
        if detail is not None:
            mapping.remove(detail)
            print("移除字典名称: %s" % filename)
            with open(cacheFile, 'w', encoding='utf-8') as f:
                f.write((filename + ","))
    except BaseException as e:
        print("移除字典名称异常: %s" % e)


def optElf(dirPath, filename):
    filePath = os.path.join(dirPath, filename)
    # 解析ELF文件
    binary = lief.ELF.parse(filePath)
    # 添加section
    addSection(binary, filename)
    # 修改依赖
    success = modifyDepend(binary, filename, cacheFile)
    # 输出文件
    if success:
        outputNewFile(binary, filename, cacheFile, dirPath, filePath)
    # print("*** done " + filename + " ***")


# def findKeepList(dir, filename):
#     keeps = []
#     filePath = os.path.join(dir, filename)
#     binary: lief.ELF.Binary = lief.ELF.parse(filePath)
#     for str in binary.strings:
#         if str.endswith(".so") and (str.startswith("lib") or str.startswith("/lib")):
#             keeps.append(str.replace("/", ""))
#             # print(f"找到so依赖:{str} :: {filePath}")
#     return keeps


def doWithExecutor(dirPath, abiArray):
    keepList = set()
    for keep in WHITE_LIST:
        keepList.add(keep)
    # for abi in abiArray:
    #     if abi != "":
    #         dir = os.path.join(dirPath, abi)
    #         for filename in os.listdir(dir):
    #             if filename.endswith(".so") and filename != "libdu.so":
    #                 finds = findKeepList(dir, filename)
    #                 for find in finds:
    #                     keepList.add(find)
    stripRecord(keepList)

    for abi in abiArray:
        if abi != "":
            print("deal with abi: " + abi)
            dir = os.path.join(dirPath, abi)
            for filename in os.listdir(dir):
                if filename.endswith(".so") and filename != "libdu.so" and filename != "libZegoLiveRoom.so":
                    optElf(dir, filename)
    print(f"ObscureOutputSo:{json.dumps(mapping, default=lambda o: o.__dict__).replace(' ', '')}")


if __name__ == '__main__':
    dirPath = sys.argv[1]
    abi = sys.argv[2].split(",")

    filePath = sys.argv[3]
    if len(filePath) > 0:
        try:
            data = ""
            with open(filePath, 'r', encoding='utf-8') as file:
                data = file.read()
            mapping = json.loads(data)
        except Exception:
            print(f"{filePath} cannot read")

    if os.path.isdir(dirPath):
        # 异常so包名称存储
        cacheFile = os.path.join(dirPath, "errorSo")
        if os.path.exists(cacheFile):
            os.remove(cacheFile)
        # 使用线程池处理文件
        doWithExecutor(dirPath, abi)

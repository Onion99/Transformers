# -*- coding: utf-8 -*-
# 版本：python 3.9
# 使用命令行执行，注意修改根目录

import json
import os
import io
import uuid
import string
import random
import xml.dom.minidom
from xml.dom import minidom
from xml.sax import saxutils
import sys


# 全局默认编码
# reload(sys)
# sys.setdefaultencoding( "utf-8" )

class ChangeRecord:
    name = ""
    changeNodes = []


class ChangeNode:
    currentNode = 0
    tagName = ""
    key = ""
    value = ""


# Node下标
randomNode = 0
currentNode = 0
isLayout = False
currentChangeNodes = []
androidName = "android"
toolsName = "tools"
appName = "app"
rootHasAndroid = False
rootHasTool = False
rootHasApp = False

# 字典，懒得改
mapping = []
currentMapChangeRecord = {}


# 获取元素数量
def getNotesCount(root, count):
    if root.childNodes:
        for node in root.childNodes:
            if node.nodeType is minidom.Node.ELEMENT_NODE:
                # print "get" + node.nodeName
                count = getNotesCount(node, count + 1)
        return count
    else:
        return count


# 随机attr
def random_arrtibute(node, must, isLayout):
    if node.tagName == "layout":
        return

    global androidName
    global toolsName
    global appName

    android1 = androidName + ":contentDescription"
    tools1 = toolsName + ":contentDescription"
    tools2 = toolsName + ":level"
    tools3 = toolsName + ":times"
    tools4 = toolsName + ":kind"
    tools5 = toolsName + ":checkCk"
    tools6 = toolsName + ":kindTT"
    app1 = appName + ":oa"
    app2 = appName + ":ob"
    app3 = appName + ":oc"
    app4 = appName + ":od"

    attrs = node._get_attributes()
    a_names = list(attrs.keys())
    for a_name in a_names:
        if a_name == android1:
            node.removeAttribute(android1)
        if a_name == tools1:
            node.removeAttribute(tools1)
        if a_name == tools2:
            node.removeAttribute(tools2)
        if a_name == tools3:
            node.removeAttribute(tools3)
        if a_name == tools4:
            node.removeAttribute(tools4)
        if a_name == tools5:
            node.removeAttribute(tools5)
        if a_name == tools6:
            node.removeAttribute(tools6)
        if a_name == app1:
            node.removeAttribute(app1)
        if a_name == app2:
            node.removeAttribute(app2)
        if a_name == app3:
            node.removeAttribute(app3)
        if a_name == app4:
            node.removeAttribute(app4)

    tNodes = []
    changeKeys = []

    if currentMapChangeRecord is not None and currentMapChangeRecord['changeNodes'] is not None and len(
            currentMapChangeRecord['changeNodes']) > 0:
        for mapNode in currentMapChangeRecord['changeNodes']:
            changeNode = ChangeNode()
            changeNode.tagName = node.tagName
            changeNode.currentNode = currentNode
            if mapNode['tagName'] == changeNode.tagName and mapNode['currentNode'] == changeNode.currentNode:
                changeNode.key = mapNode['key']
                changeNode.value = mapNode['value']

                if changeNode.key not in changeKeys:
                    changeKeys.append(changeNode.key)
                    node.setAttribute(changeNode.key, changeNode.value)
                    currentChangeNodes.append(changeNode)
                # print(f'找到匹配属性:{changeNode.tagName} {changeNode.currentNode} {changeNode.key} {changeNode.value}')
    else:
        changes_points_size = random.randint(1, 2)
        for i in range(0, changes_points_size):
            changeNode = ChangeNode()
            changeNode.tagName = node.tagName
            changeNode.currentNode = currentNode

            start = 1
            end = 22
            if isLayout:
                start = 0
            if must:
                start = 5
                end = 8
            r = random.randint(start, end)
            if randomNode == 1 and changeNode.tagName == "translate":
                r = 0
            if r == 0:
                tagUuid = uuid.uuid4()
                changeNode.key = android1
                changeNode.value = str(tagUuid)
            elif r == 1:
                tagUuid = uuid.uuid4()
                changeNode.key = tools1
                changeNode.value = str(tagUuid)
            elif r == 2:
                ran_str = ''.join(random.sample(string.ascii_letters + string.digits, 8))
                changeNode.key = tools2
                changeNode.value = ran_str
            elif r == 3:
                ran_str = ''.join(random.sample(string.ascii_letters + string.digits, 9))
                changeNode.key = tools3
                changeNode.value = ran_str
            elif r == 4:
                ran_str = ''.join(random.sample(string.ascii_letters + string.digits, 10))
                changeNode.key = tools4
                changeNode.value = ran_str
            elif r == 5:
                # ran_str = ''.join(random.sample(string.ascii_letters + string.digits, 7))
                # changeNode.key = tools5
                # changeNode.value = ran_str
                ran_str = ''.join(random.sample(string.ascii_letters + string.digits, 2))
                changeNode.key = app1
                changeNode.value = ran_str
            elif r == 6:
                ran_str = ''.join(random.sample(string.ascii_letters + string.digits, 2))
                changeNode.key = app2
                changeNode.value = ran_str
            elif r == 7:
                ran_str = ''.join(random.sample(string.ascii_letters + string.digits, 2))
                changeNode.key = app3
                changeNode.value = ran_str
            elif r == 8:
                ran_str = ''.join(random.sample(string.ascii_letters + string.digits, 2))
                changeNode.key = app4
                changeNode.value = ran_str

            if len(changeNode.key) > 0 and len(changeNode.value) > 0:
                if changeNode.key not in changeKeys:
                    changeKeys.append(changeNode.key)
                    node.setAttribute(changeNode.key, changeNode.value)
                    currentChangeNodes.append(changeNode)


# 重写XML写入文件方法
# indent = current indentation
# addindent = indentation to add to higher levels
# newl = newline string
def fixed_writexml(self, writer, indent="", addindent="", newl=""):
    global currentNode
    global randomNode
    global isLayout
    global androidName
    global toolsName
    global appName
    global rootHasAndroid
    global rootHasTool
    global rootHasApp

    # 判断XML中有无数据（比如<string></string>中间的字符串）
    hasData = False
    hasCDate = False
    if self.childNodes:
        if self.firstChild.nodeType == minidom.Node.CDATA_SECTION_NODE:
            hasCDate = True
        elif self.firstChild.nodeType != minidom.Node.ELEMENT_NODE:
            if self.firstChild.data.replace('\n', '').replace('\r', '').strip() != "":
                hasData = True
    # 需要展示为单行的标签
    needOneLine = self.tagName in ("resources", "attr", "style")

    isRoot = indent == ""
    isVpRoot = isRoot or (self.parentNode is not None
                          and self.parentNode.tagName == "layout"
                          and self.parentNode.parentNode.nodeName == "#document")
    parentHasData = indent == "ParentHasData"
    if parentHasData:
        indent = ""

    # 开始写入
    writer.write(indent + "<" + self.tagName)

    # 处理root标签
    if isRoot or isVpRoot:
        attrs = self._get_attributes()
        a_names = list(attrs.keys())
        # 写入 xmlns 中的 android & tools，并记录名称
        hasTools = False
        hasAndroid = False
        hasApp = False
        for a_name in a_names:
            if a_name.startswith("xmlns:"):
                # xmlns:android="http://schemas.android.com/apk/res/android"
                if attrs[a_name].value == "http://schemas.android.com/apk/res/android":
                    # 获取xmlns的名字
                    androidName = a_name.split(":")[1]
                    hasAndroid = True
                    if isRoot:
                        rootHasAndroid = True
                # xmlns:tools="http://schemas.android.com/tools"
                elif attrs[a_name].value == "http://schemas.android.com/tools":
                    hasTools = True
                    if isRoot:
                        rootHasTool = True
                    # 获取xmlns的名字
                    toolsName = a_name.split(":")[1]
                elif attrs[a_name].value == "http://schemas.android.com/apk/res-auto":
                    hasApp = True
                    if isRoot:
                        rootHasApp = True
                    # 获取xmlns的名字
                    appName = a_name.split(":")[1]

        if not hasApp and self.tagName != "layout":
            if isRoot or (isVpRoot and not rootHasApp):
                if needOneLine:
                    writer.write("%sxmlns:%s=\"http://schemas.android.com/apk/res-auto\"" % (" ", appName))
                else:
                    writer.write(
                        "%s%s%sxmlns:%s=\"http://schemas.android.com/apk/res-auto\"" % (
                            newl, indent, addindent, appName))
        if not hasAndroid and self.tagName != "layout":
            if isRoot or (isVpRoot and not rootHasAndroid):
                if needOneLine:
                    writer.write("%sxmlns:%s=\"http://schemas.android.com/apk/res/android\"" % (" ", androidName))
                else:
                    writer.write(
                        "%s%s%sxmlns:%s=\"http://schemas.android.com/apk/res/android\"" % (
                            newl, indent, addindent, androidName))

        if not hasTools and self.tagName != "layout":
            if isRoot or (isVpRoot and not rootHasTool):
                if needOneLine:
                    writer.write("%sxmlns:%s=\"http://schemas.android.com/tools\"" % (" ", toolsName))
                else:
                    writer.write(
                        "%s%s%sxmlns:%s=\"http://schemas.android.com/tools\"" % (
                            newl, indent, addindent, toolsName))

        # 写入剩余 xmlns
        for a_name in a_names:
            if a_name.startswith("xmlns:"):
                if needOneLine:
                    writer.write("%s%s=\"" % (" ", a_name))
                else:
                    writer.write("%s%s%s%s=\"" % (newl, indent, addindent, a_name))
                minidom._write_data(writer, attrs[a_name].value)
                writer.write("\"")

    # 添加随机属性
    currentNode += 1
    must = (randomNode == currentNode)
    random_arrtibute(self, must, isLayout)
    # 获取所有属性
    attrs = self._get_attributes()
    a_names = list(attrs.keys())
    sorted(a_names)

    # 写入 attrs
    isOne = len(a_names) == 1
    for a_name in a_names:
        if not (isRoot or isVpRoot) or not a_name.startswith("xmlns:"):
            if (isOne and not isRoot) or hasCDate or hasData or needOneLine:
                writer.write("%s%s=\"" % (" ", a_name))
            else:
                writer.write("%s%s%s%s=\"" % (newl, indent, addindent, a_name))
            minidom._write_data(writer, attrs[a_name].value)
            writer.write("\"")

    # 处理子标签，递归调用
    if self.childNodes:
        if isLayout:
            writer.write(">%s%s" % (newl, newl))
        elif hasCDate:
            writer.write("><![CDATA[%s]]>" % self.firstChild.data)
        elif hasData:
            data = saxutils.escape(self.firstChild.data)
            writer.write(">%s" % data)
        else:
            writer.write(">%s" % newl)

        for node in self.childNodes:
            if node.nodeType is minidom.Node.ELEMENT_NODE:
                if hasData:
                    node.writexml(writer, "ParentHasData", addindent, newl)
                else:
                    node.writexml(writer, indent + addindent, addindent, newl)
            elif node.nodeType is minidom.Node.COMMENT_NODE:
                # print(u'描述'+": %s" % node.data)
                # 注解<!---->在这里面写出，这里保留注释
                node.writexml(writer, indent + addindent, addindent, newl)

        if parentHasData:
            writer.write("</%s>" % self.tagName)
        elif hasCDate or hasData:
            writer.write("</%s>%s" % (self.tagName, newl))
        else:
            writer.write("%s</%s>%s" % (indent, self.tagName, newl))
    elif isLayout:
        writer.write("%s/>%s%s" % (" ", newl, newl))
    else:
        writer.write("%s/>%s" % (" ", newl))


minidom.Element.writexml = fixed_writexml


# 处理XML文件
def opLayoutXml(path):
    global currentChangeNodes
    global currentMapChangeRecord
    global toolsName
    global androidName
    global appName
    global rootHasTool
    global rootHasAndroid
    global rootHasApp
    changeRecords = []

    for root, dirs, files in os.walk(path):
        # 遍历所有文件
        for filename in files:
            if filename.endswith(".xml"):
                currentChangeNodes = []
                currentMapChangeRecord = None
                toolsName = "tools"
                androidName = "android"
                appName = "app"
                rootHasAndroid = False
                rootHasTool = False
                rootHasApp = False
                for record in mapping:
                    if record['name'] == filename:
                        # print(f'找到匹配文件:{filename}')
                        currentMapChangeRecord = record
                        break

                path = os.path.join(root, filename)  # .decode('gbk').encode('utf-8');
                global isLayout
                isLayout = ("/res/layout" in path) or ("/release/layout" in path)

                if isLayout or ("/res/drawable" in path) or ("/release/drawable" in path) \
                        or ("/res/anim" in path) or ("/release/anim" in path) \
                        or ("/res/color" in path) or ("/release/color" in path) \
                        or ("/res/interpolator" in path) or ("/release/interpolator" in path) \
                        or ("/res/navigation" in path) or ("/release/navigation" in path) \
                        or ("/res/values" in path) or ("/release/values" in path):
                    print(path)
                    domTree = minidom.parse(path)
                    rootNode = domTree.documentElement

                    global currentNode
                    global randomNode
                    count = getNotesCount(rootNode, 1)
                    randomNode = random.randint(1, count)
                    # print("node count: " + str(count) + ", random index: " + str(randomNode))
                    currentNode = 0

                    with open(path, 'w', encoding='utf-8') as outfile:
                        # outfile.write(domTree.toprettyxml(indent="    ", encoding="utf-8"));
                        domTree.writexml(outfile, addindent='    ', newl='\n', encoding='utf-8')
                        print("success")

                if len(currentChangeNodes) > 0 and len(filename) > 0:
                    record = ChangeRecord()
                    record.name = filename
                    record.changeNodes = currentChangeNodes.copy()
                    changeRecords.append(record)

    print(f"ObscureOutputXml:{json.dumps(changeRecords, default=lambda o: o.__dict__).replace(' ', '')}")


def obscure_xml(path):
    # 项目路径
    # dirPath = os.path.dirname(__file__)
    # dirPath = sys.path[0]
    dirPath = path
    print(dirPath)
    opLayoutXml(dirPath)


if __name__ == '__main__':
    args = sys.argv
    if len(args) < 2:
        obscure_xml(os.getcwd())
    elif len(args) < 3:
        obscure_xml(args[1])
    else:
        filePath = args[2]
        if len(filePath) > 0:
            try:
                data = ""
                with open(filePath, 'r', encoding='utf-8') as file:
                    data = file.read()
                mapping = json.loads(data)
            except Exception:
                print(f"{filePath} cannot read")
        obscure_xml(args[1])

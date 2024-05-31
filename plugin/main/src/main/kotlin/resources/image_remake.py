import json
import os.path
import random
import re
import sys
# 通过一下命令添加依赖
# python3 -m pip install --upgrade pip
# python3 -m pip install --upgrade Pillow
import time

from PIL import Image, ImageSequence
from PIL.ImageDraw import ImageDraw

support_images = ["png", "jpg", "jpeg", "webp"]
mapping = []


# 获取文件扩展名
def get_file_type(file):
    result = re.match(".*\\.(.+)$", file)
    if result:
        return str.lower(result[1])
    else:
        return ""


def get_file_name(file):
    return file.split("/")[-1]


# 获取文件类型的打开Mode
def get_image_open_mode(image_type):
    return "RGB" if image_type == "jpg" or image_type == "jpeg" else "RGBA"


# 获取图片类型保存的类型
def get_image_save_type(image_type):
    return "JPEG" if image_type == "jpg" or image_type == "jpeg" else str.upper(image_type)


# 查找所有的jpg, webp, png文件
def find_all_image_files(target_dir):
    root_abs_path = os.path.abspath(target_dir)
    if os.path.isdir(root_abs_path):
        result = []
        children = os.listdir(root_abs_path)
        for child in children:
            abs_child = os.path.join(root_abs_path, child)
            if os.path.isfile(abs_child):
                file_type = get_file_type(child)
                if file_type in support_images:
                    result.append(abs_child)
            elif os.path.isdir(abs_child):
                result += find_all_image_files(abs_child)
        return result
    else:
        raise Exception(root_abs_path + ' is not a directory.')


# 随机修改图片的一个像素Alpha值（加1或者减1）
def remake_image(path):
    image_type = get_file_type(path)
    image_name = get_file_name(path)
    change_log = {"name": image_name, "change_points": []}
    for m in mapping:
        if m["name"] == image_name:
            for (x, y) in m["change_points"]:
                # print(f'找到mapping匹配 {image_name}:{x},{y}')
                change_log["change_points"].append((x, y))
            break
    image_o = Image.open(path)
    if image_o and image_type in support_images:
        # webp有可能是动图
        if image_type == "webp":
            index = 0
            read_images = []
            durations = []
            for frame in ImageSequence.Iterator(image_o):
                # 只是修改第一帧图片
                if index == 0:
                    (new_frame, change_points) = remake_image_frame(frame, image_o.mode, False,
                                                                    change_log["change_points"], True)
                    read_images.append(new_frame.copy())
                    change_log["change_points"] = change_points
                else:
                    read_images.append(frame.copy())
                index += 1
                frame.load()
                durations.append(frame.info["duration"])
            frame_size = len(read_images)
            if frame_size > 0:
                loop = image_o.info["loop"]
                # image_n = Image.open(path)
                if frame_size == 1:
                    # images = read_images
                    image_o.save(path, formats=get_image_save_type(image_type), save_all=True)
                else:
                    read_images[0].save(path, formats=get_image_save_type(image_type), save_all=True,
                                        append_images=read_images[1:], duration=durations, loop=loop)
                print(f"Has remake file: {path}")
                return change_log
        else:
            (image_n, change_points) = remake_image_frame(image_o, get_image_open_mode(image_type),
                                                          path.endswith(".9.png"), change_log["change_points"], False)
            change_log["change_points"] = change_points
            image_n.save(path, get_image_save_type(image_type))
            print(f"Has remake file: {path}")
            return change_log


def remake_image_frame(frame, mode, is_9_png, points, isWebp):
    (w, h) = frame.size
    if isWebp:
        image_l = frame
        image_n = frame
    else:
        image_l = frame.convert(mode)
        image_n = Image.new(mode, (w, h), (255, 255, 255, 255) if mode == "RGBA" else (255, 255, 255))

    image_n_draw = ImageDraw(image_n)
    # 是否为9 png图
    change_points = []
    # 提取mapping修改，或随机修改1--5个像素点。
    if len(points) > 0:
        change_points = points
    else:
        changes_points_size = random.randint(1, 5)
        for i in range(0, changes_points_size):
            # 9 png图片过滤掉边缘部分
            change_x = random.randint(1 if is_9_png else 0, (w - 2) if is_9_png else (w - 1))
            change_y = random.randint(1 if is_9_png else 0, (h - 2) if is_9_png else (h - 1))
            change_points.append((change_x, change_y))

    for x in range(0, w):
        for y in range(0, h):
            if mode == "RGBA":
                r, g, b, a = image_l.getpixel((x, y))
                if (x, y) in change_points:
                    new_a = a + (1 if a % 2 == 0 else -1)
                    # change_log += f"    x={x}, y={y}, change alpha: {a} -> {new_a}\n"
                    a = new_a
                image_n_draw.point((x, y), (r, g, b, a))
            else:
                r, g, b = image_l.getpixel((x, y))
                if (x, y) in change_points:
                    new_b = b + (1 if b % 2 == 0 else -1)
                    # change_log += f"    x={x}, y={y}, change blue: {b} -> {new_b}\n"
                    b = new_b
                image_n_draw.point((x, y), (r, g, b))
    return image_n, change_points


def remake_images(path="./", logs_path=None):
    images = find_all_image_files(path)
    logs = []
    for image in images:
        try:
            c_log = remake_image(image)
            logs.append(c_log)
        except:
            print(f'Remake file fail: {image}')
    print(f"ObscureOutputImg:{json.dumps(logs).replace(' ','')}")


# 参数1：查找路径，如果没有提供路径，会以当前目录为查找目录
# 参数2：是否生成日志文件，默认不生成.
if __name__ == "__main__":
    args = sys.argv
    if len(args) < 2:
        remake_images('./')
    elif len(args) < 3:
        remake_images(args[1])
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
        remake_images(args[1])

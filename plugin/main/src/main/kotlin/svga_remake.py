# 需要通过以下命令安装protobuf
# pip install protobuf
import json
import time
import svga_remake_pb2
import zlib
import os
import sys
import zipfile

mapping = []


def findRecord(fileName):
    detail = None
    for record in mapping:
        if record['newName'] == fileName:
            detail = record
            break
    return detail


# svga文件
def find_all_svga_files(target_dir):
    root_abs_path = os.path.abspath(target_dir)
    if os.path.isdir(root_abs_path):
        result = []
        children = os.listdir(root_abs_path)
        for child in children:
            abs_child = os.path.join(root_abs_path, child)
            if os.path.isfile(abs_child):
                if child.endswith(".svga"):
                    result.append(abs_child)
            elif os.path.isdir(abs_child):
                result += find_all_svga_files(abs_child)
        return result
    else:
        raise Exception(root_abs_path + ' is not a directory.')


# SVGA V1是通过zip格式压缩文件的
def is_svga_v1(path):
    return zipfile.is_zipfile(path)


# 处理V1版本的SVGA文件
def remake_svga_v1(path):
    zip_file_data = {}
    remake_time = 0
    zip_file = zipfile.ZipFile(path, "r")
    filename = path.split('/').pop()
    detail = findRecord(filename)
    if detail is None:
        return
    sections = detail['sections']
    if len(sections) == 0:
        sections.append({
            'name': "remake",
            'data': int(time.time())
        })

    for child in zip_file.namelist():
        if child == "movie.spec":
            json_str = str(zip_file.read("movie.spec"), "utf-8")
            json_obj = json.loads(json_str)
            for section in sections:
                secName = section['name']
                content = section['data']
                if secName == "remake":
                    json_obj["remake"] = content
                    remake_time = content
            json_str = json.dumps(json_obj)
            zip_file_data["movie.spec"] = json_str.encode("utf-8")
        else:
            zip_file_data[child] = zip_file.read(child)
    zip_file.close()
    zip_file = zipfile.ZipFile(path, "w")
    for (name, data) in zip_file_data.items():
        zip_file.writestr(name, data)
    zip_file.close()
    return remake_time


# 处理V2版本的SVGA文件
def remake_svga_v2(path):
    read_file = open(path, "rb")
    movie_entity = svga_remake_pb2.MovieEntity()
    movie_entity.ParseFromString(zlib.decompress(read_file.read()))
    filename = path.split('/').pop()
    read_file.close()

    detail = findRecord(filename)
    if detail is None:
        return
    sections = detail['sections']
    if len(sections) == 0:
        sections.append({
            'name': "remake",
            'data': int(time.time())
        })

    remake_time = 0
    for section in sections:
        secName = section['name']
        content = section['data']
        if secName == "remake":
            remake_time = content
            movie_entity.remake = content
    remade_bytes = zlib.compress(movie_entity.SerializeToString())
    write_file = open(path, "wb")
    write_file.write(remade_bytes)
    write_file.flush()
    write_file.close()
    return remake_time


def remake_svga(path="./", logs_path=None):
    svgas = find_all_svga_files(path)
    change_logs = None
    if logs_path is not None:
        if not os.path.exists(logs_path):
            os.makedirs(logs_path)
        change_logs = open(f"{logs_path}/svga_remake_logs_{time.strftime('%Y-%m-%d_%H-%M-%S', time.localtime())}.txt", mode="w")
    for svga in svgas:
        try:
            if is_svga_v1(svga):
                remake_time = remake_svga_v1(svga)
                print(f"Has remade svga v1: {svga}")
                if change_logs is not None:
                    change_logs.write(f"svga v1 -> path: {svga}, remake time: {remake_time}\n")
            else:
                remake_time = remake_svga_v2(svga)
                print(f"Has remade svga v2: {svga}")
                if change_logs is not None:
                    change_logs.write(f"svga v2 -> path: {svga}, remake time: {remake_time}\n")
        except:
            print(f'Remake svga fail: {svga}')
    if change_logs is not None:
        change_logs.flush()
        change_logs.close()
    print(f"ObscureOutputSvga:{json.dumps(mapping, default=lambda o: o.__dict__).replace(' ', '')}")


# 参数1：修改文件的目录 (没有指定，为当前目录)
# 参数2：日志文件的目录 (没有指定不会输出日志文件)
if __name__ == "__main__":
    args = sys.argv
    # if len(args) < 2:
    #     remake_svga()
    # elif len(args) < 3:
    #     remake_svga(args[1])
    # else:
    filePath = sys.argv[2]
    if len(filePath) > 0:
        try:
            data = ""
            with open(filePath, 'r', encoding='utf-8') as file:
                data = file.read()
            mapping = json.loads(data)
        except Exception:
            print(f"{filePath} cannot read")

    remake_svga(args[1], args[3])
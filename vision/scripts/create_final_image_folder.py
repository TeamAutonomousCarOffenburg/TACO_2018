from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
import argparse

import os.path
import glob

from utils import tools
import shutil


def main():
    parser = argparse.ArgumentParser(description='Process some integers.')
    parser.add_argument('--final_config_file', required=True)
    parser.add_argument('--final_folder', required=True)
    parser.add_argument('--src_folder', required=True)
    args = parser.parse_args()

    if not os.path.isfile(args.final_config_file):
        print("final_config_file does not exist...")
        exit()

    if not os.path.isdir(args.src_folder):
        print("src_folder does not exist...")
        exit()

    if not os.path.isdir(args.final_folder):
        os.makedirs(args.final_folder)
        exit()

    final_config = []
    config = tools.read_json_file(args.final_config_file)

    for image in config:
        if image["filename"] not in final_config and len(
                image["annotations"]) > 0:
            final_config.append(image)

    folder_id = 1
    image_set = []

    print("split {} images".format(len(final_config)))

    for image in final_config:
        image_set.append(image)

        if len(image_set) == 251:
            file = write_images(image_set, args, folder_id)
            print("file: {} created".format(file))
            folder_id += 1
            image_set = []

    if len(image_set) > 0:
        write_images(image_set, args, folder_id)
        print("last file: {} created".format(file))


def write_images(images, args, folder_id):
    folder = os.path.join(args.final_folder, "set_{}".format(folder_id))
    os.mkdir(folder)
    file = os.path.join(folder, "set_{}.json".format(folder_id))
    tools.write_json_file(file, images)

    for image in images:
        shutil.copy(
            os.path.join(args.src_folder, image["filename"]),
            os.path.join(folder, image["filename"]))

    return file


if __name__ == '__main__':
    main()

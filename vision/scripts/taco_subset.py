import argparse
import os
import random
import shutil

import taco_tools as tt


def get_args():
    parser = argparse.ArgumentParser(description='Skript to create a random subset of an existing Set  with image and annotation.')
    parser.add_argument(
        '-s',
        '--image-dir', required=True)
    parser.add_argument(
        '-o',
        '--output-dir', required=True)
    parser.add_argument(
        '-j',
        '--source-json', required=True)

    parser.add_argument(
        '-tj',
        '--target-json',
        required=False,
        default="\\final_config.json",
        help="filename for destination json file, which will generated in output_dir")

    parser.add_argument(
        '-n',
        '--num-of-files',
        required=False,
        default=50,
        help="How many random files do you want from this set")


    args = parser.parse_args()

    if not os.path.isdir(args.image_dir):
        print("[ERROR] Image dir does not exist... {}".format(args.image_dir))

    if os.path.isdir(args.output_dir):
        for file in os.scandir(args.output_dir):
            os.unlink(file.path)
    else:
        os.makedirs(args.output_dir)

    return args


def get_n_random_image_annotation(json_data, n=50):

    result_image_annoation = list()

    nums = [x for x in range(len(json_data))]
    random.shuffle(nums)
    nums = nums[:n]

    nums.sort()
    for i in nums:
        result_image_annoation.append(json_data[i])

    return result_image_annoation


def copy_image_files(target_json, source_path, target_path):

    for element in target_json:
        image_name = element['filename']
        shutil.copy2(source_path + '\\' + image_name, target_path + '\\' + image_name)


def main():

    args = get_args()

    json_data = tt.read_json(args.source_json)

    args.num_of_files = int(args.num_of_files)

    target_json = get_n_random_image_annotation(json_data, args.num_of_files)

    copy_image_files(target_json, args.image_dir, args.output_dir)
    tt.write_json(args.output_dir + args.target_json, target_json)


if __name__ == '__main__':
    main()

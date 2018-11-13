import json
import os
import argparse


num_fused = 0
num_appended = 0


def fuse_annotations(path, json_files):
    global num_fused
    global num_appended
    with open(path + json_files[0]) as data_file:
        baseContent = json.load(data_file)

    for file in json_files[1:]:
        with open(path + file) as data_file:
            data = json.load(data_file)
        for image in data:
            fused = False
            for baseImage in baseContent:
                if baseImage["filename"] == image["filename"]:
                    baseImage["annotations"] += image["annotations"]
                    fused = True
                    num_fused += 1
                    break
            if fused == False:
                baseContent.append(image)
                num_appended += 1
    return baseContent


def get_args():
    parser = argparse.ArgumentParser(description='Fuses annotations (appends images and merges annotations if image already exists)')
    parser.add_argument('-s', '--source-dir',
                        required=True,
                        help='source directory with all annotations files that should merged to one')
    parser.add_argument('-tj', '--target-json',
                        required=False,
                        default="\\final_config.json",
                        help="filename for target json file, which will generated in source-dir")
    args = parser.parse_args()

    # append the trailing slash if needed
    args.source_dir = os.path.join(args.source_dir, '')

    if not os.path.isdir(args.source_dir):
        raise NotADirectoryError("source-dir ist not a valid directory")
    else:
        if os.path.isfile(args.source_dir + args.target_json):
            raise FileExistsError("targe-json already exists")
    return args


def main():

    args = get_args()

    json_files = [file for file in os.listdir(args.source_dir) if file.endswith('.json')]

    print('FilesCount: {}'.format(len(json_files)))
    json_data = fuse_annotations(args.source_dir, json_files)
    print('Final num of images: {}'.format(len(json_data)))
    print('images appended: {}'.format(num_appended))
    print('images merged: {}'.format(num_fused))

    with open(args.source_dir + args.target_json, 'w') as outfile:
        json.dump(json_data, outfile)


main()


